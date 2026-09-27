# Claims ledger — kafka-course.html — quiz

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1222 · quiz · L1 · quiz
> 1. Which statement about Apache Kafka 4.x is true? → Kafka 4.0 removed ZooKeeper mode; clusters run in KRaft mode only — 💡 Since 4.0, ZooKeeper mode is gone. A ZooKeeper-based cluster must first migrate to KRaft on a 3.x release (3.9 is the bridge) before upgrading to 4.x.
- pass 1: ✅ operations/kraft.md — "you need to use a bridge release. The last bridge release is Kafka 3.9"; upgrade.md "ZooKeeper mode has been removed"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/kraft.md:296 — "The last bridge release is Kafka 3.9."

### C1223 · quiz · L1 · quiz
> 2. Kafka guarantees that consumers read records in the order they were written… → …within each partition — 💡 Order is per partition only. Records with the same key go to the same partition, so per-key order holds. Across partitions, reads can interleave in any way.
- pass 1: ✅ docs/getting-started/introduction.md:83 — "will always read that partition's events in exactly the same order as they were written"
- pass 2: ✅ kafka-4.3.1-src/docs/design/design.md (ordering per partition); BuiltInPartitioner.java:330 same key → same partition — per-partition order only

### C1224 · quiz · L1 · quiz
> 3. A topic has 3 partitions and you produce keyed records. Which records end up in the same partition? → Records with the same key (hash of the key mod the partition count) — 💡 The default partitioner computes murmur2(keyBytes) made positive, mod the number of partitions.
- pass 1: ✅ clients/.../producer/internals/BuiltInPartitioner.java:330 — "Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions"
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/internals/BuiltInPartitioner.java:330 — "Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions"

### C1225 · quiz · L1 · quiz
> 4. (L2) You increase a keyed topic from 3 to 6 partitions. What can happen? → New records for a key may land in a different partition than its old records — 💡 The mapping is hash mod partition count, so changing the count changes where keys go. Old records stay where they are; per-key order across the change is not guaranteed.
- pass 1: ✅ tools/.../TopicCommand.java:760-761 — "If partitions are increased for a topic that has a key, the partition logic or ordering … affected"
- pass 2: ✅ BuiltInPartitioner.java:330 — partition = hash % numPartitions, so changing the count remaps keys; existing records are not moved

### C1226 · quiz · L1 · quiz
> 5. How does a producer find out which broker leads partition orders-1? → It fetches metadata from any broker, then talks to the leader directly — 💡 bootstrap.servers is only for the first contact. Any broker can answer a metadata request, and clients then connect straight to partition leaders.
- pass 1: ✅ docs/design/design.md:125 — "all Kafka nodes can answer a request for metadata … without any intervening routing tier"
- pass 2: ✅ design/protocol docs: any broker answers Metadata; clients connect to partition leaders; bootstrap.servers only for initial connection (CommonClientConfigs BOOTSTRAP_SERVERS_DOC)

### C1227 · quiz · L1 · quiz
> 6. (L2) A broker answers a produce request with NOT_LEADER_OR_FOLLOWER. What does the Java producer do? → Treats it as stale metadata, requests a metadata refresh and retries — 💡 The error is an InvalidMetadataException (retriable). The Sender requests a metadata update, and the batch is retried against the new leader.
- pass 1: ✅ clients/.../producer/internals/Sender.java:713-730 + common/errors/NotLeaderOrFollowerException.java:27 — "Going to request metadata update now"; extends InvalidMetadataException
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/NotLeaderOrFollowerException.java:27 — "extends InvalidMetadataException"; Sender.java:713 requests metadata update on InvalidMetadataException

### C1228 · quiz · L1 · quiz
> 7. What is the default linger.ms in Kafka 4.x? → 5 — 💡 It changed from 0 to 5 in Apache Kafka 4.0, because larger batches typically give similar or lower latency.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:158-160,405 — "The default changed from 0 to 5 in Apache Kafka 4.0"
- pass 2: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:285 — "default linger.ms changed from 0 to 5 in Apache Kafka 4.0 … similar or lower producer latency"

### C1229 · quiz · L1 · quiz
> 8. producer.send(record) returned. What do you know? → The record is in the producer's buffer; the outcome arrives later via the Future or callback — 💡 send() is asynchronous: it appends to the RecordAccumulator and returns. A background sender thread transmits batches.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:126-127 — "adds the record to a buffer of pending record sends and immediately returns"
- pass 2: ✅ kafka-4.3.1-src/KafkaProducer.java doSend appends to RecordAccumulator and returns a Future; Sender thread transmits

### C1230 · quiz · L1 · quiz
> 9. The producer buffer (buffer.memory) is full because the broker is slow. What happens on the next send()? → send() blocks for up to max.block.ms (default 60 s), then the record fails (via the Future/callback) — 💡 The buffer.memory docs say the producer blocks for max.block.ms. After that the record fails with a BufferExhaustedException (a TimeoutException), delivered through the returned Future or callback rather than thrown from send().
- pass 1: ✅ revised after pass-2 finding — C1230 quiz Q9 → KafkaProducer.doSend catches ApiException, returns FutureFailure + callback (KafkaProducer.java:1049-1061)
- pass 2: ✅ ProducerConfig.java:214-215 "producer will block for max.block.ms after which it will fail"; BufferExhaustedException extends TimeoutException, caught as ApiException → FutureFailure/callback

### C1231 · quiz · L1 · quiz
> 10. A consumer group has 5 consumers and the topic has 4 partitions. What happens? → One consumer is idle — 💡 Within a group each partition has exactly one owner, so the partition count caps a group's parallelism.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99 — "each partition is assigned to exactly one consumer in the group"
- pass 2: ✅ design docs: each partition consumed by exactly one consumer in the group; 5 consumers on 4 partitions → 1 idle

### C1232 · quiz · L1 · quiz
> 11. What is the difference between a consumer's position and its committed offset? → Position = next offset to read (in memory); committed = last offset saved in Kafka, used after a restart — 💡 The position advances with every poll(). The committed offset is stored via the group coordinator in __consumer_offsets.
- pass 1: ✅ KafkaConsumer.java:82-87 + docs/implementation/distribution.md:33 — "offset of the next record that will be given out"; "__consumer_offsets"
- pass 2: ✅ KafkaConsumer javadoc (position vs committed position); committed offsets stored in __consumer_offsets via group coordinator

### C1233 · quiz · L1 · quiz
> 12. A brand-new consumer group starts on a topic full of old data and reads nothing. Why? → auto.offset.reset defaults to latest — 💡 With no committed offset, auto.offset.reset decides where to start. The default, latest, means "only new records". Use earliest to read existing data.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:172-175,546 — "What to do when there is no initial offset"; default LATEST
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:546 — auto.offset.reset default "AutoOffsetResetStrategy.LATEST"

### C1234 · quiz · L1 · quiz
> 13. (L2) Your consumer processes a batch, then crashes before committing. What happens after restart? → Those records are delivered again (at-least-once) — 💡 Process-then-commit gives at-least-once: work done after the last commit is redone. Make handlers idempotent.
- pass 1: ✅ docs/design/design.md:200 — "crashes after processing messages but before saving its position … at-least-once"
- pass 2: ✅ KafkaConsumer javadoc — process then commit gives at-least-once; records after last commit are redelivered

### C1235 · quiz · L1 · quiz
> 14. With acks=1, when can an acknowledged record be lost? → When the leader fails after acking but before followers replicated it — 💡 acks=1 means the leader wrote it locally. If the leader dies before a follower copies it, the new leader doesn't have it.
- pass 1: ✅ ProducerConfig.java:135-137 — "should the leader fail immediately after acknowledging … the record will be lost"
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java ACKS_DOC — acks=1: "if the leader fail[s] immediately after acknowledging … but before the followers have replicated it then the record will be lost"

