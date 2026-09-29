# Animowane strony wiedzy — HTML tools

Samodzielne narzędzia HTML w duchu
[„Useful patterns for building HTML tools”](https://simonwillison.net/2025/Dec/10/html-tools/)
Simona Willisona: pojedyncze pliki HTML, bez Reacta, bez build-stepu, bez backendu.

## Narzędzia

| Narzędzie | Rozmiar | Uwagi |
|---|---|---|
| [`docs/cassandra-course.html`](docs/cassandra-course.html) | ~160 KB | Animowany kurs wewnętrznej architektury i zasad designu Apache Cassandra w stylu „Head First” (scrollytelling, SVG/Canvas, IntersectionObserver). Źródło: `src/cassandra-course.html`, kopiowane przez `build.py` |
| [`docs/copilot-pm-course.html`](docs/copilot-pm-course.html) | ~95 KB | Animowany przewodnik po Microsoft 365 Copilot i Outlook Copilot dla kierowników projektów w stylu „Head First”: grounding przez Microsoft Graph, uprawnienia, RAID log, nietrywialne scenariusze PM krok po kroku. Źródło: `src/copilot-pm-course.html`, kopiowane przez `build.py` |
| [`docs/claude-code-course.html`](docs/claude-code-course.html) | ~180 KB | Animowany przewodnik (po angielsku) „Claude Code od zera do eksperta” w stylu „Head First”: pętla agenta, okno kontekstu, CLAUDE.md i pamięć, tryby i reguły uprawnień, plan mode, prompting, checkpointy, skille, subagenci, hooki, MCP i pluginy, worktrees i praca równoległa, tryb headless/CI, modele i effort, antywzorce; 16 interaktywnych diagramów, quiz i ściąga. Źródło: `src/claude-code-course.html`, kopiowane przez `build.py` |
| [`docs/claude-code-course-pl.html`](docs/claude-code-course-pl.html) | ~180 KB | Polskie tłumaczenie powyższego przewodnika po Claude Code. Źródło: `src/claude-code-course-pl.html`, kopiowane przez `build.py` |
| [`docs/kafka-course.html`](docs/kafka-course.html) | ~640 KB | Animowany przewodnik (po angielsku) „Kafka od środka: od zera do eksperta” w stylu „Head First”, zgodny z Apache Kafka 4.3.1 i Spring for Apache Kafka 4.1.1: log, partycje i offsety, segmenty i indeksy, format record batch, KRaft, replikacja (LEO/HW/ISR), rebalanse (eager, cooperative, KIP-848), idempotencja, transakcje (KIP-890), kompakcja, potok żądań w brokerze, leader epochs i ELR, tiered storage, share groups (KIP-932), Kafka Streams, kwoty, tuning, troubleshooting, MirrorMaker 2, aktualizacje i feature versions, obsługa błędów w klientach, dobre praktyki Spring Kafka i porównanie z alternatywami; 4 poziomy głębokości, 28 interaktywnych diagramów, quiz i ściąga. Każda teza sprawdzona dwukrotnie (autor + niezależny weryfikator). Źródło: `src/kafka-course.html`, kopiowane przez `build.py` |
| [`docs/system-design-course.html`](docs/system-design-course.html) | ~105 KB | Animowany przewodnik (po angielsku) po podstawach system designu w stylu „Head First”: skalowanie poziome/pionowe, redundancja i failover, twierdzenie CAP/PACELC, load balancing, cachowanie (cache-aside, stampede), sharding i spójne hashowanie, replikacja i kworum (W+R>N), odporność (timeouty, retry, circuit breaker, back-pressure), idempotencja i bezstanowość, kontrakty REST API (zasoby, kody statusów, wersjonowanie, paginacja, HATEOAS), kontrakty danych i zgodność wsteczna/w przód, kontrakty zdarzeniowe (event vs. command vs. notification, ewolucja schematu, wzorzec outbox), oraz przewodnik krok po kroku po projektowaniu systemu; 15 interaktywnych diagramów, quiz i ściąga. Każda teza sprawdzona dwukrotnie (autor + niezależny weryfikator). Źródło: `src/system-design-course.html`, kopiowane przez `build.py` |
| [`docs/spark-course.html`](docs/spark-course.html) | ~140 KB | Animowany przewodnik (po angielsku) „Spark od środka” w stylu „Head First”, zgodny z dokumentacją Apache Spark 4.2.0: driver, executory i cluster manager, RDD, leniwa ewaluacja i lineage, joby/stage/taski (DAGScheduler, TaskScheduler, FetchFailed), shuffle i skew, zunifikowany model pamięci, Catalyst, whole-stage codegen i AQE, a następnie przetwarzanie topiców Kafki przez DStreams: model mikro-batchy, direct stream (kafka-0-10), offsety i gwarancje dostarczenia, okna, stan i checkpointy, backpressure i limity; przykłady w Javie; 3 poziomy głębokości, 11 interaktywnych diagramów, quiz i ściąga. Każda teza sprawdzona dwukrotnie (autor + niezależny weryfikator). Źródło: `src/spark-course.html`, kopiowane przez `build.py` |

## Skille

- [`skills/animated-knowledge-page`](skills/animated-knowledge-page/SKILL.md) — ogólna (niezależna od repo) metoda budowy
  animowanych, scrollytellingowych stron wiedzy w stylu „Head First” (jak `cassandra-course.html` czy
  `claude-code-course.html`): plan rozdziałów, weryfikacja faktów w aktualnej dokumentacji, gotowy szablon
  ([`templates/shell.html`](skills/animated-knowledge-page/templates/shell.html)), katalog boxów i typów diagramów,
  quiz, ściąga, dwa tryby (prosty / głęboki z planem i wyborem głębokości), trzy budżety tokenów (`lean` — oszczędny, `standard`, `max`; [raport zużycia](skills/animated-knowledge-page/token-report-kafka-session.md)), treści wielopoziomowe (części,
  poziomy L1–L3, przełącznik głębokości, mapa kursu), podwójna walidacja każdej tezy (autor + niezależny weryfikator,
  [`extract-claims.cjs`](skills/animated-knowledge-page/scripts/extract-claims.cjs)), testy headless
  ([`check-page.cjs`](skills/animated-knowledge-page/scripts/check-page.cjs),
  [`click-through.cjs`](skills/animated-knowledge-page/scripts/click-through.cjs)) i znane pułapki. Aby używać jej
  wszędzie: `cp -R skills/animated-knowledge-page ~/.claude/skills/`.

## Przebudowa

```bash
# build (do docs/)
python3 build.py --clean

# lokalny podgląd
python3 -m http.server 8099 -d docs
```