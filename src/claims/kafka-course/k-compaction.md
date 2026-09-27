# Claims ledger — kafka-course.html — k-compaction

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0534 · k-compaction · L3 · p
> Time-based retention is blunt: after seven days the whole segment goes, whether or not it holds the only copy of a customer's current address. For a stream of state — "account 123 now has email X" — you want something smarter: forget the superseded entries, keep the latest one for every key, forever. That's cleanup.policy=compact.
- pass 1: ✅ docs/design/design.md:395-397; clients/src/main/java/org/apache/kafka/common/config/TopicConfig.java:156-160 — "The "compact" policy will enable log compaction, which retains the latest value for each key"
- pass 2: ✅ docs/design/design.md:370,390 — time retention "old log data is discarded after a fixed period"; compaction retains "at least the last update for each primary key"; log.retention.hours=168 default

### C0535 · k-compaction · L3 · p
> In the ledger archive, a compacted book is one where an archivist periodically recopies old pages, leaving out every line that a later line for the same account has overruled. The crucial detail: the line numbers are copied too. Line 38 stays line 38, even if lines 36 and 37 were dropped. Offsets never change; compaction only creates gaps.
- pass 1: ✅ docs/design/design.md:409 — "the messages in the tail of the log retain the original offset assigned when they were first written"
- pass 2: ✅ docs/design/design.md:407 — "messages in the tail of the log retain the original offset… that never changes" (archivist analogy n/a)

### C0536 · k-compaction · L3 · p
> A consumer's committed offset is 37, but offset 37 was compacted away. Where does its next fetch start? And if a key should be deleted, how do you express that in a log where you can only append?
- pass 1: n/a (brain question)
- pass 2: n/a — brain-teaser question

### C0537 · k-compaction · L3 · h3
> Head, tail and the cleaner
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0538 · k-compaction · L3 · p
> A compacted log has two regions. The tail (older, already cleaned) holds at most one record per key. The head (newer, "dirty") is an ordinary log with dense offsets. The log cleaner — a pool of background threads, log.cleaner.threads, default 1 — picks the log with the highest dirty ratio, builds a compact map of key → last offset for the dirty section, and recopies segments from the beginning, dropping every record whose key appears again later. Cleaned segments are swapped in as it goes, so the extra disk needed is about one segment, not a copy of the log.
- pass 1: ✅ docs/design/design.md:409,429-433; storage/src/main/java/org/apache/kafka/storage/internals/log/CleanerConfig.java:38; storage/src/main/java/org/apache/kafka/storage/internals/log/LogToClean.java:54 — "the additional disk space required is just one additional log segment"
- pass 2: ✅ docs/design/design.md:429-433 — "pool of background threads… chooses the log that has the highest ratio… additional disk space required is just one additional log segment"; CleanerConfig.java:35 LOG_CLEANER_THREADS = 1

### C0539 · k-compaction · L3 · p
> Answer to the brain question, part one: every offset stays a valid position. Reading from a compacted-away offset simply returns the next record that still exists — the docs' example is that a read starting at 36, 37 or 38 all begin at 38.
- pass 1: ✅ docs/design/design.md:409 — "a read beginning at any of these offsets would return a message set beginning with 38"
- pass 2: ✅ docs/design/design.md:407 — "offsets 36, 37, and 38 are all equivalent positions and a read beginning at any of these offsets would return a message set beginning with 38"

### C0540 · k-compaction · L3 · figcaption
> Simplified: the real cleaner only picks a log once its dirty ratio exceeds min.cleanable.dirty.ratio (0.5) or max.compaction.lag.ms forces it — here you run it by hand. Keys, values and timings are illustrative.
- pass 1: ✅ revised after pass-2 finding — C0540 figcaption + C0571 bullet — "reaches" / "≥" → "exceeds" / ">" — storage/src/main/java/org/apache/kafka/storage/internals/log/LogCleanerManager.java:283 "ltc.cleanableRatio() > ltc.log().config().minCleanableRatio"
- pass 2: ✅ LogCleanerManager.java:283 — cleanable if "cleanableRatio() > ...minCleanableRatio" or needCompactionNow (max.compaction.lag.ms); default 0.5 (illustrative parts n/a)

