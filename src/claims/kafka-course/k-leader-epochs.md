# Claims ledger — kafka-course.html — k-leader-epochs

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0614 · k-leader-epochs · L3 · p
> Part II showed the happy path of replication: followers fetch, the high watermark (HW) advances, committed records are safe. This chapter is about the unhappy path: a leader dies holding records its followers never got, a new leader writes different records at the same offsets, and the old leader comes back. Two photocopies of the same ledger book now disagree about what's on line 6. Somebody has to tear out pages — the question is who, and how many.
- pass 1: n/a (chapter motivation)
- pass 2: n/a — chapter intro / analogy

### C0615 · k-leader-epochs · L3 · p
> A returning follower knows its own log and its own (possibly stale) high watermark. Why is "truncate everything above my high watermark, then fetch" not safe? Hint: followers learn the HW one fetch round after the leader advances it.
- pass 1: n/a (brain question)
- pass 2: n/a — thinking prompt; hint matches KIP-101: "the follower takes an extra round of RPC to update its high watermark"

### C0616 · k-leader-epochs · L3 · h3
> Why the high watermark wasn't enough
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0617 · k-leader-epochs · L3 · p
> Early Kafka truncated a restarting follower to its own HW. KIP-101 describes what went wrong. Because a follower learns the new HW only on its next fetch, its local HW lags. If it truncates to that stale HW and then becomes leader before re-fetching, records that were already committed vanish. And after a multi-broker power loss, replicas could end up with different records at the same offsets — divergent lineages that no amount of fetching would repair.
- pass 1: ✅ https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation — "should that follower become leader before it has caught up, some messages may be lost due to the truncation"
- pass 2: ✅ cwiki KIP-101 Motivation (Scenario 1 & 2) — "the follower takes an extra round of RPC to update its high watermark"; divergence after multiple hard failures

### C0618 · k-leader-epochs · L3 · h3
> Leader epochs: numbering the reigns
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0619 · k-leader-epochs · L3 · p
> The fix: every time the controller elects a new leader for a partition, the leader epoch goes up (the KRaft controller also bumps it on some other partition changes, such as certain reassignments), and every batch the leader writes is stamped with it (the partitionLeaderEpoch field in the batch header you met in Part II). Each replica also keeps a small leader epoch cache — a list of epoch → first offset of that epoch, persisted in the partition directory as leader-epoch-checkpoint.
- pass 1: ❌ fixed in part file — PartitionChangeBuilder triggerLeaderEpochBumpForReplicaReassignmentIfNeeded / triggerLeaderEpochBumpForIsrShrinkIfNeeded
- pass 2: ✅ storage/.../epoch/LeaderEpochFileCache.java:44-47 — "cache of (LeaderEpoch => Offset) mappings"; LeaderEpochCheckpointFile.java:45 "leader-epoch-checkpoint"; PartitionChangeBuilder.java:364,382 extra bumps (reassignment, ISR shrink)

