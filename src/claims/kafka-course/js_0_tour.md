# Claims ledger — kafka-course.html — js_0_tour

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1314 · js:0. tour · L- · script
> 1 · Producer. Your app serializes the record, picks a partition (by key hash) and adds it to a batch in memory.
- pass 1: ✅ design.md:127 — "client controls which partition it publishes messages to ... hash of the key"; producer RecordAccumulator batches (see part-1 notes)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:996-1029 — serialize, partition(), accumulator.append(); BuiltInPartitioner.partitionForKey hashes key

### C1315 · js:0. tour · L- · script
> 2 · Leader. The batch goes straight to the broker that leads that partition. It appends the batch to the end of its log segment file.
- pass 1: ✅ design.md:125 — "The producer sends data directly to the broker that is the leader for the partition"; implementation/log.md — appends to active segment
- pass 2: ✅ docs/design/design.md:125 — "sends data directly to the broker that is the leader for the partition"; docs/implementation/log.md:39 "serial appends which always go to the last file"

### C1316 · js:0. tour · L- · script
> 3 · Followers. Follower replicas on other brokers fetch the new records from the leader — replication is pull, not push.
- pass 1: ✅ design.md:287 — "Followers consume messages from the leader just as a normal Kafka consumer would"
- pass 2: ✅ docs/design/design.md:287 — "Followers consume messages from the leader just as a normal Kafka consumer would"

### C1317 · js:0. tour · L- · script
> 4 · Committed. Once every in-sync replica has the record (and the ISR still has at least min.insync.replicas members), the high watermark moves past it and an acks=all producer gets its answer.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ core/.../cluster/Partition.scala:1010-1014,969-975 — "if (isUnderMinIsr) ... Not increasing HWM"; ack when "leaderLog.highWatermark >= requiredOffset" and minIsr <= ISR size

### C1318 · js:0. tour · L- · script
> 5 · Consumer. The consumer that owns this partition in its group pulls the record — but only records below the high watermark.
- pass 1: ✅ design.md:302 — "Only committed messages are ever given out to the consumer"
- pass 2: ✅ docs/design/design.md:302 — "Only committed messages are ever given out to the consumer"

### C1319 · js:0. tour · L- · script
> 6 · Bookmark. After processing, the consumer commits its offset to the internal __consumer_offsets topic, so a restart resumes from there.
- pass 1: ✅ implementation/distribution.md:29-31 "Consumer Offset Tracking" — "commit offsets so that it can resume from those offsets in the event of a restart" (stored in __consumer_offsets)
- pass 2: ✅ docs/implementation/distribution.md:33 — "appends the request to a special compacted Kafka topic named __consumer_offsets"

### C1320 · js:0. tour · L- · script
> Press ▶ to follow one record — every station links to the chapter that explains it.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1321 · js:0. tour · L- · script
> That is the whole journey. Every arrow hides a mechanism — batching, fetch sessions, ISR, rebalances. The chapters open them one by one.
- pass 1: n/a (pedagogy)
- pass 2: n/a — narrative summary

### C1322 · js:0. tour · L- · script
> The record reached the leader…
- pass 1: n/a (narration step)
- pass 2: n/a — narration

### C1323 · js:0. tour · L- · script
> 💥 The leader broker dies before followers fetched the record. It is not below the high watermark yet, so no consumer has seen it and an acks=all producer never got a success.
- pass 1: ✅ design.md:302 — "Only committed messages are ever given out to the consumer"; Partition.scala:968 acks=all waits for HW
- pass 2: ✅ docs/design/design.md:302 — only committed messages given to consumers; acks=all acks only after full ISR (clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:138)

### C1324 · js:0. tour · L- · script
> Recovery: the controller quorum elects a new leader from the in-sync replicas, the producer retries against the new leader, and when the old broker returns it truncates the unreplicated copy. Had an earlier attempt survived, idempotence would stop the retry from becoming a duplicate. Parts II–III show each step.
- pass 1: ✅ design.md (Replication) — new leader chosen from ISR; leader epoch truncation per KIP-101 (see part-3 notes k-leader-epochs); idempotence dedupe design.md:193
- pass 2: ✅ docs/design/design.md:296,335 — controller elects from ISR; rejoining replica "must fully re-sync again even if it lost unflushed data"; idempotence dedup per clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:339
