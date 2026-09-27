# Claims ledger — kafka-course.html — k-retention

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0206 · k-retention · L1 · p
> Reading doesn't delete, so something else must, or disks fill up forever. That's retention. The archive has a policy like "we keep ledger entries for seven days". Old records are removed based on time or size, never based on whether someone read them. A consumer that is days behind can still catch up, as long as it stays inside the retention window.
- pass 1: ✅ docs/getting-started/introduction.md:81 — "events are not deleted after consumption … define for how long Kafka should retain your events"
- pass 2: ✅ clients/.../common/config/TopicConfig.java:76-78 — retention by time/size, "an SLA on how soon consumers must read their data"; introduction.md:81 "events are not deleted after consumption"

### C0207 · k-retention · L1 · h3
> Two cleanup policies
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0208 · k-retention · L1 · p
> Each topic has a cleanup.policy:
- pass 1: n/a (lead-in)
- pass 2: ✅ clients/.../common/config/TopicConfig.java:156

### C0209 · k-retention · L1 · li
> delete (the default): discard old data when it's older than retention.ms or when the partition is bigger than retention.bytes. Good for event streams where each record stands alone: clicks, logs, payments.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:156-158 + ServerLogConfigs.java:89 — "The \"delete\" policy (which is the default) will discard old segments when their retention time or size limit has been reached" (use cases = opinion)
- pass 2: ✅ clients/.../common/config/TopicConfig.java:156-158 — "'delete' policy (which is the default) will discard old segments when their retention time or size limit has been reached"

### C0210 · k-retention · L1 · li
> compact: keep at least the latest record for each key and remove older records with the same key. The archive only keeps the newest entry per account. Good for "current state" topics, like a customer's latest address. Chapter 14 covers compaction.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:159 + docs/design/design.md:368,397 — "retains the latest value for each key"
- pass 2: ✅ clients/.../common/config/TopicConfig.java:158-159 "retains the latest value for each key"; design.md log compaction section "retain at least the last known value"

### C0211 · k-retention · L1 · li
> Both at once, delete,compact: compact, and also drop old segments by time/size.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:160-162 — "old segments will be discarded per the retention time and size configuration, while retained segments will be compacted"
- pass 2: ✅ clients/.../common/config/TopicConfig.java:160-162 — "old segments will be discarded per the retention time and size configuration, while retained segments will be compacted"

### C0212 · k-retention · L1 · h3
> The defaults
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0213 · k-retention · L1 · tr
> Config | Default | What it does | Use it when
- pass 1: n/a (table header)
- pass 2: n/a table header

### C0214 · k-retention · L1 · tr
> cleanup.policy | delete | Delete old segments, or compact by key | compact for latest-value-per-key topics
- pass 1: ✅ ServerLogConfigs.java:89 + clients/.../common/config/TopicConfig.java:156-159 — "CLEANUP_POLICY_DELETE"
- pass 2: ✅ server-common/.../ServerLogConfigs.java:89 LOG_CLEANUP_POLICY_DEFAULT = delete

### C0215 · k-retention · L1 · tr
> retention.ms | 604800000 (7 days) | Max age before a segment may be deleted; -1 = no time limit | Match how far back consumers must be able to re-read
- pass 1: ✅ storage/.../log/LogConfig.java:128,202 + clients/.../common/config/TopicConfig.java:76-79 — "24 * 7 * 60 * 60 * 1000L"; "If set to -1, no time limit is applied"
- pass 2: ✅ storage/.../log/LogConfig.java:128,202 DEFAULT_RETENTION_MS = 7 days, atLeast(-1); clients/.../common/config/TopicConfig.java:79 "If set to -1, no time limit"

### C0216 · k-retention · L1 · tr
> retention.bytes | -1 (no limit) | Max size per partition before old segments go | Cap disk use on busy topics
- pass 1: ✅ ServerLogConfigs.java:81 + clients/.../common/config/TopicConfig.java:67-70 — "-1L"; "limit is enforced at the partition level"
- pass 2: ✅ server-common/.../ServerLogConfigs.java:81 LOG_RETENTION_BYTES_DEFAULT = -1L; clients/.../common/config/TopicConfig.java:67-70 per partition

