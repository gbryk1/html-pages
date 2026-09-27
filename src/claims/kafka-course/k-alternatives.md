# Claims ledger — kafka-course.html — k-alternatives

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1117 · k-alternatives · L4 · p
> After twenty-six chapters it would be easy to believe every messaging problem is a Kafka problem. It isn't. Our city ledger archive is superb at one thing: keeping an ordered, replayable, replicated record that many teams can read independently at their own pace. Some jobs look more like a post office: hand each parcel to exactly one courier, route it by address, and forget it once delivered. Others want the archive's model with a different building underneath. This chapter compares Kafka 4.3 with the main alternatives, fairly and with dates.
- pass 1: n/a (analogy / framing)
- pass 2: n/a — chapter intro/analogy

### C1118 · k-alternatives · L4 · p
> Every vendor statement below comes from that vendor's own documentation, fetched on 2026-09-25: RabbitMQ 4.3 docs, Apache Pulsar docs ("next") with 4.2.4 the newest stable line and 4.0 the LTS, Redpanda 26.2 docs, and the AWS Kinesis Data Streams and Google Cloud Pub/Sub docs. These products move fast. Re-check anything you're about to bet a platform on. Performance claims are the vendors' own marketing and are not repeated here as fact.
- pass 1: ✅ https://pulsar.apache.org/download/; https://www.rabbitmq.com/docs/queues; https://docs.redpanda.com/current/get-started/architecture/ — "4.2.4 (03 Aug 2026) / 4.0 (LTS) / Page Version 26.2 / Document Version 4.3"
- pass 2: ✅ rabbitmq.com/docs/quorum-queues version selector shows 4.3; pulsar.apache.org/download — 4.2.4 (Aug 3 2026) current stable, 4.0 LTS; docs.redpanda.com architecture page is v26.2 (fetched 2026-09-27)

### C1119 · k-alternatives · L4 · h3
> The one question that sorts everything: log or queue?
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C1120 · k-alternatives · L4 · p
> A log keeps records after they are read. Consumers track their own position, many independent readers can read the same data, and you can rewind (replay). Order is kept per partition. A queue hands each message to one consumer, tracks per-message acknowledgement, and deletes the message once it's acknowledged. Kafka consumer groups are log semantics. RabbitMQ classic and quorum queues are queue semantics. The interesting systems now offer both: Kafka added share groups (queue-like consumption over a log), RabbitMQ added streams (a log inside a broker built around queues), and Pulsar exposes both through subscription types.
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://www.rabbitmq.com/docs/streams; kafka-4.3.1-src/docs/getting-started/upgrade.md:84 — "removed from the queue after ack / non-destructive consumer semantics / alternative to consumer groups"
- pass 2: ✅ conceptual; share groups (upgrade.md:84), RabbitMQ streams (rabbitmq.com/docs/streams "non-destructive consumer semantics"), Pulsar subscription types (concepts-messaging) all confirmed

### C1121 · k-alternatives · L4 · h3
> RabbitMQ: queues, quorum queues, streams
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C1122 · k-alternatives · L4 · p
> RabbitMQ's model starts with routing. Publishers send to exchanges, which route to queues by binding rules. A queue "is an ordered collection of messages", FIFO, but priorities, multiple consumers and requeues after negative acknowledgements can change the order. After a consumer acknowledges, the message is removed. Quorum queues are "a modern queue type which implements a durable, replicated queue based on the Raft consensus algorithm" and the docs call them "the default choice when needing a replicated, highly available queue". They have per-message poison handling built in: since RabbitMQ 4.0 the delivery limit defaults to 20, after which the message is dropped or dead-lettered.
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://www.rabbitmq.com/docs/quorum-queues — "an ordered collection of messages / default choice when needing a replicated, highly available queue / defaults to 20"
- pass 2: ✅ rabbitmq.com/docs/queues — "A queue in RabbitMQ is an ordered collection of messages", priorities/redelivery change order; docs/confirms — acked message "can be discarded"; docs/quorum-queues — "a modern queue type which implements a durable, replicated queue based on the Raft consensus algorithm", "default choice when needing a replicated, highly available queue", delivery limit 20 since 4.0, then "dropped (removed) or dead-lettered"

