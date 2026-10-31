package dev.notevault.account;

import javax.sql.DataSource;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;

/**
 * Jedyny UserDetailsService w aplikacji. Konta czyta JdbcUserDetailsManager,
 * a ta klasa dokłada blokadę po zbyt wielu nieudanych próbach.
 */
@Service
public class UserAccounts implements UserDetailsService, UserDetailsPasswordService {

	private final JdbcUserDetailsManager users;
	private final LoginAttempts attempts;
	private final PasswordEncoder encoder;

	UserAccounts(DataSource dataSource, LoginAttempts attempts, PasswordEncoder encoder) {
		this.users = new JdbcUserDetailsManager(dataSource);
		this.attempts = attempts;
		this.encoder = encoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		UserDetails user = users.loadUserByUsername(username);
		if (!attempts.isLocked(username)) {
			return user;
		}
		return User.withUserDetails(user).accountLocked(true).build();
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		return users.updatePassword(user, newPassword);
	}

	public boolean exists(String username) {
		return users.userExists(username);
	}

	public void register(String username, String rawPassword, String... roles) {
		users.createUser(User.withUsername(username)
			.password(encoder.encode(rawPassword))
			.roles(roles)
			.build());
	}

}
