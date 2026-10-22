package dev.tickethub;

import org.springframework.boot.SpringApplication;

public class TestTickethubApplication {

	public static void main(String[] args) {
		SpringApplication.from(TickethubApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
