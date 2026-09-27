# Claims ledger — kafka-course.html — k-troubleshooting

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0847 · k-troubleshooting · L4 · p
> At 3 a.m. nobody reads the design docs. You get a symptom ("lag is climbing", "producers throw NotEnoughReplicasException") and you need to go from that symptom to a cause and a safe fix in minutes. This chapter is that map. It leans on everything you already know: ISR and high watermark (ch. 10), rebalances (ch. 11), idempotence (ch. 12), the request pipeline (ch. 15), leader epochs and ELR (ch. 16), KRaft (ch. 9).
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (intro/pedagogy; cross-references to chapters)

### C0848 · k-troubleshooting · L4 · p
> Archive version: an inspector walks in, checks a few gauges on the wall (metrics), asks the front desk a few questions (CLI tools), and only then moves furniture. The worst incidents come from moving furniture first, like adding consumers, deleting data or lowering min.insync.replicas, before knowing which gauge is red.
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (analogy/advice)

### C0849 · k-troubleshooting · L4 · figcaption
> Each path shows the first checks and the most likely causes. Real incidents can combine several. Metric and command names are real (4.3). The consumer group picture in 💥 uses illustrative lag numbers.
- pass 1: n/a (figcaption; names verified in rows below; lag numbers labelled illustrative)
- pass 2: ✅ docs/operations/monitoring.md:152-815 — all metric names found; CLI flags found in tools/src/main (TopicCommand.java:774-778, MetadataQuorumCommand.java:183-189); lag numbers labelled illustrative

### C0850 · k-troubleshooting · L4 · h3
> The symptom table
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0851 · k-troubleshooting · L4 · tr
> Consumer lag keeps growing (LAG column, records-lag-max) | Processing slower than the produce rate; too few consumers or partitions; frequent rebalances pausing work; a quota throttling the fetch. | Profile the handler first. Scale consumers up to the partition count; beyond that, add partitions (keys will remap). Check fetch-throttle-time-avg.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:146 — "CURRENT-OFFSET  LOG-END-OFFSET  LAG" ; docs/operations/monitoring.md:763 — "Attribute: records-lag-max" ; docs/operations/basic-kafka-operations.md:63 — "Key Distribution Changes" ; clients/src/main/java/org/apache/kafka/clients/consumer/internals/FetchMetricsRegistry.java:106 — "fetch-throttle-time-avg"
- pass 2: ✅ docs/design/design.md:157 — "each of which is consumed by exactly one consumer within each subscribing consumer group"; FetchMetricsRegistry.java:106 "fetch-throttle-time-avg"; records-lag-max monitoring.md:763

### C0852 · k-troubleshooting · L4 · tr
> UnderReplicatedPartitions > 0 | A broker is down, or a follower can't keep up (disk, network, GC), so the ISR shrank. | Find the common broker in --under-replicated-partitions. Restore it, or fix its disk/network. Consider more num.replica.fetchers.
- pass 1: ✅ docs/operations/monitoring.md:598 — "If a broker goes down, ISR for some of the partitions will shrink" ; tools/src/main/java/org/apache/kafka/tools/TopicCommand.java:774 — "parser.accepts("under-replicated-partitions"" ; server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:97 — "NUM_REPLICA_FETCHERS_DOC"
- pass 2: ✅ TopicCommand.java:774 "under-replicated-partitions"; ReplicationConfigs.java:96 NUM_REPLICA_FETCHERS_DEFAULT = 1; monitoring.md:503 UnderReplicatedPartitions 0

### C0853 · k-troubleshooting · L4 · tr
> Producers get NotEnoughReplicasException; UnderMinIsrPartitionCount > 0 | ISR size < min.insync.replicas while acks=all. The broker refuses the write on purpose. | Bring replicas back into the ISR. Lowering min.insync.replicas trades away the durability you asked for, so do it only as a conscious, temporary decision.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasException.java:20 — "lower than min.insync.replicas" ; docs/operations/monitoring.md:516 — "name=UnderMinIsrPartitionCount"
- pass 2: ✅ docs/design/design.md:305 — "The number of the in-sync replicas is no less than the min.insync.replicas setting"; Errors.java:220 "rejected since there are fewer in-sync replicas than required"; monitoring.md:516

