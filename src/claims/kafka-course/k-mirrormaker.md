# Claims ledger — kafka-course.html — k-mirrormaker

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0888 · k-mirrormaker · L4 · p
> Replication inside a cluster (ch. 5, 10) keeps photocopies in other branches of the same archive, coordinated by one city council. Geo-replication is different: two independent archives in two cities, each with its own council, its own offsets and its own consumer groups. Between them sits a copying office that reads one archive and writes into the other. In Kafka that office is MirrorMaker 2 (MM2). The original MirrorMaker 1 was removed in 4.0.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:54 — "intra-cluster replication" ; docs/getting-started/upgrade.md:252 — "The original MirrorMaker (MM1) and related classes were removed"
- pass 2: ✅ docs/getting-started/upgrade.md:252 — "The original MirrorMaker (MM1) and related classes were removed." (4.0 notes); rest is analogy

### C0889 · k-mirrormaker · L4 · p
> People use it for disaster recovery, for feeding edge clusters into a central one, for separating production from testing, for cloud migrations, and for legal requirements about where data lives. MM2 replicates topic data and topic configurations, consumer group offsets and ACLs. It preserves partitioning and discovers new topics and partitions automatically.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:35 — "Feeding edge clusters into a central, aggregate cluster" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:44 — "Replicates topics (data plus configurations)" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:47 — "Preserves partitioning" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:48 — "Automatically detects new topics and partitions"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:34-48 — "Disaster recovery", "Feeding edge clusters", "production vs. testing", "Cloud migration", "Legal"; "Replicates ACLs", "Preserves partitioning", "Automatically detects new topics and partitions"

### C0890 · k-mirrormaker · L4 · h3
> Built from Connect parts
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0891 · k-mirrormaker · L4 · p
> MM2 is not a separate engine. It's a set of Kafka Connect source connectors, which is why it inherits Connect's scaling (run more MM2 processes and they share the work) and Connect's metrics:
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:42 — "MirrorMaker is built on top of the Kafka Connect framework" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:414 — "load balance their work" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:447 — "inherits all of Connect's metrics"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:192 — "MirrorMaker uses Kafka Connect source connectors"; :447 "inherits all of Connect's metrics"; :49 "horizontally scalable"

### C0892 · k-mirrormaker · L4 · li
> MirrorSourceConnector: "Replicate data, configuration, and ACLs between clusters." Consumes from the source, produces to the target, and writes offset syncs that pair upstream offsets with downstream ones.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConnector.java:80 — "Replicate data, configuration, and ACLs between clusters" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:37 — "Stores offset syncs and performs offset translation"
- pass 2: ✅ MirrorSourceConnector.java:80 — "Replicate data, configuration, and ACLs between clusters."; OffsetSync pairs upstream/downstream offsets (written by MirrorSourceTask via OffsetSyncWriter)

### C0893 · k-mirrormaker · L4 · li
> MirrorCheckpointConnector: "Replicate consumer group state between clusters. Emits checkpoint records." It translates each group's committed offsets into target offsets.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorCheckpointConnector.java:53 — "Replicate consumer group state between clusters"
- pass 2: ✅ MirrorCheckpointConnector.java:53 — "Replicate consumer group state between clusters. Emits checkpoint records."

### C0894 · k-mirrormaker · L4 · li
> MirrorHeartbeatConnector: "Emits heartbeats to Kafka." A tiny heartbeats topic that proves the flow is alive and lets tools trace the replication topology.
- pass 1: ✅ revised after pass-2 finding — C0894: quote "Emits heartbeats." → "Emits heartbeats to Kafka." Source: connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorHeartbeatConnector.java:33.
- pass 2: ✅ connect/mirror/.../MirrorHeartbeatConnector.java:33 — "Emits heartbeats to Kafka."; RemoteClusterUtils.upstreamClusters/replicationHops use heartbeats

