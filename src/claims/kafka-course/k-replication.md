# Claims ledger — kafka-course.html — k-replication

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0173 · k-replication · L1 · p
> Disks die. Machines reboot. A whole rack loses power. If each partition lived on only one broker, any of those would make data unavailable or lose it. So Kafka keeps replicas: copies of each partition on several brokers. In the archive, every ledger book has photocopies kept in other branches, and the copies are updated as new lines are written.
- pass 1: ✅ docs/design/design.md:281 + docs/getting-started/introduction.md:87 — "automatic failover to these replicas when a server in the cluster fails" (analogy)
- pass 2: ✅ docs/design/design.md:281 — "replicates the log for each topic's partitions across a configurable number of servers" (rest is analogy)

### C0174 · k-replication · L1 · h3
> Leader and followers
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0175 · k-replication · L1 · p
> The number of copies is the topic's replication factor (RF). You set it per topic, and it includes the original. One replica is the leader. It receives all writes (and normally all reads). The others are followers. They fetch new records from the leader, just like a consumer would, and append them to their own copy. All replicas have the same records at the same offsets in the same order. A follower may just be a few records behind at the tail.
- pass 1: ✅ docs/design/design.md:285-287 — "total number of replicas including the leader constitute the replication factor"; "reads can go to the leader or the followers"; "Followers consume messages from the leader"
- pass 2: ✅ docs/design/design.md:285-287 — "total number of replicas including the leader constitute the replication factor. All writes go to the leader... reads can go to the leader or the followers"; "Followers consume messages from the leader just as a normal Kafka consumer would"

### C0176 · k-replication · L1 · p
> The broker-wide default for new topics is default.replication.factor=1, which means no copies at all. The docs call RF 3 a common production setting. Set it explicitly (for example --replication-factor 3).
- pass 1: ✅ server/.../ReplicationConfigs.java:40-41 + docs/getting-started/introduction.md:87 + TopicCommand.java:765 — "REPLICATION_FACTOR_DEFAULT = 1"; "A common production setting is a replication factor of 3"
- pass 2: ✅ server/.../ReplicationConfigs.java:42 REPLICATION_FACTOR_DEFAULT = 1; docs/getting-started/introduction.md:87 — "A common production setting is a replication factor of 3"

### C0177 · k-replication · L1 · h3
> In-sync replicas (ISR)
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0178 · k-replication · L1 · p
> Not every follower is always up to date. The leader tracks the in-sync replicas (ISR): the replicas that are caught up. A replica is in sync if its broker has a live session with the controller and it keeps up with the leader. A follower that can't catch up to the leader's log end within replica.lag.time.max.ms (default 30000 ms) is removed from the ISR. It rejoins after catching up fully.
- pass 1: ✅ docs/design/design.md:289-298,335 + ReplicationConfigs.java:53-55 — "Replicas that cannot catch up to the end of the log on the leader within the max time"; "30000L"; "must fully re-sync"
- pass 2: ✅ docs/design/design.md:290-298 — "active session with the controller"; "Replicas that cannot catch up to the end of the log on the leader within... replica.lag.time.max.ms... are removed"; server/.../ReplicationConfigs.java:55 default 30000L

### C0179 · k-replication · L1 · p
> A record is committed when all current ISR members have it and the ISR has at least min.insync.replicas members (more on that setting below). Only committed records are handed to consumers. If the leader dies, the controller picks the new leader from the ISR. Every ISR member has every committed record, so nothing committed is lost. With RF f+1, Kafka can survive f failed replicas without losing committed records.
- pass 1: ✅ revised after pass-2 finding — C0179 — "committed when all current ISR members have it and the ISR has at least min.insync.replicas members" — Partition.scala:999,1011-1013; clients/.../TopicConfig.java:182-183 "messages will not be visible to the consumers until they are replicated to all 
- pass 2: ✅ docs/design/design.md:191,302 — "committed only when all replicas in the in-sync replicas"; + min ISR visibility condition; f+1 replicas tolerate f failures

### C0180 · k-replication · L1 · h3
> acks and min.insync.replicas: your durability dial
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0181 · k-replication · L1 · p
> The producer's acks decides when it hears "done":
- pass 1: n/a (lead-in)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:127