### C0854 · k-troubleshooting · L4 · tr
> Rebalance storm (group keeps flipping state; throughput collapses) | Poll loop slower than max.poll.interval.ms (300 s); members missing heartbeats (session.timeout.ms 45 s); rolling deploys without static membership. | Lower max.poll.records or speed up processing; use group.instance.id; move to group.protocol=consumer (KIP-848, incremental and server-side).
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:629 — "300000," ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:438 — "45000," ; docs/operations/consumer-rebalance-protocol.md:66 — "The `group.protocol` configuration must be set to `consumer`"
- pass 2: ✅ ConsumerConfig.java:627-629 max.poll.interval.ms 300000; :436-438 session.timeout.ms 45000; group.protocol=consumer is KIP-848 (docs/operations/consumer-rebalance-protocol.md)

### C0855 · k-troubleshooting · L4 · tr
> RecordTooLargeException / MESSAGE_TOO_LARGE; BytesRejectedPerSec rising | Record above producer max.request.size (1048576), or batch above broker message.max.bytes / topic max.message.bytes (1048588). | Raise the producer and topic limits consistently (followers still replicate oversized batches, because replica.fetch.max.bytes is a soft limit), or store big payloads elsewhere and send a reference.
- pass 1: ✅ revised after pass-2 finding — C0855: smoothed the wording to "Raise the producer and topic limits consistently (followers still replicate oversized batches, because replica.fetch.max.bytes is a soft limit)…". Source: server/.../ReplicationConfigs.java:69-71 "This is not an absolute maximum
- pass 2: ✅ ProducerConfig.java:410-412 max.request.size 1024*1024; ServerLogConfigs.java:177 "1024 * 1024 + Records.LOG_OVERHEAD" (=1048588); ReplicationConfigs.java:69 "This is not an absolute maximum"; monitoring.md:308 BytesRejectedPerSec

### C0856 · k-troubleshooting · L4 · tr
> Disk full / OfflineLogDirectoryCount > 0; clients see KafkaStorageException | Retention too long for the volume; skewed partitions; a failed disk. | Check with kafka-log-dirs.sh --describe. Shorten retention or add capacity. Move replicas with kafka-reassign-partitions.sh (with a throttle). Replace the failed disk.
- pass 1: ✅ docs/operations/monitoring.md:386 — "name=OfflineLogDirectoryCount" ; clients/src/main/java/org/apache/kafka/common/errors/KafkaStorageException.java:20 — "Miscellaneous disk-related IOException" ; tools/src/main/java/org/apache/kafka/tools/LogDirsCommand.java:177 — "parser.accepts("describe"" ; docs/operations/basic-kafka-operations.md:587 — "--throttle 50000000"
- pass 2: ✅ monitoring.md:386 OfflineLogDirectoryCount 0; LogDirsCommand.java:177 --describe; ReassignPartitionsCommandOptions.java:96 --throttle; KafkaStorageException.java:20 "disk-related IOException"

### C0857 · k-troubleshooting · L4 · tr
> Controller / quorum lost: no metadata changes, topic creation hangs | Majority of controllers down or partitioned. The sum of ActiveControllerCount over the controllers ≠ 1. | kafka-metadata-quorum.sh describe --status and --replication. Restore a majority of voters before anything else.
- pass 1: ✅ docs/operations/monitoring.md:442 — "only one broker in the cluster should have 1" ; docs/operations/kraft.md:230 — "describe --status" ; docs/operations/hardware-and-os.md:135 — "describe --replication"
- pass 2: ✅ docs/operations/kraft.md:47 — "A majority of the controllers must be alive in order to maintain availability"; monitoring.md:438 "only one broker in the cluster should have 1"; MetadataQuorumCommand.java:183,189 --status/--replication

