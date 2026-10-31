package dev.notevault.notes;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

	private final NoteRepository notes;

	NoteService(NoteRepository notes) {
		this.notes = notes;
	}

	/** Usunąć może właściciel albo admin. Reguła siedzi przy metodzie, więc obowiązuje każdego wołającego. */
	@PreAuthorize("hasRole('ADMIN') or @noteRepository.isOwner(#id, authentication.name)")
	public void delete(long id) {
		notes.delete(id);
	}

}
