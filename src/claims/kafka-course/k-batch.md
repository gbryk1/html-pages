# Claims ledger — kafka-course.html — k-batch

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0294 · k-batch · L2 · p
> Kafka never really stores "a message". It stores record batches. Even a single send() becomes a batch of one. In archive terms, a batch is a sealed envelope delivered to the ledger office: the clerk stamps the envelope once (offset, checksum, producer id) instead of stamping every letter inside. This is the "message set" idea from the design docs. Group messages so that network round-trips, disk writes and checksums are paid per batch, not per record.
- pass 1: ✅ docs/implementation/message-format.md:29 — "always written in batches… a record batch containing a single record" (envelope = analogy)
- pass 2: ✅ docs/implementation/message-format.md:29 — "Messages (aka Records) are always written in batches ... a record batch containing a single record"; docs/design/design.md:86 — "'message set' abstraction that naturally groups messages together"

### C0295 · k-batch · L2 · p
> The same bytes travel producer → broker → disk → consumer unchanged whenever possible. That's why the format is a stable, versioned interface: the current magic value is 2 (introduced in Kafka 0.11; older formats are documented only for history). Here is the envelope, field by field. Click any field.
- pass 1: ✅ message-format.md:39,119; design.md:90 — "current magic value is 2"; "Prior to Kafka 0.11… message sets"
- pass 2: ✅ docs/implementation/log.md:31 — "binary format for records is versioned and maintained as a standard interface"; message-format.md:39 "current magic value is 2"; :119 "Prior to Kafka 0.11, messages were ... stored in message sets"

### C0296 · k-batch · L2 · figcaption
> Field names, types and the 61-byte header size come from the v2 format. Record sizes and the compression ratio are illustrative: real ratios depend entirely on your data and batch size.
- pass 1: ✅ clients/.../record/internal/DefaultRecordBatch.java:104-131 — RECORD_BATCH_OVERHEAD = RECORDS_OFFSET = 61 (sizes labelled illustrative)
- pass 2: ✅ clients/.../record/internal/DefaultRecordBatch.java:131 RECORD_BATCH_OVERHEAD = RECORDS_OFFSET; field sum = 61 (python: 8+4+4+1+4+2+4+8+8+8+2+4+4 → 61); sizes labelled illustrative

### C0297 · k-batch · L2 · h3
> Records inside the envelope: deltas and varints
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0298 · k-batch · L2 · p
> Each record inside the batch is tiny on purpose. It has a varint length, one unused attributes byte, a timestampDelta (varlong) and an offsetDelta (varint), both relative to the batch's base values, then key, value and headers, each prefixed by a varint length. Varints use the same encoding as Protobuf, so small numbers cost one byte. A record whose offset is baseOffset + 3 and that was produced 2 ms after the first costs one byte for each delta instead of 8 + 8.
- pass 1: ✅ message-format.md:90-101,115 — "timestampDelta: varlong / offsetDelta: varint"; "same varint encoding as Protobuf"
- pass 2: ✅ docs/implementation/message-format.md:90-115 — "attributes: int8 bit 0~7: unused ... timestampDelta: varlong offsetDelta: varint"; "same varint encoding as Protobuf" (zigzag 3→6, 2→4: one byte each)

### C0299 · k-batch · L2 · p
> Headers (key/value pairs of metadata, like tracing ids) are part of every record. Their key is never null, the value may be null, and their order is preserved from producer to consumer.
- pass 1: ✅ message-format.md:113 — "key… guaranteed to be non-null, while the value… may be null. The order of headers… is preserved"
- pass 2: ✅ docs/implementation/message-format.md:113 — "key of a record header is guaranteed to be non-null ... order of headers in a record is preserved"

### C0300 · k-batch · L2 · p
> The producer doesn't know the final offsets, because the leader assigns them. Yet the batch has a baseOffset field and every record carries an offsetDelta. How can the broker assign offsets to a compressed batch without decompressing and recompressing it?
- pass 1: n/a (brain-power question)
- pass 2: n/a — think-first question

### C0301 · k-batch · L2 · summary
> Think, then open
- pass 1: n/a (summary label)
- pass 2: n/a — UI label

### C0302 · k-batch · L2 · p
> Only baseOffset (in the uncompressed header) changes. The deltas inside stay valid because they are relative. The broker writes a new base offset in the header and recomputes nothing inside. It doesn't even recompute the CRC for this, because the CRC covers the bytes from attributes onward, and baseOffset, batchLength and partitionLeaderEpoch sit before it. That's exactly why partitionLeaderEpoch was placed outside the CRC: the leader stamps it on every batch it receives.
- pass 1: ✅ message-format.md:67; storage/.../log/LogValidator.java:272-283 — "partition leader epoch field is not included in the CRC"; in-place assignment when codecs match
- pass 2: ✅ LogValidator.java:370-380 in-place path: firstBatch.setLastOffset(...) + setPartitionLeaderEpoch, compressed payload "written as is"; message-format.md:67 — "CRC covers the data from the attributes to the end ... partition leader epoch field is not included"

