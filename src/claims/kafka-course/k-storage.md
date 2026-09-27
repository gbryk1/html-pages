# Claims ledger — kafka-course.html — k-storage

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0246 · k-storage · L2 · p
> In Part I a partition was one bound ledger book. Time to confess: nobody binds a book that never ends. In the archive, each "book" is really a shelf of thin volumes. Clerks only ever write in the last volume; when it gets thick, they shelve it and start a fresh one. Kafka calls those volumes segments.
- pass 1: n/a (analogy)
- pass 2: n/a — analogy; mechanism matches docs/implementation/log.md:37 — "serial appends which always go to the last file ... rolled over to a fresh file"

### C0247 · k-storage · L2 · p
> On disk, a topic called my-topic with two partitions is simply two directories, my-topic-0 and my-topic-1. Inside each directory the segment files are named after the offset of the first record they contain, zero-padded to 20 digits, so the first one is always 00000000000000000000.log. That file name is the segment's base offset.
- pass 1: ✅ docs/implementation/log.md:29 — "two directories (namely `my-topic-0` and `my-topic-1`)… 00000000000000000000.log"
- pass 2: ✅ docs/implementation/log.md:29 — "two directories (namely my-topic-0 and my-topic-1) ... first file created will be 00000000000000000000.log"; LogFileUtils.java:104 setMinimumIntegerDigits(20)

### C0248 · k-storage · L2 · p
> A segment is not one file but a small family that shares the same base-offset name:
- pass 1: ✅ storage/.../internals/log/LogFileUtils.java:27-52 — ".log", ".index", ".timeindex", ".txnindex", ".snapshot" suffixes
- pass 2: ✅ storage/.../log/LogFileUtils.java:27-52 — suffixes .log, .index, .timeindex, .txnindex, .snapshot all from filenamePrefixFromOffset(offset)

### C0249 · k-storage · L2 · tr
> .log | The record batches, in the same binary format the producer sent (the broker stamps offsets and leader epoch; Chapter 8) | The data. Appends only ever go to the newest (active) segment
- pass 1: ✅ revised after pass-2 finding — C0249 — .log row: "byte-for-byte in the same format" → "in the same binary format the producer sent (the broker stamps offsets and leader epoch)" — docs/implementation/message-format.md:67 "partition leader epoch… assigned for every batch that is received by t
- pass 2: ✅ DefaultRecordBatch format; broker assigns offsets/partitionLeaderEpoch on append (holds with default compression.type=producer; recompression/LogAppendTime would alter it)

### C0250 · k-storage · L2 · tr
> .index | Sparse map: offset → byte position in the .log. 8-byte entries (4-byte relative offset + 4-byte position) | Jump close to an offset without scanning the whole file
- pass 1: ✅ storage/.../internals/log/OffsetIndex.java:30-45,56 — "4 byte relative offset and a 4 byte file location"; ENTRY_SIZE = 8
- pass 2: ✅ OffsetIndex.java:56 ENTRY_SIZE = 8; LogSegment.java:57 — "OffsetIndex that maps from logical offsets to physical file positions"; 4-byte relative offset + 4-byte position (OffsetIndex.java:204-208)

### C0251 · k-storage · L2 · tr
> .timeindex | Sparse map: timestamp → offset. 12-byte entries (8-byte timestamp + 4-byte relative offset) | "Give me the first record after 09:00" (offsetsForTimes, time-based retention)
- pass 1: ✅ storage/.../internals/log/TimeIndex.java:34-36,56 — "8 bytes timestamp and a 4 bytes relative offset"; ENTRY_SIZE = 12
- pass 2: ✅ TimeIndex.java:35 — "a 8 bytes timestamp and a 4 bytes 'relative' offset"; ENTRY_SIZE = 12 (TimeIndex.java:56)

