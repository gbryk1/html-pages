# Claims ledger — kafka-course.html — k-producer

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0087 · k-producer · L1 · p
> A producer is the client that writes records to Kafka. In the archive, it's a courier. You hand it letters (records); it sorts them by destination book (partition) into sacks (batches), and a van (a background thread) drives full sacks to the right branch. Sending one letter per trip would be absurdly slow, so batching is at the heart of Kafka's performance.
- pass 1: ✅ docs/design/design.md:86-88,131 — "Batching is one of the big drivers of efficiency" (courier = analogy)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:123-125 — "pool of buffer space ... as well as a background I/O thread"; analogy n/a

### C0088 · k-producer · L1 · h3
> What happens when you call send()
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0089 · k-producer · L1 · p
> The Java KafkaProducer.send() is asynchronous. It puts the record into a buffer and returns right away with a Future. You can wait on that future or pass a callback to learn the outcome later. Inside, the record goes through a small pipeline:
- pass 1: ✅ clients/.../producer/KafkaProducer.java:126-127,868 — "The send() method is asynchronous … immediately returns"; "returns a Future"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:127-128 — "send() method is asynchronous ... adds the record to a buffer of pending record sends and immediately returns"; returns Future (:868)

### C0090 · k-producer · L1 · li
> Metadata. The producer needs to know the topic's partitions (chapter 2). The first send to a topic may wait for metadata.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:203-207 — "For send() this timeout bounds the total time waiting for both metadata fetch and buffer allocation"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:988 — waitOnMetadata; javadoc "block for up to max.block.ms ... waiting for topic's metadata"

### C0091 · k-producer · L1 · li
> Serializer. Your key and value objects become bytes via key.serializer and value.serializer, e.g. StringSerializer.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:110-113 (+doSend serialize) — "org.apache.kafka.common.serialization.StringSerializer"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:998-1006 — keySerializerPlugin/valueSerializerPlugin serialize

### C0092 · k-producer · L1 · li
> Partitioner. Picks the partition. If you set one explicitly, it's used. Otherwise, with a key, it's murmur2(key bytes) made positive, mod the partition count. Without a key, the producer uses a sticky partition that changes after at least batch.size bytes have been sent to it.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:314-321 + BuiltInPartitioner.java:330 — "choose the sticky partition that changes when at least batch.size bytes are produced"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:1469-1487 explicit partition, then key hash; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:317 "until at least batch.size bytes is produced to the partition"

### C0093 · k-producer · L1 · li
> Record accumulator. The record is appended to the open batch for that partition, in a memory pool of size buffer.memory.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:214 + RecordAccumulator.java:60-66 — "total bytes of memory the producer can use to buffer records"; "accumulates records into MemoryRecords"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:1029 accumulator.append; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:389 buffer.memory

### C0094 · k-producer · L1 · li
> Sender thread. A background I/O thread (named kafka-producer-network-thread…) collects ready batches, groups them per leader broker into produce requests, and sends them.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:124,245 + RecordAccumulator.java:950-959 — "background I/O thread"; "kafka-producer-network-thread"; drain "on a per-node basis"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:245,467 — "kafka-producer-network-thread" + " | " + clientId; Sender drains per node (RecordAccumulator.drain)

### C0095 · k-producer · L1 · p
> A batch is "ready" when it's full (batch.size, default 16384 bytes) or it has waited linger.ms. Since Kafka 4.0 the default linger.ms is 5 ms (it was 0 before). The source says the change was made because larger batches usually give similar or lower latency despite the extra wait.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:153-160,401,405 — "The default changed from 0 to 5 in Apache Kafka 4.0 … similar or lower producer latency"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:401,405,100 — batch.size 16384, linger.ms 5, "default changed from 0 to 5 in Apache Kafka 4.0 as the efficiency gains ... similar or lower producer latency"

### C0096 · k-producer · L1 · figcaption
> Illustrative model: 12 records arrive 1 ms apart, then one straggler for P0 at 20 ms; a batch is "full" at 4 records, and each batch is shown as its own request (a real request carries one batch per partition for the same broker). Real producers also batch with linger.ms=0 whenever records pile up while a request is in flight. Only the rules are real: sent when full or when linger.ms expires, and send() blocks when the buffer is full.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:89,149,214-215 — "one for each partition"; "Normally this occurs only under load"; blocks max.block.ms (numbers labelled illustrative)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:148-149 — "Normally this occurs only under load when records arrive faster than they can be sent out"; illustrative numbers n/a

### C0097 · k-producer · L1 · h3
> acks: when is a write "done"?
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0098 · k-producer · L1 · p
> The acks setting decides how much confirmation the producer waits for. acks=0: don't wait at all. acks=1: the leader wrote it to its own log. acks=all (same as -1, and the default): the leader waits until all in-sync replicas have it. Chapter 5 shows what each one can lose.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:127-141,391-396 — "acks=all … equivalent to the acks=-1 setting"; default "all"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:130-140,391-394 — acks=1 "leader will write the record to its local log"; acks=all "equivalent to the acks=-1"; default "all"