### C0858 · k-troubleshooting · L4 · tr
> ISR flapping (IsrShrinksPerSec / IsrExpandsPerSec non-zero without failures) | Followers repeatedly fall more than replica.lag.time.max.ms (30000) behind: GC pauses, saturated disks or network. | Fix the resource. Don't just raise the lag time, because that hides slow replicas inside the ISR and slows acks=all.
- pass 1: ✅ docs/operations/monitoring.md:598 — "the expected value for both ISR shrink rate and expansion rate is 0" ; server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:55 — "REPLICA_LAG_TIME_MAX_MS_DEFAULT = 30000L" (causes/advice = operational reasoning)
- pass 2: ✅ ReplicationConfigs.java:55 REPLICA_LAG_TIME_MAX_MS_DEFAULT = 30000L; design.md:298 "if the follower lags too far behind ... remove it from the ISR"; monitoring.md:594 expected shrink/expand rate 0

### C0859 · k-troubleshooting · L4 · tr
> Offset out of range (consumer jumps to start or end) | The committed offset was deleted by retention (the consumer was down longer than retention), so auto.offset.reset kicks in. | Decide the policy explicitly (earliest, latest, by_duration:…, or none to fail loudly). Recover with --reset-offsets --to-datetime.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:172 — "current offset does not exist any more on the server" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:176 — "by_duration:" ; docs/operations/basic-kafka-operations.md:238 — "--to-datetime <String: datetime>"
- pass 2: ✅ ConsumerConfig.java:172-178 — "if the current offset does not exist any more on the server (e.g. because that data has been deleted)"; options earliest/latest/by_duration/none; ConsumerGroupCommandOptions.java:166 --to-datetime

### C0860 · k-troubleshooting · L4 · tr
> OfflinePartitionsCount > 0 | Every replica eligible to lead is down. | Bring back a replica from the ISR or ELR (ch. 16). Unclean election (kafka-leader-election.sh --election-type unclean) is a last resort that can lose acknowledged data.
- pass 1: ✅ docs/operations/monitoring.md:2264 — "name=OfflinePartitionsCount" ; tools/src/main/java/org/apache/kafka/tools/LeaderElectionCommand.java:312 — "for unclean leader election" (ELR/unclean data-loss: ch.16)
- pass 2: ✅ monitoring.md:2258 "number of offline topic partitions"; LeaderElectionCommand.java:312 "\"unclean\" for unclean leader election"; design.md:347 unclean risks losing data

### C0861 · k-troubleshooting · L4 · p
> NotEnoughReplicasAfterAppendException is the nasty sibling. The broker had already appended the batch when it discovered the ISR was too small. Its javadoc says it outright: "Producer retries will cause duplicates", unless the producer is idempotent (enable.idempotence defaults to true; ch. 12). One more reason not to turn idempotence off.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasAfterAppendException.java:21 — "Producer retries will cause duplicates" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:527 — "define(ENABLE_IDEMPOTENCE_CONFIG"
- pass 2: ✅ NotEnoughReplicasAfterAppendException.java:21 — "discovered *after* the message was already appended to the log. Producer retries will cause duplicates."; ProducerConfig.java:527-529 enable.idempotence default true

### C0862 · k-troubleshooting · L4 · h3
> The dashboard: metrics worth an alert
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0863 · k-troubleshooting · L4 · tr
> kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions | 0 | Replication is behind somewhere.
- pass 1: ✅ docs/operations/monitoring.md:503 — "name=UnderReplicatedPartitions"
- pass 2: ✅ monitoring.md:503 — "kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions | 0"

### C0864 · k-troubleshooting · L4 · tr
> kafka.server:type=ReplicaManager,name=UnderMinIsrPartitionCount | 0 | acks=all writes to these partitions are failing.
- pass 1: ✅ docs/operations/monitoring.md:516 — "name=UnderMinIsrPartitionCount" ; clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasException.java:20 — "lower than min.insync.replicas"
- pass 2: ✅ monitoring.md:516 — "kafka.server:type=ReplicaManager,name=UnderMinIsrPartitionCount | 0"; design.md:305 acks=all needs ISR ≥ min.insync.replicas

