package dev.notevault.notes;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** API dla klientów z tokenem z Keycloaka (aplikacja mobilna, integracje). */
@RestController
class ExtNoteController {

	private final NoteRepository notes;

	ExtNoteController(NoteRepository notes) {
		this.notes = notes;
	}

	@GetMapping("/ext/notes")
	List<Note> mine(Authentication authentication) {
		return notes.findByOwner(authentication.getName());
	}

	@GetMapping("/ext/admin/stats")
	Map<String, Long> stats() {
		return Map.of("notes", notes.count());
	}

}
