# Claims ledger — kafka-course.html — js_6_retention_basics

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1370 · js:6. retention basics · L- · script
> today: day …retention.ms = … dayslog start offset …log end offset …
- pass 1: n/a (UI label)
- pass 2: n/a UI labels

### C1371 · js:6. retention basics · L- · script
> Day …. One segment per day for the demo. A segment may go when its newest record is more than … days old. Consumer billing keeps up.
- pass 1: ✅ UnifiedLog.java:2010 — "startMs - segment.largestTimestamp() > retentionMs" (one segment/day illustrative)
- pass 2: ✅ mechanism: docs/implementation/log.md:72 (segment judged by newest timestamp); one-segment-per-day illustrative

### C1372 · js:6. retention basics · L- · script
> Day …: segment ….log deleted. Its newest record (day …) is now more than … days old. The log start offset moves to …. Whole file, no record-by-record work.
- pass 1: ✅ docs/implementation/log.md:72 + UnifiedLog.java deleteSegments — log start advanced; whole segment
- pass 2: ✅ mechanism: storage/.../log/UnifiedLog.java:1941-1946 whole segment removed, log start offset moved to next segment's base offset

### C1373 · js:6. retention basics · L- · script
> Day …: a new segment was rolled. Nothing is older than … days yet, so nothing is deleted.
- pass 1: ✅ UnifiedLog.java:2010 (roll per day illustrative)
- pass 2: ✅ mechanism: nothing deleted until newest record exceeds retention.ms (UnifiedLog.java:2011)

### C1374 · js:6. retention basics · L- · script
> Day …: the log slides forward. Old segments dropped off the front while new ones were added at the back. billing read everything in time, so nothing it needed was lost.
- pass 1: n/a (scenario summary)
- pass 2: ✅ mechanism consistent with log.md:72

### C1375 · js:6. retention basics · L- · script
> Consumer group reports stopped at offset 150 (segment 00100.log) and goes on vacation…
- pass 1: n/a (scenario setup)
- pass 2: n/a narration

### C1376 · js:6. retention basics · L- · script
> Segment 00100.log is gone. Offset 150 no longer exists (log start is now …). When reports comes back, its fetch gets OFFSET_OUT_OF_RANGE…
- pass 1: ✅ Errors.java:182 + docs/implementation/log.md:47 — OFFSET_OUT_OF_RANGE
- pass 2: ✅ Errors.java:182 OFFSET_OUT_OF_RANGE; docs/implementation/log.md:47

### C1377 · js:6. retention basics · L- · script
> auto.offset.reset=latest (default) → jumped to the end. About … offsets were never processed: … deleted by retention, and the rest skipped even though they're still on disk. Alert on lag before the window closes, or use earliest / none.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:172-175,546 — reset to latest when offset gone
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:172-175 latest resets to end; skipped records remain on disk (retention-independent)
