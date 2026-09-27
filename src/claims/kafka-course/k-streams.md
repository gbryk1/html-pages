# Claims ledger — kafka-course.html — k-streams

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0722 · k-streams · L3 · p
> Kafka Streams is a library, not a cluster. You write a topology — "read orders, group by customer, count, write order-counts" — and run as many copies of your app as you like. The library splits the work, keeps local state, survives crashes and can give you exactly-once, using nothing but the Kafka features from this and the previous parts: consumer groups, compacted topics and transactions.
- pass 1: ✅ docs/streams/architecture.md:49; docs/design/design.md:207 — "Kafka Streams is not a resource manager, but a library"
- pass 2: ✅ docs/streams/architecture.md:49 — "Kafka Streams is not a resource manager, but a library that \"runs\" anywhere"; EOS via transactions (config-streams.md:1620), changelogs compacted (architecture.md:88)

### C0723 · k-streams · L3 · p
> Archive analogy: each clerk keeps a private notebook (a state store) with running totals. Every change to the notebook is also written as a line in a dedicated ledger book in the archive (the changelog topic, compacted). If the clerk quits, a replacement rebuilds the notebook by reading that ledger — or, faster, takes over from a colleague who has been keeping a shadow copy all along (a standby replica).
- pass 1: n/a (analogy)
- pass 2: n/a (analogy; mechanism matches docs/streams/architecture.md:88-90 changelog restore + standby replicas)

### C0724 · k-streams · L3 · h3
> Topology, sub-topologies, tasks
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0725 · k-streams · L3 · p
> Your topology is grouped into sub-topologies: sets of connected processors that are linked to each other only through Kafka topics — typically a repartition topic, created when data must be re-keyed. Each sub-topology is instantiated once per input partition as a task, named <subtopology>_<partition> — so 0_2 is sub-topology 0 on partition 2. The docs stress that "the assignment of partitions to tasks never changes": a task is the fixed unit of parallelism, and the maximum parallelism is bounded by the number of input partitions. Threads (num.stream.threads, default 1) and instances only decide where tasks run.
- pass 1: ❌ fixed in part file — TopologyDescription javadoc: sub-topologies connected only via topics
- pass 2: ✅ docs/streams/architecture.md:45 — "The assignment of partitions to tasks never changes so that each task is a fixed unit of parallelism"; TaskId.java:76 subtopology + "_" + partition; tutorial.md:410 sub-topologies linked via repartition topic; StreamsConfig.java:1010-1012 num.stream.threads default 1

### C0726 · k-streams · L3 · h3
> State stores and changelogs
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0727 · k-streams · L3 · p
> A stateful task owns a local store (RocksDB or in-memory). For each store Kafka Streams maintains a changelog topic named <application.id>-<storeName>-changelog, with one partition per task, and log compaction enabled (windowed stores use delete,compact). Repartition topics use delete with infinite retention; Streams purges them itself once records are consumed. When a task moves to another instance, its store is restored by replaying its changelog partition before processing resumes — that restore is usually the dominant cost of a rebalance.
- pass 1: ✅ docs/streams/architecture.md:88; streams/src/main/java/org/apache/kafka/streams/Topology.java:690-693; docs/streams/developer-guide/manage-topics.md:66-68; docs/streams/developer-guide/config-streams.md:997-1005 — "The changelog topic will be named "${applicationId}-<storename>-changelog""
- pass 2: ✅ Topology.java:690 — "named \"${applicationId}-<storename>-changelog\""; manage-topics.md:66-68 — "repartition topics ... delete ... retention time is -1 (infinite)", windowed "delete,compact"; upgrade-guide.md:389 — "purges old data explicitly via \"delete record\" requests"; architecture.md:90 restore dominates re-init cost

### C0728 · k-streams · L3 · figcaption
> Simplified: one sub-topology, one store, one thread per instance. Record counts and restore times are illustrative — real restore time depends on changelog size, network and the store.
- pass 1: n/a (figcaption, labelled illustrative)
- pass 2: n/a (figure caption, labelled illustrative)

