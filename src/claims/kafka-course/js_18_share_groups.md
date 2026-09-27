# Claims ledger — kafka-course.html — js_18_share_groups

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1517 · js:18. share groups · L- · script
> Three share consumers, one partition. Press ▶ to let them poll.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1518 · js:18. share groups · L- · script
> …: ShareFetch. Each live consumer acquires up to … available records with a time-limited lock; their delivery counts go up.
- pass 1: ✅ docs/design/design.md:265; server/src/main/java/org/apache/kafka/server/share/fetch/InFlightState.java:163-168 — "Records are acquired for delivery to this share consumer with a time-limited acquisition lock"
- pass 2: ✅ docs/design/design.md:265 acquisition lock; delivery count incremented on acquisition (server/.../share/fetch/InFlightState.java updatedDeliveryCount); counts n/a

### C1519 · js:18. share groups · L- · script
> offset … hit the delivery limit → archived
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/share/fetch/InFlightState.java:163-164 — "deliveryCount >= maxDeliveryCount) { newState = RecordState.ARCHIVED"
- pass 2: ✅ server/.../share/fetch/InFlightState.java:163-164 — deliveryCount >= maxDeliveryCount → ARCHIVED

### C1520 · js:18. share groups · L- · script
> All records acknowledged. Three consumers shared one partition — impossible in a consumer group — and SPSO moved to the end: everything below it is done.
- pass 1: ✅ docs/design/design.md:254; core/src/main/java/kafka/server/share/SharePartition.java:279-282 — "partitions may be assigned to multiple consumers"
- pass 2: ✅ docs/design/design.md:254 partitions may be assigned to multiple consumers; SPSO = start of in-flight window (ShareSnapshotValue StartOffset)

### C1521 · js:18. share groups · L- · script
> Everyone fetches. C2 now holds offsets 2 and 3 under an acquisition lock.
- pass 1: ✅ docs/design/design.md:265 — "While a record is acquired, it is unavailable to other consumers"
- pass 2: n/a — illustrative step

### C1522 · js:18. share groups · L- · script
> C2 crashes mid-processing. C1 and C3 accept their records. Offsets 2 and 3 stay locked — nobody else may take them yet — so SPSO is stuck at 2.
- pass 1: ✅ docs/design/design.md:265; SharePartition.java:279-282 — "unavailable to other consumers in the same share group for the duration of the lock"
- pass 2: ✅ docs/design/design.md:265 — "While a record is acquired, it is unavailable to other consumers"; SPSO cannot pass unacknowledged records

### C1523 · js:18. share groups · L- · script
> The lock expires (30 s by default). 2 and 3 become available again, delivery count 1 kept.
- pass 1: ✅ docs/design/design.md:267; server/src/main/java/org/apache/kafka/server/share/fetch/InFlightState.java:163-168 — "The lock is released automatically once its duration elapses"
- pass 2: ✅ docs/design/design.md:267 lock released automatically after duration (30 s default); delivery count retained (only DECREASE op lowers it, server/.../share/fetch/InFlightState.java:163)

### C1524 · js:18. share groups · L- · script
> Redelivered and done. C1/C3 picked up 2 and 3 (delivery count 2). At-least-once: if C2 had already done side effects, they may happen twice.
- pass 1: ✅ docs/design/design.md:267 — "making the record available to another delivery attempt"
- pass 2: ✅ mechanism: redelivery increments delivery count; at-least-once semantics follow from lock expiry redelivery (docs/design/design.md:267)

### C1525 · js:18. share groups · L- · script
> Offset 4 is a poison record — every consumer fails on it and releases it.
- pass 1: ✅ docs/design/design.md:270 — "Release the record, making it available for another delivery attempt."
- pass 2: ✅ docs/design/design.md:270 release → available for another attempt

### C1526 · js:18. share groups · L- · script
> Poison contained. After … deliveries offset 4 was archived instead of made available — the queue keeps flowing and SPSO passes it. Kafka 4.3.1 does not write it to a dead-letter topic (KIP-1191 DLQ support is only groundwork so far): if you need the record, copy it somewhere yourself, then REJECT it.
- pass 1: ❌ fixed in part file (author pass 1) — server-common/.../share/dlq/NoOpShareGroupDLQManager.java is the only ShareGroupDLQ implementation; no DLQ config in 4.3.1
- pass 2: ✅ server/.../share/fetch/InFlightState.java:163-164 archived at limit; server-common/.../dlq/NoOpShareGroupDLQManager.java only no-op DLQ in 4.3.1; advice n/a