### C0217 · k-retention · L1 · tr
> segment.bytes | 1073741824 (1 GiB) | Size at which a new segment file starts | Smaller on low-volume topics so retention can act sooner
- pass 1: ✅ storage/.../log/LogConfig.java:125,188 — "1024 * 1024 * 1024"
- pass 2: ✅ storage/.../log/LogConfig.java:125 DEFAULT_SEGMENT_BYTES = 1024*1024*1024; clients/.../common/config/TopicConfig.java:32-34

### C0218 · k-retention · L1 · tr
> segment.ms | 604800000 (7 days) | Age at which a new segment starts, even if not full | Same reason as above
- pass 1: ✅ storage/.../log/LogConfig.java:126,190 — "24 * 7 * 60 * 60 * 1000L"
- pass 2: ✅ storage/.../log/LogConfig.java:126 DEFAULT_SEGMENT_MS = 24*7*60*60*1000L; clients/.../common/config/TopicConfig.java:37-39

### C0219 · k-retention · L1 · tr
> log.retention.check.interval.ms (broker) | 300000 (5 min) | How often the broker looks for deletable segments | Rarely changed
- pass 1: ✅ ServerLogConfigs.java:84-85 + core/.../LogManager.scala:626 — "log.retention.check.interval.ms"; "5 * 60 * 1000L"; schedule("kafka-log-retention")
- pass 2: ✅ server-common/.../ServerLogConfigs.java:84-85 log.retention.check.interval.ms default 5*60*1000L

### C0220 · k-retention · L1 · p
> The broker-level equivalents (log.retention.hours = 168, log.retention.bytes, log.cleanup.policy…) set the default for every topic. The topic-level settings override them for one topic.
- pass 1: ✅ storage/.../log/LogConfig.java:160,162,164 + ServerLogConfigs.java:75,80,88 + server-common/.../ServerTopicConfigSynonyms.java:63,75 — log.retention.hours = toHours(7d) = 168; log.retention.bytes; log.cleanup.policy synonyms
- pass 2: ✅ server-common/.../ServerLogConfigs.java:75,80,88; storage/.../log/LogConfig.java:160 log.retention.hours default = toHours(7 days) = 168

### C0221 · k-retention · L1 · h3
> Deletion happens per segment
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0222 · k-retention · L1 · p
> Remember from chapter 1 that a partition is a series of segment files (the book's volumes). Time and size retention never delete single records. They delete whole segments, oldest first. For time-based retention, the largest timestamp in the segment counts. A segment goes only when even its newest record is older than retention.ms. So data often lives longer than retention.ms: up to about one extra segment's worth of time, plus the time until the next retention check.
- pass 1: ❌ fixed in part file — design.md log compaction removes records; retention deletes segments (UnifiedLog.deleteOldSegments)
- pass 2: ✅ docs/implementation/log.md:72 — "Data is deleted one log segment at a time... largest timestamp in a segment file... defining the retention time for the entire segment"; storage/.../log/UnifiedLog.java:2008-2011

### C0223 · k-retention · L1 · p
> If both limits are set, a segment is deleted when either the time or the size rule says so.
- pass 1: ✅ docs/implementation/log.md:72 — "a segment that is eligible for deletion due to either policy will be deleted"
- pass 2: ✅ docs/implementation/log.md:72 — "a segment that is eligible for deletion due to either policy will be deleted"

### C0224 · k-retention · L1 · figcaption
> Illustrative: one segment per day (really: a new segment at segment.bytes 1 GiB or segment.ms 7 days, whichever comes first), 100 records per segment, and deletion as soon as the day turns (really: at the next retention check). The rules are real: whole segments only, judged by their newest record's timestamp, and an out-of-range consumer falls back to auto.offset.reset.
- pass 1: ✅ storage/.../log/LogConfig.java:125-126 + docs/implementation/log.md:72 + clients/.../consumer/ConsumerConfig.java:172-173 (numbers labelled illustrative)
- pass 2: ✅ figcaption labels simplifications; rules match log.md:72 and ConsumerConfig auto.offset.reset doc

### C0225 · k-retention · L1 · p
> A slow consumer can lose data silently. If a consumer's committed offset points into a segment that has already been deleted, the offset no longer exists. The consumer then applies auto.offset.reset. With the default latest, it jumps to the end and skips everything in between, with only a log line to show for it. For consumers that must not skip data, alert on lag well before the retention window runs out. You can also set auto.offset.reset=earliest (re-read what's left) or none (fail loudly).
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:172-175 + clients/.../consumer/internals/SubscriptionState.java:458 — "does not exist any more on the server (e.g. because that data has been deleted)"; log.info "Resetting offset for partition"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:172-173 "current offset does not exist any more on the server (e.g. because that data has been deleted)"; default latest; docs/implementation/log.md:47