### C0729 · k-streams · L3 · h3
> Standby replicas and warm-up
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0730 · k-streams · L3 · p
> num.standby.replicas (default 0) keeps shadow copies of each task's store on other instances by continuously reading the changelog. On failover the task is assigned where a standby exists, so only a small tail needs replaying. Since 2.6 the classic assignor also only assigns a stateful task to an instance with a caught-up copy if one exists (within acceptable.recovery.lag, default 10000 records), warming others up in the background with up to max.warmup.replicas (default 2) extra "warm-up" tasks.
- pass 1: ✅ docs/streams/architecture.md:90; streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:896-898,920-922,1004-1006; docs/streams/developer-guide/config-streams.md:1473 — "Starting in 2.6, Kafka Streams will guarantee that a task is only ever assigned to an instance with a fully caught-up local copy"
- pass 2: ✅ StreamsConfig.java:896-898 num.standby.replicas 0; :920-922 acceptable.recovery.lag 10_000L; :1004-1006 max.warmup.replicas 2; architecture.md:90 — "Starting in 2.6, Kafka Streams will guarantee that a task is only ever assigned to an instance with a fully caught-up"

### C0731 · k-streams · L3 · h3
> Exactly-once v2
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0732 · k-streams · L3 · p
> processing.guarantee is at_least_once by default; exactly_once_v2 turns on the Chapter-13 machinery end to end: output records, changelog writes and input offsets are committed in one transaction per commit, consumers read with read_committed, and commit.interval.ms drops from 30000 to 100 ms by default. The docs warn that EOS with a replication factor below 3 "effectively voids EOS" — use RF 3 and min.insync.replicas=2 for all involved topics.
- pass 1: ✅ docs/streams/developer-guide/config-streams.md:1620; streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:167-168,1020-1023; docs/design/design.md:203,211 — "if exactly-once processing is enabled, the default for parameter commit.interval.ms changes to 100ms"
- pass 2: ✅ StreamsConfig.java:1020-1022 default AT_LEAST_ONCE; :167-168 commit 30000 / EOS 100; :1333 consumer READ_COMMITTED; config-streams.md:1623 — "replication factor lower than 3 effectively voids EOS" (docs write min.in.sync.replicas=2, the real config is min.insync.replicas)

### C0733 · k-streams · L3 · p
> Without standbys, a crash can mean minutes of downtime per task. Replaying a multi-GB changelog before a task processes anything again is the classic Streams outage. Budget restore time, enable standby replicas for large stores, and don't let changelog topics lose compaction (check cleanup.policy on the internal topics).
- pass 1: n/a (opinion / operational advice; restore cost per docs/streams/architecture.md:90)
- pass 2: n/a (operational advice; consistent with architecture.md:90 restore cost / standby replicas)

### C0734 · k-streams · L3 · h3
> The Streams rebalance protocol (KIP-1071)
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0735 · k-streams · L3 · p
> Classically, Streams runs on a consumer group with a client-side assignor (StreamsPartitionAssignor) that computes task assignments during a group-wide rebalance. KIP-1071 applies the KIP-848 idea to Streams: the app registers a dedicated streams group, sends its topology to the broker in StreamsGroupHeartbeat (sub-topologies, source, changelog and repartition topics), and the broker computes active and standby task assignments continuously and incrementally (the heartbeat has a WarmupTasks field, but warm-up tasks are not supported yet). The core feature set is production-ready in 4.2, and the feature is enabled by default on new 4.2 clusters (streams.version=1); clients opt in with group.protocol=streams.
- pass 1: ✅ revised after pass-2 finding — C0735 streams → docs/streams/developer-guide/streams-rebalance-protocol.md "Only the sticky assignor is supported. This implies that warmup tasks ... are not supported yet"
- pass 2: ✅ docs/streams/developer-guide/streams-rebalance-protocol.md:60,80 — "warmup tasks ... are not supported yet"; "enabled by default on new clusters starting with Apache Kafka 4.2"; StreamsGroupHeartbeatRequest.json Topology/WarmupTasks

