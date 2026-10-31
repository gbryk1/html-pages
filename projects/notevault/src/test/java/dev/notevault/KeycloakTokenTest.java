package dev.notevault;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/** Prawdziwe tokeny z prawdziwego Keycloaka: podpis, issuer i audience sprawdza Spring, nie mock. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
class KeycloakTokenTest {

	@Container
	static GenericContainer<?> keycloak = new GenericContainer<>("quay.io/keycloak/keycloak:26.8")
		.withCommand("start-dev", "--import-realm")
		.withCopyFileToContainer(MountableFile.forHostPath("keycloak/notevault-realm.json"),
				"/opt/keycloak/data/import/notevault-realm.json")
		.withExposedPorts(8080)
		.waitingFor(Wait.forHttp("/realms/notevault").forStatusCode(200)
			.withStartupTimeout(Duration.ofMinutes(5)));

	@DynamicPropertySource
	static void keycloakUrls(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KeycloakTokenTest::realm);
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
				() -> realm() + "/protocol/openid-connect/certs");
	}

	static String realm() {
		return "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080) + "/realms/notevault";
	}

	@LocalServerPort int port;
	final HttpClient http = HttpClient.newHttpClient();

	/** Password grant tylko na potrzeby testu i deweloperki; przeglądarka używa Authorization Code + PKCE. */
	String token(String clientId, String username, String password) throws Exception {
		String form = "grant_type=password&client_id=" + clientId + "&username=" + username + "&password=" + password;
		var response = http.send(HttpRequest.newBuilder(URI.create(realm() + "/protocol/openid-connect/token"))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.POST(BodyPublishers.ofString(form))
			.build(), BodyHandlers.ofString());
		Matcher m = Pattern.compile("\"access_token\":\"([^\"]+)\"").matcher(response.body());
		assertThat(m.find()).as(response.body()).isTrue();
		return m.group(1);
	}

	int call(String path, String token) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
			.header("Authorization", "Bearer " + token)
			.build(), BodyHandlers.discarding()).statusCode();
	}

	@Test
	void userTokenOpensExtApi() throws Exception {
		String token = token("notevault-cli", "alice", "alice-dev-password");
		assertThat(call("/ext/notes", token)).isEqualTo(200);
		assertThat(call("/ext/admin/stats", token)).isEqualTo(403);
	}

	@Test
	void realmRoleAdminBecomesRoleAdmin() throws Exception {
		String token = token("notevault-cli", "admin", "admin-dev-password");
		assertThat(call("/ext/admin/stats", token)).isEqualTo(200);
	}

	@Test
	void tokenForAnotherAudienceIsRejected() throws Exception {
		String token = token("other-app", "alice", "alice-dev-password");
		assertThat(call("/ext/notes", token)).isEqualTo(401);
	}

	@Test
	void tamperedSignatureIsRejected() throws Exception {
		String token = token("notevault-cli", "alice", "alice-dev-password");
		char last = token.charAt(token.length() - 5);
		String tampered = token.substring(0, token.length() - 5) + (last == 'A' ? 'B' : 'A') + token.substring(token.length() - 4);
		assertThat(call("/ext/notes", tampered)).isEqualTo(401);
	}

}
