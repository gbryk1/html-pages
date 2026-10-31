package dev.notevault.account;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AccountController {

	record Registration(@NotBlank @Pattern(regexp = "[a-z0-9_]{3,50}") String username,
			@NotBlank @Size(min = 12, max = 128) String password) {
	}

	private final UserAccounts accounts;

	AccountController(UserAccounts accounts) {
		this.accounts = accounts;
	}

	@PostMapping("/api/register")
	ResponseEntity<Void> register(@Valid @RequestBody Registration registration) {
		if (accounts.exists(registration.username())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		accounts.register(registration.username(), registration.password(), "USER");
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/api/me")
	Map<String, Object> me(Authentication authentication) {
		List<String> authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.toList();
		return Map.of("username", authentication.getName(), "authorities", authorities);
	}

	/** Odczyt tokenu zapisuje go w ciasteczku XSRF-TOKEN, z którego czyta SPA. */
	@GetMapping("/api/csrf")
	Map<String, String> csrf(CsrfToken token) {
		return Map.of("headerName", token.getHeaderName());
	}

}
