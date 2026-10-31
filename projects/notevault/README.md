# NoteVault — projekt referencyjny

Rozwiązanie wzorcowe do strony [`spring-security-project.html`](../../src/spring-security-project.html) (projekt ćwiczeniowy do
kursu Spring Security). To stan po ostatnim zadaniu: z tego kodu pochodzą fragmenty rozwiązań, kody odpowiedzi, nagłówki
i komunikaty błędów pokazane na stronie.

Stos: Java 21, Spring Boot 4.1.1, Spring Security 7.1.1, Spring Session 4.1.1 (Redis), Flyway, PostgreSQL 18, Redis 8,
Keycloak 26.8, Testcontainers 2.0.5.

```bash
./mvnw test              # 27 testów na PostgreSQL, Redisie i Keycloaku w Testcontainers (wymaga Dockera)
./mvnw spring-boot:run   # aplikacja; Docker Compose sam podnosi compose.yaml (hasła tylko deweloperskie)

# cały system w kontenerach, hasła jako Docker secrets
./secrets/generate.sh
docker compose -f compose.full.yaml up -d --build    # APP_PORT=8088 …, gdy 8080 jest zajęty
./scripts/smoke.sh                                   # APP=http://localhost:8088 ./scripts/smoke.sh
docker compose -f compose.full.yaml down -v
```

| Gdzie | Co |
|---|---|
| `src/main/java/dev/notevault/security` | `SecurityConfig` (dwa łańcuchy: `/ext/**` na JWT i przeglądarka na sesji), `KeycloakRoles` |
| `src/main/java/dev/notevault/account` | `UserAccounts` (UserDetailsService z blokadą), `LoginAttempts` (licznik w Redisie), rejestracja, `AdminBootstrap` |
| `src/main/java/dev/notevault/notes` | notatki (`JdbcClient`), `@PreAuthorize` w `NoteService`, API `/ext` |
| `src/main/resources/static` | minimalny frontend (`index.html`, `app.js`) z obsługą tokenu CSRF |
| `keycloak/notevault-realm.json` | realm deweloperski: role, klienci `notevault-cli` i `other-app`, użytkownicy `alice` i `admin` |
| `AccessRulesTest` | reguły dostępu w MockMvc: `user()`, `@WithMockUser`, `jwt()`, `csrf()`, nagłówki, CORS |
| `LoginFlowTest` | prawdziwe HTTP: CSRF, session fixation, limit sesji, blokada konta, wylogowanie |
| `KeycloakTokenTest` | prawdziwe tokeny z Keycloaka: role, audience, zmieniony podpis |
| `BrokenIdorTest` | zepsuta wersja z zadania 4 (`findById` bez właściciela) |

Hasła w `compose.yaml` i w realmie Keycloaka są wyłącznie deweloperskie. `secrets/*.txt` są w `.gitignore`.
