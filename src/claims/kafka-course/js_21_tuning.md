# Claims ledger — kafka-course.html — js_21_tuning

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1542 · js:21. tuning · L- · script
> linger.ms=0: send as soon as the sender is free. Lowest idle latency, but smaller batches and more requests.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:151 — "rather than immediately sending out a record" (heuristic figure)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:153-157 linger delay tradeoff (mechanism); linger.ms=0 sends without waiting

### C1543 · js:21. tuning · L- · script
> linger.ms=50: batches get a chance to fill. Throughput up, but up to 50 ms extra latency when traffic is light.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:157 — "would add up to 50ms of latency"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:156-157 — "linger.ms=50 ... reducing the number of requests ... add up to 50ms of latency ... in the absence of load"

### C1544 · js:21. tuning · L- · script
> linger.ms=5: the 4.0+ default, a small wait that usually pays for itself under load.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:100 — "The default changed from 0 to 5 in Apache Kafka 4.0"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:405 default 5; :158 changed in 4.0 ("usually pays for itself" = docs' "typically result in similar or lower producer latency")

### C1545 · js:21. tuning · L- · script
> batch.size=16384: the default per-partition batch buffer.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:401 — "define(BATCH_SIZE_CONFIG, Type.INT, 16384"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:401 default 16384; batch.size is per partition (:82-88)

### C1546 · js:21. tuning · L- · script
> batch.size=…: bigger batches amortise request overhead and compress better. Each one allocates a full buffer.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:93 — "buffer of the specified batch size in anticipation" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:237 — "more batching means better compression"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:92 — "always allocate a buffer of the specified batch size"; :237 more batching means better compression

### C1547 · js:21. tuning · L- · script
> No compression: zero CPU cost, full bytes on the wire and on disk.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:235 — "The default is none (i.e. no compression)" (CPU/bytes trade = heuristic figure)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:235 default none (no compression)

### C1548 · js:21. tuning · L- · script
> …: compresses whole batches, trading CPU for network and disk. …
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:237 — "Compression is of full batches of data" (zstd/lz4 remarks heuristic, figure labelled)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:237 — "Compression is of full batches of data"

### C1549 · js:21. tuning · L- · script
> acks=all: wait for the in-sync replicas. Adds a follower round trip, survives leader loss.
- pass 1: n/a (acks semantics sourced in ch.5; heuristic figure narration)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:138 — "leader will wait for the full set of in-sync replicas"

### C1550 · js:21. tuning · L- · script
> acks=1: leader-only ack. Faster, but a leader crash can lose acknowledged records.
- pass 1: n/a (acks semantics sourced in ch.5; heuristic figure narration)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:135-137 acks=1 — leader writes locally and responds; record lost if leader fails before replication

### C1551 · js:21. tuning · L- · script
> Defaults loaded: linger.ms=5, batch.size=16384, no compression, acks=all. Change a knob or press ▶.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:405 — "define(LINGER_MS_CONFIG, Type.LONG, 5" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:401 — "define(BATCH_SIZE_CONFIG, Type.INT, 16384" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:235 — "The default is none" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:393 — ""all","
- pass 2: ✅ clients/.../producer/ProducerConfig.java defaults: linger.ms 5 (:405), batch.size 16384 (:401), compression none (:397), acks all (:393)

### C1552 · js:21. tuning · L- · script
> Throughput preset: bigger, fuller, compressed batches and still acks=all. Throughput and CPU efficiency improve; light-load latency grows by up to the linger time. Verify with the perf tool before you believe it.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:157 — "would add up to 50ms of latency" (heuristic figure)
- pass 2: ✅ mechanism: bigger batches + compression improve efficiency, latency up to linger (ProducerConfig.java:153-158, :237); verify advice n/a

### C1553 · js:21. tuning · L- · script
> "Make it fast!" with linger.ms=0 and acks=1: idle latency drops a little, but throughput falls (tiny batches, more requests, more CPU per MB) and a leader crash can now lose acknowledged writes. Fast in a demo, fragile in production.
- pass 1: n/a (labelled heuristic figure narration)
- pass 2: ✅ mechanism (illustrative): smaller batches → more requests (clients/.../producer/ProducerConfig.java:91, :150); acks=1 loses acked writes on leader crash (clients/.../producer/ProducerConfig.java:135-137)
