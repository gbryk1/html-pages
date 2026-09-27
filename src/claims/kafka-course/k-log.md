# Claims ledger — kafka-course.html — k-log

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0013 · k-log · L1 · p
> Your bank shows you a balance, but underneath it keeps the list of every transaction, in order, forever. If someone wiped the balance you could rebuild it from that list. If someone wiped the list, the balance would mean nothing. Which of the two is the real data?
- pass 1: n/a (pedagogy question)
- pass 2: n/a — analogy/motivation

### C0014 · k-log · L1 · p
> Kafka's answer is the list. Everything in Kafka is built on one simple data structure: the log. Here "log" doesn't mean a debug log. It means an append-only sequence of records. You can add records at the end and you can read from any position. You can't change or insert a record in the middle.
- pass 1: ✅ docs/design/design.md:72 + docs/implementation/log.md:39 — "simple reads and appends to files"; "serial appends which always go to the last file"
- pass 2: ✅ docs/implementation/log.md:39 — "The log allows serial appends which always go to the last file"; reads by offset (docs/implementation/log.md:43)

### C0015 · k-log · L1 · h3
> Meet the city ledger archive
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0016 · k-log · L1 · p
> We'll use one picture for the whole course, so let's set it up carefully. Imagine a city's ledger archive, an old-fashioned records office. Clerks write every event that happens in the city into bound ledger books. They never erase a line and never squeeze a line between two others. A new event always goes on the next empty line.
- pass 1: n/a (analogy)
- pass 2: n/a — analogy

### C0017 · k-log · L1 · tr
> In the archive | In Kafka | Where you meet it
- pass 1: n/a (table header)
- pass 2: n/a — table header

### C0018 · k-log · L1 · tr
> One entry: "Alice paid Bob $200" | Record (event, message) | this chapter
- pass 1: ✅ docs/getting-started/introduction.md:71 — "It is also called record or message in the documentation"
- pass 2: n/a — analogy mapping (record = event/message per docs/getting-started/introduction.md:71)

### C0019 · k-log · L1 · tr
> A ledger series, e.g. "Payments" | Topic | this chapter
- pass 1: ✅ docs/getting-started/introduction.md:81 — "Events are organized and durably stored in topics"
- pass 2: n/a — analogy mapping

### C0020 · k-log · L1 · tr
> One bound ledger book in that series | Partition | this chapter
- pass 1: ✅ docs/getting-started/introduction.md:83 — "Topics are partitioned"
- pass 2: n/a — analogy mapping

### C0021 · k-log · L1 · tr
> The printed line number | Offset | this chapter
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:72 — "Kafka maintains a numerical offset for each record in a partition"
- pass 2: n/a — analogy mapping

### C0022 · k-log · L1 · tr
> An archive branch building | Broker | ch. 2
- pass 1: ✅ docs/getting-started/introduction.md:65 — "Some of these servers form the storage layer, called the brokers"
- pass 2: n/a — analogy mapping

### C0023 · k-log · L1 · tr
> A courier who bundles letters into sacks | Producer | ch. 3
- pass 1: ✅ docs/getting-started/introduction.md:79 + clients/.../producer/KafkaProducer.java:123 — "Producers are those client applications that publish"; batching buffer
- pass 2: n/a — analogy mapping

### C0024 · k-log · L1 · tr
> A team of clerks sharing the books, each with a bookmark | Consumer group, committed offset | ch. 4
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:93-96 + clients/.../consumer/KafkaConsumer.java:88-89 — "All consumer instances sharing the same group.id"; "committed position"
- pass 2: n/a — analogy mapping

### C0025 · k-log · L1 · tr
> Photocopies of a book kept in other branches | Replicas | ch. 5
- pass 1: ✅ docs/design/design.md:281 — "replicates the log for each topic's partitions across a configurable number of servers"
- pass 2: n/a — analogy mapping

