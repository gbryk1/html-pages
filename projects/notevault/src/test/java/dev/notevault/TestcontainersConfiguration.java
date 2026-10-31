package dev.notevault;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Prawdziwy PostgreSQL i Redis w kontenerach; @ServiceConnection sam ustawia URL-e i hasła. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgres() {
		return new PostgreSQLContainer("postgres:18");
	}

	@Bean
	@ServiceConnection(name = "redis")
	GenericContainer<?> redis() {
		return new GenericContainer<>("redis:8").withExposedPorts(6379);
	}

}
