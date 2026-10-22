# TicketHub — projekt referencyjny

Rozwiązanie wzorcowe do strony [`hibernate-project.html`](../../src/hibernate-project.html) (projekt ćwiczeniowy do kursu
Hibernate i Spring Data JPA). To stan po ostatnim zadaniu: z tego kodu pochodzą fragmenty rozwiązań, SQL, komunikaty błędów
i liczby zapytań pokazane na stronie.

Stos: Java 21, Spring Boot 4.1.1, Hibernate ORM 7.4.5, Spring Data JPA 4.1.1, Flyway, PostgreSQL 18, Testcontainers.

```bash
./mvnw test              # wszystkie testy na PostgreSQL 18 w Testcontainers (wymaga Dockera)
./mvnw spring-boot:run   # aplikacja; Docker Compose sam podnosi bazę z compose.yaml
```

| Gdzie | Co |
|---|---|
| `src/main/resources/db/migration` | migracje V1–V8, identyczne z tymi na stronie |
| `src/main/java/dev/tickethub` | encje, repozytoria i serwisy (`catalog`, `inventory`, `sales`, `audit`, `report`) |
| `TicketHubTests` | N+1, batch, paginacja, Specification, `@Version`, `SKIP LOCKED`, checkout, Envers, L2 |
| `PartOneTests` … `PartFiveTests` | zachowania z poszczególnych działów (SQL w logu, zepsute warianty) |
| `BatchFetchTests` | `default_batch_fetch_size` bez cache L2 |
| `OpeningDayTest` | finał: 16 wątków kupuje 2 miejsca naraz; niezmienniki w bazie |

Uwaga: `CheckoutService` to wersja z zadania 13/24 (audyt w środku transakcji). Pod obciążeniem wyczerpuje pulę połączeń;
poprawiona architektura to `CheckoutFacade` + `FastCheckoutService` z zadania finałowego. Klasy `BrokenCheckout`,
`RawEventController` i `Propagation` w testach celowo pokazują błędy opisane na stronie.