### C0026 · k-log · L1 · tr
> Shredding the oldest volumes of a book | Retention | ch. 6
- pass 1: ✅ docs/implementation/log.md:72 — "Data is deleted one log segment at a time"
- pass 2: n/a — analogy mapping

### C0027 · k-log · L1 · tr
> The city council keeping the official registry | KRaft controller quorum | ch. 2, 9
- pass 1: ✅ docs/design/design.md:289 + docs/operations/kraft.md — "a special node known as the \"controller\""; KRaft controllers
- pass 2: n/a — analogy mapping

### C0028 · k-log · L1 · p
> The unit of data is a record. The docs also call it an event or a message. A record says that "something happened". It has a key, a value, a timestamp and optional headers (small key/value metadata). The official introduction uses this example: key "Alice", value "Made a payment of $200 to Bob", and a timestamp. To Kafka, the key and value are just bytes. Your application decides what they mean (JSON, Avro, a plain string…).
- pass 1: ✅ docs/getting-started/introduction.md:71-75 + clients/.../producer/KafkaProducer.java doSend ("byte[] serializedKey") — "an event has a key, value, timestamp, and optional metadata headers"
- pass 2: ✅ docs/getting-started/introduction.md:71-74 — "an event has a key, value, timestamp, and optional metadata headers"; "Made a payment of $200 to Bob"

### C0029 · k-log · L1 · p
> Records are organized and durably stored in topics, for example payments or page-views. That's our ledger series. A topic is multi-producer and multi-subscriber: zero, one or many applications can write to it, and zero, one or many can read from it. And, unlike a classic message queue, reading a record does not delete it. Records stay until the topic's retention rule throws them away (chapter 6), so the same data can be read again and again, by different readers, at different times.
- pass 1: ✅ docs/getting-started/introduction.md:81 — "multi-producer and multi-subscriber … events are not deleted after consumption"
- pass 2: ✅ docs/getting-started/introduction.md:81 — "multi-producer and multi-subscriber ... events are not deleted after consumption"

### C0030 · k-log · L1 · h3
> Partitions and offsets
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0031 · k-log · L1 · p
> One ledger book would be a bottleneck: one book can only be written by one hand at a time. So a topic is split into partitions, several books in the same series. The partitions can live on different servers, so many clients can read and write the topic in parallel. That's how Kafka scales.
- pass 1: ✅ docs/getting-started/introduction.md:83 — "allows client applications to both read and write the data from/to many brokers at the same time" (one-hand bottleneck = analogy)
- pass 2: ✅ docs/getting-started/introduction.md:83 — "read and write the data from/to many brokers at the same time"

### C0032 · k-log · L1 · p
> Inside a partition, every record gets a sequential number called the offset: 0, 1, 2, 3… It's the printed line number in the book. Offsets are per partition. Partition 0 and partition 1 both have a record at offset 5, and the two are unrelated. To name a record uniquely you need three things: topic, partition and offset.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:72-73 + docs/implementation/log.md:33 — "unique identifier of a record within that partition"; "per-partition atomic counter"
- pass 2: ✅ docs/implementation/log.md:33 — "simple per-partition atomic counter"; offsets unique only within a partition

### C0033 · k-log · L1 · p
> Which book does a new record go into? The producer picks. With the default rules, records with the same key always go to the same partition because the partition is chosen from a hash of the key. All of Alice's payments land in one book, in the order they were written. Records without a key are spread over partitions (chapter 3 shows how).
- pass 1: ✅ docs/getting-started/introduction.md:83 + clients/.../producer/ProducerConfig.java:314-321 — "Events with the same event key … are written to the same partition"; sticky for no key
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:1483-1485 — "hash the keyBytes to choose a partition"; docs/getting-started/introduction.md:83 "same event key ... written to the same partition"

