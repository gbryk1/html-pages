# Claims ledger — kafka-course.html — js_9_kraft

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1404 · js:9. kraft · L- · script
> Three controllers (C1 active) and three brokers, all at metadata offset 4. Press ➕.
- pass 1: n/a (figure initial state)
- pass 2: n/a — simulation setup

### C1405 · js:9. kraft · L- · script
> No active controller. Only … of 3 voters are alive, so no majority can elect a leader or commit anything. Metadata changes (like creating topics) are blocked until a majority is back.
- pass 1: ✅ kraft.md:47 — "A majority of the controllers must be alive in order to maintain availability"
- pass 2: ✅ kraft.md:47 — "A majority of the controllers must be alive in order to maintain availability"

### C1406 · js:9. kraft · L- · script
> 1 · Admin → broker → active controller. CreateTopics("…") is forwarded to …, the only node allowed to write metadata.
- pass 1: ✅ core/.../KafkaApis.scala:185 — "case ApiKeys.CREATE_TOPICS => forwardToController(request)"
- pass 2: ✅ mechanism: QuorumController is the only writer (QuorumController.java:158-161 standbys "just replay the metadata log entries that the current active controller has created"); brokers forward CreateTopics

### C1407 · js:9. kraft · L- · script
> 2 · … appends TopicRecord + PartitionRecord (offsets …–…, yellow = not committed yet). Nobody acts on them until a majority of voters has them.
- pass 1: ✅ metadata/*.json TopicRecord/PartitionRecord; QuorumController.java:166-169
- pass 2: ✅ mechanism: MetadataLoader applies committed batches only (MetadataLoader.java:63); QuorumController completes after durable; offsets illustrative

### C1408 · js:9. kraft · L- · script
> 3 · … fetches from …. Now 2 of 3 voters hold the records, a majority, so the high watermark moves to …: committed (green). … answers the admin client.
- pass 1: ✅ QuorumController.java:166-169 — "will not be completed until the results… made durable to the metadata log"
- pass 2: ✅ mechanism: majority → high watermark → QuorumController.java:998 deferredEventQueue.completeUpTo(...)

### C1409 · js:9. kraft · L- · script
> 4 · … catches up on its next fetch. It wasn't needed for the commit; a slow voter doesn't block the quorum.
- pass 1: n/a (illustration of majority commit; kraft.md:47)
- pass 2: ✅ mechanism: majority commit, third voter not required

### C1410 · js:9. kraft · L- · script
> 5 · Brokers (observers) fetch the committed records and apply them to their local metadata image: every broker now knows "…" exists and who leads partition 0. No broadcast, just everybody reading the same minutes.
- pass 1: ✅ kraft.md:240 observers; core/.../BrokerMetadataPublisher.scala:148 applyDelta
- pass 2: ✅ KafkaRaftClient.java:134 observers fetch; MetadataLoader builds images for publishers

### C1411 · js:9. kraft · L- · script
> There is no active controller to kill. Press ↺ Reset.
- pass 1: n/a (UI message)
- pass 2: n/a — UI hint

### C1412 · js:9. kraft · L- · script
> 💥 … dies. Brokers and voters keep sending Fetch to it and get nothing back.
- pass 1: n/a (break-it scenario)
- pass 2: n/a — simulation narration

### C1413 · js:9. kraft · L- · script
> No majority left. … of 3 voters alive: an election needs 2 votes, so no leader can be elected and the metadata log is frozen until another controller returns. That's why production runs 3 or 5 controllers.
- pass 1: ✅ kraft.md:47,289 — majority must be alive; 2N + 1
- pass 2: ✅ kraft.md:47,289 (majority needed; 2N+1)

### C1414 · js:9. kraft · L- · script
> Fetch timeout. After controller.quorum.fetch.timeout.ms (2 000 ms default) without a successful fetch, … becomes prospective and sends a pre-vote (Vote with PreVote=true) carrying its last offset (…). A majority grants it, so it becomes a candidate and asks for real votes.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ raft/.../KafkaRaftClient.java:3326-3328 — "Transitioning to Prospective state due to fetch timeout"; VoteRequest.json:51 "PreVote ... (not persisted)"; QuorumConfig.java:83 default 2_000

### C1415 · js:9. kraft · L- · script
> … wins with 2 of 3 votes (its log is at least as complete as the voter's), becomes leader for epoch … and sends BeginQuorumEpoch so everyone knows whom to fetch from.
- pass 1: ✅ KafkaRaftClient.java:139-150 — electors compare last offset; BeginQuorumEpoch to "find the new leader"
- pass 2: ✅ KafkaRaftClient.java:140-151 — Vote "includes the last offset in the log which electors use"; BeginQuorumEpoch "so there must be a way to find the new leader"

### C1416 · js:9. kraft · L- · script
> Failover done. Everything committed before the crash is still there (committed = on a majority, and the winner had it). Brokers now fetch from …. Press ➕ to create another topic through the new leader, or 💥 again to lose the majority.
- pass 1: n/a (pedagogical summary of Raft safety; KafkaRaftClient.java:128-131)
- pass 2: ✅ mechanism: committed = majority; winner's log at least as complete (Raft vote rule, KafkaRaftClient.java:140-142)
