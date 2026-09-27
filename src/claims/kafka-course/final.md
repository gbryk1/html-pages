# Claims ledger — kafka-course.html — final

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1165 · final · L1 · p
> One or more questions per chapter. Questions tagged (L2)–(L4) belong to the deeper levels; the wrong answers are the misconceptions the chapters took apart. Click an answer to see why.
- pass 1: n/a (intro text / finale summary — each statement is a chapter claim validated there)
- pass 2: n/a — UI/pedagogy

### C1166 · final · L1 · tr
> linger.ms | 5 (since 4.0) — max wait to fill a batch | Raise for throughput; lower only for very latency-sensitive, low-volume producers
- pass 1: ✅ restates chapter claim — part-1.notes.md:60 "- linger.ms default 5; changed from 0 in 4.0; rationale; linger=50 example → ProducerConfig.java:148-160,405 → "The default changed from 0 t"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:405 — "LINGER_MS_CONFIG, Type.LONG, 5"; upgrade.md:285 (since 4.0); advice n/a

### C1167 · final · L1 · tr
> batch.size | 16384 bytes — max per-partition batch | Raise for high volume or with compression
- pass 1: ✅ restates chapter claim — part-1.notes.md:56 "- Partitioner: key → hash; no key → sticky partition changing after ≥ batch.size bytes; RoundRobinPartitioner + KAFKA-9965 note; custom Part"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:401 — "BATCH_SIZE_CONFIG, Type.INT, 16384"

### C1168 · final · L1 · tr
> acks | all — leader waits for the full ISR | Keep all; 0/1 only when losing data is acceptable
- pass 1: ✅ restates chapter claim — part-1.notes.md:49 "- send() asynchronous, adds to buffer and returns; Future; callbacks → C/clients/producer/KafkaProducer.java:126-127,840,868 → "The send() m"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:391-393 — acks default "all" (leader waits for full ISR)

### C1169 · final · L1 · tr
> auto.offset.reset | latest — where a group without a committed offset starts | earliest to read existing data; none to fail loudly
- pass 1: ✅ restates chapter claim — part-1.notes.md:90 "- auto.offset.reset default latest; earliest/latest/by_duration/none → ConsumerConfig.java:172-181,544-548"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/ConsumerConfig.java:544-546 — "AutoOffsetResetStrategy.LATEST"; none/earliest valid values

### C1170 · final · L1 · tr
> enable.auto.commit | true, every auto.commit.interval.ms = 5000 | Turn off for manual commit after processing
- pass 1: ✅ restates chapter claim — part-1.notes.md:86 "- enable.auto.commit default true; auto.commit.interval.ms default 5000 → ConsumerConfig.java:140,458-467"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/ConsumerConfig.java:458-465 — enable.auto.commit true, auto.commit.interval.ms 5000

### C1171 · final · L1 · tr
> min.insync.replicas | 1 — min ISR size for acks=all writes and for records to become committed/visible | Set 2 with RF 3
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ server-common ServerLogConfigs / core Partition.scala:1011,1234 — "The HW can only advance if the ISR size is equal or large than the min ISR"; default 1

### C1172 · final · L1 · tr
> default.replication.factor | 1 — RF for topics created without one | Set 3 in production (or pass --replication-factor 3)
- pass 1: ✅ restates chapter claim — part-1.notes.md:107 "- default.replication.factor default 1 → server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:40-41"
- pass 2: ✅ kafka-4.3.1-src/server/.../ReplicationConfigs.java:42 — "REPLICATION_FACTOR_DEFAULT = 1"

### C1173 · final · L1 · tr
> retention.ms / retention.bytes | 604800000 (7 d) / -1 (per partition, unlimited) | Match how far back consumers must re-read; cap disk
- pass 1: ✅ restates chapter claim — part-1.notes.md:130 "- retention.ms default 7 days (604800000) ; -1 no limit → storage/.../LogConfig.java:128,202; TopicConfig.java:76-79"
- pass 2: ✅ kafka-4.3.1-src/storage/.../log/LogConfig.java:128 retention 7 d; ServerLogConfigs.java:81 "LOG_RETENTION_BYTES_DEFAULT = -1L" (per partition)

