# Claims ledger — kafka-course.html — js_22_troubleshooting

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1554 · js:22. troubleshooting · L- · script
> handler slower than the produce rate
- pass 1: n/a (pedagogy / analogy)
- pass 2: ✅ mechanism: lag grows when processing < produce rate (records-lag-max, monitoring.md:763)

### C1555 · js:22. troubleshooting · L- · script
> profile and speed up processing
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (advice)

### C1556 · js:22. troubleshooting · L- · script
> scale consumers up to the partition count
- pass 1: ✅ docs/operations/basic-kafka-operations.md:44 — "the partition count impacts the maximum parallelism of your consumers"
- pass 2: ✅ docs/design/design.md:157 — "consumed by exactly one consumer within each subscribing consumer group"

### C1557 · js:22. troubleshooting · L- · script
> then add partitions (keys remap)
- pass 1: ✅ docs/operations/basic-kafka-operations.md:63 — "Key Distribution Changes"
- pass 2: ✅ BuiltInPartitioner.java:330 — "Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions" (key→partition changes with partition count)

### C1558 · js:22. troubleshooting · L- · script
> raise the tenant’s quota if justified
- pass 1: n/a (advice)
- pass 2: n/a (advice; quotas per docs/operations/multi-tenancy.md)

### C1559 · js:22. troubleshooting · L- · script
> restore the broker that repeats in the output
- pass 1: n/a (pedagogy / analogy)
- pass 2: ✅ TopicCommand.java:774 --under-replicated-partitions lists Replicas/Isr per partition; the missing broker id repeats

### C1560 · js:22. troubleshooting · L- · script
> more num.replica.fetchers if fetch threads are the limit
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:97 — "Number of fetcher threads used to replicate records from each source broker"
- pass 2: ✅ ReplicationConfigs.java:96 — num.replica.fetchers (NUM_REPLICA_FETCHERS_DEFAULT = 1)

### C1561 · js:22. troubleshooting · L- · script
> ISR size &lt; min.insync.replicas with acks=all, so the broker refuses on purpose
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasException.java:20 — "lower than min.insync.replicas"
- pass 2: ✅ docs/design/design.md:305 — "The number of the in-sync replicas is no less than the min.insync.replicas setting"; Errors.java:220

### C1562 · js:22. troubleshooting · L- · script
> bring replicas back into the ISR
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (advice)

### C1563 · js:22. troubleshooting · L- · script
> lowering min.insync.replicas = knowingly accepting less durability
- pass 1: n/a (opinion / advice)
- pass 2: ✅ docs/design/design.md:305 — min.insync.replicas is the durability floor for acks=all

### C1564 · js:22. troubleshooting · L- · script
> client logs: poll interval exceeded
- pass 1: n/a (pedagogy / analogy)
- pass 2: ✅ AbstractCoordinator.java:1152 — "consumer poll timeout has expired. This means the time between subsequent calls to poll()"

### C1565 · js:22. troubleshooting · L- · script
> processing longer than max.poll.interval.ms (300000)
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:629 — "300000,"
- pass 2: ✅ ConsumerConfig.java:627-629 — max.poll.interval.ms default 300000

### C1566 · js:22. troubleshooting · L- · script
> rolling deploys without static membership
- pass 1: n/a (operational reasoning (static membership: ch.11))
- pass 2: ✅ CommonClientConfigs.java:188 — group.instance.id "avoid group rebalances caused by transient unavailability"

### C1567 · js:22. troubleshooting · L- · script
> lower max.poll.records / speed up the loop
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:92 — "does not impact the underlying fetching behavior" (advice)
- pass 2: ✅ ConsumerConfig.java:94 max.poll.records (default 500); fewer records per poll shortens loop

### C1568 · js:22. troubleshooting · L- · script
> raise the limits consistently (producer + topic)
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:71 — "The maximum record batch size accepted by the broker is defined via"
- pass 2: ✅ ServerLogConfigs.java:177 message.max.bytes/max.message.bytes 1048588; ProducerConfig.java:410 max.request.size 1048576

### C1569 · js:22. troubleshooting · L- · script
> or store the payload elsewhere and send a reference
- pass 1: n/a (design advice)
- pass 2: n/a (advice)

### C1570 · js:22. troubleshooting · L- · script
> retention larger than the volume
- pass 1: n/a (pedagogy / analogy)
- pass 2: ✅ mechanism (retention sizing vs volume); monitoring.md:386 OfflineLogDirectoryCount

