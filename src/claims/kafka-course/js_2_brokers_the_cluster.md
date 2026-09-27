# Claims ledger — kafka-course.html — js_2_brokers_the_cluster

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1336 · js:2. brokers & the cluster · L- · script
> Topic orders: 3 partitions, replication factor 3, each partition led by a different broker. The producer knows only bootstrap.servers.
- pass 1: n/a (scenario setup)
- pass 2: n/a — illustrative setup

### C1337 · js:2. brokers & the cluster · L- · script
> Step 1: metadata. The producer asks B… (from bootstrap.servers). Any broker can answer: it keeps a copy of the cluster metadata.
- pass 1: ✅ docs/design/design.md:125 — "all Kafka nodes can answer a request for metadata"
- pass 2: ✅ docs/design/design.md:125 — "all Kafka nodes can answer a request for metadata"

### C1338 · js:2. brokers & the cluster · L- · script
> Step 2: produce. The producer sends each partition's batch straight to that partition's leader. No router in between.
- pass 1: ✅ docs/design/design.md:125 — "directly to the broker that is the leader"
- pass 2: ✅ docs/design/design.md:125 — "without any intervening routing tier"

### C1339 · js:2. brokers & the cluster · L- · script
> Done: three leaders, three direct connections. Load spreads across the cluster because leadership is spread across brokers.
- pass 1: ✅ docs/design/design.md:285,362 — leaders evenly distributed
- pass 2: ✅ docs/operations/basic-kafka-operations.md:108 — preferred replicas spread leadership

### C1340 · js:2. brokers & the cluster · L- · script
> The producer already has metadata. Now broker B2 (leader of orders-1) crashes…
- pass 1: n/a (scenario narration)
- pass 2: n/a — narration

### C1341 · js:2. brokers & the cluster · L- · script
> Send to B2 fails: connection lost. The client treats a disconnect as possibly stale metadata and requests a refresh. Retries keep failing until the controller acts.
- pass 1: ✅ clients/.../NetworkClient.java:1265 — "The disconnect may be the result of stale metadata, so request an update"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/NetworkClient.java:1264-1265 — "The disconnect may be the result of stale metadata, so request an update"

### C1342 · js:2. brokers & the cluster · L- · script
> Controller: no heartbeat from B2 within broker.session.timeout.ms (9 s by default), so B2 is fenced. It elects a new leader for orders-1 from the in-sync replicas: B3.
- pass 1: ✅ docs/design/design.md:296,364 + KRaftConfigs.java:44 + metadata/.../QuorumController.java:441 (fenceStaleBroker) — heartbeat timeout; "electing one of the remaining members of the ISR"
- pass 2: ✅ docs/design/design.md:296 — heartbeat timeout → "node is considered offline"; KRaftConfigs.java:44 9000 ms; new leader from ISR

### C1343 · js:2. brokers & the cluster · L- · script
> Recovered. Fresh metadata says orders-1 → B3, and the retry succeeds. Your code saw nothing, just a latency spike. If B2 had stayed alive but lost leadership, it would have answered NOT_LEADER_OR_FOLLOWER, which triggers the same refresh.
- pass 1: ✅ Errors.java:192 + Sender.java:713-730 — NOT_LEADER_OR_FOLLOWER → "request metadata update"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/internals/Sender.java:713-730 — NOT_LEADER_OR_FOLLOWER → metadata.requestUpdate, batch retried