### C0541 · k-compaction · L3 · h3
> Tombstones: deleting in an append-only world
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0542 · k-compaction · L3 · p
> To delete a key you append a record with that key and a null value — a tombstone. The cleaner removes every earlier record for the key. The tombstone itself must linger for a while, or consumers that are rebuilding state would never learn the key was deleted. How long? delete.retention.ms, default 24 hours. In the v2 batch format the cleaner stamps a delete horizon into the batch the first time it sees the tombstone (now + delete.retention.ms) and drops the tombstone on a later pass after that time.
- pass 1: ✅ docs/design/design.md:411,424; clients/src/main/java/org/apache/kafka/common/record/internal/MemoryRecords.java:173-180 — "deleteHorizonMs = filter.currentTime + filter.deleteRetentionMs"
- pass 2: ✅ docs/design/design.md:409 tombstone = key + null payload; LogConfig.java:129 24h; clients/.../record/internal/MemoryRecords.java:172-180 — deleteHorizonMs = filter.currentTime + filter.deleteRetentionMs set when batch has tombstones and no horizon yet; Cleaner.java:516 drops after horizon

### C0543 · k-compaction · L3 · p
> A consumer that lags more than delete.retention.ms can miss deletes. The docs say it plainly: tombstone removal happens concurrently with reads, so a consumer that takes longer than that to reach the head may never see the delete marker — and keeps the deleted key in its cache or database forever. If you rebuild state from a compacted topic, make sure a full replay finishes well within that window, or raise it.
- pass 1: ✅ docs/design/design.md:424 — "it is possible for a consumer to miss delete markers if it lags by more than delete.retention.ms"
- pass 2: ✅ docs/design/design.md:423 — "since the removal of delete markers happens concurrently with reads, it is possible for a consumer to miss delete markers if it lags by more than delete.retention.ms"