### C0099 · k-producer · L1 · p
> Producers also retry automatically (retries defaults to Integer.MAX_VALUE, bounded in time by delivery.timeout.ms, default 120000 ms). Retries raise a problem: if an ack is lost, the retry could write the record twice. That's why enable.idempotence is true by default. The broker then drops the duplicate retry (chapter 12 shows how). Idempotence needs acks=all. If you set a conflicting value like acks=1 without explicitly enabling idempotence, it is quietly turned off.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:339-347,390,406 + docs/design/design.md:193 — "Integer.MAX_VALUE"; "120 * 1000"; "If conflicting configurations are set and idempotence is not explicitly enabled, idempotence is disabled"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:390,406,345-346 — retries Integer.MAX_VALUE; delivery.timeout 120*1000; "If conflicting configurations are set and idempotence is not explicitly enabled, idempotence is disabled"

### C0100 · k-producer · L1 · tr
> Config | Default | What it does | Use it when
- pass 1: n/a (table header)
- pass 2: n/a table header

### C0101 · k-producer · L1 · tr
> bootstrap.servers | (required) | Initial brokers to contact | Always; list 2–3 brokers
- pass 1: ✅ clients/.../producer/ProducerConfig.java:376-378 + CommonClientConfigs.java:49 — NO_DEFAULT_VALUE; "recommend including more than one server"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:376-378 — bootstrap.servers NO_DEFAULT_VALUE; clients/src/main/java/org/apache/kafka/clients/CommonClientConfigs.java:49 "recommend including more than one server"

### C0102 · k-producer · L1 · tr
> key.serializer / value.serializer | (required) | Turn objects into bytes | Always; match your data format
- pass 1: ✅ clients/.../producer/ProducerConfig.java:479-486 — key/value serializer defined without default (required)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:479-485,669 — serializer classes required ("must be non-null")

### C0103 · k-producer · L1 · tr
> acks | all | How many replicas must confirm | Keep all unless losing data is acceptable
- pass 1: ✅ clients/.../producer/ProducerConfig.java:391-396 — "all"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:391-394 — default "all"

### C0104 · k-producer · L1 · tr
> linger.ms | 5 | Max wait to fill a batch | Raise (10–100) for throughput; lower for latency at low volume
- pass 1: ✅ clients/.../producer/ProducerConfig.java:405,148-157 — "Type.LONG, 5"; "upper bound on the delay for batching" (use-when = advice)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:405 — linger.ms default 5; tuning advice n/a

### C0105 · k-producer · L1 · tr
> batch.size | 16384 bytes | Max size of one partition's batch | Raise with high volume or compression
- pass 1: ✅ clients/.../producer/ProducerConfig.java:401,83-95 — "16384"; "upper bound of the batch size"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:401 — 16384; "upper bound of the batch size" (:95)

### C0106 · k-producer · L1 · tr
> buffer.memory | 33554432 (32 MiB) | Total memory for unsent records | Raise if send() blocks under bursts
- pass 1: ✅ clients/.../producer/ProducerConfig.java:389,214-215 — "32 * 1024 * 1024L"; "producer will block for max.block.ms"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:389 — "32 * 1024 * 1024L"

### C0107 · k-producer · L1 · tr
> max.block.ms | 60000 | How long send() may block (metadata, full buffer) | Lower if your caller must never hang long
- pass 1: ✅ clients/.../producer/ProducerConfig.java:435-440,203-206 — "60 * 1000"; "metadata fetch and buffer allocation"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:435-437 — 60 * 1000; clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java send javadoc blocks for metadata and buffer allocation

### C0108 · k-producer · L1 · tr
> enable.idempotence | true | Broker de-duplicates producer retries | Keep on; needs acks=all
- pass 1: ✅ clients/.../producer/ProducerConfig.java:527-531,339-343 — "true"; "acks must be 'all'"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:527-530,343 — default true; "acks must be 'all'"

### C0109 · k-producer · L1 · tr
> compression.type | none | Compress whole batches | Big or repetitive payloads (gzip, snappy, lz4, zstd)
- pass 1: ✅ clients/.../producer/ProducerConfig.java:397 + docs/design/design.md:115-119 — "CompressionType.NONE.name"; "GZIP, Snappy, LZ4 and ZStandard"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:397 — "CompressionType.NONE.name"; options gzip/snappy/lz4/zstd

### C0110 · k-producer · L1 · p
> Fire-and-forget by accident. Calling producer.send(record) and ignoring the returned future means you never find out if the write failed. Always pass a callback, or at least log failures. And close the producer on shutdown: the Javadoc warns that not closing it leaks its buffer and I/O thread, and close() is also what waits for the sending of all incomplete requests to finish.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:124-125,1367 — "Failure to close the producer after use will leak these resources"; "waits … to complete the sending of all incomplete requests"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:125,1367 — "Failure to close the producer after use will leak these resources"; "waits ... to complete the sending of all incomplete requests"

### C0111 · k-producer · L1 · div
> Doesn't waiting 5 ms make every message 5 ms slower?
- pass 1: n/a (question)
- pass 2: n/a — Q&A prompt

