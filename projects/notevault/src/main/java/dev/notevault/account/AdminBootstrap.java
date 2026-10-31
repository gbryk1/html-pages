package dev.notevault.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Zakłada konto admin przy pierwszym starcie. Hasło przychodzi z konfiguracji, nigdy z kodu. */
@Component
class AdminBootstrap implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

	private final UserAccounts accounts;
	private final String adminPassword;

	AdminBootstrap(UserAccounts accounts, @Value("${notevault.admin.password:}") String adminPassword) {
		this.accounts = accounts;
		this.adminPassword = adminPassword.strip();
	}

	@Override
	public void run(ApplicationArguments args) {
		if (accounts.exists("admin")) {
			return;
		}
		if (adminPassword.isEmpty()) {
			log.warn("notevault.admin.password is empty, admin account not created");
			return;
		}
		accounts.register("admin", adminPassword, "USER", "ADMIN");
		log.info("Admin account created");
	}

}