### C0182 · k-replication · L1 · li
> acks=0: the record is put in the socket buffer and considered sent. No guarantee the broker got it; retries don't apply. The returned offset is always -1.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:131-134 — "immediately added to the socket buffer and considered sent … retries … will not take effect … always be set to -1"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:130-134 — "immediately added to the socket buffer and considered sent... retries... will not take effect... always be set to -1"

### C0183 · k-replication · L1 · li
> acks=1: the leader wrote it to its own log. If the leader dies before a follower copies it, it's lost.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:135-137 — "should the leader fail immediately after acknowledging … the record will be lost"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:135-137 — "should the leader fail immediately after acknowledging... before the followers have replicated it then the record will be lost"

### C0184 · k-replication · L1 · li
> acks=all (default): the leader waits for the full ISR. Not lost as long as one in-sync replica survives.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:138-140 — "will not be lost as long as at least one in-sync replica remains alive"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:138-140,391-393 — "wait for the full set of in-sync replicas"; default "all"

### C0185 · k-replication · L1 · p
> There's a trap in acks=all. "All" means all current ISR members, and the ISR can shrink. If only the leader is left in the ISR, acks=all is effectively acks=1. The topic setting min.insync.replicas (default 1) closes the gap. With acks=all, if the ISR is smaller than this number, the write is rejected with NotEnoughReplicas (or NotEnoughReplicasAfterAppend) instead of being accepted with too few copies. The Kafka docs give the typical recipe: RF 3, min.insync.replicas=2, acks=all. A majority must persist each write, and you can still lose one broker and keep writing.
- pass 1: ✅ docs/design/design.md:353-356 + clients/.../common/config/TopicConfig.java:175-187 + server-common/.../ServerLogConfigs.java:155 — "writes that specify acks=all will succeed"; "NotEnoughReplicas or NotEnoughReplicasAfterAppend"; "replication factor of 3, set min.insync.replicas to 2 … acks of \"all\""
- pass 2: ✅ docs/design/design.md:353; clients/.../common/config/TopicConfig.java:175-187 — "NotEnoughReplicas or NotEnoughReplicasAfterAppend"; "replication factor of 3, set min.insync.replicas to 2... acks of all... a majority"; ServerLogConfigs.java:155 default 1

### C0186 · k-replication · L1 · figcaption
> Simplified: one partition, followers fetch in one visible step, and the "kill" happens at the exact moment that hurts most. Record values are illustrative. The acks semantics, the "new leader comes from the ISR" rule and the NotEnoughReplicas rejection follow the producer and topic config docs.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:127-141 + clients/.../common/config/TopicConfig.java:175-181 + docs/design/design.md:331 (simplification labelled)
- pass 2: ✅ figcaption; mechanisms per ProducerConfig ACKS_DOC and TopicConfig MIN_IN_SYNC_REPLICAS_DOC

### C0187 · k-replication · L1 · div
> acks=1I'm fast. The leader writes, I say "done", everyone's happy.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a dialogue

### C0188 · k-replication · L1 · div
> acks=allUntil the leader's disk dies a millisecond later and the new leader has never heard of your record. Your producer was told "done" for data that no longer exists.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:135-137 — "the record will be lost"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:135-137 (acks=1 loss on leader failure)

### C0189 · k-replication · L1 · div
> acks=1You're slower, though.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a dialogue

### C0190 · k-replication · L1 · div
> acks=allA little. I wait for the in-sync followers. With batching, that wait is paid once per batch, not once per record, so it's usually a small price.
- pass 1: ✅ docs/design/design.md:287 + clients/.../producer/ProducerConfig.java:89 — followers "naturally batch"; one batch per partition per request (cost = opinion)
- pass 2: ✅ reasonable: acks are per produce request (batched records); latency waits for ISR (design.md:333) — soft/pedagogical