### C0226 · k-retention · L1 · p
> Size a topic. A topic receives 2 MB/s on average, has 6 partitions and RF 3, and must keep 3 days of data. 1) Roughly how much disk does it need across the cluster? 2) Set its retention to 3 days. 3) What retention.bytes would cap each partition at the same amount if traffic is spread evenly?
- pass 1: n/a (exercise question)
- pass 2: n/a quiz question

### C0227 · k-retention · L1 · p
> 1) 2 MB/s × 86 400 s/day × 3 days ≈ 518 GB of data, × RF 3 ≈ 1.56 TB across the cluster, plus headroom for the extra segment per partition and traffic peaks.
- pass 1: n/a (arithmetic exercise answer (2×86400×3=518 400 MB; ×3))
- pass 2: ✅ verify-tmp python: 2*86400*3 = 518400 MB ≈ 518 GB; ×3 = 1555200 MB ≈ 1.56 TB

### C0228 · k-retention · L1 · pre
> bin/kafka-configs.sh --bootstrap-server localhost:9092 --alter \ --entity-type topics --entity-name payments --add-config retention.ms=259200000
- pass 1: ✅ core/src/main/scala/kafka/admin/ConfigCommand.scala:546,559,563,566,572 — bootstrap-server, alter, entity-type, entity-name, add-config (259200000 ms = 3 d)
- pass 2: ✅ 3*86400000 = 259200000 (python); kafka-configs.sh --alter --entity-type topics --entity-name --add-config are valid flags

### C0229 · k-retention · L1 · p
> 3) retention.bytes is per partition: 518 GB ÷ 6 ≈ 86 GB. Leaving it at -1 is also fine if the time limit is enough.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:70 — "Since this limit is enforced at the partition level" (arithmetic)
- pass 2: ✅ 518400/6 = 86400 MB ≈ 86 GB; clients/.../common/config/TopicConfig.java:70 "enforced at the partition level"

### C0230 · k-retention · L1 · div
> Isn't keeping a week of data expensive and slow?
- pass 1: n/a (question)
- pass 2: n/a question

### C0231 · k-retention · L1 · div
> Expensive in disk, yes. Slow, no. Kafka only appends and reads sequentially, so the Kafka docs say its performance is effectively constant with respect to data size. Storing data for a long time is fine. (Tiered storage, chapter 17, moves old segments to cheap object storage.)
- pass 1: ✅ revised after pass-2 finding — C0231 — "the design docs say" → "the Kafka docs say" (quote is from introduction) — docs/getting-started/introduction.md:81 "Kafka's performance is effectively constant with respect to data size"
- pass 2: ✅ docs/getting-started/introduction.md:81 — "Kafka's performance is effectively constant with respect to data size"

### C0232 · k-retention · L1 · div
> Can I set infinite retention?
- pass 1: n/a (question)
- pass 2: n/a question

### C0233 · k-retention · L1 · div
> Yes: retention.ms=-1 with retention.bytes=-1. Some teams keep full event histories that way, and compacted topics do something similar without growing forever.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:79 + ServerLogConfigs.java:81 — "If set to -1, no time limit is applied"; -1 = no size limit (teams remark = opinion)
- pass 2: ✅ clients/.../common/config/TopicConfig.java:69,79 (-1 no time limit; no size limit by default); storage/.../log/LogConfig.java:202 atLeast(-1)

### C0234 · k-retention · L2 · p
> retention.bytes is a floor, not a ceiling. In UnifiedLog.deleteRetentionSizeBreachedSegments, the broker computes how far the partition is over the limit and deletes an old segment only if the partition is still at least retention.bytes after removing it. So a partition hovers somewhere between the limit and the limit plus one segment. With 1 GiB segments and a small retention.bytes, that difference is big.
- pass 1: ✅ UnifiedLog.java:2040-2050 — "boolean delete = diff.get() - segmentSize >= 0"
- pass 2: ✅ storage/.../log/UnifiedLog.java:2040-2050 — "diff = logSize - retentionSize"; "delete = diff.get() - segmentSize >= 0"

