# Claims ledger — kafka-course.html — k-consumer

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0124 · k-consumer · L1 · p
> A consumer reads records from topics. In the archive, a consumer is a clerk who reads a ledger book line by line and does something with each entry: updates a balance, sends an email, copies it into a data warehouse.
- pass 1: n/a (analogy)
- pass 2: n/a analogy (first sentence trivially true)

### C0125 · k-consumer · L1 · h3
> Pull, not push
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0126 · k-consumer · L1 · p
> Kafka consumers pull. The consumer sends fetch requests to the partition leaders and says "give me records starting at offset N". The broker never pushes data at a consumer. The design docs explain why: with push, a slow consumer gets flooded when production outpaces it. With pull, a slow consumer just falls behind and catches up when it can. Pull also batches naturally, because each fetch takes everything available after the position (up to a size limit).
- pass 1: ✅ docs/design/design.md:137-143 — "issuing \"fetch\" requests to the brokers leading the partitions"; "the consumer simply falls behind and catches up when it can"
- pass 2: ✅ docs/design/design.md:137,141,143 — "the consumer tends to be overwhelmed when its rate of consumption falls below the rate of production"; "pulls all available messages after its current position... (or up to some configurable max size)"

### C0127 · k-consumer · L1 · p
> What if there's nothing new? The consumer doesn't spin in a tight loop. A fetch can wait on the broker (a "long poll") until at least fetch.min.bytes (default 1 byte) is available or fetch.max.wait.ms (default 500 ms) has passed.
- pass 1: ✅ docs/design/design.md:145 + clients/.../consumer/ConsumerConfig.java:187-188,206-210 — "\"long poll\""; "DEFAULT_FETCH_MIN_BYTES = 1"; "DEFAULT_FETCH_MAX_WAIT_MS = 500"
- pass 2: ✅ docs/design/design.md:145 "block in a 'long poll'"; clients/.../consumer/ConsumerConfig.java:187,210 — DEFAULT_FETCH_MIN_BYTES = 1; DEFAULT_FETCH_MAX_WAIT_MS = 500

### C0128 · k-consumer · L1 · h3
> The poll loop
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0129 · k-consumer · L1 · p
> Your code calls consumer.poll(timeout) in a loop. Each call returns a batch of records (at most max.poll.records, default 500), you process them, then you poll again:
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:94 — "DEFAULT_MAX_POLL_RECORDS = 500"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:94,621-623 — "DEFAULT_MAX_POLL_RECORDS = 500"

