# Claims ledger — kafka-course.html — js_12_idempotent_producer

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1452 · js:12. idempotent producer · L- · script
> The log already holds batch seq 0–2 at offsets 0–2. Press ▶ to send the next batch (seq 3–5) and lose its ack.
- pass 1: n/a (figure initial state)
- pass 2: n/a — illustrative initial state

### C1453 · js:12. idempotent producer · L- · script
> Leader appends the batch at offsets 3–5….
- pass 1: n/a (scenario narration)
- pass 2: ✅ mechanism: leader appends batch (offsets illustrative)

### C1454 · js:12. idempotent producer · L- · script
> 💥 The response is lost (connection dropped, request timed out). The producer can't tell "not written" from "written, ack lost", so it retries the same batch.
- pass 1: ✅ design.md:191 — "cannot be sure if this error happened before or after the message was committed"
- pass 2: ✅ design.md:193 — producer that fails to receive a response "had little choice but to resend"

### C1455 · js:12. idempotent producer · L- · script
> Duplicate detected. Same PID, same epoch, and seq 3–5 matches one of the last 5 batches in the producer state. The leader does not append; it returns the original offsets 3–5. The log has exactly one copy, and the producer is none the wiser.
- pass 1: ✅ ProducerStateEntry.java:35,128-133; UnifiedLog.java:1247-1253
- pass 2: ✅ ProducerStateEntry.java:129-136 same epoch + exact seq range among last 5; UnifiedLog.java:1247-1251 returns original offsets without appending

### C1456 · js:12. idempotent producer · L- · script
> Duplicate written. Without a producer id and sequence numbers the leader has no way to recognise the retry: offsets 6–8 are a second copy of 3–5. Every consumer will process those records twice. That is at-least-once.
- pass 1: ✅ design.md:193 — "at-least-once delivery semantics"; ProducerConfig.java:340 "may write duplicates"
- pass 2: ✅ design.md:193 — without idempotence, resending gives at-least-once / duplicates

### C1457 · js:12. idempotent producer · L- · script
> Rejected. The last sequence the leader has for PID 4000 is 2, so the next batch must start at 3. A batch starting at 9 means batches went missing in between, and appending it would break ordering. The leader refuses with OUT_OF_ORDER_SEQUENCE_NUMBER and appends nothing.
- pass 1: ✅ ProducerAppendInfo.java:188-191; Errors.java:276
- pass 2: ✅ ProducerAppendInfo.java:188-191 — gap → OutOfOrderSequenceException (OUT_OF_ORDER_SEQUENCE_NUMBER), nothing appended
