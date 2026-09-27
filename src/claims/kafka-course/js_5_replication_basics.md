# Claims ledger — kafka-course.html — js_5_replication_basics

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1360 · js:5. replication basics · L- · script
> Partition payments-0, RF 3, min.insync.replicas=…. Leader on B1; offsets 0–2 are on all three replicas (committed).
- pass 1: n/a (scenario setup)
- pass 2: n/a demo setup (illustrative)

### C1361 · js:5. replication basics · L- · script
> acked after all ISR members have it (offset 3)
- pass 1: ✅ clients/.../producer/ProducerConfig.java:138-139
- pass 2: ✅ clients/.../producer/ProducerConfig.java:138-139

### C1362 · js:5. replication basics · L- · script
> Written and replicated. … the producer heard "done" before the followers had it. Everything went fine this time.`} Offset 3 is now committed and visible to consumers.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:135-139 + docs/design/design.md:302-306 — acks=0/1 respond before followers; visible after ISR replication
- pass 2: ✅ docs/design/design.md:302 committed messages given to consumers; acks=1 responds before followers (clients/.../producer/ProducerConfig.java:135)

### C1363 · js:5. replication basics · L- · script
> Writing pay#4 with acks=…; the leader will die at the worst possible moment…
- pass 1: n/a (scenario narration)
- pass 2: n/a narration

### C1364 · js:5. replication basics · L- · script
> acked after all ISR members have it (offset 3)
- pass 1: ✅ clients/.../producer/ProducerConfig.java:138-139
- pass 2: ✅ clients/.../producer/ProducerConfig.java:138-139

### C1365 · js:5. replication basics · L- · script
> Nothing lost. B1 died after the ack, but the ack only came once B2 and B3 had offset 3. The controller elected B2 from the ISR, and B2 has pay#4.
- pass 1: ✅ docs/design/design.md:331,364 — elected from ISR; committed on all ISR
- pass 2: ✅ docs/design/design.md:309,331 — "committed message will not be lost, as long as there is at least one in sync replica alive"

### C1366 · js:5. replication basics · L- · script
> Acknowledged write lost. The producer was told "done", but only B1 had pay#4. B2 became leader from the ISR without it, and when B1 comes back it must re-sync from the new leader, so its copy of offset 3 is replaced too. …
- pass 1: ✅ clients/.../producer/ProducerConfig.java:135-137 + docs/design/design.md:335 — "the record will be lost"; "must fully re-sync"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:135-137 (acks=1 loss); docs/design/design.md:335 rejoin requires re-sync from leader

### C1367 · js:5. replication basics · L- · script
> B2 and B3 go down. After their sessions time out, the ISR shrinks to just the leader.
- pass 1: ✅ docs/design/design.md:296-298 — session loss removes from ISR
- pass 2: ✅ docs/design/design.md:298 — "controller will notice the failure through the loss of its session, and will remove the broker from the ISR"

### C1368 · js:5. replication basics · L- · script
> Write rejected. ISR size 1 &lt; min.insync.replicas=…, so an acks=all write fails with NotEnoughReplicas instead of living on one disk. Unavailable for writes, but no acknowledged data at risk.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:180-181 + Errors.java:220 — "Messages are rejected since there are fewer in-sync replicas than required"
- pass 2: ✅ core/src/main/scala/kafka/cluster/Partition.scala:1233-1236 — NotEnoughReplicasException when inSyncSize < minIsr && requiredAcks == -1

### C1369 · js:5. replication basics · L- · script
> Accepted with a single copy. min.insync.replicas is only enforced for acks=all. With acks=…, pay#4 now lives only on B1, and it isn't committed or visible to consumers until the ISR grows back to …. If B1's disk dies first, it's gone.
- pass 1: ✅ revised after pass-2 finding — C1369/C1376 — fig-repl "two followers down" narration (acks=0/1): record isn't committed or visible to consumers until the ISR grows back to min.insync.replicas — TopicConfig.java:182-183; Partition.scala:1011-1013
- pass 2: ✅ Partition.scala:1234 (min ISR checked only when requiredAcks == -1) + :1011 HW frozen under min ISR; design.md:302-305 "not be visible to the consumers until" ISR ≥ min ISR