### C0252 · k-storage · L2 · tr
> .txnindex | Aborted transactions that touch this segment | Lets read_committed consumers skip aborted data (Part III)
- pass 1: ✅ storage/.../internals/log/TransactionIndex.java (class doc) — "metadata about the aborted transactions for each segment… READ_COMMITTED"
- pass 2: ✅ storage/.../log/TransactionIndex.java (aborted-txn index per segment, used to collect aborted txns for READ_COMMITTED fetches)

### C0253 · k-storage · L2 · tr
> .snapshot | Producer-state snapshot (producer ids, epochs, last sequences), taken when a segment rolls and named after the new segment's base offset | Rebuild idempotence state quickly after restart (Chapter 12)
- pass 1: ✅ storage/.../internals/log/UnifiedLog.java:2205-2215 — "Take a snapshot of the producer state… align with the new segment offset"
- pass 2: ✅ UnifiedLog.java:2206-2215 — "useful to have the snapshot offset align with the new segment offset"; takeSnapshot on roll; LogFileUtils.java:92 name = offset + .snapshot

### C0254 · k-storage · L2 · p
> Next to them live a couple of per-partition files such as leader-epoch-checkpoint and partition.metadata. You will meet the leader-epoch file again in Part III.
- pass 1: ✅ storage/.../checkpoint/LeaderEpochCheckpointFile.java:45; PartitionMetadataFile.java:39 — "leader-epoch-checkpoint", "partition.metadata"
- pass 2: ✅ LeaderEpochCheckpointFile.java:45 "leader-epoch-checkpoint"; PartitionMetadataFile.java:39 "partition.metadata"

### C0255 · k-storage · L2 · h3
> Rolling: when does a new volume start?
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0256 · k-storage · L2 · p
> The active segment is rolled (closed, and a new one started) when it would grow past segment.bytes (default 1 GiB), when it is older than segment.ms (default 7 days), or when one of its index files is full. Why care? Because retention and compaction work one whole segment at a time. Time-based retention looks at the largest timestamp in a segment. If a topic has retention.ms of one hour but keeps writing into the same 1 GiB segment for a day, that segment's newest record is always fresh, so its oldest records stay on disk far longer than an hour. Records leave in whole volumes, never one line at a time.
- pass 1: ✅ storage/.../internals/log/LogSegment.java:167-171; LogConfig.java:125-126; docs/implementation/log.md:72 — "largest timestamp in a segment file… defining the retention time for the entire segment"
- pass 2: ✅ LogSegment.java:167-172 shouldRoll (bytes, segment.ms, index isFull); LogConfig.java:125-126 defaults 1 GiB / 7 d; docs/implementation/log.md:78 — "the largest timestamp in a segment file ... defining the retention time for the entire segment"

### C0257 · k-storage · L2 · h3
> Reading: three hops to any offset
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0258 · k-storage · L2 · p
> A consumer says "give me offset 2 600". The broker does three cheap hops. 1. Pick the segment: segments are kept in a sorted map keyed by base offset, and the broker takes the floor entry (the largest base offset ≤ 2 600). 2. Look in that segment's .index (memory-mapped, binary-searched) for the largest indexed offset ≤ 2 600, which gives a byte position. 3. Read the .log forward from that position until it meets the batch containing 2 600. Try it:
- pass 1: ✅ storage/.../internals/log/LogSegments.java:42,210-221; OffsetIndex.java:34-36 — "segments.floorEntry(offset)"; "greatest offset less than or equal to the target offset"
- pass 2: ✅ LogSegments.java:42,211 ConcurrentSkipListMap + segments.floorEntry(offset); LogSegment.java:394-396 offsetIndex().lookup(offset) then log.searchForOffsetFromPosition; AbstractIndex.java:72 MappedByteBuffer + binary search

### C0259 · k-storage · L2 · figcaption
> Illustrative numbers: 4 segments, batches of 50 records, one index entry roughly every few batches (in reality: about one entry per index.interval.bytes = 4096 bytes of appended data). Real index entries store offsets relative to the segment's base offset; the figure shows absolute offsets for readability.
- pass 1: n/a (labelled illustrative; index.interval.bytes 4096 ✅ server-common/.../ServerLogConfigs.java:97)
- pass 2: ✅ (numbers labelled illustrative) TopicConfig.java:116 — "we index a message roughly every 4096 bytes"; OffsetIndex.java:51 relative offsets