### C1123 · k-alternatives · L4 · p
> RabbitMQ streams are RabbitMQ's answer to the log: "an append-only log of messages that can be repeatedly read until they expire", with "non-destructive consumer semantics". They are always replicated and persistent, retention is set with x-max-length-bytes / x-max-age, consumers attach at an offset, timestamp or first/last, single active consumer gives ordered exclusive consumption, and super streams partition one logical stream across several streams. The docs' use cases are large fan-outs, replay/time-travel, throughput and large backlogs.
- pass 1: ✅ https://www.rabbitmq.com/docs/streams — "append-only log of messages that can be repeatedly read until they expire"
- pass 2: ✅ rabbitmq.com/docs/streams (4.3) — "append-only log of messages that can be repeatedly read until they expire", "non-destructive consumer semantics", always persistent+replicated, x-max-length-bytes/x-max-age, first/last/next/offset/timestamp, SAC, super streams, use cases fan-out/replay/throughput/large backlogs

### C1124 · k-alternatives · L4 · h3
> Apache Pulsar: brokers without disks
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C1125 · k-alternatives · L4 · p
> Pulsar separates serving from storage. Brokers are stateless ("The Pulsar message broker is a stateless component"), message data lives in Apache BookKeeper bookies as append-only ledgers, and a metadata store coordinates the cluster (Oxia recommended, ZooKeeper, or RocksDB). The Kafka analogy is a branch clerk who keeps no books at all: every page is written to a separate vault service. That makes brokers easy to scale out and replace, at the cost of running more distinct components. On top it has built-in multi-tenancy (tenants/namespaces), geo-replication, and four subscription types: Exclusive, Failover, Shared (round-robin, queue-like) and Key_Shared (messages with the same key go to one consumer). It also has negative acknowledgement, a retry letter topic and a dead letter topic built in. By default acknowledged messages are deleted; retention policies keep them for replay.
- pass 1: ✅ https://pulsar.apache.org/docs/next/concepts-architecture-overview/; https://pulsar.apache.org/docs/next/concepts-messaging/ — "The Pulsar message broker is a stateless component / Oxia (recommended), ZooKeeper, or RocksDB"
- pass 2: ✅ pulsar.apache.org/docs/next/concepts-architecture-overview — "The Pulsar message broker is a stateless component", BookKeeper ledgers append-only, Oxia (recommended)/ZooKeeper/RocksDB; concepts-messaging — Exclusive/Failover/Shared ("round-robin")/Key_Shared, nack, retry letter topic, DLT, "immediately delete all messages that have been acknowledged" + retention (analogy n/a)

### C1126 · k-alternatives · L4 · h3
> Redpanda: the Kafka API on a different engine
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C1127 · k-alternatives · L4 · p
> Redpanda is not a different messaging model. It's a different implementation of Kafka's model. Its docs say it is "compatible with the Kafka API", is "Built on C++", is "packaged as a single binary: it doesn't rely on any external systems", uses a thread-per-core model via the Seastar library, runs "Raft consensus … for data replication" where "every topic partition forms a Raft group", and offers tiered storage. Your Kafka clients and most tools connect unchanged. What you're comparing is operations, cost, performance and licensing, not semantics. Check which newer Kafka protocol features you depend on before assuming parity. Redpanda's Kafka-compatibility page says it "does not implement the server-side portion of KIP-890", so Kafka 4.x clients fall back to the original transaction protocol (no Transactions V2). KIP-848 consumer groups and share groups (KIP-932) are not mentioned in its compatibility docs.
- pass 1: ✅ revised after pass-2 finding — C1127: Redpanda paragraph now states it "does not implement the server-side portion of KIP-890" (clients fall back, no Transactions V2); KIP-848/share groups not mentioned in its compatibility docs; bullet C1161 updated likewise (part-5.html). Table/chooser un
- pass 2: ✅ docs.redpanda.com intro-to-events + get-started/architecture + develop/kafka-clients — "Built on C++"; "packaged as a single binary: it doesn't rely on any external systems"; "does not implement the server-side portion of KIP-890"; "Kafka 4.x clients detect that Transactions V2 is unsupported and fall back"; no mention of 848/share groups

