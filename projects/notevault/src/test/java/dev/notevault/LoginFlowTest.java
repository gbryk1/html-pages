package dev.notevault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import dev.notevault.account.UserAccounts;

/** Prawdziwy serwer na losowym porcie i prawdziwe HTTP: ciasteczka, CSRF, sesja w Redisie. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class LoginFlowTest {

	@LocalServerPort int port;
	@Autowired UserAccounts accounts;

	private void ensureUser(String name) {
		if (!accounts.exists(name)) {
			accounts.register(name, name + "-password-123", "USER");
		}
	}

	@Test
	void loginWithoutCsrfTokenIsRejected() throws Exception {
		ensureUser("carol");
		Browser browser = new Browser(port);
		var response = browser.post("/api/login", "username=carol&password=carol-password-123",
				"application/x-www-form-urlencoded");
		assertThat(response.statusCode()).isEqualTo(403);
	}

	@Test
	void loginChangesSessionIdAndOpensApi() throws Exception {
		ensureUser("dave");
		Browser browser = new Browser(port);
		browser.get("/api/csrf");
		String before = browser.cookie("SESSION");

		assertThat(browser.login("dave", "dave-password-123").statusCode()).isEqualTo(204);

		assertThat(browser.cookie("SESSION")).isNotNull().isNotEqualTo(before);
		assertThat(browser.get("/api/me").body()).contains("\"username\":\"dave\"");
	}

	@Test
	void fiveBadPasswordsLockTheAccount() throws Exception {
		ensureUser("erin");
		Browser browser = new Browser(port);
		for (int i = 0; i < 5; i++) {
			assertThat(browser.login("erin", "wrong-password").statusCode()).isEqualTo(401);
		}
		// Teraz nawet dobre hasło nie wpuszcza: LockedException, zanim hasło zostanie sprawdzone
		assertThat(browser.login("erin", "erin-password-123").statusCode()).isEqualTo(401);
	}

	@Test
	void secondLoginExpiresTheFirstSession() throws Exception {
		ensureUser("frank");
		Browser laptop = new Browser(port);
		Browser phone = new Browser(port);
		laptop.login("frank", "frank-password-123");
		assertThat(laptop.get("/api/me").statusCode()).isEqualTo(200);

		phone.login("frank", "frank-password-123");

		assertThat(phone.get("/api/me").statusCode()).isEqualTo(200);
		assertThat(laptop.get("/api/me").statusCode()).isEqualTo(401);
	}

	@Test
	void logoutEndsTheSession() throws Exception {
		ensureUser("grace");
		Browser browser = new Browser(port);
		browser.login("grace", "grace-password-123");
		assertThat(browser.post("/api/logout", "", "text/plain").statusCode()).isEqualTo(200);
		assertThat(browser.get("/api/me").statusCode()).isEqualTo(401);
	}

	@Test
	void registrationRejectsShortPassword() throws Exception {
		Browser browser = new Browser(port);
		browser.get("/api/csrf");
		var response = browser.post("/api/register", "{\"username\":\"heidi\",\"password\":\"short\"}",
				"application/json");
		assertThat(response.statusCode()).isEqualTo(400);
	}

	@Test
	void adminFromConfigurationCanLogIn() throws Exception {
		Browser browser = new Browser(port);
		assertThat(browser.login("admin", "admin-test-password-123").statusCode()).isEqualTo(204);
		assertThat(browser.get("/api/admin/users").statusCode()).isEqualTo(200);
	}

}