### C0130 · k-consumer · L1 · pre
> consumer.subscribe(List.of("payments")); while (running) { ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); for (ConsumerRecord<String, String> r : records) { handle(r.key(), r.value()); // your business logic } // with enable.auto.commit=true (default), progress is committed in the background }
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java usage examples §172ff + clients/.../consumer/ConsumerConfig.java:458-462 — subscribe/poll loop; enable.auto.commit default true
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:458-460 — enable.auto.commit Type.BOOLEAN default true; poll(Duration)/subscribe(Collection) are real KafkaConsumer APIs

### C0131 · k-consumer · L1 · p
> Two rules to remember from the Javadoc. The KafkaConsumer is not thread-safe: use one instance per thread. And you must call poll() often enough. If more than max.poll.interval.ms (default 300000 ms, 5 minutes) passes between polls, the consumer is considered failed and its partitions are given to someone else.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:62,440 + clients/.../consumer/ConsumerConfig.java:627-631 + clients/.../CommonClientConfigs.java:192-195 — "NOT thread-safe"; "300000"; "considered failed and the group will rebalance"
- pass 2: ✅ clients/.../consumer/KafkaConsumer.java:62 "The consumer is not thread-safe"; clients/.../consumer/ConsumerConfig.java:627-629 default 300000; CommonClientConfigs.java:192-195 — "the consumer is considered failed and the group will rebalance"

### C0132 · k-consumer · L1 · h3
> Position vs committed offset: the bookmark
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0133 · k-consumer · L1 · p
> A consumer tracks two numbers per partition:
- pass 1: n/a (lead-in)
- pass 2: ✅ clients/.../consumer/KafkaConsumer.java:79 — "There are actually two notions of position"

### C0134 · k-consumer · L1 · li
> Position: the offset of the next record it will receive. It's one more than the highest offset it has seen, and it advances automatically with every poll(). It lives in the consumer's memory.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:82-84 — "one larger than the highest offset the consumer has seen … automatically advances every time the consumer receives messages"
- pass 2: ✅ clients/.../consumer/KafkaConsumer.java:81-83 — "one larger than the highest offset the consumer has seen... automatically advances every time... poll"

### C0135 · k-consumer · L1 · li
> Committed offset: the last position stored safely in Kafka. If the consumer crashes and restarts (or another consumer takes over), it resumes from here.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:86-87 — "last offset that has been stored securely. Should the process fail and restart, this is the offset"
- pass 2: ✅ clients/.../consumer/KafkaConsumer.java:85-86 — "the last offset that has been stored securely. Should the process fail and restart... recover to"

### C0136 · k-consumer · L1 · p
> In the archive, the committed offset is a bookmark the clerk slips into the book and registers at the front desk. The position is just where the clerk's finger is right now. If the clerk faints, the finger's position is lost, but the bookmark stays.
- pass 1: n/a (analogy)
- pass 2: n/a analogy

### C0137 · k-consumer · L1 · p
> Where do bookmarks live? In an internal, compacted Kafka topic called __consumer_offsets. Each group has one broker acting as its group coordinator. The consumer finds it with a FindCoordinator request and sends its offset commits there. The coordinator writes each commit to __consumer_offsets and replies only after all replicas of that topic have it. By default the topic has 50 partitions (offsets.topic.num.partitions) and replication factor 3 (offsets.topic.replication.factor).
- pass 1: ✅ docs/implementation/distribution.md:31-33 + group-coordinator/.../GroupCoordinatorConfig.java:114,123 — "FindCoordinatorRequest"; "only after all the replicas of the offsets topic receive the offsets"; 50; 3
- pass 2: ✅ docs/implementation/distribution.md:31,33 — "FindCoordinatorRequest"; "compacted Kafka topic named __consumer_offsets... only after all the replicas of the offsets topic receive"; GroupCoordinatorConfig.java:114,123 — defaults 50 and 3

### C0138 · k-consumer · L1 · p
> By default (enable.auto.commit=true), the consumer commits its position automatically every auto.commit.interval.ms (default 5000 ms). You can switch that off and call commitSync() or commitAsync() yourself.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:140,458-467 + clients/.../consumer/KafkaConsumer.java:88-89 — "periodically committed in the background"; "5000"; commitSync/commitAsync
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:458-467 — enable.auto.commit default true, auto.commit.interval.ms default 5000; clients/.../consumer/KafkaConsumer.java:88 commitSync/commitAsync

### C0139 · k-consumer · L1 · p
> The order of process and commit decides your delivery guarantee. Commit then process: a crash in between skips records (at-most-once). Process then commit: a crash in between re-delivers records (at-least-once). At-least-once is Kafka's default promise. So write handlers that can safely see the same record twice.
- pass 1: ✅ docs/design/design.md:199-200,207 — "at-most-once"; "at-least-once"; "Kafka guarantees at-least-once delivery by default"
- pass 2: ✅ docs/design/design.md:198-200,207 — "at-most-once"/"at-least-once" orderings; "Kafka guarantees at-least-once delivery by default"

### C0140 · k-consumer · L1 · h3
> Consumer groups: a team of clerks
- pass 1: n/a (heading)
- pass 2: n/a heading

### C0141 · k-consumer · L1 · p
> One clerk can't keep up with a big series. So consumers join a consumer group by sharing the same group.id. Kafka splits the topic's partitions among the group's members so that each partition is read by exactly one consumer in the group. A topic with four partitions and two consumers gives each consumer two partitions. When a consumer joins, leaves or crashes, the partitions are redistributed. That's called a rebalance.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:96-112 — "each partition is assigned to exactly one consumer in the group … four partitions … two processes … two partitions"; "rebalancing"
- pass 2: ✅ docs/design/design.md:157 — "consumed by exactly one consumer within each subscribing consumer group at any given time"; 4/2 split is arithmetic

### C0142 · k-consumer · L1 · p
> Different groups are completely independent. Each group gets all the records and keeps its own bookmarks. That covers both classic messaging styles. Queue-like: all workers in one group share the load. Pub-sub-like: each application uses its own group and sees everything.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:114-122 — "any number of consumer groups"; "semantics similar to a queue … pub-sub"
- pass 2: ✅ docs/getting-started/introduction.md / design.md consumer-group model — each group independently tracks its own offsets; queue vs pub-sub via one vs many groups (standard Kafka docs framing)

### C0143 · k-consumer · L1 · tr
> P0 | C1 | 100 | 120 | 20
- pass 1: n/a (labelled illustrative (figure table))
- pass 2: n/a illustrative row (arithmetic consistent: 120-100=20)

### C0144 · k-consumer · L1 · tr
> P1 | C1 | 90 | 95 | 5
- pass 1: n/a (labelled illustrative (figure table))
- pass 2: n/a illustrative row (95-90=5)

### C0145 · k-consumer · L1 · tr
> P2 | C1 | 131 | 140 | 9
- pass 1: n/a (labelled illustrative (figure table))
- pass 2: n/a illustrative row (140-131=9)

### C0146 · k-consumer · L1 · tr
> P3 | C1 | 104 | 110 | 6
- pass 1: n/a (labelled illustrative (figure table))
- pass 2: n/a illustrative row (110-104=6)

### C0147 · k-consumer · L1 · figcaption
> The assignment is a simple even split for illustration. The real assignor depends on group.protocol and assignor settings (chapter 11). Offsets and lag numbers are illustrative. The rule that each partition has exactly one owner within the group is real.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99 — "each partition is assigned to exactly one consumer in the group" (assignment labelled illustrative)
- pass 2: ✅ figcaption correctly labels illustration; one-owner rule = design.md:157

### C0148 · k-consumer · L1 · div
> My brand-new group started and read nothing, even though the topic is full of data. Broken?
- pass 1: n/a (question)
- pass 2: n/a question

### C0149 · k-consumer · L1 · div
> No. That's auto.offset.reset. When a group has no committed offset for a partition (or the offset no longer exists), this setting decides where to start. The default is latest: only records produced from now on. Use earliest to start from the oldest available record, none to get an exception, or by_duration:<ISO-8601 duration> to start that far back in time.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:172-179,546 — "no initial offset … or if the current offset does not exist any more"; LATEST default; earliest/by_duration/none
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:172-178,544-546 — "no initial offset... or if the current offset does not exist any more"; default LATEST; earliest/none/by_duration:<ISO8601>

### C0150 · k-consumer · L1 · div
> Can I have more consumers than partitions to go faster?
- pass 1: n/a (question)
- pass 2: n/a question

### C0151 · k-consumer · L1 · div
> Not within one group. The extra consumers sit idle, because a partition can't be shared inside a group. The partition count is the ceiling on a group's parallelism. (Share groups, production-ready since Kafka 4.2, let several consumers share one partition queue-style. Chapter 18 covers them.)
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99 + docs/getting-started/upgrade.md:84 — "exactly one consumer in the group"; "Queues for Kafka (KIP-932) is production-ready in Apache Kafka 4.2"
- pass 2: ✅ design.md:157 (one consumer per partition per group); docs/getting-started/upgrade.md:84 — "Queues for Kafka (KIP-932) is production-ready in Apache Kafka 4.2"

### C0152 · k-consumer · L1 · p
> Consume payments as group billing, stop it with Ctrl-C, then inspect the group:
- pass 1: n/a (exercise lead-in)
- pass 2: n/a instruction

### C0153 · k-consumer · L1 · pre
> bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \ --topic payments --group billing --from-beginning bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group billing
- pass 1: ✅ ConsoleConsumerOptions.java:74,163,175,195; ConsumerGroupCommandOptions.java:127,131,141 — --topic, --from-beginning, --bootstrap-server, --group, --describe
- pass 2: ✅ tools/.../ConsoleConsumerOptions.java (--topic, --group, --from-beginning); docs/operations/basic-kafka-operations.md:146 (kafka-consumer-groups.sh --describe --group)

### C0154 · k-consumer · L1 · p
> 1) What do CURRENT-OFFSET, LOG-END-OFFSET and LAG mean? 2) Run the console consumer again with the same group and --from-beginning. Why don't the old records come back? 3) How would you replay them?
- pass 1: n/a (exercise question)
- pass 2: n/a quiz question

