package dev.notevault.notes;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AdminController {

	private final JdbcClient jdbc;

	AdminController(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	// Reguła URL w SecurityConfig i @PreAuthorize tutaj: dwie warstwy, jedna wystarczy, by zatrzymać
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/api/admin/users")
	List<String> users() {
		return jdbc.sql("select username from users order by username").query(String.class).list();
	}

}