### C0235 · k-retention · L2 · p
> Future timestamps block deletion. Time retention compares the broker clock with each segment's largest record timestamp. A producer with a clock set years ahead makes that segment "not old yet" for years, and the broker logs a warning about future timestamps.
- pass 1: ✅ UnifiedLog.java:2007-2010 — "contains future timestamp(s), making it ineligible to be deleted"
- pass 2: ✅ storage/.../log/UnifiedLog.java:2006-2010 — "contains future timestamp(s), making it ineligible to be deleted"; "startMs - segment.largestTimestamp() > retentionMs"

### C0236 · k-retention · L3 · summary
> L3🔬 Go deeper: how deletion stays safe for readers
- pass 1: n/a (summary heading)
- pass 2: n/a heading

### C0237 · k-retention · L3 · p
> A background task in the log manager runs every log.retention.check.interval.ms. For each log it calls UnifiedLog.deleteOldSegments(), which (for the delete policy) runs deleteRetentionSizeBreachedSegments and deleteRetentionMsBreachedSegments. These walk segments oldest-first and stop at the first one that isn't deletable, so deletion is always a prefix of the log. The log start offset moves forward before files are removed. A partition always keeps at least one segment: if every segment would go, a fresh empty one is rolled first.
- pass 1: ❌ fixed in part file — LogManager.scala:1434 calls deleteOldSegments(); UnifiedLog.java:1967-1973
- pass 2: ✅ core/.../log/LogManager.scala:625-629 ("kafka-log-retention" every retentionCheckMs); storage/.../log/UnifiedLog.java:1967-1973 (also runs deleteLogStartOffsetBreachedSegments), 1877-1905 oldest-first stop at first non-deletable, 1932-1949 "we must always have at least one segment... create a new one first"; "increment the... log-start-offset before removing the segment"

### C0238 · k-retention · L3 · p
> Readers aren't blocked. The implementation docs describe a copy-on-write segment list, so a fetch doing a binary search over segments sees an immutable snapshot while deletions proceed. A consumer asking for an offset below the new log start offset gets an OFFSET_OUT_OF_RANGE error (code 1), and that's what triggers auto.offset.reset.
- pass 1: ✅ docs/implementation/log.md:47,72 + Errors.java:182 — "copy-on-write style segment list"; "OutOfRangeException"; OFFSET_OUT_OF_RANGE(1
- pass 2: ✅ docs/implementation/log.md:72 — "copy-on-write style segment list... binary search to proceed on an immutable static snapshot"; Errors.java:182 OFFSET_OUT_OF_RANGE(1, ...)

### C0239 · k-retention · L1 · li
> Retention removes data by time or size, never because it was consumed.
- pass 1: ✅ docs/getting-started/introduction.md:81 + clients/.../common/config/TopicConfig.java:157-158
- pass 2: ✅ clients/.../common/config/TopicConfig.java:76-78; introduction.md:81

### C0240 · k-retention · L1 · li
> cleanup.policy: delete (default), compact (latest per key), or both.
- pass 1: ✅ clients/.../common/config/TopicConfig.java:156-162
- pass 2: ✅ clients/.../common/config/TopicConfig.java:156-160

### C0241 · k-retention · L1 · li
> Defaults: retention.ms 7 days, retention.bytes -1 (unlimited, and per partition).
- pass 1: ✅ storage/.../log/LogConfig.java:128 + ServerLogConfigs.java:81 + clients/.../common/config/TopicConfig.java:70
- pass 2: ✅ storage/.../log/LogConfig.java:128; server-common/.../ServerLogConfigs.java:81; clients/.../common/config/TopicConfig.java:70

### C0242 · k-retention · L1 · li
> Deletion is by whole segment, judged by the segment's newest timestamp, so data can outlive retention.ms.
- pass 1: ✅ docs/implementation/log.md:72 — "largest timestamp in a segment file … defining the retention time for the entire segment"
- pass 2: ✅ docs/implementation/log.md:72

### C0243 · k-retention · L1 · li
> A consumer that falls behind retention gets out-of-range and follows auto.offset.reset. latest silently skips.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:172-175 + Errors.java:182
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:172-175; docs/implementation/log.md:47

### C0244 · k-retention · L1 · li
> Size a topic: throughput × retention × RF, plus headroom.
- pass 1: n/a (sizing rule of thumb (opinion/arithmetic))
- pass 2: n/a heuristic (sizing rule of thumb)
