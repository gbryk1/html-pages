# Claims ledger — kafka-course.html — js_11_group_coordination

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1428 · js:11. group coordination · L- · script
> Six partitions p0–p5, C1 owns p0–p2, C2 owns p3–p5, in all three lanes. Press ➕.
- pass 1: n/a (figure initial state)
- pass 2: n/a — illustrative initial state

### C1429 · js:11. group coordination · L- · script
> JoinGroup barrier: everyone revoked ALL partitions and rejoins.
- pass 1: ✅ ConsumerPartitionAssignor.java:250-252
- pass 2: ✅ eager protocol: members revoke all partitions before JoinGroup (default EAGER protocol of Range/RoundRobin/Sticky)

### C1430 · js:11. group coordination · L- · script
> Rebalance #1 (JoinGroup/SyncGroup): owners keep their partitions; assignor withholds p2 and p5.
- pass 1: ✅ ConsumerPartitionAssignor.java:254-258
- pass 2: ✅ cooperative (KIP-429): owners keep partitions; assignor withholds those that must move

### C1431 · js:11. group coordination · L- · script
> C3 heartbeats → new group epoch; coordinator computes the target and tells C1/C2 to revoke p2/p5.
- pass 1: ✅ MemberState.java:34-38 — revoke before moving to the next epoch
- pass 2: ✅ new member heartbeat bumps group epoch; TargetAssignmentBuilder computes target; members told to revoke (MemberState UNREVOKED_PARTITIONS)

### C1432 · js:11. group coordination · L- · script
> C3 arrives. Eager: the whole group stops. Cooperative: only partitions that must move are revoked. KIP-848: same idea, but driven by heartbeats and the server-side assignor.
- pass 1: ✅ ConsumerPartitionAssignor.java:250-258; consumer-rebalance-protocol.md:32
- pass 2: ✅ mechanism summary (see C0410/C0411/C0417)

### C1433 · js:11. group coordination · L- · script
> Waiting for all members to finish poll() and rejoin; leader runs RangeAssignor; SyncGroup.
- pass 1: ✅ ConsumerPartitionAssignor.java:54,75; ClassicGroupState.java:55
- pass 2: ✅ classic barrier: coordinator waits for members to rejoin, leader runs client-side assignor then SyncGroup (ClassicGroupState.java:55)

### C1434 · js:11. group coordination · L- · script
> C1 and C2 revoke p2/p5 and must rejoin: rebalance #2 is needed to hand them out.
- pass 1: ✅ ConsumerPartitionAssignor.java:255-257 — "reassigned to other consumers in the next rebalance event"
- pass 2: ✅ cooperative: revoked partitions need a follow-up rebalance to be assigned (KIP-429)

### C1435 · js:11. group coordination · L- · script
> C1 confirmed p2 released in its heartbeat → C3 gets p2 right away. C2 is still revoking p5.
- pass 1: ✅ MemberState.java:40-45 — UNRELEASED_PARTITIONS until previous owners revoke
- pass 2: ✅ MemberState.java:41-45 UNRELEASED_PARTITIONS — new owner gets partition once previous owner releases it, per-member

### C1436 · js:11. group coordination · L- · script
> Next round. Eager is still waiting at the barrier; cooperative needs a second rebalance; KIP-848 reconciles each member on its own heartbeat.
- pass 1: ✅ consumer-rebalance-protocol.md:32
- pass 2: ✅ mechanism summary

### C1437 · js:11. group coordination · L- · script
> SyncGroup done: new ranges. p2, p4 and p5 moved; ALL six partitions were paused.
- pass 1: ✅ revised after pass-2 finding — C1437/C1444 — eager lane note "p2, p3 moved" → "p2, p4 and p5 moved" (painted end state C1{p0,p1} C2{p2,p3} C3{p4,p5} unchanged, already Range-correct) — clients/.../consumer/RangeAssignor.java:43 "lay out the available partitions in numeric order"
- pass 2: ✅ src/kafka-course.html:4730,4768 (eager lane, RangeAssignor) — before {C1:[0,1,2],C2:[3,4,5]} → after {C1:[0,1],C2:[2,3],C3:[4,5]}: p2,p4,p5 move; eager revokes all

### C1438 · js:11. group coordination · L- · script
> Rebalance #2 assigns p2 and p5 to C3. Only 2 partitions were ever paused.
- pass 1: n/a (figure outcome, labelled)
- pass 2: ✅ cooperative second rebalance assigns withheld partitions; only moved ones paused (illustrative partition choice n/a)

