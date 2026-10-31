package dev.notevault;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import dev.notevault.account.UserAccounts;
import dev.notevault.notes.Note;
import dev.notevault.notes.NoteRepository;

/** Wersja z zadania 4 przed poprawką: zalogowany Bob czyta notatkę Alice. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, BrokenIdorTest.Broken.class })
@ActiveProfiles("test")
class BrokenIdorTest {

	@TestConfiguration(proxyBeanMethods = false)
	@RestController
	static class Broken {

		private final NoteRepository notes;

		Broken(NoteRepository notes) {
			this.notes = notes;
		}

		@GetMapping("/api/broken-notes/{id}")
		ResponseEntity<Note> one(@PathVariable long id) {
			return ResponseEntity.of(notes.findById(id));
		}

	}

	@Autowired MockMvc mvc;
	@Autowired NoteRepository notes;
	@Autowired UserAccounts accounts;

	@Test
	void bobReadsAlicesNote() throws Exception {
		if (!accounts.exists("alice")) {
			accounts.register("alice", "alice-password-123", "USER");
		}
		long id = notes.create("alice", "PIN do sejfu", "1234");
		mvc.perform(get("/api/broken-notes/{id}", id).with(user("bob")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.body").value("1234"));
	}

}
