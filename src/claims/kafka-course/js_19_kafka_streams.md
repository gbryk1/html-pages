# Claims ledger — kafka-course.html — js_19_kafka_streams

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1527 · js:19. kafka streams · L- · script
> Four tasks (0_0…0_3) of one stateful sub-topology on three instances. Pick a failure.
- pass 1: n/a (UI hint)
- pass 2: n/a (figure setup; naming 0_0…0_3 matches TaskId.java:76)

### C1528 · js:19. kafka streams · L- · script
> num.standby.replicas = 1: every task has a shadow copy of its store on another instance, kept up to date from the changelog.
- pass 1: ✅ docs/streams/architecture.md:90 — "standby replicas of local states (i.e. fully replicated copies of the state)"
- pass 2: ✅ architecture.md:90 standby replicas = fully replicated copies of local state, kept from changelog

### C1529 · js:19. kafka streams · L- · script
> num.standby.replicas = 0 (the default): each store exists only on the instance running the task — and in its changelog topic.
- pass 1: ✅ docs/streams/architecture.md:88; streams/src/main/java/org/apache/kafka/streams/StreamsConfig.java:896-898 — "its own dedicated changelog topic partition"
- pass 2: ✅ StreamsConfig.java:898 default 0; architecture.md:88 state backed by changelog topic

### C1530 · js:19. kafka streams · L- · script
> I1 dies. Tasks 0_0 and 0_1 stop. After the session timeout the group reassigns them.
- pass 1: ✅ docs/streams/architecture.md:86 — "Kafka Streams automatically restarts the task in one of the remaining running instances"
- pass 2: ✅ mechanism: failed member detected after session timeout, tasks restarted on other instances (architecture.md:49 — "all its assigned tasks will be automatically restarted on other instances")

### C1531 · js:19. kafka streams · L- · script
> Reassigned to the instances holding standbys. They promote the standby and replay only the small tail they had not yet applied (~… records, illustrative).
- pass 1: ✅ docs/streams/architecture.md:90 — "assign a task to an application instance where such a standby replica already exists"
- pass 2: ✅ architecture.md:90 — "assign a task to an application instance where such a standby replica already exists" (record count illustrative)

### C1532 · js:19. kafka streams · L- · script
> Reassigned to I2 and I3 with empty stores. Each must replay its whole changelog partition (~… records, illustrative) before processing a single new input record.
- pass 1: ✅ docs/streams/architecture.md:88 — "replaying the corresponding changelog topics prior to resuming the processing"
- pass 2: ✅ architecture.md:88 — "restore ... by replaying the corresponding changelog topics prior to resuming the processing" (count illustrative)

### C1533 · js:19. kafka streams · L- · script
> Back in business almost immediately. The standby copies turned a full restore into a short catch-up. (The assignor will later place new standbys to restore redundancy.)
- pass 1: ✅ docs/streams/architecture.md:90; StreamsConfig.java:896-898 — "To minimize this restoration time … standby replicas"
- pass 2: ✅ mechanism consistent with architecture.md:90; re-placing standbys on next assignment is standard assignor behaviour

### C1534 · js:19. kafka streams · L- · script
> Finally caught up. Correct — the compacted changelog rebuilt the exact state — but slow: that restore window was downtime for 0_0 and 0_1. Standby replicas exist to shrink it.
- pass 1: ✅ docs/streams/architecture.md:88-90 — "restore their associated state stores to the content before the failure"
- pass 2: ✅ architecture.md:88-90 compacted changelog restore; standbys minimize re-init cost