### C0895 · k-mirrormaker · L4 · h3
> Flows, aliases and remote topics
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0896 · k-mirrormaker · L4 · p
> You name clusters with aliases and enable directional flows source->target. A flow is off until you set {source}->{target}.enabled = true. Topologies are just sets of flows: active/passive A->B, active/active A->B, B->A, aggregation A->K, B->K, fan-out K->A, K->B.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:231 — "set to `true` to enable the replication flow (default: `false`)" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:64 — "Active/Active high availability deployments"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:231 — "set to true to enable the replication flow (default: false)"; :64-68 topology patterns A->B, A->B B->A, A->K B->K, K->A K->B

### C0897 · k-mirrormaker · L4 · pre
> clusters = us-west, us-east us-west.bootstrap.servers = broker1-west:9092 us-east.bootstrap.servers = broker3-east:9092 us-west->us-east.enabled = true us-west->us-east.topics = orders.*
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:120 — "us-west->us-east.enabled = true"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:114-121 — same syntax "clusters = us-west, us-east", "us-west->us-east.enabled = true", "us-west->us-east.topics"

### C0898 · k-mirrormaker · L4 · p
> With the DefaultReplicationPolicy, a replicated ("remote") topic is renamed {source}.{topic}, so orders from us-west becomes us-west.orders in us-east. That prefix is not decoration. It stops two clusters from writing into the same topic-partition, and it's how active/active avoids infinite loops: MM2 knows us-west.orders came from us-west and won't send it back. IdentityReplicationPolicy keeps names unchanged, but its javadoc warns that MM2 then "is not able to prevent cycles", so use it only for one-way topologies.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:269 — "`{source}.{source_topic_name}`" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:269 — "ensure that events (aka records, messages) from different clusters are not written to the same topic-partition" ; connect/mirror-client/src/main/java/org/apache/kafka/connect/mirror/IdentityReplicationPolicy.java:28 — "not able to prevent cycles"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:269 — "ensure that events ... from different clusters are not written to the same topic-partition", format {source}.{source_topic_name}; :356 loops prevented; IdentityReplicationPolicy.java:28 "MirrorMaker is not able to prevent cycles when using this replication policy"

