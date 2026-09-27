# Claims ledger — kafka-course.html — js_20_quotas

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1535 · js:20. quotas · L- · script
> Three tenants share one broker, each with a producer_byte_rate quota of 10 MB/s (illustrative). Press ▶.
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (figure setup, labelled illustrative)

### C1536 · js:20. quotas · L- · script
> Measuring one window: every tenant sends at the rate it wants…
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (figure narration)

### C1537 · js:20. quotas · L- · script
> analytics averaged 15 MB/s against a 10 MB/s quota: 50% over. payments and search are well under theirs.
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative numbers; arithmetic 15/10 = 50% over is correct)

### C1538 · js:20. quotas · L- · script
> The broker answers immediately with ThrottleTimeMs=…, computed as (15 − 10) / 10 × 2 s window, and mutes the channel. The client also holds back on its own.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/quota/QuotaUtils.java:44 — "difference / e.bound()" ; docs/design/design.md:509 — "returns a response with the delay immediately" ; docs/design/design.md:509 — "the Kafka client will also refrain from sending further requests" (numbers illustrative)
- pass 2: ✅ mechanism QuotaUtils.java:42-44 (value−bound)/bound × window; design.md:509 immediate response + mute + client refrains (2 s window illustrative)

### C1539 · js:20. quotas · L- · script
> After the pause, analytics’ average rate is back at 10 MB/s. It was delayed, not rejected, and payments and search never noticed.
- pass 1: ✅ docs/design/design.md:509 — "bring the violating client under its quota" (numbers illustrative)
- pass 2: ✅ design.md:509 delay brings client back under quota; delayed, not rejected

### C1540 · js:20. quotas · L- · script
> Quotas removed (the default: unlimited). The load test runs flat out…
- pass 1: ✅ docs/operations/basic-kafka-operations.md:690 — "By default, clients receive an unlimited quota"
- pass 2: ✅ QuotaConfig.java:87 QUOTA_BYTES_PER_SECOND_DEFAULT = Long.MAX_VALUE (unlimited)

### C1541 · js:20. quotas · L- · script
> Noisy neighbour. 34 MB/s of demand on a broker that can handle ~30 (illustrative): request queues and network saturate, and payments and search see latency rise even though their traffic didn’t change.
- pass 1: ✅ docs/operations/multi-tenancy.md:114 — "impact other clients" (capacity numbers labelled illustrative)
- pass 2: ✅ mechanism: design.md:472 — "monopolize broker resources, cause network saturation and generally DOS other clients" (numbers illustrative)