### C0191 · k-replication · L1 · p
> min.insync.replicas equal to RF (e.g. 3 with RF 3) sounds safe but means any single broker restart blocks all acks=all writes to that partition. Use RF − 1. The other trap is RF 1 in production: the default. One lost disk and the data is gone, whatever acks says.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:180-181 — "ISR set contains fewer than min.insync.replicas members, then the producer will raise an exception"; ReplicationConfigs.java:41 default RF 1 (RF−1 advice = opinion)
- pass 2: ✅ docs/design/design.md:356 "unavailable for writes if the number of in-sync replicas drops below the minimum"; RF default 1 (server/.../ReplicationConfigs.java:42); "RF − 1" is advice

### C0192 · k-replication · L1 · p
> A topic has RF 3 and min.insync.replicas=2. Producers use acks=all. For each case, can producers write, and can committed data be lost? a) One broker down. b) Two brokers down. c) Same as b) but the producer uses acks=1.
- pass 1: n/a (exercise question)
- pass 2: n/a quiz question

### C0193 · k-replication · L1 · p
> a) Writes succeed (ISR = 2 ≥ 2); every acknowledged record is on two brokers. b) acks=all writes are rejected with NotEnoughReplicas (ISR = 1 < 2). The partition stays readable from the leader. No acknowledged data at risk, but it's unavailable for writes. c) Writes are accepted because min.insync.replicas is only checked for acks=all. But they are not committed, so consumers can't see them until the ISR grows back to 2. Meanwhile each record lives on one broker. Lose it and those records are gone.
- pass 1: ✅ revised after pass-2 finding — C0193 — pencil answer c): acks=1 writes accepted but not committed/visible until ISR grows back to 2; lost if that broker dies — TopicConfig.java:182-183 "Regardless of the acks setting, the messages will not be visible to the consumers until …"
- pass 2: ✅ Partition.scala:1234 min ISR only for acks=-1; :1011 HW frozen under min ISR; design.md:302-305 visibility conditions

### C0194 · k-replication · L1 · p
> To try it for real, create a topic with the setting: bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic safe --partitions 3 --replication-factor 3 --config min.insync.replicas=2 (needs 3 brokers).
- pass 1: ✅ TopicCommand.java:730,747,760,765 — accepts("create"), ("config"), ("partitions"), ("replication-factor")
- pass 2: ✅ kafka-topics.sh --create --topic --partitions --replication-factor --config are valid flags; min.insync.replicas topic config (TopicConfig.java:175)