### C1174 · final · L1 · tr
> cleanup.policy | delete — or compact (latest value per key) | compact for current-state topics
- pass 1: ✅ restates chapter claim — part-1.notes.md:129 "- cleanup.policy default delete; compact; "delete,compact" → TopicConfig.java:156-162; ServerLogConfigs.java:89"
- pass 2: ✅ ServerLogConfigs.java:89 — "LOG_CLEANUP_POLICY_DEFAULT = TopicConfig.CLEANUP_POLICY_DELETE"

### C1175 · final · L1 · tr
> kafka-consumer-groups.sh --describe --group G | CURRENT-OFFSET, LOG-END-OFFSET, LAG per partition | Checking if a group keeps up
- pass 1: ✅ restates chapter claim — part-1.notes.md:15 "- kafka-topics flags --bootstrap-server --create --topic --partitions --describe → TopicCommand.java:720-760 (accepts(...))"
- pass 2: ✅ kafka-4.3.1-src/tools/.../consumer/group/ConsumerGroupCommand.java:342 — columns "CURRENT-OFFSET", "LOG-END-OFFSET", "LAG"

### C1176 · final · L1 · tr
> segment.bytes | 1073741824 (1 GiB): max segment file size | Lower it for compacted or short-retention topics so data becomes deletable sooner
- pass 1: ✅ restates chapter claim — part-1.notes.md:132 "- segment.bytes default 1 GiB; segment.ms default 7 days → LogConfig.java:125-126,188-190"
- pass 2: ✅ kafka-4.3.1-src/storage/.../log/LogConfig.java:125 — "DEFAULT_SEGMENT_BYTES = 1024 * 1024 * 1024"

### C1177 · final · L1 · tr
> segment.ms | 604800000 (7 d): force a roll after this age | Low-volume topics where retention/compaction must act on time
- pass 1: ✅ restates chapter claim — part-1.notes.md:132 "- segment.bytes default 1 GiB; segment.ms default 7 days → LogConfig.java:125-126,188-190"
- pass 2: ✅ kafka-4.3.1-src/storage/.../log/LogConfig.java:126 — "DEFAULT_SEGMENT_MS = 24 * 7 * 60 * 60 * 1000L"

### C1178 · final · L1 · tr
> index.interval.bytes | 4096: bytes between sparse offset-index entries | Almost never changed
- pass 1: ✅ restates chapter claim — part-2.notes.md:16 "- index.interval.bytes 4096 → server-common/src/main/java/org/apache/kafka/server/config/ServerLogConfigs.java:97; doc clients/.../common/co"
- pass 2: ✅ ServerLogConfigs.java:97 — "LOG_INDEX_INTERVAL_BYTES_DEFAULT = 4096"

### C1179 · final · L1 · tr
> compression.type (producer / topic) | none / producer: batch codec; topic "producer" keeps the producer's codec | Enable zstd/lz4 on producers; leave the topic at producer to avoid recompression
- pass 1: ✅ restates chapter claim — part-1.notes.md:70 "- compression.type default none; gzip/snappy/lz4/zstd → ProducerConfig.java:397; design.md:119"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:397 producer default none; ServerLogConfigs.java:178 topic/broker default "producer"

### C1180 · final · L1 · tr
> kafka-dump-log.sh --files f.log [--cluster-metadata-decoder] | Print batch headers (offsets, producerId, sequence, codec, crc) or decoded metadata records | Inspect segments, idempotence state, KRaft metadata
- pass 1: ✅ restates chapter claim — part-2.notes.md:37 "- kafka-dump-log.sh batch line fields (baseOffset… compresscodec crc isvalid); index dump "offset: … position: …" → tools/src/main/java/org/"
- pass 2: ✅ kafka-4.3.1-src/tools/.../DumpLogSegments.java:512-533 prints baseOffset, producerId, baseSequence, compresscodec, crc; :821 files, :853 cluster-metadata-decoder