### C1571 · js:22. troubleshooting · L- · script
> failed disk, surfacing to clients as KafkaStorageException
- pass 1: ✅ clients/src/main/java/org/apache/kafka/common/errors/KafkaStorageException.java:20 — "Miscellaneous disk-related IOException"
- pass 2: ✅ KafkaStorageException.java:20 — "Miscellaneous disk-related IOException occurred when handling a request."

### C1572 · js:22. troubleshooting · L- · script
> kafka-reassign-partitions.sh --throttle to move replicas
- pass 1: ✅ docs/operations/basic-kafka-operations.md:587 — "--throttle 50000000"
- pass 2: ✅ ReassignPartitionsCommandOptions.java:96 — "The movement of partitions between brokers will be throttled to this value (bytes/sec)"

### C1573 · js:22. troubleshooting · L- · script
> describe --replication (Lag per voter)
- pass 1: ✅ docs/operations/hardware-and-os.md:135 — "describe --replication" ; docs/operations/hardware-and-os.md:136 — "NodeId	DirectoryId           	LogEndOffset	Lag"
- pass 2: ✅ MetadataQuorumCommand.java:209 — columns "NodeId", "DirectoryId", "LogEndOffset", "Lag", ...

### C1574 · js:22. troubleshooting · L- · script
> a majority of controller voters is down or partitioned
- pass 1: n/a (Raft majority reasoning (ch.9))
- pass 2: ✅ docs/operations/kraft.md:47 — "A majority of the controllers must be alive in order to maintain availability"

### C1575 · js:22. troubleshooting · L- · script
> replace a lost controller disk only while a healthy majority holds all committed data (check describe --replication); never let a majority start from empty log dirs
- pass 1: ✅ revised after pass-2 finding — C1582 (fig-triage ctl fix; the verifier listed it as C1575): "reformat a lost controller disk only once the majority has caught up" → "replace a lost controller disk only while a healthy majority holds all committed data (check describe --replication); never l
- pass 2: ✅ docs/operations/hardware-and-os.md:132-135 — "should not be formatted and started until the majority of the controllers have all of the committed data"; uses describe --replication

### C1576 · js:22. troubleshooting · L- · script
> committed offset deleted by retention (consumer down longer than retention)
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:172 — "current offset does not exist any more on the server"
- pass 2: ✅ ConsumerConfig.java:172-173 — "current offset does not exist any more on the server (e.g. because that data has been deleted)"

### C1577 · js:22. troubleshooting · L- · script
> auto.offset.reset (default latest) decides where it lands
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:546 — "AutoOffsetResetStrategy.LATEST.name()"
- pass 2: ✅ ConsumerConfig.java:544-546 — auto.offset.reset default AutoOffsetResetStrategy.LATEST

### C1578 · js:22. troubleshooting · L- · script
> choose auto.offset.reset deliberately (none fails loudly)
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:178 — "none: throw exception to the consumer"
- pass 2: ✅ ConsumerConfig.java:178 — "none: throw exception to the consumer if no previous offset is found"

### C1579 · js:22. troubleshooting · L- · script
> longer retention for slow consumers
- pass 1: n/a (advice)
- pass 2: n/a (advice)

### C1580 · js:22. troubleshooting · L- · script
> Pick a symptom, or press ▶ to walk the most common one.
- pass 1: n/a (UI hint)
- pass 2: n/a (UI hint)

### C1581 · js:22. troubleshooting · L- · script
> Read the gauges before moving furniture.
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (analogy)

### C1582 · js:22. troubleshooting · L- · script
> Match the readings to a cause.
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (UI narration)

### C1583 · js:22. troubleshooting · L- · script
> …: check → cause → fix, lit in order. Real incidents can combine several causes; fix the first red gauge first.
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (UI narration/advice)

### C1584 · js:22. troubleshooting · L- · script
> The billing group lags by 120k records. Four partitions, four busy consumers. Someone shouts "just add consumers!"
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative scenario numbers)

### C1585 · js:22. troubleshooting · L- · script
> Eight consumers now, and the group rebalances…
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (illustrative narration)

### C1586 · js:22. troubleshooting · L- · script
> Lag didn’t drop; it grew during the rebalance. A partition goes to at most one consumer in a group, so consumers 5–8 sit idle. Diagnose first: if the handler is slow, speed it up; if you really need more parallelism, add partitions.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:44 — "the partition count impacts the maximum parallelism of your consumers" (lag numbers illustrative)
- pass 2: ✅ docs/design/design.md:157 — each partition "consumed by exactly one consumer within each subscribing consumer group at any given time"; extra consumers idle, rebalance pauses work
