# Claims ledger — kafka-course.html — js_10_replication_protocol

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1417 · js:10. replication protocol · L- · script
> consumer can read: offsets 0–…
- pass 1: ✅ design.md:302
- pass 2: n/a — figure UI label

### C1418 · js:10. replication protocol · L- · script
> All three replicas hold offsets 0–3 (LEO 4, HW 4). Press ▶.
- pass 1: n/a (figure initial state)
- pass 2: n/a — illustrative initial state (LEO = next offset 4, HW 4 is consistent with definitions)

### C1419 · js:10. replication protocol · L- · script
> Produce: the leader appends offsets …–… (yellow: not committed). With acks=all the producer's request waits until the HW passes ….
- pass 1: ✅ design.md:191,302 — acks=all waits for commit by ISR
- pass 2: ✅ mechanism: leader appends, records above HW uncommitted; acks=all waits for HW ≥ lastOffset+1 (ReplicaManager.scala:839, Partition.scala:971)

### C1420 · js:10. replication protocol · L- · script
> Fetch round 1: … send Fetch(FetchOffset=…) and receive the 2 new records. The response carries HW …, so their HW doesn't move yet.…
- pass 1: ✅ FetchRequest.json:99; FetchResponse.json:73
- pass 2: ✅ mechanism: leader's HW only moves after it learns follower LEOs from the next FetchOffset, so round-1 response carries the old HW (Partition.scala:984-988; FetchResponse.json:73)

### C1421 · js:10. replication protocol · L- · script
> Fetch round 2: … fetch from offset …, which tells the leader their LEO. The smallest LEO in the ISR is now … → HW = …. The producer gets its ack, consumers may read up to …, and the followers learn the new HW in this response.
- pass 1: ✅ Partition.scala:992; FetchResponse.json:73
- pass 2: ✅ Partition.scala:992 HW = smallest LEO in ISR; delayed produce completes when HW ≥ required offset (Partition.scala:971)

### C1422 · js:10. replication protocol · L- · script
> Fetch round 2: F1 reports LEO …, but F2 is still in the ISR with LEO …. HW = min over ISR = …: stuck. The producer keeps waiting and consumers can't see the new records.
- pass 1: ✅ Partition.scala:992 — min LEO of in-sync replicas
- pass 2: ✅ Partition.scala:1025-1030 — HW takes min LEO of replicas in maximal ISR, so an in-sync laggard holds it

### C1423 · js:10. replication protocol · L- · script
> The log was full, so the figure reset. Press ▶ again.
- pass 1: n/a (UI message)
- pass 2: n/a — UI message

### C1424 · js:10. replication protocol · L- · script
> 💥 F2 stalls (long GC pause, dying disk). It stays in the ISR for now; the leader only notices lag over time.
- pass 1: n/a (break-it scenario setup)
- pass 2: ✅ ReplicationConfigs.java:56 — removal only after replica.lag.time.max.ms elapses (stuck follower case, Partition.scala:1145)

### C1425 · js:10. replication protocol · L- · script
> ⏱ replica.lag.time.max.ms (30 s default) passes without F2 catching up to the leader's LEO. The leader asks the controller to shrink the ISR (AlterPartition).
- pass 1: ✅ ReplicationConfigs.java:55-57; AlterPartitionRequest.json:17
- pass 2: ✅ ReplicationConfigs.java:55 default 30000 ms; shrink is submitted via AlterPartition (Partition.scala submitAlterPartition)

### C1426 · js:10. replication protocol · L- · script
> acknowledged ✔ (after the delay)
- pass 1: n/a (UI label)
- pass 2: n/a — UI label

### C1427 · js:10. replication protocol · L- · script
> ISR = {L, F1}. HW jumps to …, the producer is finally acknowledged, consumers see the records. The ISR still has … ≥ min.insync.replicas (…) members, so writes stay allowed. One more failure and acks=all writes would get NOT_ENOUGH_REPLICAS, and the HW would freeze for every write until the ISR recovers.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ Partition.scala:1011,1234 — "inSyncSize < minIsr && requiredAcks == -1 ... NotEnoughReplicasException"; HW not increased while under min ISR (mechanism; … placeholders n/a)