### C0620 · k-leader-epochs · L3 · p
> Now a returning follower doesn't guess. It asks the leader: "the last epoch I have is 1 — where did epoch 1 end in your log?" The leader looks in its cache: epoch 2 started at offset 6, so epoch 1 ended at 6. The follower truncates everything from offset 6 up and fetches the leader's version. Only records that genuinely diverge are removed. In the ledger analogy: each reign of a head archivist uses a different ink colour, and the register at the front of the book says on which line each colour started. Comparing registers tells you exactly where two copies part ways.
- pass 1: ✅ https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation; clients/src/main/resources/common/message/FetchResponse.json:79-80 — "Each replica keeps a vector of [LeaderEpoch => StartOffset]"
- pass 2: ✅ LeaderEpochFileCache.java:284 endOffsetFor + FetchResponse.json:80 — "largest epoch and its end offset such that subsequent records are known to diverge" (end offset = next epoch's start); analogy n/a

### C0621 · k-leader-epochs · L3 · figcaption
> Simplified: one partition, RF 3, offsets and timings illustrative. The ▼ marks the high watermark. Modern followers carry LastFetchedEpoch in their Fetch requests and get a DivergingEpoch back; older paths use a separate OffsetForLeaderEpoch request — the outcome is the same truncation point.
- pass 1: ✅ clients/src/main/resources/common/message/FetchRequest.json:101; FetchResponse.json:79-80; OffsetForLeaderEpochRequest.json:46 — "LastFetchedEpoch / DivergingEpoch (figure numbers labelled illustrative)"
- pass 2: ✅ FetchRequest.json:46-47,101 — "Version 12 adds ... epoch validation through the `LastFetchedEpoch` field"; FetchResponse.json:79 DivergingEpoch; OffsetForLeaderEpochRequest.json exists

### C0622 · k-leader-epochs · L3 · h3
> Unclean leader election: availability over truth
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0623 · k-leader-epochs · L3 · p
> If every in-sync replica is gone, Kafka faces the dilemma the design docs spell out: wait for an ISR member to come back (maybe never), or promote whatever replica is alive, even if it's missing committed records. unclean.leader.election.enable (default false) chooses between them. When it's on and an out-of-sync replica becomes leader, its log becomes the source of truth: committed records it lacked are gone, and the other replicas truncate to match when they return. Consumers that had already read past that point detect it — with no automatic reset policy the consumer throws LogTruncationException with the first diverging offset.
- pass 1: ✅ docs/design/design.md:347; storage/src/main/java/org/apache/kafka/storage/internals/log/LogConfig.java:133; clients/src/main/java/org/apache/kafka/clients/consumer/LogTruncationException.java:20-27 — "DEFAULT_UNCLEAN_LEADER_ELECTION_ENABLE = false / the consumer will detect the truncation and raise this exception"
- pass 2: ✅ docs/design/design.md:347 — "its log becomes the source of truth even though it is not guaranteed to have every committed message"; LogConfig.java:133 default false; SubscriptionState.java:594-605 throws LogTruncation when no reset policy

### C0624 · k-leader-epochs · L3 · p
> In KRaft, turning the flag on dynamically doesn't act instantly: the controller checks leaderless partitions every unclean.leader.election.interval.ms (default 5 minutes). To force it now, the config docs point to kafka-leader-election.sh with the unclean option.
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:122-130 — "wait for the unclean leader election thread to trigger election periodically (default is 5 minutes)"
- pass 2: ✅ server/.../ReplicationConfigs.java:122-130 — "wait for the unclean leader election thread to trigger election periodically (default is 5 minutes)" (note: interval.ms is defineInternal, an internal config)

### C0625 · k-leader-epochs · L3 · p
> Unclean election is data loss you asked for. Records acknowledged with acks=all can disappear, and consumers can see offsets reused for different data. Use it as a deliberate, per-topic emergency lever (e.g. a metrics topic where availability beats completeness), never as a cluster default for business data.
- pass 1: n/a (opinion / operational advice, consequence per docs/design/design.md:347)
- pass 2: ✅ ReplicationConfigs.java:127 — "even though doing so may result in data loss"; usage advice n/a

### C0626 · k-leader-epochs · L3 · h3
> ELR: electing safely outside the ISR (KIP-966)
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0627 · k-leader-epochs · L3 · p
> There's a middle ground the ISR alone can't see. With min.insync.replicas enforced, the HW can't advance while the ISR is smaller than min ISR. So a replica that was in the ISR when it dropped below min ISR still has every committed record — nothing new could be committed after it left. Such replicas are safe leaders even though they're not in the ISR any more.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:31 — "the high watermark for the data partition can't advance if the size of the ISR is smaller than the min ISR"
- pass 2: ✅ docs/operations/eligible-leader-replicas.md:31 — "high watermark ... can't advance if the size of the ISR is smaller than the min ISR"

### C0628 · k-leader-epochs · L3 · p
> Eligible Leader Replicas track exactly those. The KRaft controller stores them in the partition's metadata record (EligibleLeaderReplicas, plus LastKnownElr). When the ISR shrinks below min ISR, former ISR members move into the ELR — except replicas that had an unclean shutdown, which may have lost unflushed data. When the ISR is back at or above min ISR, the ELR is cleared. Leader election order becomes:
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:31; metadata/src/main/java/org/apache/kafka/controller/PartitionChangeBuilder.java:532-553; metadata/src/main/resources/common/metadata/PartitionRecord.json:47-52 — "If the ISR is larger or equal to the min ISR, clear the ELR / Exclude unclean shutdown replicas."
- pass 2: ✅ PartitionRecord.json:21,47-52 EligibleLeaderReplicas/LastKnownElr; PartitionChangeBuilder.java:535-554 — "If the ISR is larger or equal to the min ISR, clear the ELR"; "Exclude unclean shutdown replicas"

### C0629 · k-leader-epochs · L3 · li
> If the ISR is not empty, pick from the ISR.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:33 — "If ISR is not empty, select one of them."
- pass 2: ✅ docs/operations/eligible-leader-replicas.md:33 — "If ISR is not empty, select one of them."

### C0630 · k-leader-epochs · L3 · li
> Else, if the ELR is not empty, pick an unfenced ELR member.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:34 — "If ELR is not empty, select one that is not fenced."
- pass 2: ✅ docs/operations/eligible-leader-replicas.md:34 — "If ELR is not empty, select one that is not fenced."

### C0631 · k-leader-epochs · L3 · li
> Else, the last known leader if it's unfenced (roughly the pre-4.0 behaviour when all replicas were offline).
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:35 — "This is a similar behavior prior to the 4.0 when all the replicas are offline."
- pass 2: ✅ docs/operations/eligible-leader-replicas.md:35 — "Select the last known leader if it is unfenced. This is a similar behavior prior to the 4.0"

### C0632 · k-leader-epochs · L3 · p
> ELR arrived in 4.0 behind the eligible.leader.replicas.version feature (off by default in 4.0) and is enabled by default on new clusters since 4.1. Enabling it has side effects on min.insync.replicas: it gets a cluster-level value, the broker-level setting is removed and can't be altered, and changing it at cluster or topic level clears ELR state.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:31,41,51-56; docs/getting-started/upgrade.md:173 — "ELR is enabled by default on new clusters starting 4.1"
- pass 2: ✅ eligible-leader-replicas.md:31,41,49-56 — "ELR is enabled by default on new clusters starting 4.1"; "not enabled by default for 4.0"; broker-level removed/not alterable; updates clean ELR

### C0633 · k-leader-epochs · L3 · div
> Does ELR make unclean election unnecessary?
- pass 1: n/a (Q&A question)
- pass 2: n/a — FAQ question

### C0634 · k-leader-epochs · L3 · div
> It makes it necessary less often. ELR only helps when a safe replica exists outside the ISR. If every replica that has all committed data is permanently gone, you're back to the old dilemma: wait, or elect uncleanly.
- pass 1: n/a (reasoning derived from docs/operations/eligible-leader-replicas.md:31-35)
- pass 2: ✅ eligible-leader-replicas.md:31 (ELR = non-ISR replicas safe to lead) + design.md:347 dilemma — reasoning consistent

### C0635 · k-leader-epochs · L3 · div
> Why does it matter whether a broker shut down cleanly?
- pass 1: n/a (Q&A question)
- pass 2: n/a — FAQ question

### C0636 · k-leader-epochs · L3 · div
> Kafka doesn't fsync every write by default. A broker that crashed may have lost records that were only in the page cache, so it can't be trusted to hold all committed data — the controller filters unclean-shutdown replicas out of the ELR.
- pass 1: ✅ docs/design/design.md:335; metadata/src/main/java/org/apache/kafka/controller/PartitionChangeBuilder.java:545-553 — "we do not want to require the use of fsync on every write / filter out … unclean shutdown Replicas"
- pass 2: ✅ TopicConfig.java:51-54 flush.messages — "we recommend you not set this and use replication for durability" (default Long.MAX); PartitionChangeBuilder.java:553 filters uncleanShutdownReplicas (detected when the broker re-registers, ClusterControlManager.java:437)

### C0637 · k-leader-epochs · L3 · p
> On a local 4.3 cluster: check whether ELR is enabled, see a partition's ELR, and (in a lab only!) force an unclean election for a leaderless partition.
- pass 1: n/a (exercise prompt)
- pass 2: n/a — lab intro

### C0638 · k-leader-epochs · L3 · pre
> bin/kafka-features.sh --bootstrap-server localhost:9092 describe # eligible.leader.replicas.version bin/kafka-features.sh --bootstrap-server localhost:9092 upgrade --feature eligible.leader.replicas.version=1 bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic orders # shows Elr / LastKnownElr bin/kafka-leader-election.sh --bootstrap-server localhost:9092 \ --election-type unclean --topic orders --partition 0
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:139,157; tools/src/main/java/org/apache/kafka/tools/TopicCommand.java:358-370; tools/src/main/java/org/apache/kafka/tools/LeaderElectionCommand.java:~290-311 — "feature=level format / \tElr: / "unclean" for unclean leader election"
- pass 2: ✅ tools/.../FeatureCommand.java:139,148,157 describe/upgrade --feature feature=level; EligibleLeaderReplicasVersion.java:29; TopicCommand.java:358-366 prints Elr/LastKnownElr; LeaderElectionCommand.java --election-type unclean --topic --partition

### C0639 · k-leader-epochs · L3 · p
> The docs say ELR fields are exposed through the DescribeTopicPartitions API, which the admin client uses when describing topics.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:47 — "The ELR fields can be checked through the API DescribeTopicPartitions."
- pass 2: ✅ docs/operations/eligible-leader-replicas.md:47 — "ELR fields can be checked through the API DescribeTopicPartitions. The admin client can fetch the ELR info"

### C0640 · k-leader-epochs · L3 · summary
> L3🔬 Go deeper: where epochs live in code and protocol
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C0641 · k-leader-epochs · L3 · p
> org.apache.kafka.storage.internals.epoch.LeaderEpochFileCache — "a cache of (LeaderEpoch => Offset) mappings for a particular replica", where the offset is "the offset of the first message in each epoch". It's checkpointed by LeaderEpochCheckpointFile to leader-epoch-checkpoint.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/epoch/LeaderEpochFileCache.java (class javadoc); LeaderEpochCheckpointFile.java:45 — "Represents a cache of (LeaderEpoch => Offset) mappings for a particular replica."
- pass 2: ✅ storage/.../epoch/LeaderEpochFileCache.java:44,47 — "cache of (LeaderEpoch => Offset) mappings for a particular replica"; "offset of the first message in each epoch"

### C0642 · k-leader-epochs · L3 · p
> FetchRequest v12+ carries LastFetchedEpoch; if the leader detects divergence, the FetchResponse contains DivergingEpoch: "the largest epoch and its end offset such that subsequent records are known to diverge". DelayedFetch completes immediately in that case ("Case H") so the follower can truncate. OffsetForLeaderEpoch still exists; its CurrentLeaderEpoch fences stale callers with FENCED_LEADER_EPOCH or UNKNOWN_LEADER_EPOCH. Consumers use the same machinery to validate their position after a leader change.
- pass 1: ✅ FetchRequest.json:101; FetchResponse.json:79-80; core/src/main/scala/kafka/server/DelayedFetch.scala:65; OffsetForLeaderEpochRequest.json:46 — "Case H: A diverging epoch was found, return response to trigger truncation"
- pass 2: ✅ FetchRequest.json:101 LastFetchedEpoch 12+; FetchResponse.json:80 quote exact; core/.../DelayedFetch.scala:65 "Case H: A diverging epoch was found"; OffsetForLeaderEpochRequest.json:46 FENCED/UNKNOWN_LEADER_EPOCH; consumer OffsetsForLeaderEpochUtils.java

### C0643 · k-leader-epochs · L3 · p
> ELR logic lives in the controller's PartitionChangeBuilder: maybePopulateTargetElr() clears ELR when the target ISR ≥ min ISR, otherwise unions the current ISR into the ELR, removing the target ISR and unclean-shutdown replicas; an unclean election clears the ELR fields. PartitionRecord version 2 added the tagged fields EligibleLeaderReplicas and LastKnownElr.
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/PartitionChangeBuilder.java:506,532-553; PartitionRecord.json:21 — "Version 2 implements Eligible Leader Replicas and LastKnownElr as described in KIP-966."
- pass 2: ✅ metadata/.../PartitionChangeBuilder.java:532-554,506 — "union the current ISR and current elr, then filter out the target ISR and unclean shutdown"; PartitionRecord.json:21 version 2 tagged fields

### C0644 · k-leader-epochs · L3 · li
> Every leader election bumps the leader epoch; batches are stamped with it; replicas keep an epoch → start-offset cache.
- pass 1: ❌ fixed in part file — PartitionChangeBuilder: election sets leaderEpoch+1; LeaderEpochFileCache epoch→start offset
- pass 2: ✅ LeaderEpochFileCache.java:44-47; KIP-101 — epoch bumped on each leader change, stamped on batches

### C0645 · k-leader-epochs · L3 · li
> Returning followers truncate to where their last epoch ended on the leader — not to their stale HW (KIP-101).
- pass 1: ✅ https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation — "Followers now query leaders via OffsetForLeaderEpoch requests"
- pass 2: ✅ KIP-101 title "use Leader Epoch rather than High Watermark for Truncation"; FetchResponse.json:80 DivergingEpoch

### C0646 · k-leader-epochs · L3 · li
> Only uncommitted (never acked with acks=all) records get truncated in a clean failover.
- pass 1: ✅ docs/design/design.md:319; https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation — "if we tell the client a message is committed, and the leader fails, the new leader we elect must also have that message"
- pass 2: ✅ docs/design/design.md:191 "committed only when all replicas in the in-sync replicas" + 331 ISR election + KIP-101 — clean (ISR) failover preserves committed records; only uncommitted tail truncated

### C0647 · k-leader-epochs · L3 · li
> unclean.leader.election.enable=false by default; turning it on trades committed data for availability.
- pass 1: ✅ storage/.../LogConfig.java:133; server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:127 — "even though doing so may result in data loss"
- pass 2: ✅ LogConfig.java:133 DEFAULT_UNCLEAN_LEADER_ELECTION_ENABLE = false; design.md:347 tradeoff availability vs consistency

### C0648 · k-leader-epochs · L3 · li
> ELR (KIP-966, default on new clusters since 4.1) lets the controller elect a safe non-ISR replica when the ISR fell below min ISR.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:31 — "ELR is enabled by default on new clusters starting 4.1"
- pass 2: ✅ eligible-leader-replicas.md:31 — "ELR is enabled by default on new clusters starting 4.1"; safe non-ISR replicas when ISR < min ISR