### C0736 · k-streams · L3 · p
> The docs list what's not yet supported: static membership, significant topology changes (need a new group), the high-availability assignor (so no warm-up tasks or rack awareness — only a sticky assignor), regex subscriptions, online migration, and supplying the main consumer via a custom KafkaClientSupplier. Migration is offline only; the upgrade notes recommend against migrating classic → streams on 4.2.0 because of a broker bug fixed in 4.2.1.
- pass 1: ✅ docs/streams/developer-guide/streams-rebalance-protocol.md (What's Not Supported); docs/getting-started/upgrade.md:85 — "Static Membership: Setting a client instance.id will be rejected."
- pass 2: ✅ streams-rebalance-protocol.md:44-54 — static membership, topology updates, HA assignor (no warmup/rack), regex, online migration, custom KafkaClientSupplier main consumer; upgrade-guide.md:144 — "we recommend against doing migrations from classic to streams groups in 4.2.0 ... fix is available in 4.2.1"

### C0737 · k-streams · L3 · tr
> processing.guarantee | at_least_once | exactly_once_v2 = transactions + read_committed | Results must not double-count
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:403-411,1020-1023 — "in(AT_LEAST_ONCE, EXACTLY_ONCE_V2)"
- pass 2: ✅ StreamsConfig.java:1020-1023 default AT_LEAST_ONCE, in(AT_LEAST_ONCE, EXACTLY_ONCE_V2); config-streams.md:1620 — "consumers are configured with isolation.level=\"read_committed\""

### C0738 · k-streams · L3 · tr
> num.standby.replicas | 0 | Shadow copies of each task's state | Large state, fast failover needed
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:896-898 — "define(NUM_STANDBY_REPLICAS_CONFIG, Type.INT, 0"
- pass 2: ✅ StreamsConfig.java:896-898 — NUM_STANDBY_REPLICAS_CONFIG, Type.INT, 0

### C0739 · k-streams · L3 · tr
> num.stream.threads | 1 | Threads per instance running tasks | More cores than instances
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:1010-1012 — "define(NUM_STREAM_THREADS_CONFIG, Type.INT, 1"
- pass 2: ✅ StreamsConfig.java:1010-1012 — NUM_STREAM_THREADS_CONFIG, Type.INT, 1

### C0740 · k-streams · L3 · tr
> commit.interval.ms | 30000 (100 with EOS) | How often offsets/transactions are committed | Latency vs throughput under EOS
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:167-168 — "DEFAULT_COMMIT_INTERVAL_MS = 30000L / EOS_DEFAULT_COMMIT_INTERVAL_MS = 100L"
- pass 2: ✅ config-streams.md:465 — "30000 (30 seconds) (at-least-once) / 100 (exactly-once)"

### C0741 · k-streams · L3 · tr
> acceptable.recovery.lag / max.warmup.replicas | 10000 / 2 | "Caught-up" threshold and warm-up budget (classic assignor) | Tuning task movement on scale-out
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:920-922,1004-1006 — "10_000L / 2"
- pass 2: ✅ StreamsConfig.java:920-922 (10_000L) and :1004-1006 (2); streams-rebalance-protocol.md lists both as ignored under streams protocol, so classic-only is right

### C0742 · k-streams · L3 · tr
> group.protocol | classic | streams = KIP-1071 broker-side assignment | 4.2+ brokers and clients, no unsupported features in use
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:581-582; docs/streams/developer-guide/streams-rebalance-protocol.md (Client Configuration) — "DEFAULT_GROUP_PROTOCOL = GroupProtocol.CLASSIC"
- pass 2: ✅ StreamsConfig.java:582 DEFAULT_GROUP_PROTOCOL = CLASSIC; streams-rebalance-protocol.md — "Both brokers and clients must be running Apache Kafka 4.2 or later"

### C0743 · k-streams · L3 · tr
> group.streams.num.standby.replicas (broker) / streams.num.standby.replicas (group) | 0 (max 2) | Standbys under the streams protocol, set on the broker/group | Streams groups — the broker assigns standbys
- pass 1: ✅ group-coordinator/src/main/java/org/apache/kafka/coordinator/group/GroupCoordinatorConfig.java:352-357; docs/streams/developer-guide/streams-rebalance-protocol.md (Group Configuration) — "STREAMS_GROUP_NUM_STANDBY_REPLICAS_DEFAULT = 0 / MAX_STANDBY_REPLICAS_DEFAULT = 2"
- pass 2: ✅ GroupCoordinatorConfig.java:353 STREAMS_GROUP_NUM_STANDBY_REPLICAS_DEFAULT = 0; :357 MAX_STANDBY_REPLICAS_DEFAULT = 2; streams-rebalance-protocol.md group config streams.num.standby.replicas

### C0744 · k-streams · L3 · p
> You run a Streams app with group.protocol=streams and application.id=counter-app on a 4.3 cluster. How do you check the feature, list/describe the streams group with its members and task assignment, and give it one standby replica per task?
- pass 1: n/a (exercise prompt)
- pass 2: n/a (exercise prompt)

### C0745 · k-streams · L3 · pre
> bin/kafka-features.sh --bootstrap-server localhost:9092 describe # streams.version bin/kafka-streams-groups.sh --bootstrap-server localhost:9092 --list bin/kafka-streams-groups.sh --bootstrap-server localhost:9092 --describe --group counter-app --members --verbose bin/kafka-configs.sh --bootstrap-server localhost:9092 --alter --entity-type groups \ --entity-name counter-app --add-config streams.num.standby.replicas=1
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/streams/StreamsGroupCommandOptions.java (list, describe, members, verbose); core/src/main/scala/kafka/admin/ConfigCommand.scala:57 — "accepts("members" / accepts("verbose""
- pass 2: ✅ FeatureCommand.java:139 describe subcommand; kafka-streams-group-sh.md:67,81 — "--list", "--describe --group my-streams-app --members --verbose"; streams-rebalance-protocol.md — "--alter --entity-type groups --entity-name wordcount --add-config streams.num.standby.replicas=1"

### C0746 · k-streams · L3 · p
> Under the streams protocol, standbys are a broker/group setting (bounded by group.streams.max.standby.replicas, default 2), not the client's num.standby.replicas.
- pass 1: ✅ group-coordinator/src/main/java/org/apache/kafka/coordinator/group/GroupCoordinatorConfig.java:352-357; docs/streams/developer-guide/streams-rebalance-protocol.md (Broker Configuration) — "Maximum for dynamic configurations of the standby replica configuration."
- pass 2: ✅ streams-rebalance-protocol.md — "num.standby.replicas are group-level configurations, which are ignored when they are set on the client side"; GroupCoordinatorConfig.java:357 max default 2

### C0747 · k-streams · L3 · summary
> L3🔬 Go deeper: what the broker learns about your app
- pass 1: n/a (heading)
- pass 2: n/a (summary heading)

### C0748 · k-streams · L3 · p
> StreamsGroupHeartbeatRequest carries MemberEpoch, a Topology (with Epoch and Subtopologies, each listing SourceTopics, StateChangelogTopics, RepartitionSinkTopics, RepartitionSourceTopics and CopartitionGroups), the member's current ActiveTasks / StandbyTasks / WarmupTasks, its ProcessId, UserEndpoint (for interactive queries), and TaskOffsets / TaskEndOffsets so the broker can judge how caught-up each member's state is. ShutdownApplication lets one member request a coordinated shutdown of the whole app.
- pass 1: ✅ clients/src/main/resources/common/message/StreamsGroupHeartbeatRequest.json:24-91 — "TaskEndOffsets / ShutdownApplication"
- pass 2: ✅ clients/.../message/StreamsGroupHeartbeatRequest.json fields MemberEpoch, Topology{Epoch, Subtopologies{SourceTopics, StateChangelogTopics, RepartitionSinkTopics, RepartitionSourceTopics, CopartitionGroups}}, ActiveTasks/StandbyTasks/WarmupTasks, ProcessId, UserEndpoint ("User-defined endpoint for Interactive Queries"), TaskOffsets/TaskEndOffsets, ShutdownApplication ("Whether all Streams clients in the group should shut down")

### C0749 · k-streams · L3 · p
> Broker defaults (GroupCoordinatorConfig): streams session timeout 45000 ms (bounds 45000–60000), heartbeat 5000 ms (5000–15000), initial rebalance delay 3000 ms, assignment interval 1000 ms (new in 4.3, KIP-1263).
- pass 1: ✅ group-coordinator/src/main/java/org/apache/kafka/coordinator/group/GroupCoordinatorConfig.java:325-374; docs/getting-started/upgrade.md:54 — "STREAMS_GROUP_INITIAL_REBALANCE_DELAY_MS_DEFAULT = 3000"
- pass 2: ✅ GroupCoordinatorConfig.java:325-345 session 45000 (min 45000, max 60000), heartbeat 5000 (5000–15000); :361 initial delay 3000; :366 assignment interval 1000; upgrade.md:54 — "group.streams.assignment.interval.ms ... default to an interval of 1 second ... KIP-1263"

### C0750 · k-streams · L3 · p
> org.apache.kafka.streams.processor.TaskId.toString() is subtopology + "_" + partition (prefixed with the topology name for named topologies). With EOS v2 each stream thread uses one transactional producer for all its tasks.
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/processor/TaskId.java:75-76; streams/.../processor/internals/ActiveTaskCreator.java:64,95 — "subtopology + "_" + partition / private final StreamsProducer streamsProducer"
- pass 2: ✅ TaskId.java:76 — "topologyName + NAMED_TOPOLOGY_DELIMITER + subtopology + \"_\" + partition"; ActiveTaskCreator.java:64,95-107 single StreamsProducer per thread with TRANSACTIONAL_ID_CONFIG

### C0751 · k-streams · L3 · li
> Topology → sub-topologies (linked only via topics, e.g. repartition topics) → one task per input partition; tasks are the fixed unit of parallelism.
- pass 1: ❌ fixed in part file — TopologyDescription javadoc; streams/architecture.md one task per input partition
- pass 2: ✅ architecture.md:45 fixed unit of parallelism; tutorial.md:410 sub-topologies linked via repartition topic

### C0752 · k-streams · L3 · li
> State stores are backed by compacted changelog topics; restoring = replaying the changelog.
- pass 1: ✅ docs/streams/architecture.md:88 — "Log compaction is enabled on the changelog topics"
- pass 2: ✅ architecture.md:88 — "Log compaction is enabled on the changelog topics ... replaying the corresponding changelog topics"

### C0753 · k-streams · L3 · li
> num.standby.replicas (default 0) keeps warm copies for fast failover.
- pass 1: ✅ streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:896-898; docs/streams/architecture.md:90 — "standby replicas of local states"
- pass 2: ✅ StreamsConfig.java:896-898 default 0; architecture.md:90 standby replicas minimize re-init cost

### C0754 · k-streams · L3 · li
> exactly_once_v2 = transactions over output, changelogs and offsets; commit interval 100 ms; use RF 3.
- pass 1: ✅ docs/streams/developer-guide/config-streams.md:1620-1624 — "using a replication factor lower than 3 effectively voids EOS"
- pass 2: ✅ StreamsConfig.java:168 EOS commit 100 ms; config-streams.md:1623 — "strongly recommended to use a replication factor of 3"

### C0755 · k-streams · L3 · li
> KIP-1071 streams groups (group.protocol=streams, core GA in 4.2): broker-side task assignment, with documented gaps (static membership, warm-ups, regex, online migration).
- pass 1: ✅ docs/streams/developer-guide/streams-rebalance-protocol.md; docs/getting-started/upgrade.md:85 — "production-ready for its core feature set"
- pass 2: ✅ upgrade.md:85 — "production-ready for its core feature set"; streams-rebalance-protocol.md:44-54 lists static membership, HA assignor (no warmup tasks), regex, online migration as unsupported
