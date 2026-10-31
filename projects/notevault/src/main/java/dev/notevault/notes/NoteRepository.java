package dev.notevault.notes;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class NoteRepository {

	private static final String COLUMNS = "select id, owner, title, body, created_at from note ";

	private final JdbcClient jdbc;

	NoteRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public List<Note> findByOwner(String owner) {
		return jdbc.sql(COLUMNS + "where owner = ? order by id").param(owner).query(Note.class).list();
	}

	/** Bez sprawdzania właściciela: używaj tylko tam, gdzie autoryzacja jest gdzie indziej. */
	public Optional<Note> findById(long id) {
		return jdbc.sql(COLUMNS + "where id = ?").param(id).query(Note.class).optional();
	}

	public Optional<Note> findByIdAndOwner(long id, String owner) {
		return jdbc.sql(COLUMNS + "where id = ? and owner = ?").params(id, owner).query(Note.class).optional();
	}

	public boolean isOwner(long id, String username) {
		return findByIdAndOwner(id, username).isPresent();
	}

	public long create(String owner, String title, String body) {
		return jdbc.sql("insert into note (owner, title, body) values (?, ?, ?) returning id")
			.params(owner, title, body)
			.query(Long.class)
			.single();
	}

	public void delete(long id) {
		jdbc.sql("delete from note where id = ?").param(id).update();
	}

	public long count() {
		return jdbc.sql("select count(*) from note").query(Long.class).single();
	}

}