### C0034 · k-log · L1 · p
> That sentence is the most important one in this chapter. The docs put it this way: any consumer of a given topic-partition will always read that partition's events in exactly the same order as they were written. Across two partitions there is no such promise. Play with it below. Type a key, append it, and watch where it lands. Then press the break-it button.
- pass 1: ✅ docs/getting-started/introduction.md:83 — "will always read that partition's events in exactly the same order as they were written"
- pass 2: ✅ docs/getting-started/introduction.md:83 — "will always read that partition's events in exactly the same order as they were written"

### C0035 · k-log · L1 · figcaption
> The partition is picked exactly like Kafka's default for keyed records: murmur2(keyBytes) made positive, modulo the partition count (ported from BuiltInPartitioner.partitionForKey). The "#n" badge shows the order in which records were produced. It's for illustration: Kafka doesn't store such a global number. The two readers are independent consumers of partition 0.
- pass 1: ✅ clients/.../producer/internals/BuiltInPartitioner.java:329-330 — "Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions" (JS port verified vs UtilsTest vectors; #n labelled illustrative)
- pass 2: ✅ clients/.../producer/internals/BuiltInPartitioner.java:330 — "Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions"

### C0036 · k-log · L1 · div
> If records are never deleted when read, how is it different from a database table?
- pass 1: n/a (question)
- pass 2: n/a — Q&A prompt

### C0037 · k-log · L1 · div
> A table stores the current state and lets you update rows in place. A log stores the history of changes and only ever appends. You can derive the current state from the log by replaying it. You can't recover the history from the table.
- pass 1: ✅ docs/design/design.md:395 — "Using this complete log, we could restore to any point in time by replaying" (rest pedagogy)
- pass 2: n/a — conceptual explanation (table vs log)

### C0038 · k-log · L1 · div
> Can I change a record after it's written?
- pass 1: n/a (question)
- pass 2: n/a — Q&A prompt

### C0039 · k-log · L1 · div
> No. You append a new record with the same key that says "this is the new value". Consumers see both, in order. The archive clerks never erase ink; they write a correction on the next line.
- pass 1: ✅ docs/design/design.md:372-386 — "every time a user updates their email address we send a message … using their user id as the primary key"
- pass 2: ✅ docs/implementation/log.md:39 — append-only log; no in-place update API exists (correction = new record with same key)

### C0040 · k-log · L1 · div
> Does anyone need a global order across the whole topic?
- pass 1: n/a (question)
- pass 2: n/a — Q&A prompt

### C0041 · k-log · L1 · div
> Usually what you need is order per entity: per account, per order ID, per device. Use that entity's ID as the key and Kafka gives you exactly that. If you truly need one total order, you need one partition, which also means one partition's worth of throughput.
- pass 1: ✅ docs/getting-started/introduction.md:83 + docs/design/design.md:127 — "Events with the same event key … written to the same partition"; single-partition total order = logical consequence
- pass 2: ✅ docs/getting-started/introduction.md:83 — ordering guaranteed per topic-partition only; advice part n/a

### C0042 · k-log · L1 · p
> The key-to-partition mapping is hash(key) % numberOfPartitions. If you add partitions to an existing topic, the same key can start landing in a different partition. New records for Alice may go into a different book than her old ones, and a reader may see them out of order. The kafka-topics.sh help text warns about exactly this. Choose the partition count for keyed topics up front.
- pass 1: ✅ tools/.../TopicCommand.java:760-761 + BuiltInPartitioner.java:330 — "If partitions are increased for a topic that has a key, the partition logic or ordering … affected"
- pass 2: ✅ tools/src/main/java/org/apache/kafka/tools/TopicCommand.java:761 — "If partitions are increased for a topic that has a key, the partition logic or ordering ... will be affected"

### C0043 · k-log · L1 · p
> Start a local single-node Kafka 4.3 cluster as in the official quickstart (bin/kafka-storage.sh format --standalone … then bin/kafka-server-start.sh config/server.properties). Then:
- pass 1: ✅ docs/getting-started/quickstart.md:55,61 — "bin/kafka-storage.sh format --standalone"; "bin/kafka-server-start.sh config/server.properties"
- pass 2: ✅ docs/getting-started/quickstart.md:55,61 — "bin/kafka-storage.sh format --standalone -t $KAFKA_CLUSTER_ID -c config/server.properties"; "bin/kafka-server-start.sh config/server.properties"

### C0044 · k-log · L1 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 \ --create --topic payments --partitions 3 bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic payments \ --reader-property parse.key=true --reader-property key.separator=: > alice:paid 10 > carol:paid 5 > alice:paid 7 bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic payments \ --from-beginning --formatter-property print.key=true \ --formatter-property print.partition=true --formatter-property print.offset=true
- pass 1: ✅ TopicCommand.java:720-760; ConsoleProducer.java:262-275 (reader-property: parse.key, key.separator); ConsoleConsumerOptions.java:139-163 (formatter-property print.key/partition/offset, from-beginning) — flags exist
- pass 2: ✅ tools/.../ConsoleProducer.java:262 "reader-property"; LineMessageReader.java:70,72 parse.key/key.separator; ConsoleConsumerOptions.java:139,163 "formatter-property","from-beginning"; DefaultMessageFormatter.java:62,65,74 print.key/offset/partition; TopicCommand.java:760 --partitions

### C0045 · k-log · L1 · p
> 1) Do both of Alice's records show the same partition? 2) Which offsets do they have? 3) Run the consumer command again. Do you get the records a second time?
- pass 1: n/a (exercise question)
- pass 2: n/a — exercise questions