### C1236 · quiz · L1 · quiz
> 15. RF 3, min.insync.replicas=2, acks=all. Two brokers are down. What happens to writes? → They are rejected with NotEnoughReplicas — 💡 With acks=all, if the ISR has fewer members than min.insync.replicas, the write is rejected with NotEnoughReplicas.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:180-181 — "fewer than min.insync.replicas members, then the producer will raise an exception (either NotEnoughReplicas…"
- pass 2: ✅ design.md / min.insync.replicas doc — "producer will raise an exception (either NotEnoughReplicas or NotEnoughReplicasAfterAppend)"; ISR 1 < 2

### C1237 · quiz · L1 · quiz
> 16. (L2) Why does Kafka elect new leaders only from the ISR? → Every ISR member has every committed record, so no committed data is lost — 💡 A record is committed only when all ISR members have it (and the ISR has at least min.insync.replicas members). Electing from the ISR keeps every committed record. Unclean election (off by default) breaks this rule.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ design.md:191,302-305 — committed = on all ISR (+ min ISR); unclean.leader.election.enable default false

### C1238 · quiz · L1 · quiz
> 17. With cleanup.policy=delete, what removes a record? → The retention time or size limit, applied to whole segments — 💡 Reading never deletes. Retention drops whole segments once they break retention.ms or retention.bytes.
- pass 1: ✅ TopicConfig.java:156-158 + docs/implementation/log.md:72 — "discard old segments when their retention time or size limit has been reached"
- pass 2: ✅ UnifiedLog.java:2011 retention deletes whole segments by retention.ms / retention.bytes; consumption does not delete

