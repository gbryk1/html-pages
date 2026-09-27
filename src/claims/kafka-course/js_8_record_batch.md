# Claims ledger — kafka-course.html — js_8_record_batch

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1386 · js:8. record batch · L- · script
> baseOffset (int64): offset of the first record in the batch. The leader writes it on append; records only carry deltas.
- pass 1: ✅ message-format.md:36,90-101 — baseOffset int64; records carry offsetDelta
- pass 2: ✅ message-format.md:36; LogValidator.java:376 setLastOffset on append (base offset derived); records carry offsetDelta

### C1387 · js:8. record batch · L- · script
> batchLength (int32): bytes from right after this field to the end of the batch. Total on-disk size = batchLength + 12.
- pass 1: ✅ message-format.md:65 — "batchLength + 12 bytes"
- pass 2: ✅ message-format.md:65 — "total size of a record batch on disk is batchLength + 12 bytes"

### C1388 · js:8. record batch · L- · script
> partitionLeaderEpoch (int32): stamped by the leader on every batch it receives. Deliberately outside the CRC so the broker can set it without recomputing the checksum.
- pass 1: ✅ message-format.md:67 — "not included in the CRC computation… assigned for every batch that is received by the broker"
- pass 2: ✅ message-format.md:67 — "not included in the CRC computation to avoid the need to recompute the CRC when this field is assigned"

### C1389 · js:8. record batch · L- · script
> magic (int8): format version, currently 2. Readers must parse it before interpreting the rest of the header.
- pass 1: ✅ message-format.md:39,67 — "current magic value is 2"; "must parse the magic byte"
- pass 2: ✅ message-format.md:39,67 — "current magic value is 2"; "must parse the magic byte before deciding how to interpret"

### C1390 · js:8. record batch · L- · script
> crc (uint32): CRC-32C over everything from attributes to the end of the batch. Detects corruption on disk and on the wire.
- pass 1: ✅ message-format.md:67 — CRC-32C from attributes to the end
- pass 2: ✅ message-format.md:67; ConsumerConfig.java:294 check.crcs — "ensures no on-the-wire or on-disk corruption"

### C1391 · js:8. record batch · L- · script
> lastOffsetDelta (int32): last record offset minus baseOffset. Lets readers know the batch's offset range without decompressing it.
- pass 1: ✅ message-format.md:53,63 — lastOffsetDelta in the header; compressed data follows the count
- pass 2: ✅ message-format.md:53 lastOffsetDelta in uncompressed header; LogValidator.java:449 batch.lastOffset() read without decompression

### C1392 · js:8. record batch · L- · script
> baseTimestamp (int64): timestamp of the first record; records store a varlong timestampDelta relative to it.
- pass 1: ✅ message-format.md:54,94 — baseTimestamp; timestampDelta varlong
- pass 2: ✅ message-format.md:54,94 baseTimestamp int64, timestampDelta varlong

### C1393 · js:8. record batch · L- · script
> maxTimestamp (int64): largest timestamp in the batch, used by the time index and by time-based retention.
- pass 1: ✅ message-format.md:55; LogSegment.java:262-266; log.md:72 — maxTimestamp → time index; largest timestamp for retention
- pass 2: ✅ message-format.md:55; LogSegment.java:264-272 batch.maxTimestamp() feeds timeIndex; log.md:78 largest timestamp drives retention

### C1394 · js:8. record batch · L- · script
> producerEpoch (int16): fences older incarnations of the same producer id.
- pass 1: ✅ ProducerAppendInfo.java:117-121 — older epoch rejected
- pass 2: ✅ message-format.md:57 producerEpoch int16 (epoch fencing)

### C1395 · js:8. record batch · L- · script
> baseSequence (int32): sequence number of the first record, per producer and partition. It powers duplicate detection.
- pass 1: ✅ message-format.md:58,69 — "base sequence number must be preserved for duplicate checking"
- pass 2: ✅ message-format.md:58,69 — "broker checks incoming Produce requests for duplicates by verifying that the first and last sequence numbers"

### C1396 · js:8. record batch · L- · script
> recordsCount (int32): number of records. The broker checks it matches what it finds when validating a compressed batch.
- pass 1: ✅ design.md:117 — "validates that the number of records in the batch is same as what batch header states"
- pass 2: ✅ docs/design/design.md:117 — "validates that the number of records in the batch is same as what batch header states"

### C1397 · js:8. record batch · L- · script
> records: each record = varint length, attributes, timestampDelta, offsetDelta, key, value, headers. When compressed, this whole section is one compressed blob.
- pass 1: ✅ message-format.md:63,90-101
- pass 2: ✅ message-format.md:63,90-101

### C1398 · js:8. record batch · L- · script
> Header = … bytes, then the records. Click a field, or press ▶.
- pass 1: ✅ DefaultRecordBatch.java:131 — 61 bytes
- pass 2: n/a — UI hint

### C1399 · js:8. record batch · L- · script
> That's the envelope: … header bytes shared by every record inside. The more records per batch, the smaller the per-record overhead, and the better compression works.
- pass 1: n/a (pedagogy; ProducerConfig.java:237 "more batching means better compression")
- pass 2: ✅ mechanism; design.md:115 (compression across messages); header shared per batch

### C1400 · js:8. record batch · L- · script
> Compressed with zstd: attributes bits 0–2 now say 4 (zstd), and the records section shrinks. The header stays uncompressed, so the broker can still read offsets, producer id and CRC without decompressing. Sizes are illustrative.
- pass 1: ✅ message-format.md:42-47,63 — 4 = zstd; header before compressed records
- pass 2: ✅ message-format.md:47 "4: zstd"; header fields uncompressed (compressed data follows recordsCount, :63); sizes labelled illustrative

### C1401 · js:8. record batch · L- · script
> Uncompressed again: codec bits back to 0. Same records, bigger batch.
- pass 1: ✅ message-format.md:43 — "0: no compression"
- pass 2: ✅ message-format.md:43 "0: no compression"

### C1402 · js:8. record batch · L- · script
> A single bit flips inside the records (a bad disk, a faulty NIC buffer)…
- pass 1: n/a (break-it scenario setup)
- pass 2: n/a — simulation narration

### C1403 · js:8. record batch · L- · script
> CRC-32C mismatch. The broker validates batches on append and rejects this one with CORRUPT_MESSAGE. On disk, recovery truncates the log at the last valid batch. Consumers also verify CRCs, so corruption doesn't reach your application silently.
- pass 1: ✅ UnifiedLog.java:1559-1561; Errors.java:184; log.md:76; ConsumerConfig.java:550-552 check.crcs=true
- pass 2: ✅ UnifiedLog.java:1558-1561 — "check the validity of the message by checking CRC ... CorruptRecordException" (→ CORRUPT_MESSAGE, Errors.java:184); log.md:84 truncation; ConsumerConfig.java:552 check.crcs default true
