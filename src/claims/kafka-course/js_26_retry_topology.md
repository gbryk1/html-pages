# Claims ledger — kafka-course.html — js_26_retry_topology

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1634 · js:26. retry topology · L- · script
> Record A1 (key A) will fail twice with a transient error, then succeed. A2 has the same key; B1 has another key. Pick a strategy.
- pass 1: n/a (scenario setup, illustrative)
- pass 2: n/a (scenario setup)

### C1635 · js:26. retry topology · L- · script
> Blocking: DefaultErrorHandler with FixedBackOff(1000L, 2L) — up to 3 deliveries of A1 before recovery.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:167 — "retry a delivery up to 2 times (3 delivery attempts) with a back off of 1 second"
- pass 2: ✅ spring-kafka docs kafka/annotation-error-handling.adoc:162-167 — FixedBackOff(1000L, 2L) "retry a delivery up to 2 times (3 delivery attempts) with a back off of 1 second"

### C1636 · js:26. retry topology · L- · script
> Delivery … of A1 failed. The handler seeks p0 back to A1 and waits 1 s; A2 and B1 wait too, even though B1 has another key.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:128-176 — "seeks so that all the remaining records (including the failed record) will be redelivered"
- pass 2: ✅ annotation-error-handling.adoc:121,172 — default BackOffHandler "suspends the thread until the back off time passes"; seeks so "next poll() returns the ... unprocessed records" (whole partition waits, key-agnostic)

### C1637 · js:26. retry topology · L- · script
> Retries exhausted: DeadLetterPublishingRecoverer publishes A1 to orders-dlt (same partition) and the container moves on past it.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:874-876 — "sent to a topic named <originalTopic>-dlt ... same partition"
- pass 2: ✅ annotation-error-handling.adoc:875 "<originalTopic>-dlt ... same partition as the original record"; recovered record is skipped ("recover (skip) a record", :137)

### C1638 · js:26. retry topology · L- · script
> A1 is parked in orders-dlt with kafka_dlt-* headers for replay. Order was kept while retrying, but A2 was applied without A1: for per-key state you may need to park the whole key.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~897-911 — "KafkaHeaders.DLT_ORIGINAL_OFFSET ... (advice part = opinion)"
- pass 2: ✅ annotation-error-handling.adoc:900-912 KafkaHeaders.DLT_* headers (KafkaHeaders.java:32 prefix "kafka_"); later same-key records are processed after recovery (park-the-key advice n/a)

### C1639 · js:26. retry topology · L- · script
> Order kept, throughput paid. A1 succeeded on the 3rd delivery, then A2 and B1 ran. The whole partition stalled for ~2 s of back-off, and a long back-off would risk max.poll.interval.ms.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:121-123,167 — "back off of 1 second / max.poll.interval.ms"
- pass 2: ✅ annotation-error-handling.adoc:121-123,167 — 2 back-offs of 1 s block the partition; long delays vs max.poll.interval.ms motivate ContainerPausingBackOffHandler

### C1640 · js:26. retry topology · L- · script
> A1 failed once and was forwarded to orders-retry with a due timestamp. The main partition keeps flowing…
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:4 — "forwarded to a retry topic with a back off timestamp"
- pass 2: ✅ retrytopic/how-the-pattern-works.adoc:4 "forwarded to a retry topic with a back off timestamp"; single "-retry" topic for fixed back-off (RetryableTopic.java:200 SINGLE_TOPIC, change-history.adoc:421)

### C1641 · js:26. retry topology · L- · script
> A2 overtook A1. Same key, wrong order. This is the docs' warning: "you lose Kafka's ordering guarantees for that topic".
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "you lose Kafka's ordering guarantees for that topic"
- pass 2: ✅ how-the-pattern-works.adoc:12 "By using this strategy you lose Kafka's ordering guarantees for that topic"

### C1642 · js:26. retry topology · L- · script
> After 1 s the retry consumer resumes: attempt 2 fails, and A1 goes back to the same orders-retry topic (SINGLE_TOPIC reuse for a fixed back-off).
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:5-6; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/topic-naming.adoc:~70-75; RetryableTopic.java:200 — "pauses the consumption / single topic ... fixed delay"
- pass 2: ✅ how-the-pattern-works.adoc:5-6 "if it's not due it pauses ... When it is due the partition consumption is resumed"; SINGLE_TOPIC reuse for same interval (RetryableTopic.java:194-200)

### C1643 · js:26. retry topology · L- · script
> Attempts exhausted: A1 lands in orders-dlt for the @DltHandler. The main topic never waited, but key A was already applied out of order.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:7; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/retry-config.adoc:50-54 — "sent to the Dead Letter Topic"
- pass 2: ✅ how-the-pattern-works.adoc:7 "attempts are exhausted, and the message is sent to the Dead Letter Topic"; retry-config.adoc:54 DltHandler (alternate branch of the simulation; mechanism correct)

### C1644 · js:26. retry topology · L- · script
> Throughput kept, order lost. A1 succeeded on attempt 3, after A2. Fine for independent events (emails, idempotent upserts); wrong for per-key state machines.
- pass 1: n/a (illustrative scenario outcome + opinion)
- pass 2: ✅ RetryableTopic.java:59 attempts default "3" (attempt 3 is the last before DLT); ordering loss per how-the-pattern-works.adoc:12 (use-case advice n/a)