### C1239 · quiz · L1 · quiz
> 18. retention.ms is 7 days. Can you still find a 9-day-old record? → Yes, if it shares a segment with records newer than 7 days (or the retention check hasn't run yet) — 💡 Time retention uses the largest timestamp in a segment. A segment goes only when its newest record is older than retention.ms, and checks run every log.retention.check.interval.ms.
- pass 1: ✅ docs/implementation/log.md:72 + ServerLogConfigs.java:84-85 — "largest timestamp in a segment file … defining the retention time for the entire segment"; check every 5 min
- pass 2: ✅ kafka-4.3.1-src/storage/.../UnifiedLog.java:2011 — "startMs - segment.largestTimestamp() > retentionMs"; checks every log.retention.check.interval.ms

### C1240 · quiz · L1 · quiz
> 19. (L2) A consumer's committed offset points into a deleted segment and auto.offset.reset is the default. What happens? → It jumps to the latest offset, skipping everything in between — 💡 The offset is out of range, so auto.offset.reset applies. The default latest jumps to the end. Use earliest or none if skipping is unacceptable.
- pass 1: ✅ ConsumerConfig.java:172-175 — "if the current offset does not exist any more on the server … latest: automatically reset the offset to the latest"
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java AUTO_OFFSET_RESET_DOC — applies "if the current offset does not exist any more on the server"; default latest

### C1241 · quiz · L1 · quiz
> 20. (L2) A consumer asks for offset 2 600. What does the broker do first? → Picks the segment with the largest base offset ≤ 2 600, then binary-searches that segment's sparse .index — 💡 Segments are kept in a map keyed by base offset (floor lookup). The .index is sparse (about one entry per index.interval.bytes = 4096 bytes) and memory-mapped, and a short forward scan of the .log finishes the job.
- pass 1: ✅ storage/.../log/LogSegments.java:210-221; OffsetIndex.java:34-36; ServerLogConfigs.java:97 — "floorEntry(offset)"; "greatest offset less than or equal to the target offset"
- pass 2: ✅ kafka-4.3.1-src/storage/.../LogSegments.java:211 — "segments.floorEntry(offset)"; ServerLogConfigs.java:97 LOG_INDEX_INTERVAL_BYTES_DEFAULT = 4096; sparse mmapped index

### C1242 · quiz · L1 · quiz
> 21. (L2) With default settings, when is an acknowledged write fsync'ed to disk? → Not on a schedule: flush.messages and flush.ms default to Long.MAX_VALUE, and durability comes from replication to the ISR — 💡 Kafka writes into the OS page cache and leaves flushing to the OS. The docs recommend not setting the flush configs and relying on replication instead.
- pass 1: ✅ server-common/.../ServerLogConfigs.java:101,109; TopicConfig.java:54 — "Long.MAX_VALUE"; "use replication for durability"
- pass 2: ✅ ServerLogConfigs.java:101,109 flush defaults Long.MAX_VALUE; docs/operations/hardware-and-os.md:66 — "recommend using the default flush settings which disable application fsync entirely"

### C1243 · quiz · L1 · quiz
> 22. (L2) Your topic has retention.ms = 1 hour but keeps records for a day. Most likely cause? → Retention deletes whole segments, and a segment is judged by its newest record. A big, slowly filling segment (segment.bytes 1 GiB, segment.ms 7 d) keeps its old records alive — 💡 Deletion is per segment, and time retention uses the largest timestamp in the segment. Smaller segment.bytes/segment.ms makes retention more precise. Consumers never block deletion.
- pass 1: ✅ docs/implementation/log.md:72; storage/.../log/LogConfig.java:125-126 — "largest timestamp in a segment file… defining the retention time for the entire segment"
- pass 2: ✅ UnifiedLog.java:2011 (largestTimestamp per segment); LogConfig.java:125-126 segment.bytes 1 GiB, segment.ms 7 d

### C1244 · quiz · L1 · quiz
> 23. (L2) Why is zero-copy (sendfile) not used on a TLS listener? → Because TLS encryption happens in user space, so bytes must be read into the JVM — 💡 The design docs: TLS libraries operate in user space and Kafka does not support in-kernel SSL_sendfile, so sendfile is not used when SSL is enabled.
- pass 1: ✅ docs/design/design.md:109 — "TLS/SSL libraries operate at the user space… `sendfile` is not used when SSL is enabled"
- pass 2: ✅ kafka-4.3.1-src/docs/design/design.md:109 — "in-kernel SSL_sendfile is currently not supported … sendfile is not used when SSL is enabled"

### C1245 · quiz · L1 · quiz
> 24. (L2) Which record batch field lies OUTSIDE the CRC so the leader can set it on every batch without recomputing the checksum? → partitionLeaderEpoch — 💡 The CRC-32C covers attributes through the end of the batch. baseOffset, batchLength and partitionLeaderEpoch come before it, so the broker can stamp offsets and the leader epoch cheaply.
- pass 1: ✅ docs/implementation/message-format.md:67 — "partition leader epoch field is not included in the CRC computation"
- pass 2: ✅ kafka-4.3.1-src/clients/.../record/DefaultRecordBatch.java:67 — "The CRC covers the data from the attributes to the end of the batch"

### C1246 · quiz · L1 · quiz
> 25. (L2) The producer sends zstd-compressed batches to a topic with compression.type=producer. What happens on the broker? → The broker validates the batch and stores it still compressed with zstd, and consumers receive it compressed — 💡 Compression is end-to-end. The broker decompresses only to validate (for example the record count) and keeps the producer's codec. A topic codec that differs from the producer's forces a rebuild of every batch.
- pass 1: ✅ docs/design/design.md:117; ServerLogConfigs.java:178; TopicConfig.java:193 — "written to disk in compressed form"; "'producer'… retain the original compression codec"
- pass 2: ✅ ServerLogConfigs.java:178 broker compression.type default producer; LogValidator keeps producer codec when topic codec is producer, recompresses when different

### C1247 · quiz · L1 · quiz
> 26. (L2) Why does compressing a whole batch usually beat compressing each record on its own? → Most redundancy is between records (repeated field names, repeated values), so a codec sees it only when records are compressed together — 💡 The design docs make exactly this point, and it's why larger batches (linger.ms, batch.size) improve the ratio.
- pass 1: ✅ docs/design/design.md:115 — "much of the redundancy is due to repetition between messages of the same type"
- pass 2: ✅ kafka-4.3.1-src/docs/design/design.md:115 — "much of the redundancy is due to repetition between messages of the same type (e.g. field names in JSON"

### C1248 · quiz · L1 · quiz
> 27. (L2) In KRaft, which nodes vote in controller elections? → Only the controllers (voters). Brokers replicate the metadata log as observers — 💡 kafka-metadata-quorum.sh describe --status lists CurrentVoters (controllers) and CurrentObservers (brokers). Voters elect the active controller by majority.
- pass 1: ✅ docs/operations/kraft.md:237-242; raft/.../KafkaRaftClient.java:133-135 — "CurrentVoters… CurrentObservers"; "Voters… take part in elections"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/kraft.md:237-240 — describe --status prints CurrentVoters (controllers) and CurrentObservers (brokers); KafkaRaftClient.java:134 voters "take part in elections"

### C1249 · quiz · L1 · quiz
> 28. (L2) How do KRaft followers get new metadata records from the active controller? → Followers pull with Fetch requests; a new leader announces itself with BeginQuorumEpoch — 💡 KafkaRaftClient describes itself as a "Kafkaesque" Raft: election is Raft-like, but replication is driven by followers fetching, the same model as partition replication.
- pass 1: ✅ raft/.../KafkaRaftClient.java:128-150 — "replication is driven by replica fetching… BeginQuorumEpoch"
- pass 2: ✅ kafka-4.3.1-src/raft/.../KafkaRaftClient.java:129-131 — "Kafkaesque version of the Raft protocol … replication is driven by replica fetching"; BeginQuorumEpoch listed in API list

### C1250 · quiz · L1 · quiz
> 29. (L2) A broker stops heartbeating to the controller. After which default timeout is it fenced? → broker.session.timeout.ms = 9 000 ms — 💡 Brokers heartbeat every broker.heartbeat.interval.ms (2 000 ms). With no heartbeat for broker.session.timeout.ms (9 000 ms) the lease expires and the broker is fenced. session.timeout.ms is the consumer group setting.
- pass 1: ✅ raft/.../KRaftConfigs.java:39-45 — "BROKER_SESSION_TIMEOUT_MS_DEFAULT = 9000"; heartbeat 2000
- pass 2: ✅ kafka-4.3.1-src/raft/src/main/java/org/apache/kafka/raft/KRaftConfigs.java — BROKER_SESSION_TIMEOUT_MS_DEFAULT = 9000, BROKER_HEARTBEAT_INTERVAL_MS_DEFAULT = 2000

### C1251 · quiz · L1 · quiz
> 30. (L2) How many controller failures can a 3-controller KRaft quorum survive while staying available? → 1 — 💡 A majority must be alive: 3 controllers tolerate 1 failure and 5 tolerate 2 (2N + 1 for N failures).
- pass 1: ✅ docs/operations/kraft.md:47 — "With 3 controllers, the cluster can tolerate 1 controller failure"
- pass 2: ✅ kafka-4.3.1-src/docs/design/design.md:329 — majority quorum: "To tolerate one failure requires three copies … two failures requires five"

### C1252 · quiz · L1 · quiz
> 31. (L2) Leader LEO = 10; followers in the ISR have LEO 10 and 7, and min.insync.replicas is satisfied. What is the high watermark and what can a consumer read? → HW 7, offsets 0–6 — 💡 HW = the smallest LEO among in-sync replicas (and it only advances while the ISR has at least min.insync.replicas members). Consumers only receive records below the HW (committed records).
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ Partition.scala:1000-1008 — HW = smallest LEO among (maximal) ISR → 7; consumers read below HW (0–6)

### C1253 · quiz · L1 · quiz
> 32. (L2) How does the leader learn how far a follower has replicated? → From the FetchOffset in the follower's next Fetch request, which equals the follower's LEO — 💡 The fetch loop is the protocol: FetchOffset reveals the follower's LEO, and the fetch response carries the leader's HighWatermark back to the follower.
- pass 1: ✅ clients/.../message/FetchRequest.json:99; FetchResponse.json:73 — FetchOffset; HighWatermark
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/resources/common/message/FetchRequest.json (FetchOffset) / FetchResponse.json (HighWatermark) — follower fetch offset = its LEO

### C1254 · quiz · L1 · quiz
> 33. (L2) A follower is 50 000 records behind but catching up quickly and reaches the leader's log end every few seconds. Does it leave the ISR? → No, ISR membership is time-based (replica.lag.time.max.ms, 30 s), not record-count based — 💡 A follower is out of sync only if it hasn't caught up to the leader's LEO within replica.lag.time.max.ms. That covers both stuck and slow followers.
- pass 1: ✅ server/.../ReplicationConfigs.java:55-57; Partition.scala:1142-1152 — "hasn't consumed up to the leader's log end offset for at least this time"
- pass 2: ✅ kafka-4.3.1-src/server/.../ReplicationConfigs.java:55-57 — 30000L; "hasn't consumed up to the leader's log end offset for at least this time"

### C1255 · quiz · L1 · quiz
> 34. (L2) RF=3, min.insync.replicas=2, acks=all. Two followers crash. What happens to new writes? → They are rejected with NOT_ENOUGH_REPLICAS because the ISR (1) is below min.insync.replicas (2) — 💡 min.insync.replicas turns "acks=all" into a real durability floor. It trades availability for not losing acknowledged data.
- pass 1: ✅ core/.../cluster/Partition.scala:1230-1236; Errors.java:220 — "insufficient to satisfy the min.isr requirement"
- pass 2: ✅ ReplicationConfigs.java / design.md — min.insync.replicas with acks=all rejects when ISR < min ISR (NOT_ENOUGH_REPLICAS)

### C1256 · quiz · L1 · quiz
> 35. (L2) With the default classic consumer config, a new consumer joins the group. What happens to the existing members? → They revoke all their partitions during the rebalance (eager), because the default strategy list starts with RangeAssignor — 💡 The default is [RangeAssignor, CooperativeStickyAssignor], which uses Range (eager) until you remove Range with a rolling bounce.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:163-164; ConsumerPartitionAssignor.java:250-252 — "will use the RangeAssignor by default"; EAGER "revoke all its owned partitions"
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:163 — "default assignor is [RangeAssignor, CooperativeStickyAssignor], which will use the RangeAssignor by default"

### C1257 · quiz · L1 · quiz
> 36. (L2) What does the KIP-848 consumer protocol (group.protocol=consumer) change compared with classic? → Assignment runs on the group coordinator and members are reconciled incrementally via heartbeats, with no JoinGroup/SyncGroup barrier — 💡 The server-side assignor (uniform by default) computes a target assignment. Each member revokes/receives partitions through ConsumerGroupHeartbeat, and the client configs session.timeout.ms, heartbeat.interval.ms and partition.assignment.strategy are no longer usable.
- pass 1: ✅ docs/operations/consumer-rebalance-protocol.md:32,47,72-76 — "fully incremental design… no longer relies on a global synchronization barrier"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/consumer-rebalance-protocol.md:49,87-91 — "uniform is the default one"; heartbeat.interval.ms, session.timeout.ms, partition.assignment.strategy "no longer usable"

### C1258 · quiz · L1 · quiz
> 37. (L2) Your consumer's processing takes 7 minutes per poll batch with default settings. What happens? → It exceeds max.poll.interval.ms (5 min), leaves the group, its partitions are reassigned, and its next commit fails — 💡 Heartbeats prove the process is alive; max.poll.interval.ms proves it is making progress. Raise it or reduce max.poll.records.
- pass 1: ✅ clients/.../CommonClientConfigs.java:192-195; KafkaConsumer.java:141-146; ConsumerConfig.java:629 — "considered failed and the group will rebalance"; CommitFailedException
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:629 max.poll.interval.ms 300000; exceeding it → consumer leaves group, later commit fails with CommitFailedException

### C1259 · quiz · L1 · quiz
> 38. (L2) Which broker hosts the group coordinator for group "billing"? → The leader of __consumer_offsets partition abs("billing".hashCode()) % offsets.topic.num.partitions — 💡 Group state and commits are records in that __consumer_offsets partition, so the coordinator lives with its leader and fails over with it.
- pass 1: ✅ group-coordinator/.../GroupCoordinatorService.java:427; core/.../KafkaApis.scala:1286-1290 — "Utils.abs(groupId.hashCode()) % numPartitions"; coordinator = partition leaderId
- pass 2: ✅ kafka-4.3.1-src/group-coordinator/.../GroupCoordinatorService.java:427 — "Utils.abs(groupId.hashCode()) % numPartitions"

### C1260 · quiz · L1 · quiz
> 39. (L2) An idempotent producer retries a batch whose ack was lost; the original was already appended. What does the leader do? → Recognises the same PID, epoch and sequence range among the last 5 batches, skips the append and returns the original offsets — 💡 The per-partition producer state keeps metadata for the last 5 batches (NUM_BATCHES_TO_RETAIN = 5). A match is a duplicate: no second copy, same offsets returned.
- pass 1: ✅ storage/.../log/ProducerStateEntry.java:35; UnifiedLog.java:1247-1253 — "NUM_BATCHES_TO_RETAIN = 5"; "returning AppendInfo from duplicate batch"
- pass 2: ✅ kafka-4.3.1-src/storage/.../ProducerStateEntry.java:35 — "NUM_BATCHES_TO_RETAIN = 5"; duplicate batch is not re-appended and its original metadata is returned

### C1261 · quiz · L1 · quiz
> 40. (L2) Why must max.in.flight.requests.per.connection be ≤ 5 for idempotence? → The broker keeps state for only the last 5 batches per producer and partition, so an older retried batch could no longer be recognised — 💡 Straight from the producer docs: "broker only retains at most 5 batches for each producer". Ordering is still preserved for any allowed value.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:275-276 — "broker only retains at most 5 batches for each producer"
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:273-276 — "broker only retains at most 5 batches for each producer"; :342 "message ordering preserved for any allowable value"

### C1262 · quiz · L1 · quiz
> 41. (L2) You set acks=1 and did NOT set enable.idempotence. What do you get? → Idempotence is silently disabled (one INFO log line), so retries can create duplicates — 💡 Idempotence is on by default only if no conflicting configs are set. Set enable.idempotence=true explicitly and the same config fails loudly with a ConfigException.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:610-617 — "Idempotence will be disabled because {} is set to {}, not set to 'all'"
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:616 — log.info "Idempotence will be disabled because acks is set to …"; :345-347 explicit enable + conflict → ConfigException

### C1263 · quiz · L1 · quiz
> 42. (L3) A read_committed consumer is stuck while the partition's high watermark keeps growing. Most likely cause? → A transaction on that partition is still open, so the last stable offset (LSO) is not moving — 💡 read_committed consumers only get records below the LSO — the first offset of the oldest open transaction. One hung transaction holds back everything behind it until it commits, aborts, or hits transaction.timeout.ms.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:352-354 — "read_committed consumers will not be able to read up to the high watermark when there are in flight transactions"
- pass 2: ✅ design.md:223 / isolation.level doc — read_committed reads only up to the LSO; open txn holds LSO until commit/abort/transaction.timeout.ms

### C1264 · quiz · L1 · quiz
> 43. (L3) How does exactly-once consume–transform–produce move the consumer's offset? → The producer writes it into the transaction with sendOffsetsToTransaction, so offsets and output commit or abort together — 💡 Only the producer is transactional. The group's offset is a record in __consumer_offsets, and sendOffsetsToTransaction makes it part of the same atomic transaction as the output.
- pass 1: ✅ docs/design/design.md:203,213 — "we can write the offset to Kafka in the same transaction as the output topics"
- pass 2: ✅ KafkaProducer.sendOffsetsToTransaction javadoc — offsets committed as part of the transaction; TransactionManager.java:433

### C1265 · quiz · L1 · quiz
> 44. (L3) An old instance wakes from a GC pause and sends with the same transactional.id after a new instance called initTransactions(). What happens? → The old instance is fenced: its stale-epoch write is rejected (INVALID_PRODUCER_EPOCH), and its next coordinator call fails with ProducerFencedException — 💡 initTransactions() bumps the producer epoch for that transactional.id and aborts the old incarnation's open transaction. Writes carrying the old epoch are refused.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ AddPartitionsToTxnManager.java:172-175; TransactionManager.java:1792-1795 — stale produce → INVALID_PRODUCER_EPOCH, coordinator call → ProducerFencedException

### C1266 · quiz · L1 · quiz
> 45. (L3) What does transaction version 2 (KIP-890) change? → The producer epoch is bumped at the end of every transaction, and partitions are added to the transaction on the server side — 💡 TV2 bumps the epoch per transaction, so a late request from the previous transaction can't join the next one. The client also stops sending AddPartitionsToTxn and AddOffsetsToTxn.
- pass 1: ✅ docs/operations/transaction-protocol.md:31,43; TransactionManager.java:433-469 — "the producer epoch is bumped on every transaction"
- pass 2: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:224 — "producer epoch is bumped on every transaction"; TransactionManager.java:433 — "In transaction V2, the client will skip sending AddOffsetsToTxn"

### C1267 · quiz · L1 · quiz
> 46. (L3) A compacted topic still shows two records for key A after the cleaner ran. Why is that not a bug? → The newest A may be in the active segment (or within min.compaction.lag.ms / past the LSO), which the cleaner never touches — 💡 The cleanable range ends at the minimum of the LSO, the active segment's base offset and the min-lag boundary. Duplicates per key are normal; consumers must treat the topic as "latest wins".
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogCleanerManager.java:727-742 — "We cannot clean past: 1. The active segment 2. The last stable offset"
- pass 2: ✅ kafka-4.3.1-src/storage/.../LogCleanerManager.java:733-742 — min of lastStableOffset, activeSegment.baseOffset, min-compaction-lag segment

### C1268 · quiz · L1 · quiz
> 47. (L3) After compaction, what happens to the offsets of surviving records? → They keep their original offsets; removed offsets become gaps that reads skip over — 💡 Offsets are permanent identifiers. A fetch from a removed offset returns the next surviving record.
- pass 1: ✅ docs/design/design.md:409,423 — "The offset for a message never changes."
- pass 2: ✅ design.md log compaction section — "offset of a message never changes"; fetch of removed offset returns next higher offset

### C1269 · quiz · L1 · quiz
> 48. (L3) A consumer rebuilds a cache from a compacted topic but takes three days to catch up with delete.retention.ms at its default. What can go wrong? → It may miss tombstones that were removed in the meantime and keep deleted keys in its cache — 💡 Tombstones survive delete.retention.ms (24 h by default) after being stamped. A reader that lags longer can miss the delete.
- pass 1: ✅ docs/design/design.md:424; storage/src/main/java/org/apache/kafka/storage/internals/log/LogConfig.java:129 — "possible for a consumer to miss delete markers if it lags by more than delete.retention.ms"
- pass 2: ✅ LogConfig.java:129 — DEFAULT_DELETE_RETENTION_MS = 24h; design.md: consumer must reach head within delete.retention.ms to see tombstones

### C1270 · quiz · L1 · quiz
> 49. (L3) Produce RequestQueueTimeMs is high and RequestHandlerAvgIdlePercent is near 0. Where is the bottleneck? → I/O handler threads are saturated — look at LocalTimeMs and the disk before adding threads — 💡 Requests wait in the request queue for a free I/O thread. The handler idle gauge tells you they are all busy; LocalTimeMs tells you why.
- pass 1: ✅ docs/operations/monitoring.md:698,815 — "Time the request waits in the request queue / RequestHandlerAvgIdlePercent"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/monitoring.md:811-818 RequestHandlerAvgIdlePercent; RequestQueueTimeMs = waiting for I/O thread; LocalTimeMs = processing at leader

### C1271 · quiz · L1 · quiz
> 50. (L3) What happens when the request queue reaches queued.max.requests? → Network threads stop reading new requests until there is room — back-pressure — 💡 The docs define queued.max.requests as the number of queued requests allowed "before blocking the network threads".
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:143-145 — "before blocking the network threads"
- pass 2: ✅ kafka-4.3.1-src/server/.../SocketServerConfigs.java:145 — "number of queued requests allowed for data-plane, before blocking the network threads"

### C1272 · quiz · L1 · quiz
> 51. (L3) Why is a high RemoteTimeMs for FetchConsumer usually harmless? → An idle consumer's fetch waits in the fetch purgatory up to fetch.max.wait.ms for fetch.min.bytes of data — that is long polling — 💡 Delayed fetches sit in purgatory by design. RemoteTime on Produce (acks=all) is the one that reflects follower replication speed.
- pass 1: ✅ docs/operations/monitoring.md:672; core/src/main/scala/kafka/server/DelayedFetch.scala:55-66 — "size depends on fetch.wait.max.ms in the consumer"
- pass 2: ✅ monitoring.md RemoteTimeMs ("waiting for follower", fetch purgatory); fetch.min.bytes / fetch.max.wait.ms long polling

### C1273 · quiz · L1 · quiz
> 52. (L3) A former leader rejoins as a follower. How does it decide which records to truncate? → It uses leader epochs: the leader reports where the follower's last epoch ended in the leader's log, and the follower truncates from there — 💡 HW-based truncation could lose committed data or leave divergent replicas (KIP-101). Epoch-based truncation removes only records that really diverge.
- pass 1: ✅ https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation — "The leader responds with the offset where the follower's epoch ends"
- pass 2: ✅ design.md / KIP-101 — OffsetsForLeaderEpoch returns end offset of the epoch; follower truncates there

### C1274 · quiz · L1 · quiz
> 53. (L3) With unclean.leader.election.enable=true, an out-of-sync replica becomes leader. What can happen? → Committed (acknowledged) records it lacked are lost, and offsets can be reused for different data — 💡 Its log becomes the source of truth. Returning replicas truncate to match it, and consumers that had read past the truncation point get LogTruncationException (or reset).
- pass 1: ✅ docs/design/design.md:347; clients/src/main/java/org/apache/kafka/clients/consumer/LogTruncationException.java:20-27 — "previously committed data will be lost, and new data will be written over these offsets"
- pass 2: ✅ LogConfig.java:133 false default; kafka-4.3.1-src/clients/.../consumer/LogTruncationException.java exists — thrown on detected truncation when no reset policy

### C1275 · quiz · L1 · quiz
> 54. (L3) Why can an Eligible Leader Replica be elected safely even though it is not in the ISR? → It was in the ISR when the ISR fell below min.insync.replicas, and the HW cannot advance in that state, so it holds all committed data — 💡 With strict min ISR, nothing new gets committed while the ISR is below min ISR. Replicas with an unclean shutdown are excluded, because they may have lost unflushed data.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:31; metadata/src/main/java/org/apache/kafka/controller/PartitionChangeBuilder.java:545-553 — "the high watermark … can't advance if the size of the ISR is smaller than the min ISR"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/eligible-leader-replicas.md:31 — "high watermark … can't advance if the size of the ISR is smaller than the min ISR"; PartitionChangeBuilder.java:546 "Exclude unclean shutdown replicas"

### C1276 · quiz · L1 · quiz
> 55. (L3) A tiered topic has remote.storage.enable=true but local disk usage did not shrink at all. Most likely cause? → local.retention.ms/bytes were left at -2, which means "use the full retention.ms/bytes" locally — 💡 The local retention settings default to -2 (use total retention). Also check that uploads succeed: local segments are deleted only after they are uploaded.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:159-169; docs/operations/tiered-storage.md:109 — "Default value is -2, it represents log.retention.ms value is to be used"
- pass 2: ✅ kafka-4.3.1-src/storage/.../LogConfig.java:139-140 — local.retention.* = -2 "derived from RetentionBytes/RetentionMs"

### C1277 · quiz · L1 · quiz
> 56. (L3) Which segments may the leader copy to remote storage? → Closed (non-active) segments whose end is below the last stable offset — 💡 The copy task skips the active segment and anything not yet stable, because remote storage should contain only committed data.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManager.java:912-921 — "remote storage should contain only committed/acked messages"
- pass 2: ✅ kafka-4.3.1-src/storage/.../RemoteLogManager.java:923-931 — candidate = previous segment when next baseOffset <= lastStableOffset (active segment excluded)

### C1278 · quiz · L1 · quiz
> 57. (L3) You have 8 partitions and want 40 parallel workers processing independent jobs. What fits? → A share group: members can share partitions, so consumers can outnumber partitions — 💡 In a consumer group each partition has one owner, so 32 of 40 members would idle. Share groups assign partitions to multiple consumers and lock records individually.
- pass 1: ✅ docs/design/design.md:254-255 — "The number of consumers in a share group can exceed the number of partitions in a topic."
- pass 2: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:84 — share group consumers "cooperatively consume … without assigning each partition to just one consumer"

### C1279 · quiz · L1 · quiz
> 58. (L3) A share consumer crashes while holding acquired records. What happens to them? → Their acquisition lock expires and they become available for another delivery attempt (delivery count kept) — 💡 Locks are time-limited (30 s by default). After expiry the records are available again — at-least-once delivery.
- pass 1: ✅ docs/design/design.md:265-267 — "The lock is released automatically once its duration elapses"
- pass 2: ✅ kafka-4.3.1-src/group-coordinator/.../ShareGroupConfig.java — SHARE_GROUP_RECORD_LOCK_DURATION_MS_DEFAULT = 30000; expired locks → available again

### C1280 · quiz · L1 · quiz
> 59. (L3) A record fails every time it is processed. What does a share group do by default? → After the delivery count limit (default 5) it archives the record instead of making it available again — 💡 When a record would return to AVAILABLE with deliveryCount ≥ limit, the broker archives it. 4.3.1 has no working share-group DLQ yet (KIP-1191 is only groundwork: an interface with a no-op implementation).
- pass 1: ❌ fixed in part file (why text) — NoOpShareGroupDLQManager only; archive after delivery count limit (SharePartition)
- pass 2: ✅ ShareGroupConfig.java — SHARE_GROUP_DELIVERY_COUNT_LIMIT_DEFAULT = 5; kafka-4.3.1-src/server-common/.../share/dlq/NoOpShareGroupDLQManager.java (no-op DLQ)

### C1281 · quiz · L1 · quiz
> 60. (L3) Your Streams app reads a 6-partition topic. You start 10 instances with 1 thread each. What happens? → Only 6 tasks exist for that sub-topology, so 4 instances get no active task from it — 💡 Tasks are created per input partition and are the fixed unit of parallelism; extra instances idle (or can host standbys).
- pass 1: ✅ docs/streams/architecture.md:45-47 — "the maximum parallelism … is bounded by the maximum number of stream tasks"
- pass 2: ✅ Streams architecture docs — tasks per input partition; max parallelism = partition count; extra instances idle

### C1282 · quiz · L1 · quiz
> 61. (L3) After an instance crash, a task with a 20 GB store takes minutes to resume. Best fix? → Enable standby replicas so the task moves to an instance that already has a near-current copy of the store — 💡 Without standbys the new owner must replay the whole changelog partition before processing. num.standby.replicas keeps shadow copies up to date.
- pass 1: ✅ docs/streams/architecture.md:90 — "assign a task to an application instance where such a standby replica already exists"
- pass 2: ✅ StreamsConfig.java:896-898 num.standby.replicas default 0; standbys keep replicated state for fast failover

### C1283 · quiz · L1 · quiz
> 62. (L3) What does group.protocol=streams (KIP-1071) change? → Task assignment moves from the client-side StreamsPartitionAssignor to the broker, in a dedicated streams group — 💡 The app registers a streams group and sends its topology in StreamsGroupHeartbeat; the broker computes active/standby/warm-up assignments. Core features GA in 4.2.
- pass 1: ✅ docs/streams/developer-guide/streams-rebalance-protocol.md (Overview); docs/getting-started/upgrade.md:85 — "assignments are computed continuously on the broker"
- pass 2: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:85 — KIP-1071 "production-ready for its core feature set" in 4.2 (broker-driven)

### C1284 · quiz · L1 · quiz
> 63. (L4) A tenant exceeds its producer_byte_rate quota. What does the broker do? → Returns the response immediately with a non-zero ThrottleTimeMs and mutes the client's channel for that time — 💡 Byte-rate and request quotas throttle by delay: the broker computes the delay, returns it in the response (a throttled fetch carries no data), and stops reading from the socket until the delay is over.
- pass 1: ✅ docs/design/design.md:509 — "returns a response with the delay immediately"
- pass 2: ✅ kafka-4.3.1-src/docs/design/design.md:509 — "returns a response with the delay immediately … fetch … will not contain any data … mutes the channel"

### C1285 · quiz · L1 · quiz
> 64. (L4) A quota of producer_byte_rate=10 MB/s is set for user "etl". The user's partitions are led by 5 brokers. Roughly how much can the user produce cluster-wide? → Up to about 50 MB/s, because quotas are enforced per broker — 💡 Quotas are defined and enforced per broker. The design docs chose this because a cluster-wide quota would require sharing usage between brokers.
- pass 1: ✅ docs/design/design.md:499 — "This quota is defined on a per-broker basis"
- pass 2: ✅ kafka-4.3.1-src/docs/design/design.md:507 — "defining these quotas per broker … cluster wide … would require a mechanism to share client quota usage"

### C1286 · quiz · L1 · quiz
> 65. (L4) Which quota type rejects operations with THROTTLING_QUOTA_EXCEEDED instead of only delaying them? → controller_mutation_rate — 💡 The controller mutation quota (create topics, create partitions, delete topics, counted in partitions) is strict: operations above it are rejected with THROTTLING_QUOTA_EXCEEDED.
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/quota/ControllerMutationQuotaManager.java:162 — "rejected with a THROTTLING_QUOTA_EXCEEDED error"
- pass 2: ✅ kafka-4.3.1-src/server/.../quota/StrictControllerMutationQuota.java:28-31 — "does not accept any mutations once the quota is exhausted"; Errors.java:374 THROTTLING_QUOTA_EXCEEDED

### C1287 · quiz · L1 · quiz
> 66. (L4) What is the default linger.ms of the Java producer in Kafka 4.x? → 5 — 💡 The default changed from 0 to 5 in 4.0, because larger batches typically give similar or lower latency under load.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:405 — "define(LINGER_MS_CONFIG, Type.LONG, 5"
- pass 2: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:405 — LINGER_MS default 5; upgrade.md:285

### C1288 · quiz · L1 · quiz
> 67. (L4) RequestHandlerAvgIdlePercent is 0.05 and RequestQueueTimeMs keeps growing. What does it most likely mean? → The request handler (I/O) threads are saturated — 💡 The monitoring docs suggest the idle ratio should ideally be above 0.3. Near zero with a growing queue time means the I/O threads are the bottleneck (or something slow is holding them, like the disk).
- pass 1: ✅ docs/operations/monitoring.md:811 — "The average fraction of time the request handler threads are idle"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/monitoring.md:818 — "between 0 and 1, ideally > 0.3"

### C1289 · quiz · L1 · quiz
> 68. (L4) Why can adding 50,000 partitions to one broker crash it even with plenty of RAM? → Each log segment uses 2 memory map areas, which can exceed vm.max_map_count (often about 65535) — 💡 The OS docs: each segment uses 2 map areas (index and time index). 50000 partitions means 100000 map areas and likely an OutOfMemoryError (Map failed).
- pass 1: ✅ docs/operations/hardware-and-os.md:45 — "each log segment uses 2 map areas"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/hardware-and-os.md:45 — "each log segment uses 2 map areas … 50000 partitions … 100000 map areas … OutOfMemoryError (Map failed)"

### C1290 · quiz · L1 · quiz
> 69. (L4) A group reading a 4-partition topic lags badly. You scale from 4 to 8 consumers. What happens to the lag? → It does not improve: 4 consumers sit idle, and the rebalance pauses work briefly — 💡 Within a group each partition goes to at most one consumer. Beyond the partition count, extra consumers are idle; to get more parallelism you add partitions (and accept that keys remap).
- pass 1: ✅ docs/operations/basic-kafka-operations.md:44 — "the partition count impacts the maximum parallelism of your consumers"
- pass 2: ✅ design.md — one consumer per partition per group; extra consumers idle

### C1291 · quiz · L1 · quiz
> 70. (L4) Producers with acks=all get NotEnoughReplicasException. Which metric confirms the cause? → UnderMinIsrPartitionCount > 0 — 💡 The ISR is smaller than min.insync.replicas, so the broker refuses the write on purpose. The fix is to restore replicas, not to lower durability casually.
- pass 1: ✅ docs/operations/monitoring.md:516 — "name=UnderMinIsrPartitionCount" ; clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasException.java:20 — "lower than min.insync.replicas"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/monitoring.md:516 — "kafka.server:type=ReplicaManager,name=UnderMinIsrPartitionCount"

### C1292 · quiz · L1 · quiz
> 71. (L4) Why is NotEnoughReplicasAfterAppendException more dangerous than NotEnoughReplicasException? → The batch was already appended, so a retry can duplicate it unless the producer is idempotent — 💡 Its javadoc: the low ISR size was discovered after the message was appended to the log, and "Producer retries will cause duplicates".
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasAfterAppendException.java:21 — "Producer retries will cause duplicates"
- pass 2: ✅ kafka-4.3.1-src/clients/.../errors/NotEnoughReplicasAfterAppendException.java:20-21 — "*after* the message was already appended … Producer retries will cause duplicates"

### C1293 · quiz · L1 · quiz
> 72. (L4) MirrorMaker 2 replicates topic "orders" from cluster alias "us-west" with the default policy. What is the topic called in the target? → us-west.orders — 💡 DefaultReplicationPolicy names remote topics {source}.{topic}. The prefix prevents two clusters writing into one partition and lets active/active avoid loops.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:269 — "`{source}.{source_topic_name}`"
- pass 2: ✅ kafka-4.3.1-src/connect/mirror-client/.../DefaultReplicationPolicy.java:39 SEPARATOR_DEFAULT "." → us-west.orders

### C1294 · quiz · L1 · quiz
> 73. (L4) After an MM2 failover using translated checkpoints, what should your consumers expect? → Possibly re-reading a few records, but never skipping any — 💡 OffsetSyncStore translates conservatively (nearest preceding sync, +1 if past it) precisely to avoid skipping data, so failover is at-least-once.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:146 — "This may cause re-reading of records"
- pass 2: ✅ kafka-4.3.1-src/connect/mirror/.../OffsetSyncStore.java:144,153 — "If we overestimate … data loss"; upstreamStep 0 or 1

### C1295 · quiz · L1 · quiz
> 74. (L4) What is the default of sync.group.offsets.enabled in MirrorMaker 2? → false — 💡 Default false. When true, MM2 periodically writes translated offsets into the target's __consumer_offsets, as long as no active consumers of that group are connected to the target.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorCheckpointConfig.java:66 — "SYNC_GROUP_OFFSETS_ENABLED_DEFAULT = false"
- pass 2: ✅ kafka-4.3.1-src/connect/mirror/.../MirrorCheckpointConfig.java:65-66 — "as long as no active consumers in that group are connected"; DEFAULT = false

### C1296 · quiz · L1 · quiz
> 75. (L4) You have rolled all brokers to 4.3 but not run kafka-features.sh. What is true? → The cluster still runs the old metadata.version; reinstalling the old binaries is still an option — 💡 Rolling the binaries and finalizing are separate steps. Until you finalize with upgrade --release-version 4.3, the old metadata.version stays in effect.
- pass 1: ✅ docs/getting-started/upgrade.md:38 — "Once the cluster's behavior and performance have been verified, finalize the upgrade"
- pass 2: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:31-39 — roll binaries then finalize with kafka-features.sh; downgrade possible before finalizing

### C1297 · quiz · L1 · quiz
> 76. (L4) In 4.3.1 you run kafka-features.sh downgrade --release-version 4.2 --unsafe after finalizing 4.3. What happens? → It is refused: 4.3-IV0 has metadata changes and unsafe metadata downgrade is not supported in this version — 💡 FeatureControlManager rejects it with "Unsafe metadata downgrade is not supported in this version." (without --unsafe: "Refusing to perform the requested downgrade because it might delete metadata information.").
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:406 — "Unsafe metadata downgrade is not supported in this version"
- pass 2: ✅ kafka-4.3.1-src/metadata/.../FeatureControlManager.java:405-411 — both refusal messages as quoted; MetadataVersion.java:125 IBP_4_3_IV0(…, true)

### C1298 · quiz · L1 · quiz
> 77. (L4) Which feature flag enables Eligible Leader Replicas, and from which release is it on by default for new clusters? → eligible.leader.replicas.version, 4.1 — 💡 ELRV_1 bootstraps at MetadataVersion 4.1-IV0 (and depends on metadata.version ≥ 4.0-IV1); the 4.1 notes say ELR is enabled by default on new clusters.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/EligibleLeaderReplicasVersion.java:27 — "ELRV_1(1, MetadataVersion.IBP_4_1_IV0" ; docs/getting-started/upgrade.md:173 — "will be enabled by default on the new clusters"
- pass 2: ✅ kafka-4.3.1-src/server-common/.../EligibleLeaderReplicasVersion.java:27 — "ELRV_1(1, MetadataVersion.IBP_4_1_IV0, … IBP_4_0_IV1"; upgrade.md:173

### C1299 · quiz · L1 · quiz
> 78. (L4) A 3.7 cluster still runs ZooKeeper. What is the supported path to 4.3? → Migrate to KRaft using a bridge release (the last one is 3.9), then upgrade to 4.3 — 💡 4.x is KRaft-only. ZooKeeper clusters must migrate first, and the kraft docs say the last bridge release is Kafka 3.9.
- pass 1: ✅ docs/operations/kraft.md:296 — "The last bridge release is Kafka 3.9"
- pass 2: ✅ kafka-4.3.1-src/docs/operations/kraft.md:296 — "you need to use a bridge release. The last bridge release is Kafka 3.9."

### C1300 · quiz · L1 · quiz
> 79. (L4) Your producer callback receives a TimeoutException. With default configs, what has already happened? → The client retried the retriable error until delivery.timeout.ms (120 s) expired; re-sending blindly risks reordering and bypasses idempotence — 💡 retries defaults to Integer.MAX_VALUE and the real budget is delivery.timeout.ms (120000 ms). A retriable exception reaching your callback usually means that budget is spent (the other case: send() already waited max.block.ms for metadata or buffer space). Park it durably instead of blindly resending.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ ProducerConfig.java:390,406 — retries Integer.MAX_VALUE, delivery.timeout.ms 120*1000; KafkaProducer.java:1049-1054 max.block.ms timeout via callback

### C1301 · quiz · L1 · quiz
> 80. (L4) Which of these is NON-retriable for the producer? → RecordTooLargeException — 💡 NotEnoughReplicasException and NotLeaderOrFollowerException (an InvalidMetadataException) extend RetriableException. RecordTooLargeException is listed as non-retriable: the record will never be sent.
- pass 1: ✅ kafka-4.3.1-src/clients/.../producer/Callback.java:~33-51; NotLeaderOrFollowerException.java:27 — "Non-Retriable exceptions (fatal...): RecordTooLargeException"
- pass 2: ✅ kafka-4.3.1-src/clients/.../errors/RecordTooLargeException.java:26 "extends ApiException" (not Retriable); NotEnoughReplicasException.java:22 "extends RetriableException"

### C1302 · quiz · L1 · quiz
> 81. (L4) A consumer loop catches RecordDeserializationException, logs it and calls poll() again. What happens? → It hits the same record again forever, because the position stays on it until you seek past it — 💡 The exception message says "please seek past the record to continue consumption". Quarantine keyBuffer()/valueBuffer(), then seek(topicPartition(), offset() + 1).
- pass 1: ✅ kafka-4.3.1-src/clients/.../consumer/internals/CompletedFetch.java:266-268,344 — "please seek past the record to continue consumption"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/internals/CompletedFetch.java:344 — "please seek past the record to continue consumption"; RecordDeserializationException.java:92-112 accessors

### C1303 · quiz · L1 · quiz
> 82. (L4) commitSync() throws CommitFailedException. What is the right reaction? → Don't retry: the group rebalanced and the partitions may belong to another member. Records will be re-processed, so processing must be idempotent — 💡 Its javadoc says the commit "cannot generally be retried because some of the partitions may have already been assigned to another member". The usual cause is exceeding max.poll.interval.ms.
- pass 1: ✅ kafka-4.3.1-src/clients/.../consumer/CommitFailedException.java:20-24 — "cannot generally be retried because some of the partitions may have already been assigned"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/CommitFailedException.java:22-24 — "cannot generally be retried because some of the partitions may have already been assigned"

### C1304 · quiz · L1 · quiz
> 83. (L4) Since Kafka 4.1, what happens if you call producer.flush() inside a send callback? → It throws a KafkaException because it may deadlock — 💡 KafkaProducer.flush() detects being called on the callback path and throws: "invocation inside a callback is not permitted because it may lead to deadlock".
- pass 1: ✅ kafka-4.3.1-src/clients/.../producer/KafkaProducer.java:1218-1219 — "invocation inside a callback is not permitted because it may lead to deadlock"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/KafkaProducer.java:1219 exact message; upgrade.md:175 (4.1.0 notes) "prohibits its use inside a callback"

### C1305 · quiz · L1 · quiz
> 84. (L4) With Spring for Apache Kafka 4.1.1 and no error handler configured, a listener keeps throwing on one record. What happens? → DefaultErrorHandler tries 10 deliveries with no back-off (FixedBackOff(0, 9)), then logs the record at ERROR and skips it — 💡 SeekUtils.DEFAULT_BACK_OFF = new FixedBackOff(0, DEFAULT_MAX_FAILURES - 1) with DEFAULT_MAX_FAILURES = 10, and the default recoverer only logs. You must add DeadLetterPublishingRecoverer yourself.
- pass 1: ✅ spring-kafka-4.1.1/.../listener/SeekUtils.java:57,63; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:139 — "new FixedBackOff(0, DEFAULT_MAX_FAILURES - 1) / after ten failures, the failed record is logged"
- pass 2: ✅ spring-kafka-4.1.1/spring-kafka/.../listener/SeekUtils.java:57,63 — "DEFAULT_MAX_FAILURES = 10", "new FixedBackOff(0, DEFAULT_MAX_FAILURES - 1)"; FailedRecordTracker.java:87 logger.error

### C1306 · quiz · L1 · quiz
> 85. (L4) DeadLetterPublishingRecoverer with the default resolver publishes a failed record from orders partition 7 to… → orders-dlt, partition 7, so the DLT needs at least as many partitions as orders — 💡 Since 3.3 the default suffix is -dlt, and the default resolver keeps the original partition. The docs: the DLT "must have at least as many partitions as the original topic".
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:874-876 — "<originalTopic>-dlt ... must have at least as many partitions as the original topic"
- pass 2: ✅ spring-kafka-4.1.1/spring-kafka/.../listener/DeadLetterPublishingRecoverer.java:76 — "cr.topic() + \"-dlt\", cr.partition()"; annotation-error-handling.adoc:876; change-history 3.3

### C1307 · quiz · L1 · quiz
> 86. (L4) What is the main trade-off of @RetryableTopic (non-blocking retries) versus DefaultErrorHandler blocking retries? → Non-blocking retries keep the main partition flowing but lose ordering; blocking retries keep order but stall the partition — 💡 The docs: "By using this strategy you lose Kafka's ordering guarantees for that topic." Blocking retries redeliver in place, so later records wait.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "By using this strategy you lose Kafka's ordering guarantees for that topic"
- pass 2: ✅ spring-kafka-4.1.1/…/pages/retrytopic/how-the-pattern-works.adoc:12 — "By using this strategy you lose Kafka's ordering guarantees for that topic."

### C1308 · quiz · L1 · quiz
> 87. (L4) Which combination does Spring Kafka 4.1.1 NOT support? → @RetryableTopic with batch listeners or with container transactions — 💡 The docs mark both as unsupported: non-blocking retries don't work with batch listeners and "cannot combine with Container Transactions".
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic.adoc:12,14 — "not supported with Batch Listeners / cannot combine with Container Transactions"
- pass 2: ✅ spring-kafka-4.1.1/…/pages/retrytopic.adoc:12,14 — "not supported with … Batch Listeners"; "cannot combine with … Container Transactions"

### C1309 · quiz · L1 · quiz
> 88. (L4) Why wrap your deserializer in ErrorHandlingDeserializer? → Because deserialization happens inside poll(), before the container can handle errors. The wrapper turns failures into a header so the error handler (and DLT) can deal with the record — 💡 The docs: Spring "has no way to handle the problem, because it occurs before the poll() returns". With the wrapper, DeserializationException is fatal (no retries) and goes straight to the recoverer.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/serdes.adoc:525-527 — "Spring has no way to handle the problem, because it occurs before the poll() returns"
- pass 2: ✅ spring-kafka-4.1.1/…/pages/kafka/serdes.adoc:524 — "Spring has no way to handle the problem, because it occurs before the poll() returns"; annotation-error-handling.adoc:399 DeserializationException fatal

### C1310 · quiz · L1 · quiz
> 89. (L4) You need 12 workers on a 3-partition topic, per-record acknowledgement and a delivery-attempt limit, and ordering does not matter. Which in-Kafka option fits? → A share group (Kafka 4.2+) — 💡 Share consumers can exceed the partition count, acknowledge individually and have delivery counts (broker default limit 5). A 12-member consumer group would leave 9 idle, and twelve groups would each read every record.
- pass 1: ✅ kafka-4.3.1-src/docs/design/design.md:255-257; ShareGroupConfig.java:49 — "can exceed the number of partitions / DELIVERY_COUNT_LIMIT_DEFAULT = 5"
- pass 2: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:84 — "Queues for Kafka (KIP-932) is production-ready in Apache Kafka 4.2 … per-record acknowledgement and counting of delivery attempts"

### C1311 · quiz · L1 · quiz
> 90. (L4) Which statement about RabbitMQ 4.3 quorum queues is correct? → They are Raft-replicated queues; since 4.0 the delivery limit defaults to 20, after which messages are dropped or dead-lettered — 💡 Quorum queues are "a durable, replicated queue based on the Raft consensus algorithm", and messages are removed once acknowledged. RabbitMQ docs note that redeliveries and multiple consumers can change FIFO order.
- pass 1: ✅ https://www.rabbitmq.com/docs/quorum-queues; https://www.rabbitmq.com/docs/queues — "Starting with RabbitMQ 4.0, the delivery limit for quorum queues defaults to 20"
- pass 2: ✅ https://www.rabbitmq.com/docs/quorum-queues (4.3) — "durable, replicated queue based on the Raft"; "Starting with RabbitMQ 4.0, the delivery limit defaults to 20"; "dropped … or dead-lettered"

### C1312 · quiz · L1 · quiz
> 91. (L4) What mainly distinguishes Redpanda from Apache Kafka for an application developer? → It speaks the Kafka API but is a different implementation (C++, single binary, Raft per partition), so compare operations and feature parity, not semantics — 💡 Redpanda documents Kafka API compatibility, a C++ single binary and a Raft group per partition. Check support for newer Kafka features (KIP-848, share groups) before relying on them.
- pass 1: ✅ https://docs.redpanda.com/current/get-started/intro-to-events/ + /architecture/ — "compatible with the Kafka API / Built on C++ / every topic partition forms a Raft group"
- pass 2: ✅ https://docs.redpanda.com/current/get-started/architecture/ — "interact with Redpanda using the Kafka API"; "each topic partition forming a Raft group" (Seastar/C++)

### C1313 · quiz · L1 · quiz
> 92. (L4) Pulsar can serve both queue-like and stream-like consumption from one topic mainly because… → Each subscription keeps its own cursor, and subscription types (Exclusive, Failover, Shared, Key_Shared) choose how consumers share it — 💡 Multiple independent subscriptions per topic, each with its own cursor, plus the four subscription types. Key_Shared keeps per-key order across many consumers.
- pass 1: ✅ https://pulsar.apache.org/docs/next/concepts-messaging/ — "Each subscription maintains its own cursor ... Key_Shared ... same key ... one consumer"
- pass 2: ✅ https://pulsar.apache.org/docs/next/concepts-messaging/ — four subscription types; "an associated cursor is created"; Key_Shared same key → one consumer