### C0865 · k-troubleshooting · L4 · tr
> kafka.controller:type=KafkaController,name=OfflinePartitionsCount | 0 | Partitions with no leader: unavailable.
- pass 1: ✅ docs/operations/monitoring.md:2264 — "name=OfflinePartitionsCount"
- pass 2: ✅ monitoring.md:2258-2264 — "The number of offline topic partitions (non-internal) as observed by this Controller."

### C0866 · k-troubleshooting · L4 · tr
> kafka.controller:type=KafkaController,name=ActiveControllerCount | exactly one node has 1 | 0 everywhere means no active controller.
- pass 1: ✅ docs/operations/monitoring.md:442 — "only one broker in the cluster should have 1"
- pass 2: ✅ monitoring.md:438 — "only one broker in the cluster should have 1"; :2168 "Valid values are '0' or '1'"

### C0867 · k-troubleshooting · L4 · tr
> kafka.server:type=ReplicaManager,name=IsrShrinksPerSec / IsrExpandsPerSec | 0 outside broker restarts | Flapping followers.
- pass 1: ✅ docs/operations/monitoring.md:598 — "the expected value for both ISR shrink rate and expansion rate is 0"
- pass 2: ✅ monitoring.md:594 — "Other than that, the expected value for both ISR shrink rate and expansion rate is 0."

### C0868 · k-troubleshooting · L4 · tr
> kafka.controller:type=ControllerStats,name=UncleanLeaderElectionsPerSec | 0 | Possible data loss occurred.
- pass 1: ✅ docs/operations/monitoring.md:412 — "name=UncleanLeaderElectionsPerSec"
- pass 2: ✅ monitoring.md:412 — "UncleanLeaderElectionsPerSec | 0"; design.md:347 unclean election can lose data

### C0869 · k-troubleshooting · L4 · tr
> kafka.log:type=LogManager,name=OfflineLogDirectoryCount | 0 | A disk or log dir failed.
- pass 1: ✅ docs/operations/monitoring.md:386 — "name=OfflineLogDirectoryCount"
- pass 2: ✅ monitoring.md:386 — "kafka.log:type=LogManager,name=OfflineLogDirectoryCount | 0"

### C0870 · k-troubleshooting · L4 · tr
> kafka.network:type=SocketServer,name=NetworkProcessorAvgIdlePercent; kafka.server:type=KafkaRequestHandlerPool,name=RequestHandlerAvgIdlePercent | ideally > 0.3 | Threads are saturated; queues will grow.
- pass 1: ✅ docs/operations/monitoring.md:776 — "name=NetworkProcessorAvgIdlePercent" ; docs/operations/monitoring.md:815 — "name=RequestHandlerAvgIdlePercent"
- pass 2: ✅ monitoring.md:776,815 — "between 0 and 1, ideally > 0.3" (both metrics)

### C0871 · k-troubleshooting · L4 · tr
> kafka.network:type=RequestMetrics,name=TotalTimeMs,request=Produce (and its parts) | stable | Break it down: queue / local / remote / response.
- pass 1: ✅ docs/operations/monitoring.md:689 — "broken into queue, local, remote and response send time"
- pass 2: ✅ monitoring.md:685 — "broken into queue, local, remote and response send time"

### C0872 · k-troubleshooting · L4 · tr
> consumer records-lag-max (published by the consumer, not the broker) | bounded | The consumer is falling behind.
- pass 1: ✅ docs/operations/monitoring.md:759 — "Published by the consumer, not broker"
- pass 2: ✅ monitoring.md:763 — "kafka.consumer:type=consumer-fetch-manager-metrics ... Attribute: records-lag-max" (client-side metric)

### C0873 · k-troubleshooting · L4 · p
> Your pager says "UnderMinIsrPartitionCount = 12". Write the three commands you'd run first on a 4.3 cluster, and what you're looking for in each.
- pass 1: n/a (exercise prompt / pedagogy)
- pass 2: n/a (exercise prompt)