### C0112 · k-producer · L1 · div
> At most 5 ms, and only when traffic is light. Under load, batches fill up and leave before the linger time is up. Fewer, bigger requests also mean less work for the broker, which often makes the end-to-end latency lower.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:149,158-160 — "Normally this occurs only under load"; "similar or lower producer latency despite the increased linger"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:148-157 — batch sent immediately once batch.size reached; linger adds latency "in the absence of load"

### C0113 · k-producer · L1 · div
> Should I create a producer per thread or per message?
- pass 1: n/a (question)
- pass 2: n/a — Q&A prompt

### C0114 · k-producer · L1 · div
> Neither. KafkaProducer is thread-safe, and the Javadoc says sharing one instance across threads is generally faster than having several. One producer per message is a classic performance killer.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:103-104 — "thread safe and sharing a single producer instance across threads will generally be faster"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:103 — "thread safe and sharing a single producer instance across threads will generally be faster than having multiple instances"

### C0115 · k-producer · L3 · summary
> L3🔬 Go deeper: the classes behind send()
- pass 1: n/a (summary heading)
- pass 2: n/a — summary heading

### C0116 · k-producer · L3 · p
> KafkaProducer.doSend() waits for metadata (waitOnMetadata), serializes key and value, computes the partition, checks the estimated size against max.request.size (default 1048576), then calls RecordAccumulator.append(). The accumulator keeps a deque of ProducerBatches per partition. If the append filled a batch or opened a new one, it wakes up the Sender. The sender runs on its own KafkaThread. It drains ready batches per node and sends produce requests, with at most max.in.flight.requests.per.connection (default 5) unacknowledged requests per connection.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:975-1045 (waitOnMetadata, serialize, partition, ensureValidRecordSize, accumulator.append, :1041 "result.batchIsFull || result.newBatchCreated" → sender.wakeup); clients/.../producer/ProducerConfig.java:410-415,473-478; RecordAccumulator.java:313 Deque<ProducerBatch>; Sender.java:1138 SenderThread extends KafkaThread
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:988,1024,1029,1041-1043 — waitOnMetadata, ensureValidRecordSize (max.request.size 1024*1024, clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:410), wakeup on batchIsFull||newBatchCreated; Sender.SenderThread extends KafkaThread; max.in.flight 5 (clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:473)

### C0117 · k-producer · L3 · p
> Key hashing lives in BuiltInPartitioner.partitionForKey(): Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions. For keyless records the built-in partitioner is sticky, and with partitioner.adaptive.partitioning.enable=true (default) it sends more data to partitions on faster brokers. partitioner.ignore.keys=true makes it ignore keys completely. The only other shipped strategy is RoundRobinPartitioner; its docs note a known uneven-distribution issue (KAFKA-9965).
- pass 1: ✅ BuiltInPartitioner.java:330; clients/.../producer/ProducerConfig.java:107-121,322-327,402,404 — "produce more messages to partitions hosted on faster brokers"; RoundRobinPartitioner the only shipped Partitioner impl; "See KAFKA-9965"
- pass 2: ✅ BuiltInPartitioner.java:330; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:402,404,108,323-326 — adaptive default true "produce more messages to partitions hosted on faster brokers"; ignore.keys; RoundRobinPartitioner "known issue ... KAFKA-9965"

### C0118 · k-producer · L1 · li
> send() is asynchronous: serialize → partition → accumulate in a per-partition batch → a background sender ships it.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:126-127 + doSend — "adds the record to a buffer of pending record sends"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:127, 985-1043

### C0119 · k-producer · L1 · li
> A batch goes out when it's full (batch.size 16384) or after linger.ms (default 5 since 4.0).
- pass 1: ✅ clients/.../producer/ProducerConfig.java:401,405,158 — "16384"; "default changed from 0 to 5 in Apache Kafka 4.0"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:401,405,100

### C0120 · k-producer · L1 · li
> Keyed records: murmur2 hash mod partition count. Keyless: sticky partition.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:318-319 — "hash of the key"; "sticky partition"
- pass 2: ✅ BuiltInPartitioner.java:330; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:317

### C0121 · k-producer · L1 · li
> Defaults are safe: acks=all, enable.idempotence=true, retries bounded by delivery.timeout.ms (120 s).
- pass 1: ✅ clients/.../producer/ProducerConfig.java:391-396,527-531,406 — "all"; "true"; "120 * 1000"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:391-394,406,527-530

### C0122 · k-producer · L1 · li
> A full buffer makes send() block up to max.block.ms (60 s), then fail.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:215,435-440 — "block for max.block.ms after which it will fail with an exception"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:435 — 60 * 1000; send javadoc "Allocating a buffer if buffer pool doesn't have any free buffers"

### C0123 · k-producer · L1 · li
> Use one shared producer, check results via callback, and always close() it.
- pass 1: ✅ clients/.../producer/KafkaProducer.java:103-104,124-125 — thread safe, shared; must close (callback advice = opinion)
- pass 2: n/a — advice (supported by clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:103,125)
