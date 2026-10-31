package dev.notevault;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import dev.notevault.account.UserAccounts;
import dev.notevault.notes.NoteRepository;

/** Reguły dostępu bez prawdziwego logowania: user(), @WithMockUser i jwt() podstawiają tożsamość. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AccessRulesTest {

	@Autowired MockMvc mvc;
	@Autowired NoteRepository notes;
	@Autowired UserAccounts accounts;

	long aliceNote;

	@BeforeEach
	void data() {
		for (String name : List.of("alice", "bob")) {
			if (!accounts.exists(name)) {
				accounts.register(name, name + "-password-123", "USER");
			}
		}
		aliceNote = notes.create("alice", "PIN do sejfu", "1234");
	}

	@Test
	void anonymousGets401NotARedirect() throws Exception {
		mvc.perform(get("/api/notes")).andExpect(status().isUnauthorized());
	}

	@Test
	void healthIsPublic() throws Exception {
		mvc.perform(get("/actuator/health")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "bob")
	void userWithoutAdminRoleGets403() throws Exception {
		mvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void adminSeesUsers() throws Exception {
		mvc.perform(get("/api/admin/users")).andExpect(status().isOk());
	}

	@Test
	void ownerReadsOwnNote() throws Exception {
		mvc.perform(get("/api/notes/{id}", aliceNote).with(user("alice")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("PIN do sejfu"));
	}

	@Test
	void idorSomeoneElsesNoteLooksMissing() throws Exception {
		mvc.perform(get("/api/notes/{id}", aliceNote).with(user("bob")))
			.andExpect(status().isNotFound());
	}

	@Test
	void postWithoutCsrfTokenIsRejected() throws Exception {
		mvc.perform(post("/api/notes").with(user("alice"))
				.contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	void postWithCsrfTokenCreatesNote() throws Exception {
		mvc.perform(post("/api/notes").with(user("alice")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}"))
			.andExpect(status().isCreated());
	}

	@Test
	void methodSecurityStopsDeletingSomeoneElsesNote() throws Exception {
		mvc.perform(delete("/api/notes/{id}", aliceNote).with(user("bob")).with(csrf()))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminMayDeleteAnyNote() throws Exception {
		mvc.perform(delete("/api/notes/{id}", aliceNote).with(user("admin").roles("USER", "ADMIN")).with(csrf()))
			.andExpect(status().isNoContent());
	}

	@Test
	void extApiNeedsBearerToken() throws Exception {
		mvc.perform(get("/ext/notes")).andExpect(status().isUnauthorized())
			.andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
	}

	@Test
	void extApiWithJwt() throws Exception {
		mvc.perform(get("/ext/notes").with(jwt().jwt(j -> j.claim("preferred_username", "alice"))))
			.andExpect(status().isOk());
	}

	@Test
	void extAdminNeedsRoleFromToken() throws Exception {
		mvc.perform(get("/ext/admin/stats").with(jwt()))
			.andExpect(status().isForbidden());
		mvc.perform(get("/ext/admin/stats").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
			.andExpect(status().isOk());
	}

	@Test
	void securityHeadersArePresent() throws Exception {
		mvc.perform(get("/"))
			.andExpect(header().string("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'"))
			.andExpect(header().string("X-Content-Type-Options", "nosniff"))
			.andExpect(header().string("X-Frame-Options", "DENY"));
	}

	@Test
	void corsAllowsOnlyConfiguredOrigin() throws Exception {
		mvc.perform(options("/ext/notes")
				.header("Origin", "http://localhost:5173")
				.header("Access-Control-Request-Method", "GET"))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
		mvc.perform(options("/ext/notes")
				.header("Origin", "https://evil.example")
				.header("Access-Control-Request-Method", "GET"))
			.andExpect(status().isForbidden());
	}

}