### C0046 · k-log · L1 · p
> 1) Yes. Same key, same partition. 2) Consecutive offsets within that partition (for example 0 and 1), whatever offsets Carol's record got in its own partition. 3) Yes. Without --group, the console consumer invents a fresh random group (console-consumer-NNNNN) and doesn't commit offsets, so there's no saved position to resume from, and --from-beginning starts it at the earliest offset. Reading didn't delete anything. (In chapter 4 you'll add --group and see a different result.)
- pass 1: ✅ ConsoleConsumerOptions.java:290-295,163 — "\"console-consumer-\" + RANDOM.nextInt(100000)"; ENABLE_AUTO_COMMIT "false"; quickstart partitions consecutive per BuiltInPartitioner
- pass 2: ✅ tools/.../consumer/ConsoleConsumerOptions.java:291-295 — "console-consumer-" + RANDOM.nextInt(100000); ENABLE_AUTO_COMMIT "false"

### C0047 · k-log · L2 · p
> What a partition looks like on disk. A partition is a directory on the broker. A topic my-topic with two partitions has the directories my-topic-0 and my-topic-1. Inside, the log is split into files called segments, each named after the first offset it contains. The first file is 00000000000000000000.log, and a new one starts when the current one reaches segment.bytes or gets older than segment.ms. New records are always appended to the last file. In our analogy, a "book" is really a shelf of volumes. The last volume is still being written; the older ones are closed. Chapter 6 (retention) and chapter 7 (storage engine) build on this.
- pass 1: ✅ revised after pass-2 finding — C0047 k-log segments roll → segment.bytes / segment.ms (TopicConfig; implementation/log.md)
- pass 2: ✅ storage LogConfig segment.bytes/segment.ms; docs/implementation/log.md — segment files named by base offset (standard 00000000000000000000.log)

### C0048 · k-log · L3 · summary
> L3🔬 Go deeper: why offsets instead of message IDs?
- pass 1: n/a (summary heading)
- pass 2: n/a — summary heading

### C0049 · k-log · L3 · p
> The implementation docs say Kafka's original idea was a producer-generated GUID per message, with a GUID→offset map on each broker. They dropped it. A consumer must track an ID per server anyway, so global uniqueness adds nothing. Mapping random IDs to positions would also need a heavy, disk-synchronized index. A simple per-partition atomic counter (the offset) is enough, and it doubles as the consumer's position.
- pass 1: ✅ revised after pass-2 finding — C0049 k-log GUID → implementation/log.md:33 "a consumer must maintain an ID for each server"
- pass 2: ✅ docs/implementation/log.md:33 — "original idea was to use a GUID generated by the producer ... simple per-partition atomic counter"

### C0050 · k-log · L3 · p
> An offset is a 64-bit integer. A consumer's position is "the offset of the next record I'll read", so a consumer at position 5 has consumed offsets 0–4. Offsets are not guaranteed to be consecutive from a reader's point of view. Compacted topics (ch. 14) remove records, and transactions (ch. 13) write control markers that take up offsets but are never returned to the application. That's why you should never compute "number of records = end − start".
- pass 1: ✅ docs/implementation/log.md:29 + clients/.../consumer/KafkaConsumer.java:72-80 + docs/implementation/message-format.md:75 — "64-bit integer offset"; "offsets are not guaranteed to be consecutive"; "Control records should not be passed on to applications"
- pass 2: ✅ docs/implementation/log.md:29,43 — "64-bit integer offset"; consumer position = next offset (KafkaConsumer javadoc); control markers/compaction create gaps

### C0051 · k-log · L3 · p
> On the broker, the partition's log is managed by UnifiedLog (package org.apache.kafka.storage.internals.log), which keeps its LogSegment files through a LocalLog (a LogSegments collection).
- pass 1: ❌ fixed in part file — UnifiedLog.java:128 "private final LocalLog localLog"
- pass 2: ✅ storage/src/main/java/org/apache/kafka/storage/internals/log/UnifiedLog.java:128 "private final LocalLog localLog"; LocalLog.java:80 "private final LogSegments segments"

### C0052 · k-log · L1 · li
> A Kafka record has a key, value, timestamp and optional headers. Kafka treats key and value as bytes.
- pass 1: ✅ docs/getting-started/introduction.md:71 — "key, value, timestamp, and optional metadata headers"
- pass 2: ✅ docs/getting-started/introduction.md:71 — key, value, timestamp, optional headers; serializers turn objects into byte[]

### C0053 · k-log · L1 · li
> A topic is a named stream of records; it's split into partitions, each an append-only log.
- pass 1: ✅ docs/getting-started/introduction.md:81-83 + docs/design/design.md:315 — "At its heart a Kafka partition is a replicated log"
- pass 2: ✅ docs/getting-started/introduction.md:81-83 — topics partitioned; each partition an append-only log (docs/implementation/log.md:39)

### C0054 · k-log · L1 · li
> Each record in a partition has an offset, a per-partition sequence number.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:72 — "numerical offset for each record in a partition"
- pass 2: ✅ docs/implementation/log.md:33 — "per-partition atomic counter"

### C0055 · k-log · L1 · li
> Order is guaranteed only within a partition. Same key → same partition (by default) → per-key order.
- pass 1: ✅ docs/getting-started/introduction.md:83 — "same event key … same partition … exactly the same order"
- pass 2: ✅ docs/getting-started/introduction.md:83 — same key → same partition; order per topic-partition

### C0056 · k-log · L1 · li
> Reading doesn't delete. Many readers can read the same data independently, at their own pace.
- pass 1: ✅ docs/getting-started/introduction.md:81 — "Events in a topic can be read as often as needed"
- pass 2: ✅ docs/getting-started/introduction.md:81 — "events are not deleted after consumption"

### C0057 · k-log · L1 · li
> Adding partitions later changes where keys land. Plan the count for keyed topics.
- pass 1: ✅ TopicCommand.java:760-761 — "partition logic or ordering of the messages will be affected"
- pass 2: ✅ TopicCommand.java:761 — "partition logic or ordering of the messages will be affected"