### C0874 · k-troubleshooting · L4 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --under-min-isr-partitions bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --under-replicated-partitions bin/kafka-metadata-quorum.sh --bootstrap-server localhost:9092 describe --status
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/TopicCommand.java:778 — "parser.accepts("under-min-isr-partitions"" ; tools/src/main/java/org/apache/kafka/tools/TopicCommand.java:774 — "parser.accepts("under-replicated-partitions"" ; docs/operations/kraft.md:230 — "describe --status"
- pass 2: ✅ TopicCommand.java:774,778 "under-replicated-partitions", "under-min-isr-partitions"; MetadataQuorumCommand.java:104,178,183 --bootstrap-server, describe, --status

### C0875 · k-troubleshooting · L4 · p
> (1) Which partitions, and which replica is missing from Isr? Usually one broker id repeats. (2) How wide the damage is: under-replicated but still ≥ min ISR is a warning, not an outage. (3) Whether the controller quorum is healthy (a leader, small lag), so leadership and ISR changes can actually be committed. Then check that broker's disk (kafka-log-dirs.sh --bootstrap-server localhost:9092 --describe --broker-list 2) and its logs.
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/LogDirsCommand.java:184 — "parser.accepts("broker-list"" (interpretation = pedagogy)
- pass 2: ✅ LogDirsCommand.java:169,177,184 --bootstrap-server/--describe/--broker-list; design.md:305 writes accepted while ISR ≥ min.insync.replicas; quorum health via describe --status (MaxFollowerLag, MetadataQuorumCommand.java:273)

### C0876 · k-troubleshooting · L4 · p
> A consumer group lost its place after a long outage and jumped to latest. Replay the last 6 hours safely:
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (exercise prompt)

### C0877 · k-troubleshooting · L4 · li
> Stop all instances of the group. --reset-offsets requires inactive members.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:226 — "first make sure that the consumer instances are inactive"
- pass 2: ✅ ConsumerGroupCommandOptions.java:48 — "Supports one consumer group at the time, and instances should be inactive"; ConsumerGroupCommand.java:652 "can only be reset if the group ... is inactive"

### C0878 · k-troubleshooting · L4 · li
> Dry run (the default mode): bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --group billing --topic invoices --by-duration PT6H
- pass 1: ✅ docs/operations/basic-kafka-operations.md:230 — "(default) to display which offsets to reset" ; docs/operations/basic-kafka-operations.md:244 — "--by-duration <String: duration>"
- pass 2: ✅ ConsumerGroupCommandOptions.java:49 — "--dry-run (the default) to plan which offsets to reset"; :60 by-duration "Format: 'PnDTnHnMnS'" (4.3 prints a WARN when neither flag given; 5.0 will require one, :251)

### C0879 · k-troubleshooting · L4 · li
> Check the NEW-OFFSET column, and optionally save it with --export.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:253 — "NEW-OFFSET" ; docs/operations/basic-kafka-operations.md:232 — "--export : to export the results to a CSV format"
- pass 2: ✅ OffsetsUtils.java:84 "NEW-OFFSET" column; ConsumerGroupCommandOptions.java:56 — "Export operation execution to a CSV file. Supported operations: reset-offsets."

### C0880 · k-troubleshooting · L4 · li
> Re-run with --execute, then start the consumers. Processing must be idempotent, because you will see some records again.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:231 — "--execute : to execute --reset-offsets process"
- pass 2: ✅ ConsumerGroupCommandOptions.java:49 — "--execute to update the offsets"; re-reading records → idempotence advice