### C1181 · final · L1 · tr
> broker.session.timeout.ms | 9000: broker lease without heartbeats (heartbeat every 2000 ms) | Tune failover speed vs false fencing
- pass 1: ✅ restates chapter claim — part-1.notes.md:32 "- Controller manages broker registration; heartbeats; broker.session.timeout.ms → design.md:289-296"
- pass 2: ✅ kafka-4.3.1-src/raft/.../KRaftConfigs.java — BROKER_SESSION_TIMEOUT_MS_DEFAULT = 9000, BROKER_HEARTBEAT_INTERVAL_MS_DEFAULT = 2000

### C1182 · final · L1 · tr
> kafka-metadata-quorum.sh describe --status | --replication | Leader id, epoch, high watermark, voters/observers, follower lag | Checking controller quorum health
- pass 1: ✅ restates chapter claim — part-1.notes.md:15 "- kafka-topics flags --bootstrap-server --create --topic --partitions --describe → TopicCommand.java:720-760 (accepts(...))"
- pass 2: ✅ kafka-4.3.1-src/tools/.../MetadataQuorumCommand.java:183,189 (--status, --replication); kraft.md:230-240 LeaderId, LeaderEpoch, HighWatermark, MaxFollowerLag, voters/observers

### C1183 · final · L1 · tr
> replica.lag.time.max.ms | 30000: follower not caught up for this long → removed from the ISR | Rarely; fix slow brokers instead of raising it
- pass 1: ✅ restates chapter claim — part-1.notes.md:110 "- In-sync definition (session + not too far behind); ISR removal; replica.lag.time.max.ms → design.md:289-298"
- pass 2: ✅ ReplicationConfigs.java:55-57 — 30000L; "hasn't consumed up to the leader's log end offset … remove the follower from ISR"

### C1184 · final · L1 · tr
> group.protocol | classic: consumer rebalance protocol; "consumer" = KIP-848 | New apps on 4.x brokers: set consumer
- pass 1: ✅ restates chapter claim — part-1.notes.md:95 "- group.protocol default classic; consumer option → ConsumerConfig.java:115,653-657; GroupProtocol.java:23-26"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/ConsumerConfig.java:115-117 — "The default value is classic"; consumer-rebalance-protocol.md (consumer = KIP-848); advice n/a

### C1185 · final · L1 · tr
> max.poll.interval.ms / session.timeout.ms | 300000 / 45000: progress timeout / liveness timeout | Slow processing / faster crash detection
- pass 1: ✅ restates chapter claim — part-1.notes.md:81 "- max.poll.interval.ms default 300000; exceeded → considered failed, rebalance → ConsumerConfig.java:627-631; CommonClientConfigs.java:192-1"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/ConsumerConfig.java:629, :438 — max.poll.interval.ms 300000, session.timeout.ms 45000

### C1186 · final · L1 · tr
> enable.idempotence | true (needs acks=all, retries > 0, max.in.flight ≤ 5) | Always; set explicitly so conflicting configs fail loudly
- pass 1: ✅ restates chapter claim — part-1.notes.md:67 "- enable.idempotence default true; requires acks=all, max.in.flight ≤5, retries>0; conflicting config silently disables unless explicit → Pr"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:341-347 — requires max.in.flight ≤ 5, "retries to be greater than 0", "acks must be all"; explicit enable + conflict → ConfigException

### C1187 · final · L1 · tr
> transactional.id | null; enables transactions + fencing (implies idempotence) | atomic multi-partition writes, consume–transform–produce
- pass 1: ✅ restates chapter claim — part-3.notes.md:10 "- Consumer must set isolation.level=read_committed and enable.auto.commit=false; only producer has transactional.id; restart aborts in-fligh"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:537-539 — transactional.id default null; TRANSACTIONAL_ID_DOC: enabling it implies idempotence

