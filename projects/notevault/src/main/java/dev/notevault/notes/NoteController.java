package dev.notevault.notes;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
class NoteController {

	record NewNote(@NotBlank @Size(max = 120) String title, @Size(max = 10_000) String body) {
	}

	private final NoteRepository notes;
	private final NoteService service;

	NoteController(NoteRepository notes, NoteService service) {
		this.notes = notes;
		this.service = service;
	}

	@GetMapping
	List<Note> mine(Authentication authentication) {
		return notes.findByOwner(authentication.getName());
	}

	@GetMapping("/{id}")
	ResponseEntity<Note> one(@PathVariable long id, Authentication authentication) {
		// Właściciel jest w zapytaniu: cudza notatka wygląda jak nieistniejąca (404, nie 403)
		return ResponseEntity.of(notes.findByIdAndOwner(id, authentication.getName()));
	}

	@PostMapping
	ResponseEntity<Void> create(@Valid @RequestBody NewNote note, Authentication authentication) {
		long id = notes.create(authentication.getName(), note.title(), note.body() == null ? "" : note.body());
		return ResponseEntity.created(URI.create("/api/notes/" + id)).build();
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> delete(@PathVariable long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

}