### C0881 · k-troubleshooting · L4 · summary
> L4🔬 Go deeper: reading the error codes
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0882 · k-troubleshooting · L4 · p
> Every failed response carries an Errors code, and the broker counts them in kafka.network:type=RequestMetrics,name=ErrorsPerSec,request=…,error=… (error=NONE counts successes). Useful ones: OFFSET_OUT_OF_RANGE (1), MESSAGE_TOO_LARGE (10), RECORD_LIST_TOO_LARGE (18, a batch larger than the segment size), NOT_ENOUGH_REPLICAS (19, rejected before append), NOT_ENOUGH_REPLICAS_AFTER_APPEND (20, written but under-replicated), THROTTLING_QUOTA_EXCEEDED (89). Graphing ErrorsPerSec by error tag often explains an incident faster than any log. For disk trouble, KafkaStorageException's javadoc describes the path: after logs are loaded, an IOException triggers LogDirFailureChannel.maybeAddOfflineLogDir(), the directory goes offline, and clients are told to refresh metadata and retry against the new leaders.
- pass 1: ✅ docs/operations/monitoring.md:156 — "error=NONE indicates successful responses" ; clients/src/main/java/org/apache/kafka/common/protocol/Errors.java:182 — "OFFSET_OUT_OF_RANGE(1" ; clients/src/main/java/org/apache/kafka/common/protocol/Errors.java:202 — "MESSAGE_TOO_LARGE(10" ; clients/src/main/java/org/apache/kafka/common/protocol/Errors.java:218 — "RECORD_LIST_TOO_LARGE(18" ; clients/src/main/java/org/apache/kafka/common/protocol/Errors.java:220 — "NOT_ENOUGH_REPLICAS(19" ; clients/src/main/java/org/apache/kafka/common/protocol/Errors.java:222 — "NOT_ENOUGH_REPLICAS_AFTER_APPEND(20" ; clients/src/main/java/org/apache/kafka/common/protocol/Errors.java:374 — "THROTTLING_QUOTA_EXCEEDED(89" ; clients/src/main/java/org/apache/kafka/common/errors/KafkaStorageException.java:26 — "LogDirFailureChannel.maybeAddOfflineLogDir"
- pass 2: ✅ monitoring.md:152 "error=NONE indicates successful responses"; Errors.java:182,202,218,220,222,374 codes 1/10/18/19/20/89 (18 "batch larger than the configured segment size"); KafkaStorageException.java:21-27 "trigger LogDirFailureChannel.maybeAddOfflineLogDir()", "Client should request metadata update and retry"

### C0883 · k-troubleshooting · L4 · li
> Diagnose before acting: metric → CLI → cause → fix.
- pass 1: n/a (method summary / pedagogy)
- pass 2: n/a (advice)

### C0884 · k-troubleshooting · L4 · li
> First commands: kafka-consumer-groups --describe, kafka-topics --describe --under-replicated-partitions / --under-min-isr-partitions, kafka-metadata-quorum describe --status.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:145 — "bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-group" ; docs/operations/kraft.md:230 — "describe --status"
- pass 2: ✅ TopicCommand.java:774-778; MetadataQuorumCommand.java:183 describe --status; kafka-consumer-groups --describe exists (ConsumerGroupCommandOptions)

### C0885 · k-troubleshooting · L4 · li
> Consumers beyond the partition count sit idle, so they can't fix lag.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:44 — "the partition count impacts the maximum parallelism of your consumers"
- pass 2: ✅ docs/design/design.md:157 — "consumed by exactly one consumer within each subscribing consumer group at any given time" (classic/consumer groups; not share groups)

### C0886 · k-troubleshooting · L4 · li
> NotEnoughReplicas is a safety feature. …AfterAppend duplicates on retry without idempotence.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasAfterAppendException.java:21 — "Producer retries will cause duplicates"
- pass 2: ✅ Errors.java:220-222; NotEnoughReplicasAfterAppendException.java:21 "Producer retries will cause duplicates"

### C0887 · k-troubleshooting · L4 · li
> Offset-out-of-range means retention ate your position; make auto.offset.reset a deliberate choice.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:172 — "current offset does not exist any more on the server"
- pass 2: ✅ ConsumerConfig.java:172-173 — "if the current offset does not exist any more on the server (e.g. because that data has been deleted)"
