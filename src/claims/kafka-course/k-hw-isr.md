# Claims ledger — kafka-course.html — k-hw-isr

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0369 · k-hw-isr · L2 · p
> Part I gave you the rules: replicas, a leader, an ISR, and acks=all waits for the ISR. Now let's watch the machinery that enforces them. It runs on two numbers per replica and one simple loop.
- pass 1: n/a (intro / pedagogy)
- pass 2: n/a — pedagogical intro (recap of earlier parts)

### C0370 · k-hw-isr · L2 · p
> Followers pull. A follower is basically a consumer of its leader. It sends Fetch requests with a FetchOffset ("give me everything from offset N"), appends what it gets to its own log, and asks again. The design docs point out the bonus: pulling lets the follower batch naturally. In the archive: each branch that keeps a photocopy sends a runner to the main branch saying "I have lines up to 41, give me 42 onward".
- pass 1: ✅ docs/design/design.md:287; clients/.../message/FetchRequest.json:99 — "Followers consume messages from the leader just as a normal Kafka consumer… naturally batch"; FetchOffset
- pass 2: ✅ docs/design/design.md:287 — "Followers consume messages from the leader just as a normal Kafka consumer would… naturally batch"; FetchRequest.json:99 FetchOffset (archive analogy n/a)

### C0371 · k-hw-isr · L2 · h3
> Two numbers you must know
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0372 · k-hw-isr · L2 · li
> LEO (log end offset): the offset of the next record a replica would write. Each replica has its own.
- pass 1: ✅ storage/.../internals/log/LogSegment.java:368 — "updateMapEndOffset(batch.lastOffset() + 1)" (log end = next offset)
- pass 2: ✅ core/.../cluster/Partition.scala:1143 (LEO = leader's log end offset); design docs — log end offset = offset of next message to be appended

### C0373 · k-hw-isr · L2 · li
> HW (high watermark): the offset up to which records are committed, meaning every in-sync replica has them. On the leader, the HW is the smallest LEO among the ISR (and caught-up replicas), and it only moves while the ISR has at least min.insync.replicas members.
- pass 1: ✅ revised after pass-2 finding — HW/min-ISR (C0373, C0377, C0379, C0398, C1434, quiz C1259) — HW definition and bullet now state the HW is frozen while ISR < min.insync.replicas; consumer-visibility paragraph adds the min-ISR condition; min-ISR paragraph adds that acks=0/1 writes are accepted
- pass 2: ✅ Partition.scala:1000-1008 — "HW is determined by the smallest log end offset among all replicas that are in sync; or are considered caught-up"; "can only advance if the ISR size is ... min ISR"

### C0374 · k-hw-isr · L2 · p
> The clever part: the leader learns each follower's LEO for free, because a follower's FetchOffset is its LEO. And followers learn the HW for free, because every fetch response carries the leader's HighWatermark. There's no separate "ack" message. The fetch loop is the replication protocol.
- pass 1: ✅ FetchRequest.json:99; FetchResponse.json:73 — FetchOffset; HighWatermark per partition response
- pass 2: ✅ FetchRequest.json:99 "FetchOffset"; FetchResponse.json:73 "HighWatermark" versions 0+; Partition.maybeIncrementLeaderHW triggered by replica LEO change (Partition.scala:986-988)

### C0375 · k-hw-isr · L2 · figcaption
> Simplified: one fetch round per step, 1 record = 1 offset, no leader epochs. In reality followers fetch continuously (a follower fetch waits up to replica.fetch.wait.max.ms, default 500 ms, when there's no new data), and ISR removal happens after replica.lag.time.max.ms (default 30 s). The seconds shown are illustrative.
- pass 1: n/a (figcaption labelled illustrative; defaults ✅ server/.../ReplicationConfigs.java:55,75)
- pass 2: ✅ server/.../ReplicationConfigs.java:75 — "REPLICA_FETCH_WAIT_MAX_MS_DEFAULT = 500"; :55 "REPLICA_LAG_TIME_MAX_MS_DEFAULT = 30000L"

### C0376 · k-hw-isr · L2 · h3
> What the high watermark controls
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0377 · k-hw-isr · L2 · p
> Consumers only see committed records. A fetch from a normal consumer returns data only below the HW. Records above it exist on the leader but might vanish if the leader dies before followers copy them, so Kafka won't hand them out. The docs put it plainly: "only committed messages are ever given out to the consumer", regardless of the producer's acks. Records become visible only once they're on all in-sync replicas and the ISR has at least min.insync.replicas members.
- pass 1: ✅ revised after pass-2 finding — HW/min-ISR (C0373, C0377, C0379, C0398, C1434, quiz C1259) — HW definition and bullet now state the HW is frozen while ISR < min.insync.replicas; consumer-visibility paragraph adds the min-ISR condition; min-ISR paragraph adds that acks=0/1 writes are accepted
- pass 2: ✅ docs/design/design.md:302-305 — "Only committed messages are ever given out to the consumer"; "Regardless of the acks setting ... not be visible ... until" both conditions

### C0378 · k-hw-isr · L2 · p
> acks=all waits for the HW. The leader appends the batch, then parks the produce request until the HW has passed the batch's last offset, i.e. until every ISR member has fetched it. Only then does the producer get its acknowledgement. acks=1 answers right after the leader's local append; acks=0 doesn't wait at all.
- pass 1: ✅ design.md:191,302; server/.../purgatory/DelayedProduce.java:40 — "committed only when all replicas in the in-sync replicas… have applied it"
- pass 2: ✅ ReplicaManager.scala:839 — "lastOffset + 1, // required offset"; Partition.scala:971 — "if (leaderLog.highWatermark >= requiredOffset)" (DelayedProduce); acks=1/0 per producer acks doc

### C0379 · k-hw-isr · L2 · p
> min.insync.replicas guards the door. With acks=all, if the ISR is smaller than min.insync.replicas, the leader rejects the write with NOT_ENOUGH_REPLICAS. The partition-level code also only advances the HW when the ISR is at least min.insync.replicas. Without this, "acks=all" would degrade silently to "acks=leader-only" when followers drop out. Writes with acks=0 or 1 are still accepted below the minimum, but they stay invisible to consumers until the ISR is back up to it.
- pass 1: ✅ revised after pass-2 finding — HW/min-ISR (C0373, C0377, C0379, C0398, C1434, quiz C1259) — HW definition and bullet now state the HW is frozen while ISR < min.insync.replicas; consumer-visibility paragraph adds the min-ISR condition; min-ISR paragraph adds that acks=0/1 writes are accepted
- pass 2: ✅ Partition.scala:1233-1236,1011 — "inSyncSize < minIsr && requiredAcks == -1 ... NotEnoughReplicasException"; HW not increased under min ISR

### C0380 · k-hw-isr · L2 · h3
> ISR shrink and expand
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0381 · k-hw-isr · L2 · p
> The leader periodically checks each follower. A follower that hasn't caught up to the leader's LEO within replica.lag.time.max.ms (default 30 000 ms) is out of sync. This covers both a stuck follower (no fetches at all) and a slow one (fetching, but never reaching the end). The leader asks the controller to shrink the ISR (an AlterPartition request; the change becomes a metadata record). Once the ISR is smaller, the HW can move again, and acks=all producers unblock.
- pass 1: ✅ ReplicationConfigs.java:54-57; Partition.scala:1142-1152; core/.../ReplicaManager.scala:284; AlterPartitionRequest.json:17 — "hasn't consumed up to the leader's log end offset for at least this time"
- pass 2: ✅ ReplicationConfigs.java:56 — "If a follower hasn't sent any fetch requests or hasn't consumed up to the leader's log end offset for at least this time"; Partition.scala:1143-1150 stuck/slow followers; shrink submitted via AlterPartition

### C0382 · k-hw-isr · L2 · p
> When the lagging follower catches up (its LEO reaches the HW within the current leader epoch), the leader asks to expand the ISR again. Note the time-based definition: Kafka doesn't count "messages behind". A follower 10 000 records behind that's catching up fast stays in; one that's only 5 behind but hasn't reached the end for 30 s goes out.
- pass 1: ✅ core/.../cluster/Partition.scala:862-873 — "added to ISR if its LEO >= current hw… caught up to an offset within the current leader epoch"
- pass 2: ✅ Partition.scala:864 — "added to ISR if its LEO >= current hw… caught up to an offset within the current leader epoch"; lag rule is time-based (lastCaughtUpTimeMs, Partition.scala:1149)

### C0383 · k-hw-isr · L2 · p
> RF = 3 with min.insync.replicas=1 + acks=all is a trap. When two followers fall out of the ISR, the ISR is just the leader, and acks=all means "the leader has it". Kafka's own docs describe this: with only one in-sync replica left, acks=all writes succeed, but they're lost if that replica then fails. The common production recipe is RF = 3, min.insync.replicas=2, acks=all: one broker can fail with no data loss and no write outage.
- pass 1: ✅ docs/design/design.md:353 — "only one in sync replica remains… acks=all will succeed. However, these writes could be lost" (recipe = advice)
- pass 2: ✅ docs/design/design.md:353 — "only one in sync replica remains… acks=all will succeed. However, these writes could be lost"; TopicConfig.java:185 — "replication factor of 3, set min.insync.replicas to 2"

### C0384 · k-hw-isr · L2 · p
> Diagnosing "producers are timing out, consumers are stuck".
- pass 1: n/a (use-case title)
- pass 2: n/a — heading/lead-in

### C0385 · k-hw-isr · L2 · li
> List partitions whose ISR is below min ISR: bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --under-min-isr-partitions. Those reject acks=all writes.
- pass 1: ✅ tools/.../TopicCommand.java "under-min-isr-partitions"; Partition.scala:1234 — acks=-1 rejected below min ISR
- pass 2: ✅ tools/.../TopicCommand.java:778 — "under-min-isr-partitions… only show partitions whose isr count is less than the configured minimum"

### C0386 · k-hw-isr · L2 · li
> List partitions whose ISR is smaller than their assigned replica set: --describe --under-replicated-partitions. The HW waits on the slowest in-sync follower until it's dropped.
- pass 1: ✅ revised after pass-2 finding — C0386 — "partitions with missing replicas" → "partitions whose ISR is smaller than their assigned replica set" — tools/src/main/java/org/apache/kafka/tools/TopicCommand.java:319 "getReplicationFactor(info, reassignment) - info.isr().size() > 0"
- pass 2: ✅ tools/.../TopicCommand.java:318-319,774 — "getReplicationFactor(info, reassignment) - info.isr().size() > 0"; "only show under-replicated partitions"

### C0387 · k-hw-isr · L2 · li
> Look at which broker is common to those partitions. Check its disk, network and GC. A slow follower drags acks=all latency until replica.lag.time.max.ms expels it.
- pass 1: n/a (diagnostic advice; lag expulsion ✅ ReplicationConfigs.java:56-57)
- pass 2: n/a — operational advice; mechanism ✅ (slow in-sync follower holds HW until replica.lag.time.max.ms, Partition.scala:1143)

### C0388 · k-hw-isr · L2 · li
> Only then consider tuning num.replica.fetchers (default 1) to add parallel fetcher threads.
- pass 1: ✅ server/.../ReplicationConfigs.java:96-99 — "NUM_REPLICA_FETCHERS_DEFAULT = 1"; "increase the degree of I/O parallelism"
- pass 2: ✅ ReplicationConfigs.java:96 — "NUM_REPLICA_FETCHERS_DEFAULT = 1"; doc "Number of fetcher threads used to replicate records from each source broker"

### C0389 · k-hw-isr · L2 · tr
> NotEnoughReplicasException on produce | ISR size < min.insync.replicas with acks=all | Bring the failed/lagging brokers back; don't "fix" it by lowering min ISR in production
- pass 1: ✅ Partition.scala:1234-1236; Errors.java:220 — "fewer in-sync replicas than required" (fix = advice)
- pass 2: ✅ Partition.scala:1233 NotEnoughReplicasException when ISR < min.isr with acks=-1; remedy is advice (n/a)

### C0390 · k-hw-isr · L2 · tr
> Produce latency spikes with acks=all, then recovers | A slow follower still in the ISR holds the HW back until it's removed | Find the slow broker (disk/GC/network); consider replica fetcher tuning
- pass 1: ✅ Partition.scala:992; ReplicationConfigs.java:56-57 — HW waits for in-sync followers until removed (fix = advice)
- pass 2: ✅ Partition.scala:992-1029 HW = min LEO over (maximal) ISR, so a slow in-sync follower holds it; remedy advice n/a

### C0391 · k-hw-isr · L2 · tr
> ISR flaps (shrink/expand repeatedly) | Follower hovers around the lag limit | Fix the follower's resources; raising replica.lag.time.max.ms only hides it
- pass 1: n/a (troubleshooting heuristic; lag rule ✅ ReplicationConfigs.java:56-57)
- pass 2: ✅ mechanism (time-based shrink Partition.scala:1143 / expand :864); remedy is opinion (n/a)

### C0392 · k-hw-isr · L2 · tr
> Consumer lag at 0 but producer (acks=0/1, or a send() not yet acknowledged) says "sent" | Records above HW aren't visible yet | Expected: they appear once followers replicate
- pass 1: ✅ revised after pass-2 finding — C0392 — symptom row now "producer (acks=0/1, or a send() not yet acknowledged) says 'sent'" — docs/design/design.md:302 "Regardless of the `acks` setting, the messages will not be visible to the consumers until…"
- pass 2: ✅ design.md:302-305 + Partition.scala:1011 — records above HW not visible to consumers; lag computed against HW (row is troubleshooting advice)

### C0393 · k-hw-isr · L3 · summary
> L3🔬 Go deeper: Partition.maybeIncrementLeaderHW and friends
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0394 · k-hw-isr · L3 · p
> On the leader, kafka.cluster.Partition owns the ISR logic. maybeIncrementLeaderHW runs when the ISR changes or any replica's LEO changes. Its doc comment says the HW is "the smallest log end offset among all replicas that are in sync; or are considered caught-up and are allowed to join the ISR", and that "the HW can only advance if the ISR size is equal or larger than the min ISR". Replicas added by an in-flight AlterPartition already count, which the code calls the "maximal ISR" (KIP-497): being more restrictive is always safe.
- pass 1: ✅ core/.../cluster/Partition.scala:986-1010 — "HW can only advance if the ISR size is equal or large than the min ISR"; "maximal ISR. See KIP-497"
- pass 2: ✅ Partition.scala:984-1004 — "triggered when 1. Partition ISR changed 2. Any replica's LEO changed"; "We call this set the 'maximal' ISR. See KIP-497"

### C0395 · k-hw-isr · L3 · p
> getOutOfSyncReplicas(maxLagMs) uses each replica's lastCaughtUpTimeMs, the last time it was fully caught up, so stuck and slow followers are handled by one rule. maybeExpandIsr requires the follower's LEO ≥ HW and being caught up within the current leader epoch. Otherwise a follower could become leader before fetching committed data between the HW and LEO.
- pass 1: ✅ Partition.scala:1142-1152,862-873 — "checking the lastCaughtUpTimeMs"; "caught up to an offset within the current leader epoch"
- pass 2: ✅ Partition.scala:1149 — "lastCaughtUpTimeMs which represents the last time when the replica was fully caught up"; :864-868 leader-epoch requirement quoted reason

### C0396 · k-hw-isr · L3 · p
> Followers run ReplicaFetcherThreads (num.replica.fetchers per source broker). Waiting acks=all produces sit in a purgatory as DelayedProduce operations inside ReplicaManager until the HW passes them or the request times out. Part III covers the purgatory and the leader-epoch-based truncation that makes failover safe.
- pass 1: ✅ core/.../ReplicaFetcherThread.scala; ReplicationConfigs.java:97; server/.../purgatory/DelayedProduce.java:40 — "fetcher threads used to replicate records from each source broker"
- pass 2: ✅ core/src/main/scala/kafka/server/ReplicaFetcherThread.scala; server/.../purgatory/DelayedProduce.java; ReplicaManager.scala:839-897 delayed produce until checkEnoughReplicasReachOffset or timeout (Part III reference n/a)

### C0397 · k-hw-isr · L2 · li
> Followers pull with Fetch. Their FetchOffset tells the leader their LEO, and the response tells them the HW.
- pass 1: ✅ FetchRequest.json:99; FetchResponse.json:73 — FetchOffset / HighWatermark
- pass 2: ✅ FetchRequest.json:99 FetchOffset; FetchResponse.json:73 HighWatermark

### C0398 · k-hw-isr · L2 · li
> HW = smallest LEO in the ISR, frozen while the ISR is below min.insync.replicas. Consumers read only below it, and acks=all is acknowledged once the HW passes the batch.
- pass 1: ✅ revised after pass-2 finding — HW/min-ISR (C0373, C0377, C0379, C0398, C1434, quiz C1259) — HW definition and bullet now state the HW is frozen while ISR < min.insync.replicas; consumer-visibility paragraph adds the min-ISR condition; min-ISR paragraph adds that acks=0/1 writes are accepted
- pass 2: ✅ Partition.scala:969-975,1011 — ack when "highWatermark >= requiredOffset"; HW frozen under min ISR

### C0399 · k-hw-isr · L2 · li
> A follower that hasn't caught up for replica.lag.time.max.ms (30 s) leaves the ISR, which unblocks the HW.
- pass 1: ✅ ReplicationConfigs.java:55-57 — "30000L… the leader will remove the follower from ISR"
- pass 2: ✅ ReplicationConfigs.java:55 default 30000; Partition.scala:1143-1150

### C0400 · k-hw-isr · L2 · li
> min.insync.replicas rejects acks=all writes when the ISR is too small. The usual recipe is RF 3 / min ISR 2.
- pass 1: ✅ Partition.scala:1234; design.md:356 — "only accept writes if the size of the ISR is above a certain minimum" (recipe = advice)
- pass 2: ✅ Partition.scala:1233; TopicConfig.java:185 — "replication factor of 3, set min.insync.replicas to 2"
