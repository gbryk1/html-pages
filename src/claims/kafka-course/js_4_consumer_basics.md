# Claims ledger — kafka-course.html — js_4_consumer_basics

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1352 · js:4. consumer basics · L- · script
> Topic payments has 4 partitions. Group billing has one consumer, C1, which owns all four.
- pass 1: n/a (scenario setup)
- pass 2: n/a demo setup

### C1353 · js:4. consumer basics · L- · script
> P… is now owned by … and resumes at the committed offset …, not at 0 and not at the end.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:86-87 — committed position is where a process resumes
- pass 2: ✅ KafkaConsumer.java:85-86 — new owner resumes from committed offset

### C1354 · js:4. consumer basics · L- · script
> Five consumers is enough for this demo. Try ➖ or ↺.
- pass 1: n/a (UI hint)
- pass 2: n/a UI hint

### C1355 · js:4. consumer basics · L- · script
> … joined → rebalance. …… got nothing: there are only 4 partitions.` : ''}
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99,108-110 — partitions moved on join; one owner per partition
- pass 2: ✅ design.md:157 — one owner per partition; extra member idle

### C1356 · js:4. consumer basics · L- · script
> Keep at least one consumer, or nobody reads and lag just grows.
- pass 1: n/a (UI hint)
- pass 2: n/a pedagogy

### C1357 · js:4. consumer basics · L- · script
> … left (or crashed) → rebalance. Its partitions went to the survivors. …
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:108-109 — "if a process fails, the partitions assigned to it will be reassigned"
- pass 2: ✅ CommonClientConfigs.java:195 — reassign partitions on member failure

### C1358 · js:4. consumer basics · L- · script
> 4 consumers, 4 partitions: one each. That is the maximum useful parallelism for this group. Each consumer picked up where the previous owner's bookmark (committed offset) pointed.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99 + clients/.../consumer/KafkaConsumer.java:86-87
- pass 2: ✅ design.md:157; KafkaConsumer.java:85-86

### C1359 · js:4. consumer basics · L- · script
> 5 consumers, 4 partitions: C5 sits idle. Inside one group a partition has exactly one owner, so the partition count caps parallelism. C5 is still useful as a hot spare: it takes over if someone leaves.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99,108-110 — exactly one consumer per partition; reassigned on failure
- pass 2: ✅ design.md:157; idle member takes partitions on next rebalance