### C0195 · k-replication · L2 · p
> Why not majority voting? Many systems (Raft, ZooKeeper's Zab) commit on a majority: 2f+1 replicas to survive f failures. Kafka's ISR approach survives f failures with f+1 replicas, because it waits for all in-sync replicas and elects only from them. The design docs argue this matters for bulk data. Five copies of every byte for two-failure tolerance would cost 5× the disk and a fifth of the throughput. The cost is that a slow in-sync follower slows commits until it drops out of the ISR.
- pass 1: ✅ docs/design/design.md:323-333 — "2f+1 replicas"; "5x the disk space requirements and 1/5th the throughput"; "commit without the slowest servers is an advantage of the majority vote"
- pass 2: ✅ docs/design/design.md:323,329,331 — "to tolerate two failures requires five copies... 5x the disk space requirements and 1/5th the throughput"; "f+1 replicas... tolerate f failures"; 333 slowest-server tradeoff

### C0196 · k-replication · L2 · p
> When every replica dies. Kafka then waits for an ISR member to come back instead of electing a stale replica. That's unclean.leader.election.enable=false, the default. Turning it on trades consistency (possible loss of committed data) for availability.
- pass 1: ✅ docs/design/design.md:343-347 + storage/.../log/LogConfig.java:133 — "favor waiting for a consistent replica"; "DEFAULT_UNCLEAN_LEADER_ELECTION_ENABLE = false"
- pass 2: ✅ docs/design/design.md:341-347 — "Wait for a replica in the ISR to come back"; LogConfig.java:133 DEFAULT_UNCLEAN_LEADER_ELECTION_ENABLE = false

### C0197 · k-replication · L3 · summary
> L3🔬 Go deeper: who does what on the broker
- pass 1: n/a (summary heading)
- pass 2: n/a heading

### C0198 · k-replication · L3 · p
> On each broker, ReplicaManager owns the local replicas and handles produce and fetch requests. Each partition is a kafka.cluster.Partition object that knows its leader, ISR and log (UnifiedLog). Followers replicate by sending ordinary fetch requests to the leader. That's how a follower's progress reaches the leader, and how the leader decides when a record is committed (the high watermark, chapter 10). ISR changes are persisted in the cluster metadata through the controller, so any ISR member can be elected leader.
- pass 1: ✅ core/.../KafkaApis.scala:543,769 (replicaManager.handleProduceAppend / fetchMessages); core/.../cluster/Partition.scala; docs/design/design.md:287,331 — "Followers consume messages from the leader"; "ISR set is persisted in the cluster metadata"
- pass 2: ✅ core/.../server/ReplicaManager.scala:637,1664 (appendRecords, fetchMessages); core/src/main/scala/kafka/cluster/Partition.scala:163,202 (class Partition, log: UnifiedLog); docs/design/design.md:287,331 — "ISR set is persisted in the cluster metadata... any replica in the ISR is eligible"

### C0199 · k-replication · L3 · p
> The design docs also say the protocol doesn't require crashed replicas to keep all their data. A replica that lost unflushed data in a crash must fully re-sync before it rejoins the ISR. That's why Kafka doesn't need an fsync per write for correctness. And with the Eligible Leader Replicas feature (KIP-966, ch. 16), the meaning of min.insync.replicas changes a bit; the topic config docs point there.
- pass 1: ✅ docs/design/design.md:335 + clients/.../common/config/TopicConfig.java:188 — "must fully re-sync again even if it lost unflushed data"; "when the Eligible Leader Replicas feature is enabled, the semantics of this config changes"
- pass 2: ✅ docs/design/design.md:335 — "does not require that crashed nodes recover with all their data intact... fsync on every write... must fully re-sync"; clients/.../common/config/TopicConfig.java:188 ELR note

### C0200 · k-replication · L1 · li
> Each partition has RF replicas: one leader (takes writes) and followers that fetch from it.
- pass 1: ✅ docs/design/design.md:285-287 — "single leader and zero or more followers"
- pass 2: ✅ docs/design/design.md:285-287

### C0201 · k-replication · L1 · li
> The ISR is the set of caught-up replicas; lagging longer than replica.lag.time.max.ms (30 s) drops you out.
- pass 1: ✅ docs/design/design.md:298 + ReplicationConfigs.java:55 — "30000L"
- pass 2: ✅ docs/design/design.md:298; server/.../ReplicationConfigs.java:55 (30000)

### C0202 · k-replication · L1 · li
> Committed = on all ISR members (with ISR ≥ min.insync.replicas). Consumers only see committed records; new leaders come from the ISR.
- pass 1: ✅ revised after pass-2 finding — C0202 — bullet: "Committed = on all ISR members (with ISR ≥ min.insync.replicas)" — TopicConfig.java:182-183 "the min.insync.replicas condition is met"
- pass 2: ✅ design.md:191,302-305 — committed = on all ISR members and ISR ≥ min.insync.replicas for visibility

### C0203 · k-replication · L1 · li
> acks=0/1 can lose acknowledged writes; acks=all (default) can't while one ISR member survives.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:131-140 — acks=0 no guarantee; acks=1 "will be lost"; acks=all "will not be lost as long as at least one in-sync replica remains alive"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:130-140

### C0204 · k-replication · L1 · li
> min.insync.replicas (default 1) rejects acks=all writes when the ISR is too small.
- pass 1: ✅ ServerLogConfigs.java:155 + clients/.../common/config/TopicConfig.java:180-181 — "MIN_IN_SYNC_REPLICAS_DEFAULT = 1"
- pass 2: ✅ clients/.../common/config/TopicConfig.java:180-181; ServerLogConfigs.java:155

### C0205 · k-replication · L1 · li
> Typical recipe: RF 3, min.insync.replicas 2, acks all. Default RF is 1, so set it explicitly.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:185-186 + ReplicationConfigs.java:41 — "A typical scenario … replication factor of 3, set min.insync.replicas to 2"
- pass 2: ✅ clients/.../common/config/TopicConfig.java:185-186; server/.../ReplicationConfigs.java:42