### C0544 · k-compaction · L3 · h3
> What compaction promises (and doesn't)
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0545 · k-compaction · L3 · p
> From the design docs: a consumer that keeps up with the head sees every record; ordering is never changed; offsets never change; a consumer reading from the start sees at least the final value of every key. What it does not promise is "exactly one record per key right now". The active segment is never cleaned, nothing past the last stable offset is cleaned, and records younger than min.compaction.lag.ms are protected. So a compacted topic can contain duplicates at any time — your consumer must treat it as "latest wins", never as a unique-key table.
- pass 1: ✅ revised after pass-2 finding — C0545 (compaction promises) — "always contains some duplicates" → "can contain duplicates at any time" — docs/design/design.md:421-424 (guarantee is "at least the final state"), LogCleanerManager.java:727-742
- pass 2: ✅ docs/design/design.md:419-425 — "Ordering of messages is always maintained"; "offset for a message never changes"; "see at least the final state of all records"

### C0546 · k-compaction · L3 · div
> RetentionI just delete whole old segments. Simple, cheap, predictable.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue (retention deletes whole segments: log retention, design.md:370)

### C0547 · k-compaction · L3 · div
> CompactionAnd you throw away the only copy of a customer who hasn't changed in a week. I keep the last word for every key — forever if needed.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue; substance matches design.md:390 "retain at least the last update for each primary key"

### C0548 · k-compaction · L3 · div
> RetentionForever? Your topic never stops growing if keys never repeat.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue; logically correct

### C0549 · k-compaction · L3 · div
> CompactionThat's why people combine us: cleanup.policy=compact,delete. Old segments go by time or size, the survivors get compacted.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/config/TopicConfig.java:160-162 — "old segments will be discarded per the retention time and size configuration, while retained segments will be compacted"
- pass 2: ✅ clients/.../TopicConfig.java:160-162 — "specify both policies… old segments will be discarded per the retention time and size configuration, while retained segments will be compacted"

### C0550 · k-compaction · L3 · tr
> cleanup.policy | delete | compact, delete, both, or empty list (infinite retention) | State/changelog topics: compact
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/config/TopicConfig.java:156-163 — "An empty list means infinite retention"
- pass 2: ✅ TopicConfig.java:156-163 — "delete" is the default; "compact"; both; "An empty list means infinite retention"

### C0551 · k-compaction · L3 · tr
> min.cleanable.dirty.ratio | 0.5 | Dirty share of the log needed before the cleaner picks it | Lower → fewer duplicates, more cleaning I/O
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/config/TopicConfig.java:140-147; storage/src/main/java/org/apache/kafka/storage/internals/log/LogConfig.java:132 — "A higher ratio will mean fewer, more efficient cleanings but will mean more wasted space"
- pass 2: ✅ LogConfig.java:132 DEFAULT_MIN_CLEANABLE_DIRTY_RATIO = 0.5; TopicConfig.java:141-146 "A higher ratio will mean fewer, more efficient cleanings but will mean more wasted space"

### C0552 · k-compaction · L3 · tr
> delete.retention.ms | 86400000 (24 h) | How long tombstones survive after being stamped | Raise if full replays take longer
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogConfig.java:129; clients/src/main/java/org/apache/kafka/common/config/TopicConfig.java:125-130 — "DEFAULT_DELETE_RETENTION_MS = 24 * 60 * 60 * 1000L"
- pass 2: ✅ LogConfig.java:129 — 24*60*60*1000L = 86400000; MemoryRecords.java:180 horizon = time stamped + deleteRetentionMs

### C0553 · k-compaction · L3 · tr
> min.compaction.lag.ms | 0 | Minimum age before a record may be compacted | Consumers need to see every update for a while
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogConfig.java:130; clients/src/main/java/org/apache/kafka/common/config/TopicConfig.java:132-134 — "The minimum time a message will remain uncompacted in the log"
- pass 2: ✅ LogConfig.java:130 DEFAULT_MIN_COMPACTION_LAG_MS = 0; CleanerConfig.java:78 "minimum time a message will remain uncompacted"

### C0554 · k-compaction · L3 · tr
> max.compaction.lag.ms | Long.MAX_VALUE | Maximum time a record stays ineligible for compaction | Low-traffic topics that never reach the dirty ratio (e.g. GDPR deletes must happen)
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogConfig.java:131; docs/design/design.md:457 — "prevent log with low produce rate from remaining ineligible for compaction for an unbounded duration"
- pass 2: ✅ LogConfig.java:131 Long.MAX_VALUE; TopicConfig.java:137 "maximum time a message will remain ineligible for compaction"; design.md:449 "prevent log with low produce rate from remaining ineligible"

### C0555 · k-compaction · L3 · tr
> log.cleaner.threads | 1 | Cleaner threads per broker | Many compacted partitions falling behind
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/CleanerConfig.java:38,60 — "The number of background threads to use for log cleaning"
- pass 2: ✅ CleanerConfig.java:35,65 — 1; "number of background threads to use for log cleaning"

### C0556 · k-compaction · L3 · tr
> log.cleaner.dedupe.buffer.size | 134217728 (128 MiB) | Memory for the key→offset maps, across all cleaner threads | Huge key spaces; each pass can cover more
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/CleanerConfig.java:40,67 — "The total memory used for log deduplication across all cleaner threads"
- pass 2: ✅ CleanerConfig.java:37,67 — 128 * 1024 * 1024L; "total memory used for log deduplication across all cleaner threads"

### C0557 · k-compaction · L3 · tr
> log.cleaner.io.max.bytes.per.second | Double.MAX_VALUE (unthrottled) | Throttle cleaner read+write I/O | Cleaner competes with producers/consumers for disk
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/CleanerConfig.java:39,66 — "sum of its read and write i/o will be less than this value on average"
- pass 2: ✅ CleanerConfig.java:36,66 — Double.MAX_VALUE; "sum of its read and write i/o will be less than this value"

### C0558 · k-compaction · L3 · p
> Create a compacted topic with tiny segments so the cleaner has something to do, write keyed updates and a tombstone, then look at the raw segment. Which commands?
- pass 1: n/a (exercise prompt)
- pass 2: n/a — exercise prompt

### C0559 · k-compaction · L3 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic accounts \ --partitions 1 --config cleanup.policy=compact --config segment.ms=10000 \ --config min.cleanable.dirty.ratio=0.01 --config delete.retention.ms=60000 bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic accounts \ --reader-property parse.key=true --reader-property key.separator=: \ --reader-property null.marker=NULL > A:1 > B:1 > A:2 > B:NULL # tombstone for B bin/kafka-dump-log.sh --files /tmp/kraft-combined-logs/accounts-0/00000000000000000000.log --print-data-log
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/ConsoleProducer.java:240-262; tools/src/main/java/org/apache/kafka/tools/DumpLogSegments.java:814,821; config/server.properties:73 — "reader-property / null.marker= / print-data-log / log.dirs=/tmp/kraft-combined-logs"
- pass 2: ✅ ConsoleProducer.java:262 "reader-property"; LineMessageReader.java:43-50 parse.key, key.separator, null.marker; DumpLogSegments.java:814,821 --print-data-log/--files; config/server.properties:73 log.dirs=/tmp/kraft-combined-logs; segment.ms atLeast(1)

### C0560 · k-compaction · L3 · p
> After a segment roll and a cleaner pass (it sleeps log.cleaner.backoff.ms, 15 s, when idle), the dump shows gaps in offsets and only the latest value per key. The log directory path depends on your log.dirs.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/CleanerConfig.java:44,71 — "The amount of time to sleep when there are no logs to clean"
- pass 2: ✅ CleanerConfig.java:40,71 — LOG_CLEANER_BACKOFF_MS = 15 * 1000; "time to sleep when there are no logs to clean"

### C0561 · k-compaction · L3 · summary
> L3🔬 Go deeper: how the cleaner decides what to keep
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C0562 · k-compaction · L3 · p
> Choosing a log. LogCleanerManager computes a LogToClean per compacted partition with cleanableRatio = cleanableBytes / totalBytes and picks the dirtiest. The cleanable range ends at the first uncleanable offset, the minimum of: the last stable offset, the active segment's base offset, and (if min.compaction.lag.ms > 0) the first segment still inside the lag.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogToClean.java:54; LogCleanerManager.java:727-742 — "the active segment is always uncleanable"
- pass 2: ✅ LogToClean.java:49-54 cleanableRatio = cleanableBytes / totalBytes; LogCleanerManager.java:291 max by cleanableRatio; :731-742 min(lastStableOffset, activeSegment.baseOffset, first segment within min lag)

### C0563 · k-compaction · L3 · p
> The offset map. Cleaner builds a SkimpyOffsetMap (MD5-hashed keys; dedupe.buffer.size split across threads); the docs say each entry takes exactly 24 bytes, so "with 8GB of cleaner buffer one cleaner iteration can clean around 366GB of log head (assuming 1kB messages)". The map is filled up to log.cleaner.io.buffer.load.factor (0.9).
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogCleaner.java:480; docs/design/design.md:434; storage/src/main/java/org/apache/kafka/storage/internals/log/CleanerConfig.java:41,48 — "It uses exactly 24 bytes per entry"
- pass 2: ✅ SkimpyOffsetMap.java:68,83 MD5, bytesPerEntry = hashSize + 8 (=24); LogCleaner.java:480 dedupeBufferSize / numThreads; design.md:436 "366GB of log head"; Cleaner.java:706 slots * dupBufferLoadFactor; CleanerConfig 0.9 (prop log.cleaner.io.buffer.load.factor)

### C0564 · k-compaction · L3 · p
> The retain rule (Cleaner.shouldRetainRecord): keep a record only if its offset ≥ the map's offset for its key, and it has a value or its tombstone isn't past the batch's deleteHorizonMs. Records without a key are invalid in a compacted topic and get dropped.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/Cleaner.java:503-525 — "boolean isRetainedValue = record.hasValue() || shouldRetainDeletes"
- pass 2: ✅ storage/.../Cleaner.java:511-525 — "latestOffsetForKey = record.offset() >= foundOffset"; deleteHorizonMs check; no key → stats.invalidMessage(); return false

### C0565 · k-compaction · L3 · p
> Transactions. The cleaner also removes records of aborted transactions (it collects them from the transaction index) and, piggy-backing on tombstone retention, eventually removes transaction markers — but never before all records of that transaction are gone.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/Cleaner.java:227-232,323-324 — "we will never delete a marker until all the records from that transaction are removed"
- pass 2: ✅ Cleaner.java:231 collectAbortedTransactions; :323-324 "piggy-back on the tombstone retention logic… never delete a marker until all the records from that transaction are removed"

### C0566 · k-compaction · L3 · p
> Metrics. Watch uncleanable-partitions-count, max-clean-time-secs and max-compaction-delay-secs. A partition marked uncleanable after an error stays uncompacted and grows. Note log.cleaner.enable is deprecated since 4.1 and slated for removal in 5.0 — don't set it to false, because __consumer_offsets is compacted too.
- pass 1: ✅ docs/design/design.md:457; storage/src/main/java/org/apache/kafka/storage/internals/log/CleanerConfig.java:53,72-73; LogCleanerManager.java:88 — "deprecated and will be removed in Kafka 5.0 / partitions that have raised an unexpected error during cleaning"
- pass 2: ✅ LogCleaner.java:113-114, LogCleanerManager.java:71 metric names; CleanerConfig.java:48,73 @Deprecated(since="4.1") "will be removed in Kafka 5.0… including the internal offsets topic"

### C0567 · k-compaction · L3 · li
> Compaction keeps at least the latest record per key; offsets and order never change, only gaps appear.
- pass 1: ✅ docs/design/design.md:421-423 — "The offset for a message never changes"
- pass 2: ✅ docs/design/design.md:368,420-421 — "at least the last known value for each message key"; "never re-order"; "offset… never changes"

### C0568 · k-compaction · L3 · li
> The active segment, anything past the LSO and records younger than min.compaction.lag.ms are never cleaned.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/LogCleanerManager.java:727-742 — "We cannot clean past: 1. The active segment 2. The last stable offset"
- pass 2: ✅ LogCleanerManager.java:728-730 — uncleanable: "The active segment", "The last stable offset", segments within "minimum compaction lag time"

### C0569 · k-compaction · L3 · li
> Delete = tombstone (key + null value); tombstones live delete.retention.ms (24 h) after being stamped.
- pass 1: ✅ docs/design/design.md:411; storage/src/main/java/org/apache/kafka/storage/internals/log/LogConfig.java:129 — "A message with a key and a null payload will be treated as a delete"
- pass 2: ✅ design.md:409 tombstone; MemoryRecords.java:180 horizon = stamp time + delete.retention.ms; LogConfig.java:129 24h

### C0570 · k-compaction · L3 · li
> Lagging longer than delete.retention.ms can make a consumer miss deletes.
- pass 1: ✅ docs/design/design.md:424 — "possible for a consumer to miss delete markers if it lags"
- pass 2: ✅ docs/design/design.md:423 — "possible for a consumer to miss delete markers if it lags by more than delete.retention.ms"

### C0571 · k-compaction · L3 · li
> The cleaner runs when the dirty ratio > min.cleanable.dirty.ratio (0.5) or max.compaction.lag.ms forces it.
- pass 1: ✅ revised after pass-2 finding — C0540 figcaption + C0571 bullet — "reaches" / "≥" → "exceeds" / ">" — storage/src/main/java/org/apache/kafka/storage/internals/log/LogCleanerManager.java:283 "ltc.cleanableRatio() > ltc.log().config().minCleanableRatio"
- pass 2: ✅ LogCleanerManager.java:283 — "needCompactionNow() && cleanableBytes() > 0) || cleanableRatio() > minCleanableRatio"

### C0572 · k-compaction · L3 · li
> Consumers of compacted topics must be "latest wins" — duplicates per key are normal.
- pass 1: n/a (practical advice derived from C0545)
- pass 2: ✅ LogCleanerManager.java:728-742 (active segment etc. never cleaned) ⇒ duplicates per key possible; latest-wins advice n/a
