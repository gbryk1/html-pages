package dev.notevault.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
class SecurityConfig {

	/** Łańcuch 1: API dla klientów z tokenem JWT. Bez sesji i bez ciasteczek, więc bez CSRF. */
	@Bean
	@Order(1)
	SecurityFilterChain extApi(HttpSecurity http) throws Exception {
		JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
		jwtConverter.setPrincipalClaimName("preferred_username");
		jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoles());

		http
			.securityMatcher("/ext/**")
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/ext/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults());
		return http.build();
	}

	/** Łańcuch 2: przeglądarka. Logowanie formularzem, sesja w Redisie, CSRF dla SPA. */
	@Bean
	@Order(2)
	SecurityFilterChain web(HttpSecurity http, SpringSessionBackedSessionRegistry<?> sessionRegistry)
			throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/index.html", "/app.js", "/api/csrf", "/actuator/health", "/error").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/register").permitAll()
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.formLogin(login -> login
				.loginPage("/")
				.loginProcessingUrl("/api/login")
				.successHandler((request, response, authentication) -> response.setStatus(204))
				.failureHandler((request, response, exception) -> response.setStatus(401)))
			.logout(logout -> logout
				.logoutUrl("/api/logout")
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))
			.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.csrf(csrf -> csrf.spa())
			.sessionManagement(session -> session
				.maximumSessions(1)
				.sessionRegistry(sessionRegistry)
				.expiredSessionStrategy(event -> event.getResponse().setStatus(401)))
			.headers(headers -> headers
				.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'")));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	<S extends Session> SpringSessionBackedSessionRegistry<S> sessionRegistry(
			FindByIndexNameSessionRepository<S> sessions) {
		return new SpringSessionBackedSessionRegistry<>(sessions);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${notevault.cors.allowed-origins}") List<String> allowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET"));
		config.setAllowedHeaders(List.of("Authorization"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/ext/**", config);
		return source;
	}

}
