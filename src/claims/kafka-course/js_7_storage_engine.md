# Claims ledger — kafka-course.html — js_7_storage_engine

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1378 · js:7. storage engine · L- · script
> Type an offset between 0 and 3 699 and press Find.
- pass 1: n/a (UI prompt)
- pass 2: n/a — UI hint

### C1379 · js:7. storage engine · L- · script
> Step 1 · binary search over segment base offsets. Probe …: … …, so go ….
- pass 1: ✅ LogSegments.java:210-221 — floor lookup by base offset (figure binary search = illustrative)
- pass 2: ✅ mechanism: docs/implementation/log.md:43 — "simple binary search variation against an in-memory range maintained for each file" (real impl: ConcurrentSkipListMap.floorEntry)

### C1380 · js:7. storage engine · L- · script
> Offset out of range. This partition holds offsets 0–…. A consumer asking for … gets OFFSET_OUT_OF_RANGE and falls back to its auto.offset.reset policy.
- pass 1: ✅ docs/implementation/log.md:47 — "given an OutOfRangeException and can either reset itself or fail"
- pass 2: ✅ docs/implementation/log.md:47 — "attempts to consume a non-existent offset it is given an OutOfRangeException and can either reset"; auto.offset.reset applies on OFFSET_OUT_OF_RANGE

### C1381 · js:7. storage engine · L- · script
> Step 2 · segment … chosen. Now binary-search its sparse index (… entries for … batches) for the largest offset ≤ ….
- pass 1: ✅ OffsetIndex.java:34-36 — "greatest offset less than or equal to the target offset"
- pass 2: ✅ OffsetIndex lookup = largest indexed offset ≤ target (AbstractIndex largestLowerBound search); counts illustrative

### C1382 · js:7. storage engine · L- · script
> Step 3 · jump to position …… and scan batches forward until one contains ….
- pass 1: ✅ log.md:45 — "calculating the file-specific offset… then reading from that file offset"
- pass 2: ✅ FileRecords.java:313-328 searchForOffsetFromPosition scans batches from position until one contains target

### C1383 · js:7. storage engine · L- · script
> No index. Without .index the broker can only start at position 0 of segment … and read forward.
- pass 1: n/a (labelled break-it simulation)
- pass 2: ✅ mechanism (hypothetical): without index lookup the scan starts at position 0 (LogSegment.java:396 uses mapping.position())

### C1384 · js:7. storage engine · L- · script
> Found … after reading … batch… (… bytes). One binary search over segments, one over a tiny memory-mapped index, then a short sequential read. That's why lookups stay cheap however big the partition grows.
- pass 1: ✅ OffsetIndex.java:34-36; AbstractIndex.java:31 — memory-mapped binary search
- pass 2: ✅ mechanism; numbers illustrative

### C1385 · js:7. storage engine · L- · script
> Found …, but only after reading … batches (… bytes) from the start of the segment. With a real 1 GiB segment that could mean reading up to 1 GiB. Kafka treats index files as rebuildable: after a crash it regenerates them from the .log.
- pass 1: ✅ OffsetIndex.java:42; LogConfig.java:125 — "in the event of a crash it is rebuilt"; 1 GiB
- pass 2: ✅ LogSegment.java:478-499 recover() resets and rebuilds offset/time/txn indexes from the .log; 1 GiB = default segment.bytes