### C1439 · js:11. group coordination · L- · script
> C2 releases p5 → C3 gets it. No global barrier, no second full rebalance.
- pass 1: ✅ consumer-rebalance-protocol.md:32 — no global synchronization barrier
- pass 2: ✅ per-member reconciliation via heartbeat, no barrier (consumer-rebalance-protocol.md)

### C1440 · js:11. group coordination · L- · script
> Done. Compare the "partition-steps paused" counters: eager stops the world, cooperative and KIP-848 only pause what moves. KIP-848 also skips the second JoinGroup/SyncGroup round and the slow-member barrier. (Heuristic timing, illustrative.)
- pass 1: n/a (heuristic comparison, labelled)
- pass 2: ✅ mechanism; timing labelled heuristic (n/a)

### C1441 · js:11. group coordination · L- · script
> C2 stopped heartbeating, but the coordinator doesn't know yet.
- pass 1: n/a (scenario narration)
- pass 2: ✅ failure detected only after session timeout

### C1442 · js:11. group coordination · L- · script
> 💥 C2 dies without leaving the group. Its partitions p3–p5 are still assigned to it, and nobody is reading them.
- pass 1: ✅ CommonClientConfigs.java:208-211 — removed only after the session timeout
- pass 2: ✅ partitions remain assigned until coordinator expires the member

### C1443 · js:11. group coordination · L- · script
> Waiting for the session timeout (45 s default): p3–p5 are unread, lag grows.
- pass 1: ✅ ConsumerConfig.java:436-438 — 45000
- pass 2: ✅ session.timeout.ms default 45000 (ConsumerConfig.java:436-438)

### C1444 · js:11. group coordination · L- · script
> Tick, tock… Until the session timeout expires (session.timeout.ms classic / group.consumer.session.timeout.ms KIP-848, both 45 s by default), the coordinator assumes C2 is alive.
- pass 1: ✅ ConsumerConfig.java:438; GroupCoordinatorConfig.java:184
- pass 2: ✅ ConsumerConfig.java:438 45000; GroupCoordinatorConfig.java:184 45000

### C1445 · js:11. group coordination · L- · script
> C2 removed → rebalance: C1 revokes everything and rejoins.
- pass 1: ✅ ConsumerPartitionAssignor.java:250-252 — eager revokes all
- pass 2: ✅ eager: member removal → PreparingRebalance, remaining members revoke all and rejoin (ClassicGroupState.java:84)

### C1446 · js:11. group coordination · L- · script
> C2 fenced → new target: C1 is simply given p3–p5 on its next heartbeat.
- pass 1: ✅ consumer-rebalance-protocol.md:32; CurrentAssignmentBuilder.java doc
- pass 2: ✅ session expiry fences/removes member, new target assignment delivered on next heartbeat

### C1447 · js:11. group coordination · L- · script
> Session timeout expires. The coordinator removes C2 and reassigns its partitions.
- pass 1: ✅ CommonClientConfigs.java:208-211 — "remove this client from the group and initiate a rebalance"
- pass 2: ✅ mechanism

### C1448 · js:11. group coordination · L- · script
> C1 owns all six again, after a stop-the-world pause.
- pass 1: n/a (figure outcome)
- pass 2: ✅ eager stop-the-world

### C1449 · js:11. group coordination · L- · script
> C1 owns all six; p0–p2 never stopped.
- pass 1: ✅ consumer-rebalance-protocol.md:32 — incremental
- pass 2: ✅ KIP-848 incremental: owned partitions untouched

### C1450 · js:11. group coordination · L- · script
> C1 owns all six; p0–p2 never stopped.
- pass 1: ✅ consumer-rebalance-protocol.md:32 — incremental
- pass 2: ✅ KIP-848 incremental: owned partitions untouched

### C1451 · js:11. group coordination · L- · script
> Lesson: for a crash, the protocol barely matters. The session timeout dominates: p3–p5 sat unread for up to 45 s in every lane. A clean shutdown (LeaveGroup / leave heartbeat) or static membership with fast restarts is what shortens that window.
- pass 1: ✅ design.md:123; clients/.../requests/ConsumerGroupHeartbeatRequest.java:31 LEAVE_GROUP_MEMBER_EPOCH; LeaveGroupRequest.json (advice = opinion)
- pass 2: ✅ detection bounded by session timeout in both protocols; LeaveGroup (classic) / leave heartbeat with MemberEpoch -1 (KIP-848) avoids the wait; static membership lets a fast restart reclaim partitions
