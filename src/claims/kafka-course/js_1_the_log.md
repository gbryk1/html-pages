# Claims ledger — kafka-course.html — js_1_the_log

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1325 · js:1. the log · L- · script
> Readers on P0 (positions = next offset to read): A → … (blue underline) · B → … (green border)
- pass 1: n/a (UI label)
- pass 2: n/a — UI legend

### C1326 · js:1. the log · L- · script
> Topic payments has 3 partitions (3 ledger books). Type a key and append, or run the 8 samples.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1327 · js:1. the log · L- · script
> Type a key first. Records without a key are spread by the sticky partitioner instead (chapter 3).
- pass 1: ✅ clients/.../producer/ProducerConfig.java:319 — "choose the sticky partition"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:317 — default partitioner "send records to a partition until at least batch.size bytes is produced"

### C1328 · js:1. the log · L- · script
> "…" → P…, offset …. murmur2("…") made positive, mod 3 = …. Every record with this key lands in P… as long as the topic keeps 3 partitions.
- pass 1: ✅ BuiltInPartitioner.java:330 (exact JS port, UtilsTest vectors pass)
- pass 2: ✅ BuiltInPartitioner.java:330 — "Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions"

### C1329 · js:1. the log · L- · script
> Append #… "…" → P…, offset …. The offset counts per book, not per topic.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:72 — offset per partition
- pass 2: ✅ docs/implementation/log.md:33 — per-partition counter

### C1330 · js:1. the log · L- · script
> 8 records, 3 books. Both "alice" records sit in P0, both "dave" in P1, both "carol" in P2: same key, same partition, written in order. Offsets restart at 0 in every partition.
- pass 1: ✅ computed with verified murmur2 port: alice→0, dave→1, carol→2 (mod 3)
- pass 2: ✅ verify-tmp/m2.py (port of Utils.murmur2) output: "alice 0", "dave 1", "carol 2" for 3 partitions

### C1331 · js:1. the log · L- · script
> P0 is empty. Append some records first (▶ Append 8 samples).
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1332 · js:1. the log · L- · script
> Both readers reached the end of P0 (offset …). They'd now long-poll for new records. Nothing was deleted.
- pass 1: ✅ docs/design/design.md:145 — "long poll"
- pass 2: n/a — narration (long-poll = fetch.max.wait.ms behaviour; reading deletes nothing per docs/getting-started/introduction.md:81)

### C1333 · js:1. the log · L- · script
> Reader A is at …, reader B at …. Same book, independent positions. A reads two lines per step, B one. Reading removes nothing, so B will still see every record A saw.
- pass 1: ✅ docs/getting-started/introduction.md:81 — "not deleted after consumption"
- pass 2: ✅ docs/getting-started/introduction.md:81 — "events are not deleted after consumption"; independent positions per reader

### C1334 · js:1. the log · L- · script
> One possible read order (P0 first, then P1, then P2); red = earlier-produced record seen later:
- pass 1: n/a (UI label (one possible order, illustrative))
- pass 2: n/a — UI legend

### C1335 · js:1. the log · L- · script
> Global order broken: … record(s) show up after a record produced later. A consumer reading several partitions can interleave them in any way. Per key, though, the order …. If you need order, it has to be per key.
- pass 1: ✅ docs/getting-started/introduction.md:83 — order guaranteed per topic-partition only
- pass 2: ✅ docs/getting-started/introduction.md:83 — order guaranteed only within a topic-partition