### C0260 · k-storage · L2 · p
> Notice what the index is not: it is not an entry per record. It is sparse. The broker adds an entry only after roughly index.interval.bytes (default 4096) of data has been appended since the last one. So the index stays tiny and hot in memory, and the forward scan is at most a few kilobytes. That is the whole trick: a binary search over a small array plus a short sequential read.
- pass 1: ✅ storage/.../internals/log/LogSegment.java:270-273; ServerLogConfigs.java:97 — "if (bytesSinceLastIndexEntry > indexIntervalBytes)"; 4096
- pass 2: ✅ LogSegment.java:270-277 — "if (bytesSinceLastIndexEntry > indexIntervalBytes)"; ServerLogConfigs.java:97 default 4096 (gap = >4096 bytes plus at most one batch)

### C0261 · k-storage · L2 · div
> Why store offsets relative to the base offset in the index?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0262 · k-storage · L2 · div
> So they fit in 4 bytes. Absolute offsets are 64-bit numbers; relative ones only need to span one segment. That shrinks each index entry from 12 bytes to 8. It is also why a segment rolls if a new offset can't be expressed relative to its base.
- pass 1: ✅ revised after pass-2 finding — C0262 — "That halves the index size" → "That shrinks each index entry from 12 bytes to 8" — storage/.../log/OffsetIndex.java:56 "ENTRY_SIZE = 8"; :43-45 "relative offsets… only 4 bytes for the offset"
- pass 2: ✅ storage OffsetIndex — entry = 4-byte relative offset + 4-byte position (8 bytes); roll when offset not relative-convertible

### C0263 · k-storage · L2 · div
> If the broker crashes, can a half-written index corrupt my data?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0264 · k-storage · L2 · div
> No. The index files aren't checksummed at all. They're treated as disposable and rebuilt from the .log if needed. The .log itself is protected by a CRC on every batch, and recovery truncates at the last valid batch.
- pass 1: ✅ storage/.../internals/log/OffsetIndex.java:42; docs/implementation/log.md:76 — "No attempt is made to checksum… it is rebuilt"; "truncated to the last valid offset"
- pass 2: ✅ LogSegment.java:478-487 recover() resets offset/time/txn indexes and rebuilds them while batch.ensureValid() checks CRC; docs/implementation/log.md:84 — "log is truncated to the last valid offset"

### C0265 · k-storage · L2 · div
> Does Kafka keep records in the JVM heap to be fast?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0266 · k-storage · L2 · div
> Deliberately not. It writes into the OS page cache and lets the kernel do the caching. More on that below.
- pass 1: ✅ docs/design/design.md:62-64 — "relying on pagecache is superior to maintaining an in-memory cache"
- pass 2: ✅ docs/design/design.md:62 — "using the filesystem and relying on pagecache is superior to maintaining an in-memory cache"

### C0267 · k-storage · L2 · h3
> Why "just files" is fast: page cache, sequential I/O, zero-copy
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0268 · k-storage · L2 · p
> Kafka's design notes are blunt about it: "don't fear the filesystem". Linear reads and writes are the pattern disks and operating systems handle best (the docs quote about 600 MB/s linear versus about 100 kB/s random writes on one old disk array). The OS already spends all free memory on the page cache, so Kafka appends into that cache and doesn't build its own heap cache. That avoids storing everything twice, avoids GC pressure, and the cache stays warm when the broker process restarts.
- pass 1: ✅ docs/design/design.md:47,51,53,62 — "600MB/sec… only about 100kB/sec"; "cache will stay warm even if the service is restarted"
- pass 2: ✅ docs/design/design.md:47,51,53,62 — "about 600MB/sec but the performance of random writes is only about 100kB/sec"; "storing everything twice"; "cache will stay warm even if the service is restarted"

