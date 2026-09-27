# Claims ledger — kafka-course.html — js_16_leader_epochs

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1492 · js:16. leader epochs · L- · script
> A appends offsets 6 and 7 (epoch 1). Followers haven't fetched them yet, so the HW stays at 6 — these are not committed, and an acks=all producer is still waiting.
- pass 1: ✅ docs/design/design.md:301-305 — "the messages will not be visible to the consumers until … replicated to all the in-sync replicas"
- pass 2: ✅ design.md ISR/HW semantics — HW = min LEO of ISR, so unreplicated 6–7 not committed; acks=all waits for ISR (offsets n/a illustrative)

### C1493 · js:16. leader epochs · L- · script
> A crashes. The controller elects B from the ISR and bumps the leader epoch to 2.
- pass 1: ✅ docs/design/design.md:331; storage/.../epoch/LeaderEpochFileCache.java javadoc — "Only members of this set are eligible for election as leader / epoch assigned to each leader by the controller"
- pass 2: ✅ KIP-101 / PartitionChangeBuilder.electLeader — ISR member elected, leader epoch incremented

### C1494 · js:16. leader epochs · L- · script
> B writes offsets 6–8 in epoch 2; C replicates them; HW moves to 9. Offsets 6 and 7 now mean different things on A and on B.
- pass 1: n/a (scenario step, illustrative)
- pass 2: ✅ mechanism consistent (divergent records at same offsets, KIP-101 Scenario 2); numbers n/a

### C1495 · js:16. leader epochs · L- · script
> A returns as a follower and fetches with LastFetchedEpoch = 1. B's epoch cache says epoch 2 began at offset 6, so epoch 1 ended at 6: B answers with DivergingEpoch {epoch 1, end offset 6}.
- pass 1: ✅ clients/src/main/resources/common/message/FetchRequest.json:101; FetchResponse.json:79-80 — "the largest epoch and its end offset such that subsequent records are known to diverge"
- pass 2: ✅ FetchRequest.json:101 LastFetchedEpoch; FetchResponse.json:80 DivergingEpoch "largest epoch and its end offset"; LeaderEpochFileCache.endOffsetFor returns next epoch start

### C1496 · js:16. leader epochs · L- · script
> A truncates offsets 6–7 — exactly the records nobody else has, and that were never committed.
- pass 1: ✅ https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation — "enabling followers to truncate only truly divergent messages"
- pass 2: ✅ KIP-101 — follower truncates to divergence point; 6–7 were above HW, uncommitted

### C1497 · js:16. leader epochs · L- · script
> A fetches B's 6–8 and rejoins the ISR. All three copies share one history again, and no acknowledged (committed) record was lost. The producer whose write to 6–7 timed out or failed retries it.
- pass 1: ✅ docs/design/design.md:319,335 — "it must fully re-sync again even if it lost unflushed data"
- pass 2: ✅ mechanism: follower re-fetches, rejoins ISR; unacked producer write fails/times out and is retried by the producer

### C1498 · js:16. leader epochs · L- · script
> C goes down. ISR = {A,B}, still ≥ min ISR (2), so ELR stays empty and writes continue.
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/PartitionChangeBuilder.java:535-539 — "If the ISR is larger or equal to the min ISR, clear the ELR"
- pass 2: ✅ PartitionChangeBuilder.java:536 — ISR ≥ min ISR → ELR cleared/empty

### C1499 · js:16. leader epochs · L- · script
> B is shut down cleanly. ISR = {A} is below min ISR, so the HW can't advance — nothing new can be committed. B therefore holds every committed record: the controller puts it in the ELR.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:31; metadata/src/main/java/org/apache/kafka/controller/PartitionChangeBuilder.java:543-553 — "the high watermark for the data partition can't advance if the size of the ISR is smaller than the min ISR"
- pass 2: ✅ eligible-leader-replicas.md:31 + PartitionChangeBuilder.java:549-553 — clean-shutdown former ISR member moves into ELR when ISR < min ISR

