# Claims ledger — kafka-course.html — js_23_mirrormaker_2

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1587 · js:23. mirrormaker 2 · L- · script
> us-west (source)orders group billing committed: … MirrorMaker 2 us-east (target)us-west.orders group billing: no offsets yet
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (figure labels; remote topic name us-west.orders per DefaultReplicationPolicy, geo-replication doc :269)

### C1588 · js:23. mirrormaker 2 · L- · script
> us-west holds orders at offsets …–… (older records already expired). Group billing has processed up to …. Press ▶.
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative offsets)

### C1589 · js:23. mirrormaker 2 · L- · script
> copied … records; target offsets start at 0
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: ✅ mechanism: remote topic is a new log on the target, offsets assigned by target (OffsetSyncStore.java:141-144); numbers illustrative

### C1590 · js:23. mirrormaker 2 · L- · script
> checkpoint for billing: … (in us-west.checkpoints.internal)
- pass 1: ✅ connect/mirror-client/src/main/java/org/apache/kafka/connect/mirror/ReplicationPolicy.java:76 — "clusterAlias + ".checkpoints.internal""
- pass 2: ✅ DefaultReplicationPolicy.java:98 checkpoints topic = <source alias>.checkpoints.internal (on target)

### C1591 · js:23. mirrormaker 2 · L- · script
> MirrorSourceConnector consumes from us-west and produces to us-west.orders. The target assigns its own offsets, starting at 0.
- pass 1: ✅ docs/operations/geo-replication-(cross-cluster-data-mirroring).md:58 — "MirrorMaker uses Connectors to consume from source clusters and produce to target clusters" (offset 0 start illustrative)
- pass 2: ✅ MirrorSourceConnector.java:80 "Replicate data ... between clusters"; remote name {source}.{topic} (geo-replication doc :269)

### C1592 · js:23. mirrormaker 2 · L- · script
> Checkpoint written. The nearest sync at or below … is 1005→5. The group is past it, so the translation is 5 + 1 = …. Conservative on purpose: never guess ahead and skip data.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:153 — "long upstreamStep = upstreamOffset == offsetSync.get().upstreamOffset() ? 0 : 1" (sync points illustrative)
- pass 2: ✅ OffsetSyncStore.java:141-159 — group ahead of sync → "downstreamOffset() + upstreamStep" (step 1); "If we overestimate, then we may skip the correct offset"

### C1593 · js:23. mirrormaker 2 · L- · script
> 🌪 us-west is gone. billing starts on us-east from the translated offset ……
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative narration)

### C1594 · js:23. mirrormaker 2 · L- · script
> group billing on us-east: consumed …–…
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative narration)

### C1595 · js:23. mirrormaker 2 · L- · script
> Failover done: … record re-read (offset … = source …), none skipped. At-least-once, so the handler must be idempotent.
- pass 1: ✅ connect/mirror/src/main/java/org/apache/kafka/connect/mirror/OffsetSyncStore.java:146 — "This may cause re-reading of records"
- pass 2: ✅ OffsetSyncStore.java:146 — "This may cause re-reading of records"; at-least-once → idempotent handler

### C1596 · js:23. mirrormaker 2 · L- · script
> 💥 Someone copies the raw committed offset … to us-east…
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative narration)

### C1597 · js:23. mirrormaker 2 · L- · script
> group billing: …, but the log ends at …!
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative narration)

### C1598 · js:23. mirrormaker 2 · L- · script
> Offset … doesn’t exist in us-west.orders (0–…), so auto.offset.reset kicks in. Default latest → jump to ……
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:172 — "current offset does not exist any more on the server" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:546 — "AutoOffsetResetStrategy.LATEST.name()"
- pass 2: ✅ ConsumerConfig.java:172-175,544-546 — offset not on server → auto.offset.reset, default LATEST

### C1599 · js:23. mirrormaker 2 · L- · script
> Records 7–9 (source …–…) were never processed: silent data loss. With earliest you’d instead re-process everything from 0. Offsets are positions in one log; translate them.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:174 — "earliest: automatically reset the offset to the earliest offset" (record numbers illustrative)
- pass 2: ✅ mechanism: latest skips unprocessed records, earliest re-reads from start (ConsumerConfig.java:174-175); numbers illustrative