### C0269 · k-storage · L2 · p
> On the way out, the broker uses sendfile (Java's FileChannel.transferTo): bytes go from page cache to the socket without a round-trip through user space. The usual path is four copies and two system calls; with sendfile only the final copy to the NIC buffer remains. Because the on-disk format equals the wire format, there is nothing to convert. The docs point out that when consumers are caught up, you often see no disk reads at all, because everything is served from cache.
- pass 1: ✅ docs/design/design.md:92,103,107; clients/.../network/PlaintextTransportLayer.java:214 — "four copies and two system calls"; "no read activity on the disks"
- pass 2: ✅ docs/design/design.md:103,107 — "four copies and two system calls ... only the final copy to the NIC buffer is needed"; PlaintextTransportLayer.java:214 fileChannel.transferTo

### C0270 · k-storage · L2 · p
> TLS switches zero-copy off. Encryption happens in user space, so with SSL enabled Kafka can't use sendfile. Every byte is read into the JVM, encrypted and written out. That's the right trade for security, but budget CPU for it and don't compare plaintext benchmarks with TLS production.
- pass 1: ✅ docs/design/design.md:109 — "`sendfile` is not used when SSL is enabled" (CPU/benchmark advice = opinion)
- pass 2: ✅ docs/design/design.md:109 — "TLS/SSL libraries operate at the user space ... sendfile is not used when SSL is enabled"

### C0271 · k-storage · L2 · p
> Lagging consumers evict the cache. A consumer re-reading last week's data pulls cold segments from disk into page cache and can push out the hot tail that everyone else is reading. If "one replay job slowed down everyone", this is usually why.
- pass 1: n/a (opinion / operational heuristic, phrased as "usually")
- pass 2: ✅ docs/operations/tiered-storage.md:31 — "Tail reads leverage OS's page cache ... Older data is typically read from the disk" (eviction = general page-cache behaviour; 'usually why' phrased as heuristic)

### C0272 · k-storage · L2 · h3
> The fsync question
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0273 · k-storage · L2 · p
> By default Kafka never forces an fsync on a schedule: flush.messages and flush.ms both default to Long.MAX_VALUE. An acknowledged write may sit only in page cache. Is that reckless? No. Kafka gets durability from replication, not from the disk of one machine. A write acknowledged with acks=all is in the page cache of every in-sync replica, and a crashed replica must fully re-sync before it may rejoin the ISR (Chapter 10). The design notes put the cost of fsync-per-write at "two to three orders of magnitude" of throughput. The config docs literally say "we recommend you not set this".
- pass 1: ✅ ServerLogConfigs.java:101,109; clients/.../config/TopicConfig.java:51-56; design.md:335 — "Long.MAX_VALUE"; "recommend you not set this and use replication"; "two to three orders of magnitude"
- pass 2: ✅ ServerLogConfigs.java:101,109 both Long.MAX_VALUE; design.md:335 — "reduce performance by two to three orders of magnitude ... it must fully re-sync"; TopicConfig.java:54 — "we recommend you not set this"

### C0274 · k-storage · L2 · tr
> segment.bytes / log.segment.bytes | 1073741824 (1 GiB) | Max size of one segment file | Smaller for compacted or short-retention topics so old data becomes eligible sooner
- pass 1: ✅ storage/.../internals/log/LogConfig.java:125,150,188; TopicConfig.java:32-34 — "DEFAULT_SEGMENT_BYTES = 1024 * 1024 * 1024" (use-when = advice)
- pass 2: ✅ LogConfig.java:125,188 DEFAULT_SEGMENT_BYTES = 1024*1024*1024; TopicConfig.java:33 — "Retention and cleaning is always done a file at a time"

### C0275 · k-storage · L2 · tr
> segment.ms / log.roll.hours | 604800000 (7 d) / 168 | Force a roll after this age even if not full | Low-volume topics with short retention or compaction lag goals
- pass 1: ✅ storage/.../internals/log/LogConfig.java:126,153; TopicConfig.java:37-39 — "24 * 7 * 60 * 60 * 1000L"; "force the log to roll even if the segment file isn't full"
- pass 2: ✅ LogConfig.java:126,153 DEFAULT_SEGMENT_MS = 24*7*60*60*1000L; log.roll.hours = 168; TopicConfig.java:37 — "force the log to roll even if the segment file isn't full"

### C0276 · k-storage · L2 · tr
> segment.jitter.ms | 0 | Random jitter subtracted from the roll time | Many partitions created together that would all roll at once
- pass 1: ✅ storage/.../internals/log/LogConfig.java:127; TopicConfig.java:42-43 — "random jitter subtracted… avoid thundering herds of segment rolling"
- pass 2: ✅ LogConfig.java:127 DEFAULT_SEGMENT_JITTER_MS = 0; TopicConfig.java:42 — "maximum random jitter subtracted from the scheduled segment roll time"

### C0277 · k-storage · L2 · tr
> index.interval.bytes | 4096 | Bytes appended between offset-index entries | Almost never. Smaller means bigger index and shorter scans
- pass 1: ✅ ServerLogConfigs.java:97; TopicConfig.java:114-118 — "More frequent indexing… larger index files. You probably don't need to change this"
- pass 2: ✅ ServerLogConfigs.java:97 = 4096; TopicConfig.java:114 — "More frequent indexing allows reads to jump closer ... but results in larger index files"

### C0278 · k-storage · L2 · tr
> segment.index.bytes | 10485760 (10 MiB) | Preallocated max size of each index; full index forces a roll | Rarely. "You generally should not need to change this"
- pass 1: ✅ ServerLogConfigs.java:93; TopicConfig.java:46-48; LogSegment.java:170 — "We preallocate this index file"; "You generally should not need to change this"
- pass 2: ✅ ServerLogConfigs.java:93 = 10*1024*1024; TopicConfig.java:46 — "We preallocate this index file ... You generally should not need to change this setting."

### C0279 · k-storage · L2 · tr
> flush.messages / flush.ms | Long.MAX_VALUE | Force fsync every N messages / N ms | Single-replica setups that must survive power loss (accept the throughput hit)
- pass 1: ✅ ServerLogConfigs.java:101,109; TopicConfig.java:51-64 — "force an fsync… after every five messages" (use-when = advice)
- pass 2: ✅ ServerLogConfigs.java:101,109 Long.MAX_VALUE; TopicConfig.java:51 — "force an fsync of data written to the log" (when-to-use = advice)

### C0280 · k-storage · L2 · tr
> preallocate | false | Preallocate the file on disk when creating a segment | Specific filesystems where preallocation helps
- pass 1: ✅ storage/.../internals/log/LogConfig.java:134; TopicConfig.java:205 — "preallocate the file on disk when creating a new log segment" (use-when = advice)
- pass 2: ✅ LogConfig.java:134 DEFAULT_PREALLOCATE = false; TopicConfig.java:205 — "preallocate the file on disk when creating a new log segment"

### C0281 · k-storage · L2 · p
> On a local 4.3 quickstart node (its log.dirs is /tmp/kraft-combined-logs), create a topic with tiny 1 MiB segments, write some data and look at the files. How many segment families do you expect after ~3 MB of data? What does kafka-dump-log.sh print per batch?
- pass 1: ✅ config/server.properties:73 — "log.dirs=/tmp/kraft-combined-logs" (rest = exercise prompt)
- pass 2: ✅ config/server.properties:73 — "log.dirs=/tmp/kraft-combined-logs"

### C0282 · k-storage · L2 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic seg-demo \ --partitions 1 --config segment.bytes=1048576 bin/kafka-producer-perf-test.sh --topic seg-demo --num-records 30000 --record-size 100 \ --throughput -1 --bootstrap-server localhost:9092 ls -l /tmp/kraft-combined-logs/seg-demo-0/ bin/kafka-dump-log.sh --files /tmp/kraft-combined-logs/seg-demo-0/00000000000000000000.log | head bin/kafka-dump-log.sh --files /tmp/kraft-combined-logs/seg-demo-0/00000000000000000000.index | head
- pass 1: ✅ tools/.../TopicCommand.java accepts create/topic/partitions/config; tools/.../ProducerPerformance.java --num-records --record-size --throughput --bootstrap-server; bin/kafka-dump-log.sh
- pass 2: ✅ ProducerPerformance.java:240-307 flags --bootstrap-server --topic --num-records --record-size --throughput; DumpLogSegments --files; segment.bytes min 1 MiB (LogConfig.java:188 atLeast(1024*1024))

### C0283 · k-storage · L2 · p
> About 3 MB of payload plus batch overhead means roughly three or four .log files, each with its own .index and .timeindex (1 MiB is the minimum allowed segment.bytes). Each file name is the base offset of the first record inside. The .log dump prints one line per batch, like baseOffset: … lastOffset: … count: … baseSequence: … lastSequence: … producerId: … producerEpoch: … partitionLeaderEpoch: … isTransactional: … isControl: … deleteHorizonMs: … position: … CreateTime: … size: … magic: 2 compresscodec: none crc: … isvalid: true. The .index dump prints offset: … position: … pairs. An entry is added once more than 4 096 bytes have accumulated since the last one, so with the perf test's full ~16 KiB batches (default batch.size 16384) you'll see roughly one entry per batch. That's the sparse index you just animated.
- pass 1: ✅ revised after pass-2 finding — C0283 — dump line now lists lastSequence, isControl, deleteHorizonMs; index note now "one entry once >4096 bytes accumulated → roughly one per ~16 KiB perf-test batch" — tools/.../DumpLogSegments.java:512-533; storage/.../log/LogSegment.java:270-273 "if (bytes
- pass 2: ✅ LogConfig.java:188 "atLeast(1024 * 1024)"; LogSegment.java:270 "bytesSinceLastIndexEntry > indexIntervalBytes"; DumpLogSegments.java:512-533 batch line fields match

### C0284 · k-storage · L3 · summary
> L3🔬 Go deeper: the classes behind a partition directory
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0285 · k-storage · L3 · p
> UnifiedLog (storage module) is the per-partition log object that the broker's ReplicaManager/Partition talk to. It owns a LocalLog, which holds LogSegments: a ConcurrentSkipListMap<Long, LogSegment> keyed by base offset. The "pick a segment" hop is literally floorEntry(offset) on that map. Readers get a consistent view while retention deletes segments. The docs describe this as a copy-on-write style segment list so a binary search can run on a stable snapshot.
- pass 1: ✅ storage/.../internals/log/LogSegments.java:42,210-221; docs/implementation/log.md:72 — "new ConcurrentSkipListMap<>()"; floorEntry; "copy-on-write style segment list"
- pass 2: ✅ UnifiedLog.java:128 LocalLog localLog; LocalLog.java:80 LogSegments; LogSegments.java:42 ConcurrentSkipListMap, :211 floorEntry; docs/implementation/log.md:78 — "copy-on-write style segment list ... binary search to proceed on an immutable static snapshot"

### C0286 · k-storage · L3 · p
> Each LogSegment owns a FileRecords (the .log), a lazily-loaded OffsetIndex and TimeIndex (both extend AbstractIndex and are memory-mapped with a MappedByteBuffer) and a TransactionIndex. In LogSegment.append, the segment keeps a bytesSinceLastIndexEntry counter. Once it exceeds indexIntervalBytes, it appends (batch.lastOffset, physicalPosition) to the offset index and maybe a time-index entry. Index entries point at batches, not individual records.
- pass 1: ✅ storage/.../internals/log/LogSegment.java:102,270-273; AbstractIndex.java:31,72; LazyIndex.java — "offsetIndex().append(batchLastOffset, physicalPosition)"; MappedByteBuffer
- pass 2: ✅ LogSegment.java:79-82 FileRecords, LazyIndex<OffsetIndex>, LazyIndex<TimeIndex>, TransactionIndex; :270-273 offsetIndex().append(batchLastOffset, physicalPosition) + timeIndex().maybeAppend; AbstractIndex.java:72 MappedByteBuffer

### C0287 · k-storage · L3 · p
> LogSegment.shouldRoll returns true when the segment would exceed maxSegmentBytes, when a non-empty segment has waited longer than segment.ms − rollJitterMs, when the offset or time index is full, or when the new batch's max offset can't be converted to a relative offset. On the network side, PlaintextTransportLayer.transferFrom calls fileChannel.transferTo(position, count, socketChannel). That's the zero-copy path.
- pass 1: ✅ storage/.../internals/log/LogSegment.java:167-171; clients/.../network/PlaintextTransportLayer.java:214 — shouldRoll conditions; "fileChannel.transferTo(position, count, socketChannel)"
- pass 2: ✅ LogSegment.java:168-172 — "maxSegmentMs() - rollJitterMs ... (size > 0 && reachedRollMs) || offsetIndex().isFull() || timeIndex().isFull() || !canConvertToRelativeOffset"; PlaintextTransportLayer.java:214 fileChannel.transferTo(position, count, socketChannel)

### C0288 · k-storage · L3 · p
> On startup, recovery walks the newest segment(s) and validates every batch by size and CRC. On corruption, the log is truncated to the last valid offset. The docs explain why both truncation and "garbage appended" must be handled: the OS doesn't order inode-size updates and data-block writes.
- pass 1: ✅ docs/implementation/log.md:76-78 — "CRC32 of the message payload matches… truncated to the last valid offset"; inode write order
- pass 2: ✅ docs/implementation/log.md:84,86 — "verifies that each message entry is valid ... CRC32"; "OS makes no guarantee of the write order between the file inode and the actual block data"

### C0289 · k-storage · L2 · li
> A partition is a directory of segments; each segment is .log + .index + .timeindex (+ .txnindex, .snapshot), all named by base offset.
- pass 1: ✅ LogFileUtils.java:27-52; docs/implementation/log.md:29 — suffixes; "named with the offset of the first message"
- pass 2: ✅ LogFileUtils.java:27-52 suffixes all named by filenamePrefixFromOffset

### C0290 · k-storage · L2 · li
> Rolls happen at segment.bytes (1 GiB) or segment.ms (7 d) or when an index fills. Retention deletes whole segments, never single records.
- pass 1: ❌ fixed in part file — UnifiedLog.java:1908-1910 rolls active segment so it can be deleted
- pass 2: ✅ LogSegment.java:167-172; docs/implementation/log.md:78 — "Data is deleted one log segment at a time"

### C0291 · k-storage · L2 · li
> Offset lookup = floor segment by base offset → binary search in the sparse, memory-mapped index → short forward scan.
- pass 1: ✅ LogSegments.java:210-221; OffsetIndex.java:34-36 — floorEntry; "binary search variant… memory-map"
- pass 2: ✅ LogSegments.java:211 floorEntry; LogSegment.java:395-396 index lookup + searchForOffsetFromPosition

### C0292 · k-storage · L2 · li
> Speed comes from sequential I/O, the OS page cache and sendfile zero-copy (not available with TLS).
- pass 1: ✅ docs/design/design.md:51,92,109 — linear I/O, pagecache, sendfile; "not used when SSL is enabled"
- pass 2: ✅ docs/design/design.md:51,62,103,109

### C0293 · k-storage · L2 · li
> No fsync by default. Durability comes from replication to the ISR, not from one disk.
- pass 1: ✅ ServerLogConfigs.java:101,109; TopicConfig.java:54 — "use replication for durability"
- pass 2: ✅ ServerLogConfigs.java:101,109; docs/design/design.md:335 — "we do not want to require the use of fsync on every write"
