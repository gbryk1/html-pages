# Claims ledger — kafka-course.html — js_14_log_compaction

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1472 · js:14. log compaction · L- · script
> Keys A, B and C have been updated several times. Press 🧹 to clean.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1473 · js:14. log compaction · L- · script
> Nothing to clean. The dirty section has no superseded records, so this pass removes nothing. (The real cleaner decides whether to pick a log by its dirty-byte ratio, not by checking for superseded records.)
- pass 1: ✅ revised after pass-2 finding — C1473 (txn figure, CompleteAbort) — "so the input will be re-read" → "the consumer does not rewind by itself, so after the app seeks back to the committed offset (or restarts) the input is re-read" — docs/design/design.md:203 "although the consumer has to refe
- pass 2: ✅ storage/.../LogCleanerManager.java:283 — "(ltc.needCompactionNow() && ltc.cleanableBytes() > 0) || ltc.cleanableRatio() > ...minCleanableRatio"

### C1474 · js:14. log compaction · L- · script
> Cleaned. Removed offsets … — each had a later record for the same key. Survivors keep their original offsets; the gaps are just skipped on read.
- pass 1: ✅ docs/design/design.md:409 — "retain the original offset assigned when they were first written"
- pass 2: ✅ docs/design/design.md:407 — "retain the original offset… all offsets remain valid positions"

### C1475 · js:14. log compaction · L- · script
> Notice A=3 survives next to A=4: A=4 sits in the active segment, which the cleaner never touches.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogCleanerManager.java:735 — "the active segment is always uncleanable"
- pass 2: ✅ LogCleanerManager.java:735 — "the active segment is always uncleanable"

### C1476 · js:14. log compaction · L- · script
> Tombstone at … was past its horizon and is gone.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/Cleaner.java:515-521 — "currentTime < batch.deleteHorizonMs()"
- pass 2: ✅ Cleaner.java:516-521 — tombstone retained only while currentTime < deleteHorizonMs

### C1477 · js:14. log compaction · L- · script
> B already has a tombstone. Run the cleaner or fast-forward.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1478 · js:14. log compaction · L- · script
> Tombstone appended: key B with a null value. It lands in the active segment…
- pass 1: ✅ docs/design/design.md:411 — "A message with a key and a null payload will be treated as a delete"
- pass 2: ✅ design.md:409 — "A message with a key and a null payload will be treated as a delete"; appends go to active segment

### C1479 · js:14. log compaction · L- · script
> …and the segment rolls, so the tombstone becomes cleanable. Press 🧹 to compact: every older B disappears, the tombstone stays with a delete horizon.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogCleanerManager.java:735; clients/src/main/java/org/apache/kafka/common/record/internal/MemoryRecords.java:173-180 — "needToSetDeleteHorizon"
- pass 2: ✅ Cleaner.java:511-521 older records for key dropped; MemoryRecords.java:172-180 horizon stamped on first pass

### C1480 · js:14. log compaction · L- · script
> 24 hours pass. A new cleaner pass runs…
- pass 1: n/a (simulation step, illustrative)
- pass 2: n/a — illustrative time skip

### C1481 · js:14. log compaction · L- · script
> A consumer rebuilding a cache has read offsets 0–4 (A=2, B=2, C=1) and then stalls — a long outage, a slow restore…
- pass 1: n/a (scenario setup, illustrative)
- pass 2: n/a — illustrative scenario

### C1482 · js:14. log compaction · L- · script
> Meanwhile B is deleted (tombstone at offset 8, segment rolls).
- pass 1: n/a (scenario step, illustrative)
- pass 2: n/a — illustrative scenario

### C1483 · js:14. log compaction · L- · script
> Cleaner pass 1: older B records go, the tombstone is stamped "keep until +24 h".
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/record/internal/MemoryRecords.java:180 — "deleteHorizonMs = filter.currentTime + filter.deleteRetentionMs"
- pass 2: ✅ MemoryRecords.java:180 deleteHorizonMs = currentTime + deleteRetentionMs (default 24h, LogConfig.java:129)

### C1484 · js:14. log compaction · L- · script
> +24 h, cleaner pass 2: the tombstone is past delete.retention.ms and is removed.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/Cleaner.java:515-521 — "shouldRetainDeletes = … currentTime < batch.deleteHorizonMs()"
- pass 2: ✅ Cleaner.java:516 — tombstone dropped once currentTime ≥ deleteHorizonMs

### C1485 · js:14. log compaction · L- · script
> The consumer finally resumes from offset 5 — which no longer exists, so it reads from the next surviving offset. It never sees a delete for B, so B=2 lives on in its cache forever. Lagging longer than delete.retention.ms loses deletes.
- pass 1: ✅ docs/design/design.md:409,424 — "possible for a consumer to miss delete markers if it lags by more than delete.retention.ms"
- pass 2: ✅ design.md:407 (compacted-away offset reads from next existing) and :423 "possible for a consumer to miss delete markers if it lags by more than delete.retention.ms"