### C1188 · final · L1 · tr
> transaction.timeout.ms | 60000; coordinator aborts older open txns (≤ broker transaction.max.timeout.ms, 15 min) | lower it so hung producers stop blocking read_committed readers
- pass 1: ✅ restates chapter claim — part-3.notes.md:14 "- transaction.timeout.ms default 60000; coordinator proactively aborts; must not exceed transaction.max.timeout.ms → ProducerConfig.java:350"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:532-534 — 60000; TransactionStateManagerConfig: "TRANSACTIONS_MAX_TIMEOUT_MS_DEFAULT = … MINUTES.toMillis(15)"

### C1189 · final · L1 · tr
> isolation.level | read_uncommitted; read_committed reads only up to the LSO | downstream of transactional producers
- pass 1: ✅ restates chapter claim — part-3.notes.md:8 "- isolation.level default read_uncommitted → clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:357 → "DEFAULT_ISOL"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/ConsumerConfig.java:357 — "DEFAULT_ISOLATION_LEVEL = … READ_UNCOMMITTED"; read_committed reads up to LSO

### C1190 · final · L1 · tr
> kafka-transactions.sh list | describe | find-hanging | abort | inspect and repair transactions | read_committed consumers stuck at the LSO
- pass 1: ✅ restates chapter claim — part-3.notes.md:35 "- CLI: kafka-transactions.sh subcommands list, describe --transactional-id, find-hanging --broker-id, abort --topic --partition --start-offs"
- pass 2: ✅ kafka-4.3.1-src/tools/.../TransactionsCommand.java:101,395,468,545 — subcommands abort, describe, list, find-hanging

### C1191 · final · L1 · tr
> cleanup.policy=compact[,delete] | delete; compact keeps latest value per key | changelog / state / "current value" topics
- pass 1: ✅ restates chapter claim — part-1.notes.md:129 "- cleanup.policy default delete; compact; "delete,compact" → TopicConfig.java:156-162; ServerLogConfigs.java:89"
- pass 2: ✅ ServerLogConfigs.java:89 default delete; compact keeps latest value per key

### C1192 · final · L1 · tr
> delete.retention.ms | 86400000 (24 h) tombstone lifetime | raise if full state replays take longer
- pass 1: ✅ restates chapter claim — part-3.notes.md:46 "- Guarantees: caught-up consumer sees all; ordering kept; offsets never change; delete.retention.ms default 24h; can miss delete markers if "
- pass 2: ✅ kafka-4.3.1-src/storage/.../log/LogConfig.java:129 — "DEFAULT_DELETE_RETENTION_MS = 24 * 60 * 60 * 1000L"

### C1193 · final · L1 · tr
> num.network.threads / num.io.threads / queued.max.requests | 3 (per listener) / 8 / 500 | tune only after reading the idle-percent gauges and the TotalTimeMs breakdown
- pass 1: ✅ restates chapter claim — part-3.notes.md:71 "- num.network.threads default 3, per listener pool → server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:151-153 → "each "
- pass 2: ✅ kafka-4.3.1-src/server/.../SocketServerConfigs.java:144,152-153 — 500, 3, "each listener (except for controller listener) creates its own thread pool"; ServerConfigs NUM_IO_THREADS_DEFAULT = 8

### C1194 · final · L1 · tr
> unclean.leader.election.enable | false; true lets an out-of-sync replica lead (data loss) | emergency, per topic, when availability beats completeness
- pass 1: ✅ restates chapter claim — part-1.notes.md:120 "- unclean.leader.election.enable default false → storage/.../LogConfig.java:133,220; design.md:347"
- pass 2: ✅ kafka-4.3.1-src/storage/.../log/LogConfig.java:133 — "DEFAULT_UNCLEAN_LEADER_ELECTION_ENABLE = false"

