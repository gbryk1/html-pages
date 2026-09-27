# Claims ledger — kafka-course.html — js_3_producer_basics

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1344 · js:3. producer basics · L- · script
> linger.ms = …sim time … msrequests …records sent …longest wait in a batch … ms
- pass 1: n/a (UI label)
- pass 2: n/a — UI stats labels

### C1345 · js:3. producer basics · L- · script
> linger.ms = …. Press ▶ to send 12 records in a quick burst plus one late straggler.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1346 · js:3. producer basics · L- · script
> Sending… each record enters its partition's batch; a batch leaves when it holds … records or has waited … ms.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:153-156 — sent when batch.size reached or linger expires (counts illustrative)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:153-156 — sent when batch.size reached or after linger

### C1347 · js:3. producer basics · L- · script
> linger.ms=0 → … requests for … records. Every record left alone. Lowest latency here, but the most requests for the broker. (Real producers still batch under load, when records pile up while a request is in flight.)
- pass 1: ✅ clients/.../producer/ProducerConfig.java:149 — "Normally this occurs only under load" (counts illustrative)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:148-149 — batching "occurs only under load when records arrive faster than they can be sent out"; counts illustrative

### C1348 · js:3. producer basics · L- · script
> linger.ms=5 → … requests. A short wait let records share a batch. No record waited more than … ms. That's the 4.0+ default trade-off.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:158-160 — default 5 since 4.0
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:100 — "default changed from 0 to 5 in Apache Kafka 4.0"

### C1349 · js:3. producer basics · L- · script
> linger.ms=50 → … requests. Burst batches filled up and left early, without waiting 50 ms. But the lonely straggler waited … ms for company. Long linger only costs latency when traffic is light.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:153-157 — "once we get batch.size worth … sent immediately"; "add up to 50ms of latency … in the absence of load"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:155-157 — "linger.ms=50 ... would add up to 50ms of latency to records sent in the absence of load"

### C1350 · js:3. producer basics · L- · script
> The broker stalls (slow disk, network trouble): no requests complete, so nothing leaves the accumulator…
- pass 1: n/a (scenario narration)
- pass 2: ✅ mechanism: max.in.flight.requests.per.connection (clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:473) caps unacked requests, so batches accumulate

### C1351 · js:3. producer basics · L- · script
> Buffer full: … send() calls are now blocked. When buffer.memory is used up, send() blocks your thread for up to max.block.ms (60 s by default) and then fails with an exception. A "fire-and-forget" call can freeze a request handler.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:214-215,435-440 — "block for max.block.ms after which it will fail with an exception"; 60000
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:435 max.block.ms 60000; clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java send javadoc blocks when "buffer pool doesn't have any free buffers"; BufferPool throws BufferExhaustedException on timeout