### C0303 · k-batch · L2 · h3
> Compression: end-to-end and batch-level
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0304 · k-batch · L2 · p
> Compressing one message at a time compresses badly: the redundancy is between messages (the same JSON field names, the same user agents). So Kafka compresses the records section of a whole batch. The codec is recorded in bits 0–2 of attributes: 0 none, 1 gzip, 2 snappy, 3 lz4, 4 zstd. The compressed data follows directly after recordsCount.
- pass 1: ✅ design.md:115-117; message-format.md:42-47,63 — "redundancy is due to repetition between messages"; codec bits 0~2; "serialized directly following the count"
- pass 2: ✅ docs/design/design.md:115 — "redundancy is due to repetition between messages of the same type (e.g. field names in JSON or user agents"; message-format.md:42-47,63 codec bits and "compressed record data is serialized directly following the count"

### C0305 · k-batch · L2 · p
> And it stays compressed. The design docs describe the path: the producer compresses; the broker decompresses only to validate (for example, that the record count matches the header); the batch is written to disk still compressed; it is sent to consumers compressed; the consumer decompresses. Compression CPU is paid at the edges, and every broker hop, disk byte and replica fetch benefits.
- pass 1: ✅ design.md:117 — "broker decompresses the batch in order to validate it… written to disk in compressed form… consumer decompresses"
- pass 2: ✅ docs/design/design.md:117 — "broker decompresses the batch in order to validate it ... remain compressed in the log ... consumer decompresses"

### C0306 · k-batch · L2 · tr
> compression.type (producer) | none | Codec for batches this producer creates: none, gzip, snappy, lz4, zstd | Almost always worth turning on for text/JSON; combine with batching (linger.ms, batch.size) for better ratios
- pass 1: ✅ clients/.../producer/ProducerConfig.java:235-237,397 — "The default is none… Compression is of full batches" (use-when = advice)
- pass 2: ✅ ProducerConfig.java:397 default CompressionType.NONE.name, in(enumOptions(CompressionType)) = none, gzip, snappy, lz4, zstd (advice = opinion)

### C0307 · k-batch · L2 · tr
> compression.type (topic / broker) | producer | Final codec on disk. producer keeps whatever the producer used; uncompressed or a codec name forces that one | Leave at producer unless you must enforce a codec on disk
- pass 1: ✅ ServerLogConfigs.java:178; TopicConfig.java:191-194 — "'uncompressed'… 'producer' which means retain the original compression codec"
- pass 2: ✅ ServerLogConfigs.java:178 COMPRESSION_TYPE_DEFAULT = PRODUCER; :191-194 — "accepts 'uncompressed' ... and 'producer' which means retain the original compression codec"