### C1195 · final · L1 · tr
> remote.storage.enable + local.retention.ms | false / -2 (= full retention); local deletion only after upload | long retention on cheap object storage, non-compacted topics
- pass 1: ✅ restates chapter claim — part-3.notes.md:125 "- remote.storage.enable topic default false; local.retention.* vs retention.* → tiered-storage.md:47-56 → "If unset, The value in `retention"
- pass 2: ✅ kafka-4.3.1-src/storage/.../log/LogConfig.java:136,140 — remote.storage.enable false, local.retention.ms -2 "derived from RetentionMs"

### C1196 · final · L1 · tr
> share.record.lock.duration.ms / share.delivery.count.limit (group) | 30000 ms / 5 | tune per share group: slow jobs, poison-record tolerance
- pass 1: ✅ restates chapter claim — part-3.notes.md:148 "- Acquisition lock, 30 s default, share.record.lock.duration.ms group config; accept/release/reject/renew/do nothing → docs/design/design.md"
- pass 2: ✅ kafka-4.3.1-src/group-coordinator/.../GroupConfig.java:64,66,187-195 (group-level configs) with ShareGroupConfig defaults 30000 / 5

### C1197 · final · L1 · tr
> processing.guarantee=exactly_once_v2 | at_least_once default; EOS drops commit.interval.ms to 100 | Streams results must not double-count
- pass 1: ✅ restates chapter claim — part-3.notes.md:183 "- processing.guarantee at_least_once default, exactly_once_v2; commit.interval.ms 30000 / 100 with EOS; read_committed; idempotence; RF 3 re"
- pass 2: ✅ kafka-4.3.1-src/streams/.../StreamsConfig.java:1020-1022 default AT_LEAST_ONCE; :168 "EOS_DEFAULT_COMMIT_INTERVAL_MS = 100L"

### C1198 · final · L1 · tr
> num.standby.replicas / streams.num.standby.replicas | 0; warm shadow copies of task state | large state stores, fast failover
- pass 1: ✅ restates chapter claim — part-3.notes.md:177 "- num.standby.replicas default 0; acceptable.recovery.lag 10000; max.warmup.replicas 2 → StreamsConfig.java:896-898,920-922,1004-1006"
- pass 2: ✅ StreamsConfig.java:896-898 num.standby.replicas 0; GroupConfig.java:99 "streams.num.standby.replicas" default GroupCoordinatorConfig.java:353 = 0

### C1199 · final · L1 · tr
> kafka-configs.sh --alter --add-config 'producer_byte_rate=…' --entity-type users|clients | Set a client quota (also consumer_byte_rate, request_percentage, controller_mutation_rate); default unlimited | Multi-tenant clusters; noisy neighbours
- pass 1: ✅ restates chapter claim — part-1.notes.md:144 "- kafka-configs --alter --entity-type topics --entity-name --add-config → core/src/main/scala/kafka/admin/ConfigCommand.scala:546-572; bin/k"
- pass 2: ✅ design.md:463-509 client quotas (users/clients entities); multi-tenancy.md:116 controller_mutation_rate; unset = no quota

### C1200 · final · L1 · tr
> quota.window.num / quota.window.size.seconds | 11 samples / 1 s | Rarely changed; short windows mean short throttles
- pass 1: ✅ restates chapter claim — part-4.notes.md:28 "- quota.window.num default 11 → `server-common/src/main/java/org/apache/kafka/server/config/QuotaConfig.java:42` → "NUM_QUOTA_SAMPLES_DEFAUL"
- pass 2: ✅ kafka-4.3.1-src/server-common/.../QuotaConfig.java — quota.window.num NUM_QUOTA_SAMPLES_DEFAULT = 11, quota.window.size.seconds default 1

### C1201 · final · L1 · tr
> kafka-topics.sh --describe --under-replicated-partitions | --under-min-isr-partitions | List partitions with a shrunken ISR | First step for URP / NotEnoughReplicas alerts
- pass 1: ✅ restates chapter claim — part-1.notes.md:15 "- kafka-topics flags --bootstrap-server --create --topic --partitions --describe → TopicCommand.java:720-760 (accepts(...))"
- pass 2: ✅ kafka-4.3.1-src/tools/.../TopicCommand.java:774,778 — "under-replicated-partitions", "under-min-isr-partitions"

### C1202 · final · L1 · tr
> kafka-consumer-groups.sh --reset-offsets … --by-duration PT6H --execute | Move a stopped group's offsets (dry run without --execute) | Replaying after offset-out-of-range or a bad deploy
- pass 1: ✅ restates chapter claim — part-1.notes.md:93 "- --reset-offsets, --to-earliest, --execute, dry-run default, group must be inactive → ConsumerGroupCommandOptions.java:48-53,153-176"
- pass 2: ✅ kafka-4.3.1-src/tools/.../consumer/group/ConsumerGroupCommandOptions.java:170 "by-duration"; reset-offsets needs inactive group, --execute else dry run

### C1203 · final · L1 · tr
> {source}->{target}.enabled (MM2) | false | Turn on a MirrorMaker 2 replication flow
- pass 1: ✅ restates chapter claim — part-1.notes.md:42 "- MetadataResponse fields Brokers(NodeId, Host, Port, Rack), ClusterId, ControllerId, Topics(Name, TopicId, Partitions(PartitionIndex, Leade"
- pass 2: ✅ kafka-4.3.1-src/connect/mirror/.../MirrorMakerConfig.java:118 — parseBoolean(…"enabled") → false when unset; geo-replication doc:210 "must explicitly enable"

### C1204 · final · L1 · tr
> sync.group.offsets.enabled (MM2) | false | Pre-write translated group offsets for DR failover
- pass 1: ✅ restates chapter claim — part-4.notes.md:174 "- sync.group.offsets.enabled default false → `connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorCheckpointConfig.java:66` →"
- pass 2: ✅ kafka-4.3.1-src/connect/mirror/.../MirrorCheckpointConfig.java:66 — "SYNC_GROUP_OFFSETS_ENABLED_DEFAULT = false"

### C1205 · final · L1 · tr
> kafka-features.sh upgrade --release-version 4.3 | Finalize the upgrade: sets metadata.version and all feature defaults | After all nodes run 4.3 and behave well (4.3-IV0 cannot be downgraded)
- pass 1: ✅ restates chapter claim — part-3.notes.md:36 "- kafka-features.sh describe → tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:139 → "addParser(\"describe\")""
- pass 2: ✅ kafka-4.3.1-src/tools/.../FeatureCommand.java:153-155 — "--release-version … update all features to"; upgrade.md:39 4.3-IV0 has metadata changes, no downgrade

### C1206 · final · L1 · tr
> kafka-features.sh describe | version-mapping | --dry-run | Show finalized levels / what a release maps to / rehearse | Before and after every upgrade
- pass 1: ✅ restates chapter claim — part-3.notes.md:36 "- kafka-features.sh describe → tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:139 → "addParser(\"describe\")""
- pass 2: ✅ FeatureCommand.java:139,160,202 — describe, --dry-run, version-mapping

### C1207 · final · L1 · tr
> delivery.timeout.ms | 120000 — total per-record budget incl. retries | Tune how long the producer keeps trying; leave retries unset
- pass 1: ✅ restates chapter claim — part-1.notes.md:65 "- retries default Integer.MAX_VALUE; use delivery.timeout.ms → ProducerConfig.java:390; KafkaProducer.java:133-134"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:406 — "DELIVERY_TIMEOUT_MS_CONFIG, Type.INT, 120 * 1000"

### C1208 · final · L1 · tr
> retries | 2147483647 — attempts for transient errors | Don't set to 0: disables idempotence
- pass 1: ✅ restates chapter claim — part-1.notes.md:65 "- retries default Integer.MAX_VALUE; use delivery.timeout.ms → ProducerConfig.java:390; KafkaProducer.java:133-134"
- pass 2: ✅ kafka-4.3.1-src/clients/.../producer/ProducerConfig.java:390 Integer.MAX_VALUE (2147483647); :606 "Idempotence will be disabled because retries is set to 0"

### C1209 · final · L1 · tr
> max.poll.interval.ms / max.poll.records | 300000 / 500 | Seeing CommitFailedException / rebalances from slow processing
- pass 1: ✅ restates chapter claim — part-1.notes.md:81 "- max.poll.interval.ms default 300000; exceeded → considered failed, rebalance → ConsumerConfig.java:627-631; CommonClientConfigs.java:192-1"
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/ConsumerConfig.java:94,629 — 500 / 300000

### C1210 · final · L1 · tr
> consumer.seek(tp, offset + 1) | step over a poison pill after RecordDeserializationException | After quarantining keyBuffer()/valueBuffer() to a DLQ
- pass 1: ✅ restates chapter claim — part-1.notes.md:17 "- console consumer --from-beginning, --formatter-property print.key/print.partition/print.offset, --group → tools/.../consumer/ConsoleConsum"
- pass 2: ✅ CompletedFetch.java:344 "seek past the record"; RecordDeserializationException keyBuffer()/valueBuffer()/offset()

### C1211 · final · L1 · tr
> consumer.wakeup() | the only thread-safe KafkaConsumer call; poll throws WakeupException | Clean shutdown from another thread
- pass 1: ✅ restates chapter claims (no single notes line; verify in chapter)
- pass 2: ✅ kafka-4.3.1-src/clients/.../consumer/KafkaConsumer.java:445 — "The only exception to this rule is wakeup(), which can safely be used from an external thread"

### C1212 · final · L1 · tr
> DefaultErrorHandler(recoverer, backOff) | default FixedBackOff(0, 9) + log-and-skip | Always pass a DeadLetterPublishingRecoverer and a bounded BackOff
- pass 1: ✅ restates chapter claim — part-5.notes.md:54 "- DefaultErrorHandler default FixedBackOff(0,9), logs at ERROR after ten failures → spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/mod"
- pass 2: ✅ spring-kafka-4.1.1/spring-kafka/.../listener/SeekUtils.java:63 — "new FixedBackOff(0, DEFAULT_MAX_FAILURES - 1)"; FailedRecordTracker.java:87 logs error

### C1213 · final · L1 · tr
> DeadLetterPublishingRecoverer | <topic>-dlt, same partition, kafka_dlt-* headers | Replayable dead letters; DLT needs ≥ source partitions
- pass 1: ✅ restates chapter claim — part-5.notes.md:62 "- DeadLetterPublishingRecoverer default <topic>-dlt same partition, must have ≥ partitions → annotation-error-handling.adoc:874-876; source "
- pass 2: ✅ spring-kafka-4.1.1/spring-kafka/.../listener/DeadLetterPublishingRecoverer.java:76 "-dlt", same partition; KafkaHeaders PREFIX "kafka_" + "dlt-…"; annotation-error-handling.adoc:876

### C1214 · final · L1 · tr
> ErrorHandlingDeserializer | wraps delegate; failure → header, record to error handler | Any consumer whose payloads could be malformed
- pass 1: ✅ restates chapter claim — part-5.notes.md:65 "- ErrorHandlingDeserializer behavior; VALUE_DESERIALIZER_CLASS → spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kaf"
- pass 2: ✅ spring-kafka-docs …/kafka/serdes.adoc:526-528 — delegates; failure → null value + DeserializationException header; container error handler called

### C1215 · final · L1 · tr
> @RetryableTopic / @DltHandler | attempts 3, fixed 1000 ms, -retry / -dlt suffixes | Independent events where ordering doesn't matter
- pass 1: ✅ restates chapter claim — part-5.notes.md:73 "- @RetryableTopic defaults attempts "3", backOff fixed 1000, numPartitions "1", replicationFactor "-1", SINGLE_TOPIC → spring-kafka-4.1.1/sp"
- pass 2: ✅ spring-kafka-4.1.1/spring-kafka/.../annotation/RetryableTopic.java:59 attempts "3"; annotation/BackOff.java:63 DEFAULT_DELAY = 1000; RetryTopicConstants.java:32,37 "-retry"/"-dlt"

### C1216 · final · L1 · tr
> ContainerProperties.AckMode | BATCH (default), RECORD, TIME, COUNT, COUNT_TIME, MANUAL, MANUAL_IMMEDIATE | RECORD for slow records/retry topics; per listener via @KafkaListener(ackMode=…) since 4.1
- pass 1: ✅ restates chapter claims (no single notes line; verify in chapter)
- pass 2: ✅ spring-kafka-4.1.1/spring-kafka/.../listener/ContainerProperties.java:71-117,259 enum values, "ackMode = AckMode.BATCH"; whats-new.adoc:13 @KafkaListener ackMode (4.1)

### C1217 · final · L1 · tr
> ContainerProperties.ShareAckMode | EXPLICIT (default), MANUAL, IMPLICIT | Share consumers in Spring Kafka 4.1
- pass 1: ✅ restates chapter claims (no single notes line; verify in chapter)
- pass 2: ✅ spring-kafka-4.1.1/spring-kafka/.../listener/ContainerProperties.java:126-142,352 — ShareAckMode IMPLICIT/EXPLICIT/MANUAL, default EXPLICIT, @since 4.1

### C1218 · final · L1 · tr
> group.share.delivery.count.limit | 5 — broker default max delivery attempts per record | In-Kafka queue with a poison-message cutoff
- pass 1: ✅ restates chapter claim — part-5.notes.md:88 "- share.delivery.count.limit group config (4.3) → kafka-4.3.1-src/docs/getting-started/upgrade.md:52; broker default group.share.delivery.co"
- pass 2: ✅ kafka-4.3.1-src/group-coordinator/.../ShareGroupConfig.java:48 "group.share.delivery.count.limit", DEFAULT = 5

### C1219 · final · L1 · p
> Back to the question from the start: how can an append-only log that does not force every write to disk be fast, durable and exactly-once?
- pass 1: n/a (intro text / finale summary — each statement is a chapter claim validated there)
- pass 2: n/a — rhetorical question/heading

### C1220 · final · L1 · p
> Fast because appends are sequential writes into segment files, reads are served from the OS page cache, data moves in compressed batches from producer to disk to consumer without being re-encoded, and plain-text reads use zero-copy transfer from file to socket. Durable not because of fsync, but because of replication: a write counts as committed only when every in-sync replica has it (the high watermark), acks=all plus min.insync.replicas put a floor under that set, and leader epochs let a returning broker cut away exactly the records that never got committed. Exactly-once because idempotent producers stamp every batch with a producer ID, epoch and sequence number so retries cannot duplicate, and transactions write commit or abort markers that read_committed consumers respect — with the consumer's offsets committed inside the same transaction.
- pass 1: ✅ summary of chapter claims: design.md:94-107 sendfile/pagecache; design.md:191 committed = all ISR; Partition.scala:968 HW; design.md:193-207 idempotence + transactions; PlaintextTransportLayer.java:214 transferTo (TLS path copies)
- pass 2: ✅ summary of confirmed claims: design.md:109 (zero-copy, not TLS), hardware-and-os.md:66 (no fsync, replication), design.md committed = all ISR, KIP-101 epochs, ProducerStateEntry PID/epoch/seq, control markers + sendOffsetsToTransaction

### C1221 · final · L1 · p
> Next step: run a three-broker KRaft cluster locally, break it on purpose with the exercises from each chapter, and watch the metrics from the troubleshooting chapter while you do.
- pass 1: n/a (intro text / finale summary — each statement is a chapter claim validated there)
- pass 2: n/a — advice
