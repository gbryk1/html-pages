# HTML tools z lokalnym modelem w przeglądarce

Samodzielne narzędzia HTML w duchu
[„Useful patterns for building HTML tools”](https://simonwillison.net/2025/Dec/10/html-tools/)
Simona Willisona: pojedyncze pliki HTML, bez Reacta, bez build-stepu, bez backendu.
Modele neuronowe są wbudowane w strony jako **base64** i uruchamiane w przeglądarce przez
WebAssembly / WebGPU z pomocą [transformers.js](https://github.com/huggingface/transformers.js).
Dane użytkownika nigdy nie opuszczają urządzenia.

## Narzędzia

| Narzędzie | Model | Rozmiar | Uwagi |
|---|---|---|---|
| [`docs/speech-to-text.html`](docs/speech-to-text.html) | `onnx-community/whisper-tiny` (q8, wielojęzyczny) | 58 MB | Cały model w jednym pliku HTML |
| [`docs/chat.html`](docs/chat.html) | `onnx-community/SmolLM2-135M-Instruct-ONNX` (q4f16) | 117 MB → 156 MB base64 | Model podzielony na 3 pliki `.txt` (limit 100 MB/plik na GitHub Pages) |
| [`docs/cassandra-course.html`](docs/cassandra-course.html) | — (brak modelu ML) | ~160 KB | Animowany kurs wewnętrznej architektury i zasad designu Apache Cassandra w stylu „Head First” (scrollytelling, SVG/Canvas, IntersectionObserver). Źródło: `src/cassandra-course.html`, kopiowane przez `build.py` |
| [`docs/copilot-pm-course.html`](docs/copilot-pm-course.html) | — (brak modelu ML) | ~95 KB | Animowany przewodnik po Microsoft 365 Copilot i Outlook Copilot dla kierowników projektów w stylu „Head First”: grounding przez Microsoft Graph, uprawnienia, RAID log, nietrywialne scenariusze PM krok po kroku. Źródło: `src/copilot-pm-course.html`, kopiowane przez `build.py` |
| [`docs/claude-code-course.html`](docs/claude-code-course.html) | — (brak modelu ML) | ~180 KB | Animowany przewodnik (po angielsku) „Claude Code od zera do eksperta” w stylu „Head First”: pętla agenta, okno kontekstu, CLAUDE.md i pamięć, tryby i reguły uprawnień, plan mode, prompting, checkpointy, skille, subagenci, hooki, MCP i pluginy, worktrees i praca równoległa, tryb headless/CI, modele i effort, antywzorce; 16 interaktywnych diagramów, quiz i ściąga. Źródło: `src/claude-code-course.html`, kopiowane przez `build.py` |
| [`docs/kafka-course.html`](docs/kafka-course.html) | — (brak modelu ML) | ~640 KB | Animowany przewodnik (po angielsku) „Kafka od środka: od zera do eksperta” w stylu „Head First”, zgodny z Apache Kafka 4.3.1 i Spring for Apache Kafka 4.1.1: log, partycje i offsety, segmenty i indeksy, format record batch, KRaft, replikacja (LEO/HW/ISR), rebalanse (eager, cooperative, KIP-848), idempotencja, transakcje (KIP-890), kompakcja, potok żądań w brokerze, leader epochs i ELR, tiered storage, share groups (KIP-932), Kafka Streams, kwoty, tuning, troubleshooting, MirrorMaker 2, aktualizacje i feature versions, obsługa błędów w klientach, dobre praktyki Spring Kafka i porównanie z alternatywami; 4 poziomy głębokości, 28 interaktywnych diagramów, quiz i ściąga. Każda teza sprawdzona dwukrotnie (autor + niezależny weryfikator). Źródło: `src/kafka-course.html`, kopiowane przez `build.py` |

### Jak to działa

1. **Model jako base64.** `build.py` czyta pliki ONNX i koduje je do base64:
   * speech-to-text: base64 trafia inline do `<script type="text/plain">` w HTML,
   * chat: base64 trafia do `chat-model.b64.part*.txt`, strumieniowo pobieranych i dekodowanych do
     jednego bufora `Uint8Array`.
2. **Wbudowany model udaje serwer.** Strona podmienia `window.fetch` i przechwytuje żądania
   kierowane do `https://local-model.invalid/…`, zwracając bajty z pamięci jako `Response`.
   Dzięki temu `transformers.js` ładuje model „z sieci”, choć nic nie wychodzi z przeglądarki.
3. **Inferencja lokalna.** `pipeline('automatic-speech-recognition', …)` (Whisper) oraz
   `pipeline('text-generation', …)` (SmolLM2) działają na WebGPU, z fallbackiem na WASM.
   Biblioteka `transformers.js` ładowana jest z CDN (wzorzec „load dependencies from CDNs”).

Bajty modelu są mapowane po nazwie pliku plus aliasy (`encoder_model*.onnx` → wersja `q8`,
`model*.onnx` → wersja `q4f16`), więc kwantyzacja jest wybierana przez `dtype` w kodzie strony.

## Skille

- [`skills/animated-knowledge-page`](skills/animated-knowledge-page/SKILL.md) — ogólna (niezależna od repo) metoda budowy
  animowanych, scrollytellingowych stron wiedzy w stylu „Head First” (jak `cassandra-course.html` czy
  `claude-code-course.html`): plan rozdziałów, weryfikacja faktów w aktualnej dokumentacji, gotowy szablon
  ([`templates/shell.html`](skills/animated-knowledge-page/templates/shell.html)), katalog boxów i typów diagramów,
  quiz, ściąga, dwa tryby (prosty / głęboki z planem i wyborem głębokości), treści wielopoziomowe (części,
  poziomy L1–L3, przełącznik głębokości, mapa kursu), podwójna walidacja każdej tezy (autor + niezależny weryfikator,
  [`extract-claims.cjs`](skills/animated-knowledge-page/scripts/extract-claims.cjs)), testy headless
  ([`check-page.cjs`](skills/animated-knowledge-page/scripts/check-page.cjs),
  [`click-through.cjs`](skills/animated-knowledge-page/scripts/click-through.cjs)) i znane pułapki. Aby używać jej
  wszędzie: `cp -R skills/animated-knowledge-page ~/.claude/skills/`.

## Przebudowa

```bash
# 1. modele (nie są trzymane w repo — 160 MB)
mkdir -p models/whisper models/chat
W=https://huggingface.co/onnx-community/whisper-tiny/resolve/main
C=https://huggingface.co/onnx-community/SmolLM2-135M-Instruct-ONNX/resolve/main
for f in config.json generation_config.json preprocessor_config.json \
         tokenizer.json tokenizer_config.json added_tokens.json special_tokens_map.json; do
  curl -sL -o models/whisper/$f "$W/$f"
done
curl -sL -o models/whisper/encoder_model_quantized.onnx "$W/onnx/encoder_model_quantized.onnx"
curl -sL -o models/whisper/decoder_model_merged_quantized.onnx "$W/onnx/decoder_model_merged_quantized.onnx"
for f in config.json generation_config.json tokenizer.json tokenizer_config.json special_tokens_map.json; do
  curl -sL -o models/chat/$f "$C/$f"
done
curl -sL -o models/chat/model_q4f16.onnx "$C/onnx/model_q4f16.onnx"

# 2. build (do docs/)
python3 build.py --clean

# 3. lokalny podgląd
python3 -m http.server 8099 -d docs
```

## Uwagi

- **Rozmiar.** GitHub Pages nie przyjmuje plików >100 MB, dlatego model czatu (156 MB base64) jest
  podzielony. Przy pierwszym wejściu pobierze się ~156 MB (gzip w tranzycie zmniejsza to o ~25%).
- **WebGPU.** `q4f16` jest zoptymalizowany pod WebGPU (Chrome/Edge 113+, Safari 18+). Bez WebGPU
  strona próbuje WASM i pokazuje komunikat, jeśli format nie jest obsługiwany.
- **Prywatność.** Brak backendu, brak telemetrii; opcjonalnie całość działa offline po pierwszym
  załadowaniu, o ile biblioteka z CDN jest w cache przeglądarki.