### C0308 · k-batch · L2 · tr
> compression.gzip.level | gzip default (−1) | gzip level 1–9 (or −1 for gzip's default) | Trading CPU for ratio with gzip
- pass 1: ✅ clients/.../record/internal/CompressionType.java:34-36,59-62 — Deflater BEST_SPEED..BEST_COMPRESSION or DEFAULT_COMPRESSION (-1)
- pass 2: ✅ record/internal/CompressionType.java:34-36,59-60 MIN=Deflater.BEST_SPEED(1), MAX=BEST_COMPRESSION(9), DEFAULT=Deflater.DEFAULT_COMPRESSION(-1)

### C0309 · k-batch · L2 · tr
> compression.lz4.level | 9 | lz4 level 1–17 | Tuning lz4
- pass 1: ✅ CompressionType.java:75-77 — "MIN_LEVEL = 1; MAX_LEVEL = 17; DEFAULT_LEVEL = 9"
- pass 2: ✅ record/internal/CompressionType.java:75-77 — "MIN_LEVEL = 1; MAX_LEVEL = 17; DEFAULT_LEVEL = 9"

### C0310 · k-batch · L2 · tr
> compression.zstd.level | 3 | zstd level (up to 22) | Tuning zstd; higher = smaller, slower
- pass 1: ✅ CompressionType.java:105-109 — "MAX_LEVEL = 22… DEFAULT_LEVEL = 3" (smaller/slower = general codec behaviour)
- pass 2: ✅ record/internal/CompressionType.java:107-109 — "MAX_LEVEL = 22 ... DEFAULT_LEVEL = 3" (min is negative, -131072)

### C0311 · k-batch · L2 · p
> The level configs exist both on the producer and as topic configs.
- pass 1: ✅ ProducerConfig.java:398-399; LogConfig.java compression.*.level defines — level configs on both
- pass 2: ✅ TopicConfig.java:197-201 and ProducerConfig.java:240-248 both define compression.{gzip,lz4,zstd}.level

### C0312 · k-batch · L2 · p
> A topic codec that differs from the producer's codec makes the broker recompress every batch. The broker can only assign offsets "in place" when the source and target codecs match. Otherwise it decompresses, validates and rebuilds the batch with the topic's codec, which costs CPU on the leader for every produce request. If you set compression.type=zstd on a topic, make the producers send zstd too, or keep the topic at producer.
- pass 1: ✅ storage/.../log/LogValidator.java:272-283,366-367 — "Source and target compression codec are different" → buildRecordsAndAssignOffsets
- pass 2: ✅ LogValidator.java:282-283 — "No in place assignment situation 1: sourceCompressionType == targetCompression.type()"; :366-367 else buildRecordsAndAssignOffsets

### C0313 · k-batch · L2 · div
> Single recordI'm small and simple. Why do I have to wait in an envelope with strangers?
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue

### C0314 · k-batch · L2 · div
> BatchBecause you share my stamp. One CRC, one producer id, one base timestamp, one network round-trip for all of you. Alone, your header would be bigger than you.
- pass 1: n/a (fireside dialogue; per-batch fields ✅ message-format.md:35-60)
- pass 2: n/a — dialogue (per-batch CRC/producerId/baseTimestamp consistent with message-format.md:36-59)

### C0315 · k-batch · L2 · div
> Single recordAnd compression?
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue

### C0316 · k-batch · L2 · div
> BatchCompressed alone, you look like noise. Compressed with 500 siblings who share your field names, you shrink a lot. That's why linger.ms and batch.size affect disk usage too, not just latency.
- pass 1: n/a (fireside dialogue; ✅ ProducerConfig.java:237 "more batching means better compression")
- pass 2: n/a — dialogue (consistent with design.md:115)

### C0317 · k-batch · L2 · p
> Produce the same 20 000 records twice, once uncompressed and once with zstd, into two topics. Compare the directory sizes and look at the compresscodec field in the dump. Predict first: which one is smaller, and does the consumer need any config to read the compressed topic?
- pass 1: n/a (exercise prompt)
- pass 2: n/a — exercise prompt

### C0318 · k-batch · L2 · pre
> for c in none zstd; do bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic comp-$c --partitions 1 bin/kafka-producer-perf-test.sh --topic comp-$c --num-records 20000 --record-size 200 \ --throughput -1 --bootstrap-server localhost:9092 \ --command-property compression.type=$c linger.ms=20 done du -sh /tmp/kraft-combined-logs/comp-*-0 bin/kafka-dump-log.sh --files /tmp/kraft-combined-logs/comp-zstd-0/00000000000000000000.log | head -3
- pass 1: ✅ tools/.../ProducerPerformance.java --command-property (nargs +) / --bootstrap-server; TopicCommand.java --create --topic --partitions
- pass 2: ✅ ProducerPerformance.java:240-342 flags --topic --num-records --record-size --throughput --bootstrap-server --command-property (nargs +, example "linger.ms=10 batch.size=32768"); compression.type=none valid (ProducerConfig.java:397)

### C0319 · k-batch · L2 · p
> The perf tool's default payload is random uppercase letters (A–Z), so zstd shrinks it but far less than real JSON with repeated field names. Measure your own data, don't assume. Whatever the ratio, compresscodec: zstd appears on each batch line, and the consumer needs no configuration: the codec travels in the batch attributes, and the client decompresses automatically. compression.type is a producer (and topic) setting only.
- pass 1: ✅ tools/.../ProducerPerformance.java:170-176 — "(byte) (random.nextInt(26) + 65)"; message-format.md:42-47 codec in attributes
- pass 2: ✅ ProducerPerformance.java:176 — "payload[j] = (byte) (random.nextInt(26) + 65)"; DumpLogSegments.java:531 compresscodec; design.md:117 "consumer decompresses any compressed data"

### C0320 · k-batch · L3 · summary
> L3🔬 Go deeper: CRC-32C, control batches and what compaction keeps
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0321 · k-batch · L3 · p
> CRC. The checksum is CRC-32C (Castagnoli) over everything from attributes to the end of the batch. Because it sits after magic, a reader must parse the magic byte before it knows how to interpret the bytes in between. Total on-disk size of a batch is batchLength + 12 (the 8-byte base offset plus the 4-byte length field itself).
- pass 1: ✅ message-format.md:65,67 — "CRC-32C (Castagnoli)"; "must parse the magic byte"; "batchLength + 12 bytes"
- pass 2: ✅ docs/implementation/message-format.md:65,67 — "total size ... batchLength + 12 bytes"; "clients must parse the magic byte before deciding"; "CRC-32C (Castagnoli)"

### C0322 · k-batch · L3 · p
> Attributes bits. 0–2 codec, 3 timestamp type (CreateTime vs LogAppendTime), 4 isTransactional, 5 isControlBatch, 6 hasDeleteHorizonMs. A control batch holds a single control record whose key is (version, type) with type 0 = abort and 1 = commit. These are the transaction markers of Part III, filtered out before your application sees them.
- pass 1: ✅ message-format.md:42-51,75-82 — attribute bits; "0 indicates an abort marker, 1 indicates a commit"
- pass 2: ✅ docs/implementation/message-format.md:42-51,75-81 — "type: int16 (0 indicates an abort marker, 1 indicates a commit)"; "Control records should not be passed on to applications"

### C0323 · k-batch · L3 · p
> Compaction keeps the envelope. When the cleaner removes records, it preserves the first and last offset and sequence numbers of the original batch, even if that leaves an empty batch. That's needed so a producer's last sequence number survives and idempotence (Chapter 12) keeps working after a leader change. The baseTimestamp is not preserved; with bit 6 set it is reused as the delete horizon for tombstones and aborted-transaction markers.
- pass 1: ✅ message-format.md:69-71 — "preserve the first and last offset/sequence… empty batches… baseTimestamp field is not preserved"
- pass 2: ✅ docs/implementation/message-format.md:69,71 — "preserve the first and last offset/sequence numbers ... possible to have empty batches"; "baseTimestamp field is not preserved"; delete horizon for null payload/aborted markers

### C0324 · k-batch · L3 · p
> Broker side. LogValidator.validateMessagesAndAssignOffsetsCompressed decides between in-place offset assignment and buildRecordsAndAssignOffsets (rebuild). In-place is impossible when source and target codecs differ or when a magic conversion is needed. The header constants live in DefaultRecordBatch, where RECORD_BATCH_OVERHEAD = 61 bytes.
- pass 1: ✅ LogValidator.java:272-305,366-367; DefaultRecordBatch.java:130-131 — in-place conditions; RECORD_BATCH_OVERHEAD
- pass 2: ✅ LogValidator.java:279-301,366-367 (codec mismatch; firstBatch.magic() != toMagic → not in place); DefaultRecordBatch.java:131 RECORD_BATCH_OVERHEAD (=61)

### C0325 · k-batch · L2 · li
> Everything is a record batch (magic 2): a 61-byte header + records. Offsets, CRC, producer id/epoch/sequence live once per batch.
- pass 1: ✅ message-format.md:35-60; DefaultRecordBatch.java:104-131 — header fields; 61-byte overhead
- pass 2: ✅ message-format.md:36-60; DefaultRecordBatch.java:131

### C0326 · k-batch · L2 · li
> Records use varint lengths and deltas for offset and timestamp. Headers are ordered key/value pairs.
- pass 1: ✅ message-format.md:90-113 — varint/varlong deltas; header order preserved
- pass 2: ✅ message-format.md:90-113

### C0327 · k-batch · L2 · li
> CRC-32C covers attributes → end, so the broker can set baseOffset and partitionLeaderEpoch without recomputing it.
- pass 1: ✅ message-format.md:67 — "CRC covers the data from the attributes to the end… leader epoch field is not included"
- pass 2: ✅ message-format.md:67 — "CRC covers the data from the attributes to the end"; LogValidator.java:376,383 setLastOffset/setPartitionLeaderEpoch

### C0328 · k-batch · L2 · li
> Compression is per batch and end-to-end: producer compresses, broker validates and stores compressed, consumer decompresses.
- pass 1: ✅ design.md:117 — "remain compressed in the log… consumer decompresses"
- pass 2: ✅ docs/design/design.md:117

### C0329 · k-batch · L2 · li
> Topic compression.type=producer (default) avoids broker recompression. A mismatched topic codec forces a rebuild on every produce.
- pass 1: ✅ ServerLogConfigs.java:178; LogValidator.java:283 — default producer; "inPlaceAssignment = sourceCompressionType == targetCompression.type()"
- pass 2: ✅ ServerLogConfigs.java:178; LogValidator.java:283,366-367