### C1128 · k-alternatives · L4 · h3
> Managed cloud streams: Kinesis and Pub/Sub
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C1129 · k-alternatives · L4 · p
> Amazon Kinesis Data Streams is a log, like Kafka. A stream is a set of shards (≈ partitions), records are routed by a partition key hashed with MD5, and each shard supports writes of up to 1,000 records/s or 1 MB/s and reads of up to 2 MB/s. Retention defaults to 24 hours and can be raised to 8,760 hours (365 days) at extra cost. The Kinesis Client Library keeps consumer state in DynamoDB tables. Google Cloud Pub/Sub is closer to a queue: at-least-once delivery by default, an optional exactly-once delivery feature, and ordering disabled by default. You enable it per subscription at creation and publish related messages with the same ordering key, limited to 1 MBps of publishing per key. Both remove the operations work. You give up protocol portability (Kafka clients don't speak them) and the Kafka ecosystem (Connect, Streams). Managed Kafka services are the middle ground: the Kafka API and semantics without running brokers. Compare their Kafka version and enabled features against what you need.
- pass 1: ✅ https://docs.aws.amazon.com/streams/latest/dev/key-concepts.html; https://docs.cloud.google.com/pubsub/docs/ordering — "default of 24 hours ... up to 8760 hours / Message ordering is disabled by default / 1 MBps"
- pass 2: ✅ docs.aws.amazon.com/streams/latest/dev/key-concepts — "A Kinesis data stream is a set of shards", "An MD5 hash function is used to map partition keys", shard "1,000 records per second … 1 MB per second" writes, 2 MB/s reads, default 24 h, up to 8760 h (365 days), "Additional charges apply", KCL "uses Amazon DynamoDB tables"; docs.cloud.google.com/pubsub/docs/ordering — ordering must be set at subscription creation, "publishing throughput on each ordering key is limited to 1 MBps"; exactly-once is an optional pull-subscription feature. (Kinesis per-shard limits are provisioned-mode figures.)

### C1130 · k-alternatives · L4 · h3
> …and the option inside Kafka: share groups
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C1131 · k-alternatives · L4 · p
> Before adding a second broker technology for "we need a work queue", remember Part III. Since Kafka 4.2, share groups are production-ready. Consumers cooperatively consume the same partitions, there can be more consumers than partitions, records are acknowledged individually (accept / release / reject / renew), and delivery attempts are counted, with a broker default limit of 5 (group.share.delivery.count.limit) and a 30 s acquisition lock by default. You keep one platform, one security model and replay of the underlying topic. You give up ordering: the Kafka docs say to use share groups "in cases where records are processed one at a time, rather than as part of an ordered stream". What they don't give you (as of 4.3) is RabbitMQ-style routing or a broker-managed dead-letter topic. A rejected record simply isn't delivered again, so capture it yourself if you need it.
- pass 1: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:84; kafka-4.3.1-src/docs/design/design.md:252-271; kafka-4.3.1-src/group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:48-61 — "production-ready in Apache Kafka 4.2 / By default, the lock duration is 30 seconds / DELIVERY_COUNT_LIMIT_DEFAULT = 5"
- pass 2: ✅ upgrade.md:84 — production-ready in 4.2, "Use share groups in cases where records are processed one at a time, rather than as part of an ordered stream"; design.md:255 consumers can exceed partitions; AcknowledgeType ACCEPT/RELEASE/REJECT/RENEW; ShareGroupConfig.java:49 delivery.count.limit=5, :61 lock 30000 ms; no share-group DLQ in 4.3.1 docs/source

### C1132 · k-alternatives · L4 · tr
> Dimension (as of Sept 2026) | Kafka 4.3 consumer groups | Kafka 4.3 share groups | RabbitMQ 4.3 quorum queues | RabbitMQ 4.3 streams | Pulsar 4.x | Redpanda 26.2
- pass 1: n/a (table header)
- pass 2: n/a — table header

### C1133 · k-alternatives · L4 · tr
> Storage model | Partitioned replicated log on broker disks (+ tiered storage) | Same log; per-record in-flight state tracked by the broker (SharePartition), persisted via the share coordinator in __share_group_state | Raft-replicated queue; message removed on ack | Replicated append-only log | Stateless brokers + BookKeeper ledgers | Raft-replicated partitioned log; tiered storage
- pass 1: ❌ fixed in part file — SharePartition.java tracks in-flight records; ShareCoordinator persists to __share_group_state (design.md "The Share Consumer")
- pass 2: ✅ Kafka log+tiered storage; core/.../share/SharePartition.java + ShareCoordinatorShard.java:306 "written to the __share_group_state topic"; RabbitMQ quorum queues Raft + discard on ack; streams replicated log; Pulsar stateless brokers + BookKeeper; Redpanda Raft per partition + tiered storage

### C1134 · k-alternatives · L4 · tr
> Replay | Yes (retention, seek) | Underlying topic, yes | No (destructive) | Yes (offset/timestamp) | With retention policy | Yes (Kafka API)
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://www.rabbitmq.com/docs/streams; https://pulsar.apache.org/docs/next/concepts-messaging/ — "removed from the queue / repeatedly read / retention policies enabling storage of acknowledged messages"
- pass 2: ✅ consumer seek/retention; share groups over same topic; quorum queue discard on ack (docs/confirms); streams offset/timestamp specs; Pulsar retention policy; Redpanda Kafka API

### C1135 · k-alternatives · L4 · tr
> Ordering | Per partition | Not an ordered stream | FIFO, but redelivery / multiple consumers can reorder | Per stream; single active consumer | Per key/partition; Key_Shared per key | Per partition
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://www.rabbitmq.com/docs/streams; https://pulsar.apache.org/docs/next/concepts-messaging/; kafka-4.3.1-src/docs/getting-started/upgrade.md:84 — "Multiple active consumers can trigger redeliveries / Key_Shared ... same key ... one consumer"
- pass 2: ✅ per-partition (Kafka, Redpanda); share groups not ordered (upgrade.md:84); RabbitMQ queues FIFO with redelivery reordering (docs/queues); streams SAC; Pulsar Key_Shared per key

### C1136 · k-alternatives · L4 · tr
> Consumers > partitions | No (extras idle) | Yes | Yes (competing consumers) | No — work is split by super-stream partitions (SAC: one active consumer each) | Shared / Key_Shared | No (Kafka groups)
- pass 1: ❌ fixed in part file — rabbitmq.com/docs/streams super streams + single active consumer: one active consumer per partition
- pass 2: ✅ consumer groups extras idle; design.md:255 share consumers can exceed partitions; RabbitMQ competing consumers; streams super streams+SAC; Pulsar Shared/Key_Shared; Redpanda Kafka groups

### C1137 · k-alternatives · L4 · tr
> Per-message retry / poison limit | App-level (Spring, custom) | Delivery count (default limit 5) | Delivery limit (default 20) + dead-lettering | App-level | Nack, retry letter topic, DLT | App-level
- pass 1: ✅ kafka-4.3.1-src/group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:49; https://www.rabbitmq.com/docs/quorum-queues; https://pulsar.apache.org/docs/next/concepts-messaging/ — "delivery limit ... defaults to 20 / Retry Letter Topic / Dead Letter Topic"
- pass 2: ✅ ShareGroupConfig.java:49 default 5; RabbitMQ quorum delivery limit 20 + DLX; Pulsar nack/retry letter/DLT; others app-level

### C1138 · k-alternatives · L4 · tr
> Routing | By topic/key | By topic | Exchanges + bindings | Exchanges + bindings | Topics, namespaces, tenants | By topic/key
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://pulsar.apache.org/docs/next/concepts-architecture-overview/ — "Publishers send messages to exchanges / tenant and namespace"
- pass 2: ✅ RabbitMQ exchanges+bindings (streams are declared/bound via exchanges too, super streams route via exchange); Pulsar tenants/namespaces/topics; Kafka/Redpanda topic/key

### C1139 · k-alternatives · L4 · tr
> Client protocol | Kafka | Kafka (share APIs) | RabbitMQ (AMQP) clients | Dedicated stream protocol (recommended) or AMQP 0-9-1 | Pulsar clients | Kafka API
- pass 1: ✅ https://www.rabbitmq.com/docs/streams — "dedicated binary protocol plugin ... highly recommended / AMQP 0.9.1 client library"
- pass 2: ✅ rabbitmq.com/docs/streams — dedicated stream protocol "highly recommended" over AMQP 0.9.1; Kafka share APIs (ShareGroupHeartbeat/ShareFetch); Pulsar binary protocol; Redpanda Kafka API

### C1140 · k-alternatives · L4 · figcaption
> Heuristic only: each option gets +1 for a requirement it meets per the vendor docs (Sept 2026) and is flagged ✗ on a requirement it can't meet. Real decisions also weigh team skills, existing platforms, cost and licensing, which this toy ignores.
- pass 1: n/a (figcaption; labelled heuristic)
- pass 2: n/a — labelled heuristic

### C1141 · k-alternatives · L4 · div
> KafkaI keep everything, in order, for as long as you like, and a hundred teams can read it without bothering each other.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue

### C1142 · k-alternatives · L4 · div
> RabbitMQAnd I route a parcel by its label to exactly the right courier, retry it twenty times, and file it under "dead letters" if nobody can take it. No partition planning.
- pass 1: n/a (fireside dialogue (delivery limit 20 per quorum-queues doc))
- pass 2: n/a — dialogue (retry twenty times matches quorum delivery limit 20)

### C1143 · k-alternatives · L4 · div
> KafkaI do queues now too — share groups.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue

### C1144 · k-alternatives · L4 · div
> RabbitMQAnd I do logs now — streams. Seems we both decided the other one had a point.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue

### C1145 · k-alternatives · L4 · div
> Is Redpanda "Kafka without ZooKeeper"?
- pass 1: n/a (question)
- pass 2: n/a — question

### C1146 · k-alternatives · L4 · div
> That framing is outdated: since 4.0, Kafka itself has no ZooKeeper (KRaft, Part II). The real differences are the engine (C++, thread-per-core, single binary), operations and licensing, plus which newer Kafka features are supported.
- pass 1: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:33; https://docs.redpanda.com/current/get-started/architecture/; https://docs.redpanda.com/current/get-started/intro-to-events/ — "ZooKeeper mode has been removed / thread-per-core / single binary (licensing = opinion)"
- pass 2: ✅ upgrade.md:201 — "Apache Kafka 4.0 only supports KRaft mode - ZooKeeper mode has been removed"; Redpanda C++/Seastar/single binary per docs

### C1147 · k-alternatives · L4 · div
> If Pulsar has queues and logs, why not always use it?
- pass 1: n/a (question)
- pass 2: n/a — question

### C1148 · k-alternatives · L4 · div
> More moving parts (brokers, bookies, metadata store), a smaller ecosystem of Kafka-native tools, and a different client protocol. If you need its multi-tenancy, geo-replication model or compute/storage split, it's a strong choice. If you don't, you're paying for them anyway.
- pass 1: n/a (opinion (components per https://pulsar.apache.org/docs/next/concepts-architecture-overview/))
- pass 2: n/a — opinion/advice (components brokers/bookies/metadata store per Pulsar architecture docs)

### C1149 · k-alternatives · L4 · div
> Can share groups replace RabbitMQ?
- pass 1: n/a (question)
- pass 2: n/a — question

### C1150 · k-alternatives · L4 · div
> For "many workers, one topic, per-record ack and retry limits", often yes. For routing by header or binding patterns, priorities, or request/reply idioms, RabbitMQ still does things share groups don't.
- pass 1: n/a (opinion)
- pass 2: n/a — opinion

### C1151 · k-alternatives · L4 · p
> You have a thumbnails topic with 3 partitions and bursts that need 12 workers. Try the in-Kafka queue option on a local 4.3 cluster (share groups are enabled by default on new 4.2+ clusters; on an upgraded cluster first run bin/kafka-features.sh --bootstrap-server localhost:9092 upgrade --feature share.version=1). On a single-broker dev cluster, also set share.coordinator.state.topic.replication.factor=1 and share.coordinator.state.topic.min.isr=1 in the broker config before first use, because __share_group_state defaults to 3 replicas:
- pass 1: ✅ kafka-4.3.1-src/server-common/src/main/java/org/apache/kafka/server/common/ShareVersion.java:28,32; kafka-4.3.1-src/docs/getting-started/upgrade.md:79; kafka-4.3.1-src/docs/operations/kraft.md:97 — "LATEST_PRODUCTION = SV_1 / set ... to 1 before you start using share groups"
- pass 2: ✅ ShareVersion.java:28 SV_1 bootstrapped at IBP_4_2_IV0 (on by default for new 4.2+ clusters); FeatureCommand.java:148-157 upgrade --feature; ShareCoordinatorConfig.java:42,47 RF default 3, min.isr default 2

### C1152 · k-alternatives · L4 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic thumbnails --partitions 3 bin/kafka-console-share-consumer.sh --bootstrap-server localhost:9092 --topic thumbnails --group thumbs # start the same command in 3 more terminals, then produce 20 records
- pass 1: ✅ kafka-4.3.1-src/tools/src/main/java/org/apache/kafka/tools/consumer/ConsoleShareConsumerOptions.java:60,160 — "accepts("topic") / accepts("group")"
- pass 2: ✅ bin/kafka-console-share-consumer.sh exists; ConsoleShareConsumerOptions.java:60,160 --topic/--group

### C1153 · k-alternatives · L4 · p
> How many of the four share consumers receive records? What would happen with kafka-console-consumer.sh --group thumbs2 in four terminals?
- pass 1: n/a (exercise question)
- pass 2: n/a — exercise question

### C1154 · k-alternatives · L4 · p
> With the share group, records are spread across all four consumers even though there are only 3 partitions — "the number of consumers in a share group can exceed the number of partitions". With a classic consumer group, at most 3 consumers get partitions and the fourth is idle. The share group version gives up per-partition ordering in exchange.
- pass 1: ✅ kafka-4.3.1-src/docs/design/design.md:255 — "The number of consumers in a share group can exceed the number of partitions"
- pass 2: ✅ design.md:255 — "The number of consumers in a share group can exceed the number of partitions in a topic."; classic group ≤ one consumer per partition

### C1155 · k-alternatives · L4 · summary
> L4🔬 Go deeper: why the architectures lead to these trade-offs
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C1156 · k-alternatives · L4 · p
> Where state lives decides what's cheap. A Kafka consumer group stores one committed offset per partition in __consumer_offsets, so a million-record backlog costs the same bookkeeping as ten records. That's why fan-out and replay are nearly free and per-record acknowledgement isn't offered. Share groups add per-record state (acquired/acknowledged/archived and delivery counts) for a bounded window. The broker caps in-flight acquired records per share-partition (group.share.partition.max.record.locks) and persists share-partition state through the share coordinator in __share_group_state. That bound is how Kafka offers queue semantics without per-message state for the whole log.
- pass 1: ✅ kafka-4.3.1-src/docs/design/design.md:277; kafka-4.3.1-src/server/src/main/java/org/apache/kafka/server/share/fetch/RecordState.java:27-31; kafka-4.3.1-src/docs/getting-started/upgrade.md:79 — "group.share.partition.max.record.locks / ARCHIVED / __share_group_state"
- pass 2: ✅ __consumer_offsets per-partition offsets; SharePartition RecordState (AVAILABLE/ACQUIRED/ACKNOWLEDGED/ARCHIVED) + delivery counts; ShareGroupConfig.java:36 group.share.partition.max.record.locks; __share_group_state via share coordinator

### C1157 · k-alternatives · L4 · p
> A quorum queue keeps per-message state by design, so a message leaves once it's acknowledged. RabbitMQ streams and Kafka keep the log and move per-reader state to the consumer (offset tracking). Pulsar tracks acknowledgement per subscription (individual or cumulative), which is how one topic can serve both queue-like Shared and stream-like Exclusive/Failover subscriptions. Redpanda replaces the per-partition ISR replication you learned in Part II with a Raft group per partition. Clients see the same Kafka API, but the replication and failure behaviour underneath differs, so re-validate your durability assumptions (acks, min ISR semantics) against its docs.
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://www.rabbitmq.com/docs/streams; https://pulsar.apache.org/docs/next/concepts-messaging/; https://docs.redpanda.com/current/get-started/architecture/ — "individual and cumulative acknowledgment / Every topic partition forms a Raft group"
- pass 2: ✅ quorum queue discard on ack; RabbitMQ stream offset tracking; Pulsar individual/cumulative ack per subscription (concepts-messaging); Redpanda "every topic partition forms a Raft group" (advice n/a)

### C1158 · k-alternatives · L4 · li
> Decide log vs queue first: replay and fan-out → log; per-message ack, routing, "forget after delivery" → queue.
- pass 1: n/a (opinion / heuristic)
- pass 2: n/a — takeaway/advice

### C1159 · k-alternatives · L4 · li
> RabbitMQ 4.3: exchanges route; quorum queues are the replicated default (delivery limit 20); streams add a replayable log.
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://www.rabbitmq.com/docs/quorum-queues; https://www.rabbitmq.com/docs/streams — "default choice / defaults to 20 / append-only log"
- pass 2: ✅ quorum-queues: default replicated choice, delivery limit 20; streams page

### C1160 · k-alternatives · L4 · li
> Pulsar: stateless brokers + BookKeeper; Exclusive/Failover/Shared/Key_Shared subscriptions; built-in retry letter and DLT topics.
- pass 1: ✅ https://pulsar.apache.org/docs/next/concepts-architecture-overview/; https://pulsar.apache.org/docs/next/concepts-messaging/ — "stateless / Exclusive, Failover, Shared, Key_Shared / Retry Letter Topic"
- pass 2: ✅ Pulsar architecture + concepts-messaging (subscription types, retry letter topic, DLT)

### C1161 · k-alternatives · L4 · li
> Redpanda: Kafka API, different engine (C++, Raft per partition, single binary). Verify newer-feature parity before relying on it (e.g. no server-side KIP-890 / Transactions V2).
- pass 1: ✅ revised after pass-2 finding — C1127: Redpanda paragraph now states it "does not implement the server-side portion of KIP-890" (clients fall back, no Transactions V2); KIP-848/share groups not mentioned in its compatibility docs; bullet C1161 updated likewise (part-5.html). Table/chooser un
- pass 2: ✅ docs.redpanda.com/current/develop/kafka-clients/ — "Redpanda does not implement the server-side portion of KIP-890"; architecture page "Every topic partition forms a Raft group"

### C1162 · k-alternatives · L4 · li
> Kinesis (shards, 24 h default retention up to 365 d) and Pub/Sub (ordering keys off by default) trade Kafka portability for zero ops.
- pass 1: ✅ https://docs.aws.amazon.com/streams/latest/dev/key-concepts.html; https://docs.cloud.google.com/pubsub/docs/ordering — "default of 24 hours / up to 8760 hours (365 days) / disabled by default"
- pass 2: ✅ AWS key-concepts 24 h default, 8760 h (365 d) max; Pub/Sub ordering off unless enabled at subscription creation

### C1163 · k-alternatives · L4 · li
> Share groups (Kafka 4.2+) are the in-Kafka queue: more consumers than partitions, per-record ack, delivery limit; no ordering.
- pass 1: ✅ kafka-4.3.1-src/docs/design/design.md:254-257; kafka-4.3.1-src/docs/getting-started/upgrade.md:84 — "can exceed the number of partitions / rather than as part of an ordered stream"
- pass 2: ✅ upgrade.md:84 production-ready in 4.2; design.md:255; AcknowledgeType; delivery.count.limit

### C1164 · k-alternatives · L4 · li
> All comparisons are dated Sept 2026. Re-check vendor docs before deciding.
- pass 1: n/a (dating note)
- pass 2: n/a — advisory