### C0899 · k-mirrormaker · L4 · figcaption
> Illustrative offsets and sync points. Real offset syncs are emitted when the remote partition drifts more than offset.lag.max (default 100) and in other situations; checkpoints are emitted every emit.checkpoints.interval.seconds (default 60). The translation rule (sync's downstream offset, +1 if the group is past the sync) is the one in OffsetSyncStore.translateDownstream.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConfig.java:89 — "OFFSET_LAG_MAX_DEFAULT = 100L" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorCheckpointConfig.java:62 — "EMIT_CHECKPOINTS_INTERVAL_SECONDS_DEFAULT = 60" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:153 — "long upstreamStep = upstreamOffset == offsetSync.get().upstreamOffset() ? 0 : 1" (offsets labelled illustrative)
- pass 2: ✅ MirrorSourceConfig.java:88-89 offset.lag.max 100 "How out-of-sync a remote partition can be before it is resynced"; MirrorCheckpointConfig.java:62 emit.checkpoints.interval.seconds 60; OffsetSyncStore.java:153-159 downstream + (0 or 1)

### C0900 · k-mirrormaker · L4 · h3
> Offsets don't survive the trip
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0901 · k-mirrormaker · L4 · p
> This is the single most important idea in the chapter. The target cluster assigns its own offsets. If replication starts when the source partition begins at 1000, the remote partition begins at 0. Filtered or aborted records widen the gap further. A consumer group's committed offset "1007" means nothing in the other city.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:143 — "we cannot estimate how many offsets from the upstream topic" (1000→0 example illustrative)
- pass 2: ✅ OffsetSyncStore.java:141-144 — "we cannot estimate how many offsets from the upstream topic will be written vs dropped"; target log assigns its own offsets

### C0902 · k-mirrormaker · L4 · p
> So MM2 keeps a translation table. The source connector writes offset syncs (upstream ↔ downstream pairs) to mm2-offset-syncs.<target>.internal, which by default lives on the source cluster (offset-syncs.topic.location defaults to source). The checkpoint connector reads each group's committed offsets, translates them, and writes checkpoints to <source>.checkpoints.internal on the target. Applications can read them via RemoteClusterUtils.translateOffsets(...). Alternatively, set sync.group.offsets.enabled=true (default false) and MM2 writes the translated offsets into the target's __consumer_offsets, "as long as no active consumers in that group are connected to the target cluster".
- pass 1: ✅ revised after pass-2 finding — C0902: "mm2-offset-syncs.<alias>.internal" → "mm2-offset-syncs.<target>.internal, which by default lives on the source cluster (offset-syncs.topic.location defaults to source)". Sources: MirrorConnectorConfig.java:116 "OFFSET_SYNCS_TOPIC_LOCATION_DEFAULT = SOU
- pass 2: ✅ MirrorConnectorConfig.java:116 location default "source"; MirrorSourceConfig.java:128-132 uses target alias → "mm2-offset-syncs.<target>.internal"; MirrorCheckpointConfig.java:65-66 "as long as no active consumers ..."; default false

### C0903 · k-mirrormaker · L4 · p
> Translation is deliberately conservative. MM2 only knows the sync points exactly. For a group that's past the nearest sync, it translates to "sync's downstream offset + 1". The code comment explains why: overestimating could skip records and lose data. The result is that after a failover you re-read some records, never skip them. Plan for at-least-once and idempotent processing.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:144 — "If we overestimate, then we may skip the correct offset and have data loss" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:146 — "This may cause re-reading of records"
- pass 2: ✅ OffsetSyncStore.java:141-146 — "If we overestimate, then we may skip the correct offset and have data loss." / "may cause re-reading of records"

### C0904 · k-mirrormaker · L4 · div
> Can't MM2 just keep the same offsets?
- pass 1: n/a (question heading)
- pass 2: n/a (FAQ heading)

### C0905 · k-mirrormaker · L4 · div
> Not in general. The target is a different log with its own history (retention, earlier data, filtered transactions). Offsets are positions in that log. That's exactly why offset syncs and checkpoints exist.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:53 — "Translation will be unavailable for all topic-partitions before an initial read-to-end" (explanation = pedagogy)
- pass 2: ✅ OffsetSyncStore.java:141-144 (records may be written vs dropped; target offsets are its own)

### C0906 · k-mirrormaker · L4 · div
> Is MM2 exactly-once?
- pass 1: n/a (question heading)
- pass 2: n/a (FAQ heading)

### C0907 · k-mirrormaker · L4 · div
> It can be, since 3.5, for dedicated MM2 clusters. Set <target>.exactly.once.source.support = enabled (existing clusters go through preparing first), dedicated.mode.enable.internal.rest = true, and read the source with isolation.level=read_committed so aborted transactions aren't copied. Consumer failover via translated offsets is still at-least-once, as above.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:167 — "as of version 3.5.0" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:175 — "first set it to `preparing`" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:177 — "`dedicated.mode.enable.internal.rest` property must be set to `true`" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:186 — "isolation.level` set to `read_committed`"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:167 — "Exactly-once semantics are supported for dedicated MirrorMaker clusters as of version 3.5.0"; :172,175 enabled / "first set it to preparing"; :180 dedicated.mode.enable.internal.rest = true; :186 isolation.level read_committed filters aborted transactions

### C0908 · k-mirrormaker · L4 · p
> Two MM2 processes that target the same cluster share configuration through that cluster. If process 1 says A->B.topics = foo and process 2 says A->B.topics = bar, the docs say one of them wins depending on who is the elected leader, and you get foo or bar, not both. Keep one shared config per target. Also note that newly created remote topics use MM2's replication.factor, whose default is 2 (the heartbeats, checkpoints and offset-syncs topics default to 3). Set it explicitly for production.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:303 — "either the topic `foo` or the topic `bar` is replicated, but not both" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConfig.java:37 — "REPLICATION_FACTOR_DEFAULT = 2" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorHeartbeatConfig.java:30 — "HEARTBEATS_TOPIC_REPLICATION_FACTOR_DEFAULT = 3" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorCheckpointConfig.java:42 — "CHECKPOINTS_TOPIC_REPLICATION_FACTOR_DEFAULT = 3" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConfig.java:53 — "OFFSET_SYNCS_TOPIC_REPLICATION_FACTOR_DEFAULT = 3"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:303 — "Depending on which of the two processes is the elected leader ... either the topic foo or the topic bar is replicated, but not both"; MirrorSourceConfig.java:37 REPLICATION_FACTOR_DEFAULT = 2; :53, MirrorCheckpointConfig.java:42, MirrorHeartbeatConfig.java:30 internal topics = 3

### C0909 · k-mirrormaker · L4 · p
> Active/passive DR with "consume from remote, produce to local":
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:307 — "Best Practice: Consume from Remote, Produce to Local"
- pass 2: n/a (lead-in)

### C0910 · k-mirrormaker · L4 · li
> Run MM2 next to the target. Producers suffer more from long, unreliable links than consumers do: bin/connect-mirror-maker.sh connect-mirror-maker.properties --clusters secondary.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:309 — "Kafka producers typically struggle more with unreliable or high-latency network connections" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:322 — "connect-mirror-maker.properties --clusters secondary"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:309 — "locate MirrorMaker processes as close as possible to their target clusters"; :322 "bin/connect-mirror-maker.sh connect-mirror-maker.properties --clusters secondary"

### C0911 · k-mirrormaker · L4 · li
> Enable primary->secondary only. Set replication.factor=3 and primary.consumer.isolation.level = read_committed.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:83 — "primary->secondary.enabled = true" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConfig.java:35 — "REPLICATION_FACTOR = "replication.factor"" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:156 — "us-west.consumer.isolation.level = read_committed"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:336-337 primary->secondary.enabled = true; :156 "{alias}.consumer.isolation.level = read_committed"; replication.factor is MirrorSourceConfig.java:37 (advice to set 3)

### C0912 · k-mirrormaker · L4 · li
> Decide how groups fail over: sync.group.offsets.enabled=true (offsets pre-written to the passive side), or translate at failover time with RemoteClusterUtils.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorCheckpointConfig.java:65 — "as long as no active consumers in that group are connected" ; connect/mirror-client/src/main/java/org/apache/kafka/connect/mirror/RemoteClusterUtils.java:90 — "Translates a remote consumer group"
- pass 2: ✅ MirrorCheckpointConfig.java:65 sync.group.offsets.enabled; RemoteClusterUtils.java:97 translateOffsets

### C0913 · k-mirrormaker · L4 · li
> Monitor replication-latency-ms, record-age-ms (MirrorSourceConnector) and checkpoint-latency-ms (MirrorCheckpointConnector) under kafka.connect.mirror.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:473 — "replication-latency-ms  # time it takes records" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:469 — "record-age-ms           # age of records" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:482 — "checkpoint-latency-ms   # time it takes to replicate consumer offsets"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:447 "under the kafka.connect.mirror metric group"; :466-485 replication-latency-ms, record-age-ms (MirrorSourceConnector), checkpoint-latency-ms (MirrorCheckpointConnector)

### C0914 · k-mirrormaker · L4 · li
> On failover, point consumers at primary.orders on the secondary cluster, with idempotent processing.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:269 — "`{source}.{source_topic_name}`" (idempotent processing: consequence of C0903)
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:269-275 remote topic named {source}.{topic} (e.g. us-west.foo-topic) → primary.orders

### C0915 · k-mirrormaker · L4 · summary
> L4🔬 Go deeper: OffsetSyncStore's exponential memory
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0916 · k-mirrormaker · L4 · p
> OffsetSyncStore can't keep every sync forever, so it keeps up to 64 per partition (SYNCS_PER_PARTITION = Long.SIZE), spaced "approximately exponential[ly]": dense near the live end of the topic, sparse further back (its invariants: syncs[0] is the latest sync, syncs[63] the earliest usable one). Translation picks the sync that most closely precedes the group's offset. It returns nothing until the store has read the syncs topic to the end once ("This prevents emitting stale offsets"). If the group is behind the oldest sync it returns -1 ("too far in the past to translate accurately"). The practical effect: lagging groups translate less precisely and re-read more after a failover. Other defaults worth knowing: topics are rescanned every refresh.topics.interval.seconds (600), topic configs sync every 600 s (sync.topic.configs.enabled defaults to true), heartbeats are emitted every second, and topics.exclude defaults to mm2.*\.internal, .*\.replica, __.* in the 4.3.1 source (DefaultTopicFilter). The geo-replication docs page still shows an older pattern.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:63 — "SYNCS_PER_PARTITION = Long.SIZE" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:48 — "approximately exponential space" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:127 — "This prevents emitting stale offsets" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:135 — "Offset is too far in the past to translate accurately" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConfig.java:66 — "REFRESH_TOPICS_INTERVAL_SECONDS_DEFAULT = 10 * 60" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConfig.java:70 — "SYNC_TOPIC_CONFIGS_ENABLED_DEFAULT = true" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorHeartbeatConfig.java:37 — "EMIT_HEARTBEATS_INTERVAL_SECONDS_DEFAULT = 1" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/DefaultTopicFilter.java:36 — "TOPICS_EXCLUDE_DEFAULT ="
- pass 2: ✅ OffsetSyncStore.java:63 "SYNCS_PER_PARTITION = Long.SIZE"; :42,45 invariants A/D; :48 "approximately exponential space"; :50 "most closely precedes"; :126-127 "This prevents emitting stale offsets"; :135-139 "too far in the past to translate accurately" → -1; MirrorSourceConfig.java:66,70,73 (600 s, true, 600 s); MirrorHeartbeatConfig.java:37 (1 s); DefaultTopicFilter.java:36 "mm2.*\\.internal, .*\\.replica, __.*" vs docs/operations/geo-replication-(cross-cluster-data-mirroring).md:228 ".*[\-\.]internal, .*\.replica, __.*"

### C0917 · k-mirrormaker · L4 · li
> MM2 = Connect source connectors: Source (data, configs, ACLs), Checkpoint (group offsets), Heartbeat. MM1 is gone since 4.0.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConnector.java:80 — "Replicate data, configuration, and ACLs" ; docs/getting-started/upgrade.md:252 — "The original MirrorMaker (MM1) and related classes were removed"
- pass 2: ✅ MirrorSourceConnector.java:80, MirrorCheckpointConnector.java:53, MirrorHeartbeatConnector.java:33; upgrade.md:252 MM1 removed

### C0918 · k-mirrormaker · L4 · li
> Flows are directional and off by default. Remote topics are named {source}.{topic}, which prevents loops.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:231 — "set to `true` to enable the replication flow (default: `false`)" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:356 — "you do not need to explicitly add `topics.exclude` settings to prevent replication loops"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:231 default false; :269 {source}.{source_topic_name}; :356 loop prevention

### C0919 · k-mirrormaker · L4 · li
> Offsets differ across clusters. Offset syncs plus checkpoints translate them conservatively, so failover re-reads rather than skips.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:144 — "If we overestimate, then we may skip the correct offset"
- pass 2: ✅ OffsetSyncStore.java:141-146 — overestimate would lose data; "may cause re-reading of records"

### C0920 · k-mirrormaker · L4 · li
> sync.group.offsets.enabled defaults to false; remote-topic replication.factor defaults to 2.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorCheckpointConfig.java:66 — "SYNC_GROUP_OFFSETS_ENABLED_DEFAULT = false" ; connect/mirror/src/main/java/org/apache/kafka/connect/mirror/MirrorSourceConfig.java:37 — "REPLICATION_FACTOR_DEFAULT = 2"
- pass 2: ✅ MirrorCheckpointConfig.java:66 SYNC_GROUP_OFFSETS_ENABLED_DEFAULT = false; MirrorSourceConfig.java:37 REPLICATION_FACTOR_DEFAULT = 2

### C0921 · k-mirrormaker · L4 · li
> Run MM2 near the target; one consistent config per target cluster.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:309 — "locate MirrorMaker processes as close as possible to their target clusters" ; docs/operations/geo-replication-(cross-cluster-data-mirroring).md:305 — "keep the MirrorMaker configuration consistent across replication flows"
- pass 2: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:309 near target; :303 shared-config conflict