### C1500 · js:16. leader epochs · L- · script
> A crashes hard. The ISR is empty and the partition is leaderless. A stays in the ELR but is fenced; when it restarts, the controller sees it shut down uncleanly and drops it from the ELR.
- pass 1: ✅ revised after pass-2 finding — C1500/C1507 (epochs figure, "A crashes hard") — narration now: A stays in the ELR but fenced; controller drops it from the ELR when it re-registers after an unclean shutdown; figure state ELR={B,A} then {A} after B is elected — metadata/src/main/java/org/apach
- pass 2: ✅ metadata/.../ClusterControlManager.java:437-439 + ReplicationControlManager.java:1482-1488 — "ELR is enabled, generate unclean shutdown partition change records" (removes from ISR and ELR on re-registration)

### C1501 · js:16. leader epochs · L- · script
> B comes back. Without ELR it is not in the ISR: the partition would stay offline until A returns — or you'd need an unclean election.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:31-35; docs/design/design.md:343-347 — "Wait for a replica in the ISR to come back to life"
- pass 2: ✅ eligible-leader-replicas.md:35 + design.md:355 — without ELR/unclean election the partition waits for the last ISR member

### C1502 · js:16. leader epochs · L- · script
> ELR to the rescue: ISR empty → pick an unfenced ELR member. B becomes leader in epoch 2 with all six committed records (A, still fenced, remains in the ELR until it re-registers). No data lost, no unclean election.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ metadata/.../PartitionChangeBuilder.java:320 — "(targetIsr.contains(replica) || (targetIsr.isEmpty() && targetElr.contains(replica))) && isAcceptableLeader"; acceptable = active (unfenced)

### C1503 · js:16. leader epochs · L- · script
> Setup: C fell behind (only offsets 0–3) and left the ISR. A and B hold 0–5; HW = 6, so 4 and 5 are committed.
- pass 1: n/a (scenario setup, illustrative)
- pass 2: n/a — illustrative setup (consistent with HW = min LEO of ISR)

### C1504 · js:16. leader epochs · L- · script
> A and B both die (a rack loses power and B's disk is corrupted). Only C is alive — and C is not in the ISR.
- pass 1: n/a (scenario step, illustrative)
- pass 2: n/a — illustrative scenario

### C1505 · js:16. leader epochs · L- · script
> unclean.leader.election.enable=true: C becomes leader in epoch 2. Its log is now the truth, and it appends new records at offsets 4 and 5 (acks=1 writes; acks=all fails while ISR {C} is below min ISR, and the HW cannot pass 4 until the ISR grows).
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ Partition.scala:1011,1234 — acks=all rejected below min ISR; HW frozen "because partition is under min ISR"; acks=1 accepted (mechanism; offsets illustrative)

### C1506 · js:16. leader epochs · L- · script
> A comes back and asks where epoch 1 ended: at offset 4 on C. A's committed records 4 and 5 diverge…
- pass 1: ✅ https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation — "The leader responds with the offset where the follower's epoch ends"
- pass 2: ✅ LeaderEpochFileCache.endOffsetFor — epoch 1 end offset = start of C's epoch 2 (4); FetchResponse DivergingEpoch

### C1507 · js:16. leader epochs · L- · script
> …and are truncated. Records acknowledged with acks=all are gone, and offsets 4–5 now hold different data. A consumer that had read past offset 4 detects the truncation: with auto.offset.reset=none it gets LogTruncationException; otherwise it silently repositions to the first diverging offset, 4.
- pass 1: ✅ revised after pass-2 finding — C1500/C1507 (epochs figure, "A crashes hard") — narration now: A stays in the ELR but fenced; controller drops it from the ELR when it re-registers after an unclean shutdown; figure state ELR={B,A} then {A} after B is elected — metadata/src/main/java/org/apach
- pass 2: ✅ clients/.../SubscriptionState.java maybeCompleteValidation — "resetting offset to the first offset known to diverge"; with no reset policy returns LogTruncation → LogTruncationException