### C0155 · k-consumer · L1 · p
> 1) Committed offset, the partition's latest readable offset (its high watermark), and their difference: how far behind the group is. 2) --from-beginning only applies when the consumer doesn't already have an established offset. The group committed one, so it resumes there. 3) With all group members stopped: kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group billing --topic payments --reset-offsets --to-earliest --execute. Without --execute it's a dry run that only shows the plan.
- pass 1: ✅ revised after pass-2 finding — C0155 — sharpen-pencil answer: LOG-END-OFFSET now "the partition's latest readable offset (its high watermark)" instead of "next offset to be written" — tools/.../OffsetsUtils.java:222-223 OffsetSpec.latest(); core/.../cluster/Partition.scala:1443 (latest → hi
- pass 2: ✅ tools/.../ConsumerGroupCommandOptions.java:54-55 — "--dry-run Only show results without executing"; "--execute Execute operation"; LOG-END-OFFSET/LAG from latest offsets (HW)

### C0156 · k-consumer · L1 · tr
> Symptom | Likely cause | Fix
- pass 1: n/a (table header)
- pass 2: n/a (table header)

### C0157 · k-consumer · L1 · tr
> LAG keeps growing | Consumers process slower than producers write | Speed up handling, add consumers (up to the partition count), add partitions
- pass 1: n/a (troubleshooting advice (opinion); parallelism cap from KafkaConsumer.java:98-99)
- pass 2: ✅ advice consistent with one-owner-per-partition rule (design.md:157)

### C0158 · k-consumer · L1 · tr
> One consumer gets nothing | More consumers than partitions | Expected. Remove it or add partitions
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99 — "each partition is assigned to exactly one consumer in the group"
- pass 2: ✅ design.md:157 — one consumer per partition per group, so extras idle

### C0159 · k-consumer · L1 · tr
> Constant rebalances, commits fail after them | Processing one poll's batch takes longer than max.poll.interval.ms | Lower max.poll.records, speed up handling, or raise the interval
- pass 1: ✅ CommonClientConfigs.java:192-195 + clients/.../consumer/CommitFailedException.java — "considered failed and the group will rebalance"; "group rebalance completes before the commit"
- pass 2: ✅ CommonClientConfigs.java:192-195 (poll interval exceeded → rebalance); fixes match max.poll.records / max.poll.interval.ms docs

### C0160 · k-consumer · L1 · tr
> New group sees no old data | auto.offset.reset=latest (default) | Set earliest before the group's first start, or reset offsets
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:172-175,546 — "latest: automatically reset the offset to the latest offset"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:544-546 default latest; reset via --reset-offsets (basic-kafka-operations.md:226)

### C0161 · k-consumer · L1 · tr
> Duplicates after a crash | At-least-once: processed but not yet committed | Make processing idempotent (dedupe by key or ID)
- pass 1: ✅ docs/design/design.md:200 — "the first few messages it receives will already have been processed"
- pass 2: ✅ docs/design/design.md:200 — "first few messages it receives will already have been processed... updates are idempotent"

### C0162 · k-consumer · L2 · p
> Two group protocols. group.protocol defaults to classic. The next-generation protocol from KIP-848 (group.protocol=consumer) became generally available in Kafka 4.0. It moves partition assignment to the broker and replaces the stop-the-world rebalance with incremental, heartbeat-driven reconciliation. Chapter 11 races them against each other.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:115,653-657 + docs/getting-started/upgrade.md:223 + docs/operations/consumer-rebalance-protocol.md:30-32,54 — "Generally Available (GA) in Apache Kafka 4.0"; "fully incremental design"; "server-side assignors"
- pass 2: ✅ ConsumerConfig.java:116-117 "The default value is classic"; upgrade.md:223 "KIP-848... Generally Available (GA) in Apache Kafka 4.0"; consumer-rebalance-protocol.md:32 "fully incremental design... no longer relies on a global synchronization barrier"

### C0163 · k-consumer · L2 · p
> Bookmarks can expire. Committed offsets aren't kept forever. Once a group has no members, its offsets are discarded after offsets.retention.minutes (default 10080 minutes = 7 days). When it comes back, auto.offset.reset applies again.
- pass 1: ✅ GroupCoordinatorConfig.java:145-147 — "7 * 24 * 60"; "expired … after the consumer group loses all its consumers"
- pass 2: ✅ GroupCoordinatorConfig.java:145-147 — "7 * 24 * 60"; "expired and discarded when... retention period has elapsed after the consumer group... becomes empty"

### C0164 · k-consumer · L3 · summary
> L3🔬 Go deeper: where the numbers live in the client
- pass 1: n/a (summary heading)
- pass 2: n/a heading

### C0165 · k-consumer · L3 · p
> In the Java client, KafkaConsumer delegates to one of two implementations: ClassicKafkaConsumer (classic protocol, with ConsumerCoordinator for group membership) or AsyncKafkaConsumer (the KIP-848 consumer protocol, with a background network thread). Both keep per-partition state (assigned partitions, fetch position, paused flags) in SubscriptionState.
- pass 1: ✅ clients/.../consumer/internals/ConsumerDelegateCreator.java:64-66; AsyncKafkaConsumer.java:166 ("ConsumerNetworkThread"); SubscriptionState.java:62-69,393 — pause/position tracking
- pass 2: ✅ internals/ConsumerDelegateCreator.java:63-66 (CONSUMER→AsyncKafkaConsumer else ClassicKafkaConsumer); ClassicKafkaConsumer.java:127 ConsumerCoordinator; AsyncKafkaConsumer.java:166 ConsumerNetworkThread; SubscriptionState.java:62-69 positions/paused

### C0166 · k-consumer · L3 · p
> Why can offset tracking be this cheap? Traditional brokers keep per-message state ("sent", "acknowledged", "consumed"), and they must solve lost-ack and double-delivery problems on the server. Kafka's design gives each partition to exactly one consumer per group, so all the state is one integer per partition per group, checkpointed periodically. The side effect is a feature: you can rewind (seek or reset offsets) and re-process after fixing a bug. With a classic queue that's impossible, because the messages are gone.
- pass 1: ✅ docs/design/design.md:153-159 — "just a single integer … periodically checkpointed"; "deliberately rewind … violates the common contract of a queue"
- pass 2: ✅ docs/design/design.md:155-159 — "multiple states about every single message"; "just a single integer"; "periodically checkpointed"; "deliberately rewind"

### C0167 · k-consumer · L1 · li
> Consumers pull with fetch requests from a given offset; long polling avoids busy loops.
- pass 1: ✅ docs/design/design.md:137,145 — fetch from offset; "long poll"
- pass 2: ✅ docs/design/design.md:137,145

### C0168 · k-consumer · L1 · li
> Position = next offset to read (in memory). Committed offset = saved bookmark in __consumer_offsets.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:82-87 + docs/implementation/distribution.md:33 — "__consumer_offsets"
- pass 2: ✅ clients/.../consumer/KafkaConsumer.java:81-86; distribution.md:33

### C0169 · k-consumer · L1 · li
> Auto-commit is on by default (every 5 s). Process-then-commit gives at-least-once.
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:458-467 + docs/design/design.md:200 — true; 5000; at-least-once
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:458-467; docs/design/design.md:200

### C0170 · k-consumer · L1 · li
> A consumer group splits partitions: each partition has exactly one owner per group. Extra consumers idle.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:98-99 — "exactly one consumer in the group"
- pass 2: ✅ docs/design/design.md:157

### C0171 · k-consumer · L1 · li
> Different groups are independent and each sees every record.
- pass 1: ✅ clients/.../consumer/KafkaConsumer.java:114-116 — "any number of consumer groups for a given topic without duplicating data"
- pass 2: ✅ per-group offsets (distribution.md:31)

### C0172 · k-consumer · L1 · li
> auto.offset.reset defaults to latest. Keep polling within max.poll.interval.ms (5 min).
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:546,627-631 — LATEST; 300000
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:544-546 (latest), 627-629 (300000 ms)
