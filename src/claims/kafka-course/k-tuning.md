# Claims ledger — kafka-course.html — k-tuning

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0801 · k-tuning · L4 · p
> Every Kafka performance "secret" you've read on a forum is one of four trades: latency for throughput (batch more), CPU for bytes (compress), safety for speed (acknowledge less), and memory and file handles for parallelism (more partitions). This chapter gives you the knobs for each trade, their real defaults, and a method for turning them without guessing.
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (framing/opinion)

### C0802 · k-tuning · L4 · p
> In the archive, the clerks are fastest when patrons bring bundles of pages, not single sheets. They're slower when every bundle must be photocopied at two other branches before the patron leaves. And they're slowest of all when their desks are cluttered with ten thousand half-used ledger books. Keep those three images in mind; they explain almost every row below.
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (analogy)

### C0803 · k-tuning · L4 · h3
> The method (before any knob)
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0804 · k-tuning · L4 · li
> Reproduce with the perf tools: kafka-producer-perf-test.sh and kafka-consumer-perf-test.sh give you a baseline you can compare against.
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:240 — "parser.addArgument("--bootstrap-server")" ; tools/src/main/java/org/apache/kafka/tools/ConsumerPerformance.java:287 — "parser.accepts("bootstrap-server""
- pass 2: ✅ bin/kafka-producer-perf-test.sh and bin/kafka-consumer-perf-test.sh exist in 4.3.1 bin/

### C0805 · k-tuning · L4 · li
> Find the bottleneck, don't assume it. The broker's request metrics split TotalTimeMs into RequestQueueTimeMs, LocalTimeMs, RemoteTimeMs, ResponseQueueTimeMs and ResponseSendTimeMs (chapter 15's pipeline). High queue time means the threads are busy. High local time means the disk or log append is slow. High remote time on produce means you're waiting for followers (acks=all).
- pass 1: ✅ docs/operations/monitoring.md:689 — "broken into queue, local, remote and response send time" ; docs/operations/monitoring.md:707 — "Time the request is processed at the leader" ; docs/operations/monitoring.md:728 — "non-zero for produce requests when ack=-1"
- pass 2: ✅ docs/operations/monitoring.md:685-750 — TotalTimeMs "broken into queue, local, remote and response send time"; RemoteTimeMs "non-zero for produce requests when ack=-1" (interpretations of high queue/local time are reasonable heuristics)

### C0806 · k-tuning · L4 · li
> Check thread idleness: NetworkProcessorAvgIdlePercent and RequestHandlerAvgIdlePercent should ideally stay above 0.3 according to the monitoring docs.
- pass 1: ✅ docs/operations/monitoring.md:776 — "name=NetworkProcessorAvgIdlePercent" ; docs/operations/monitoring.md:815 — "name=RequestHandlerAvgIdlePercent"
- pass 2: ✅ monitoring.md:776-782,815-820 NetworkProcessorAvgIdlePercent / RequestHandlerAvgIdlePercent — "between 0 and 1, ideally > 0.3"

### C0807 · k-tuning · L4 · li
> Change one knob, re-run, and keep a log. Two knobs at once teach you nothing.
- pass 1: n/a (opinion / method advice)
- pass 2: n/a (advice)

### C0808 · k-tuning · L4 · figcaption
> Heuristic, illustrative model, not a benchmark. The bars show the direction each knob pushes (qualitative scores from 0 to 100). Real results depend on record size, key distribution, partition count, network and hardware. "Latency" means latency at light load. Under heavy load, batching usually lowers it, as the 4.0 release notes for linger.ms point out.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:101 — "similar or lower producer latency despite the increased linger"
- pass 2: ✅ n/a for the illustrative model; linger claim confirmed: ProducerConfig.java:158 — "efficiency gains from larger batches typically result in similar or lower producer latency" (upgrade.md:285, 4.0.0 notes)

### C0809 · k-tuning · L4 · h3
> Producer knobs
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0810 · k-tuning · L4 · tr
> linger.ms | 5 (was 0 before 4.0) | Upper bound on how long a not-yet-full batch waits for more records. A full batch is sent right away. | Raise it for throughput-heavy pipelines (how far is a judgement call; measure). The docs' example: 50 adds up to 50 ms of latency when there's no load.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:405 — "define(LINGER_MS_CONFIG, Type.LONG, 5" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:154 — "worth of records for a partition it will be sent immediately" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:157 — "would add up to 50ms of latency" ; docs/getting-started/upgrade.md:285 — "The default `linger.ms` changed from 0 to 5"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:405 LINGER_MS default 5; :153-158 — "once we get batch.size worth of records ... sent immediately", "linger.ms=50 ... add up to 50ms of latency ... in the absence of load", "default changed from 0 to 5 in Apache Kafka 4.0"

### C0811 · k-tuning · L4 · tr
> batch.size | 16384 | Per-partition batch buffer size, and the upper bound of a batch. 0 disables batching. | Raise it with linger.ms when records are small and throughput matters. Each batch allocates the full buffer, so memory use grows.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:401 — "define(BATCH_SIZE_CONFIG, Type.INT, 16384" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:91 — "a batch size of zero will disable" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:93 — "buffer of the specified batch size in anticipation"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:401 BATCH_SIZE 16384; :91-94 — "a batch size of zero will disable batching entirely ... always allocate a buffer of the specified batch size"; "upper bound of the batch size"

### C0812 · k-tuning · L4 · tr
> compression.type | none | gzip, snappy, lz4, zstd. Compresses whole batches. | Often worth it for text/JSON (heuristic; measure with compression-rate-avg). Better batching means better compression. Levels: compression.{gzip,lz4,zstd}.level.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:235 — "The default is none (i.e. no compression)" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:237 — "Compression is of full batches of data" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:240 — "COMPRESSION_GZIP_LEVEL_CONFIG = "compression.gzip.level"" (the "often worth it" cell is labelled heuristic)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:397 default NONE; :235-237 — "none, gzip, snappy, lz4, or zstd ... Compression is of full batches ... more batching means better compression"; :398-400 compression.{gzip,lz4,zstd}.level; compression-rate-avg in SenderMetricsRegistry (worth-it for JSON labelled heuristic)

### C0813 · k-tuning · L4 · tr
> acks | all | How many replicas must have the batch before the send succeeds. | Keep all with min.insync.replicas ≥ 2 for anything you'd hate to lose. 1 trades durability for lower latency.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:393 — ""all"," (use-when cell = advice consistent with ch.5)
- pass 2: ✅ clients/.../producer/ProducerConfig.java:391-394 ACKS default "all"; :135-138 acks=1 leader-only, acks=all waits for full ISR

### C0814 · k-tuning · L4 · tr
> buffer.memory | 33554432 (32 MiB) | Total memory for unsent batches. When it's full, send() blocks up to max.block.ms (60000). | Raise it if bufferpool-wait-ratio is non-zero with many partitions per producer.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:389 — "define(BUFFER_MEMORY_CONFIG, Type.LONG, 32 * 1024 * 1024L" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:437 — "60 * 1000," ; docs/operations/monitoring.md:3237 — "bufferpool-wait-ratio"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:389 BUFFER_MEMORY 32*1024*1024L; :214-215 — "the producer will block for max.block.ms after which it will fail"; :435-437 max.block.ms 60*1000; bufferpool-wait-ratio in producer/internals/BufferPool.java

### C0815 · k-tuning · L4 · tr
> max.request.size | 1048576 | Largest request the producer will build. It also caps a single record. | Raise it (with broker/topic max.message.bytes) only for genuinely large records.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:412 — "1024 * 1024," ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:189 — "MAX_REQUEST_SIZE_DOC"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:410-412 MAX_REQUEST_SIZE 1024*1024; :189-193 — "effectively a cap on the maximum uncompressed record batch size"; KafkaProducer.java:419 maxRequestSize used to reject oversized records

### C0816 · k-tuning · L4 · h3
> Consumer knobs
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0817 · k-tuning · L4 · tr
> fetch.min.bytes | 1 | The broker waits until this much data is available (or fetch.max.wait.ms passes) before answering. | Raise it for throughput and fewer fetch requests, at "some additional latency".
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:187 — "DEFAULT_FETCH_MIN_BYTES = 1" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:188 — "at the cost of some additional latency"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:187-188 DEFAULT_FETCH_MIN_BYTES = 1 — "improve server throughput a bit at the cost of some additional latency"

### C0818 · k-tuning · L4 · tr
> fetch.max.wait.ms | 500 | Maximum time the broker holds a fetch in purgatory waiting for fetch.min.bytes. | Lower it only if you raised fetch.min.bytes and latency suffers.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:210 — "DEFAULT_FETCH_MAX_WAIT_MS = 500"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:206-210 DEFAULT_FETCH_MAX_WAIT_MS = 500 — "maximum amount of time the server will block ... fetch.min.bytes" (advice n/a)

### C0819 · k-tuning · L4 · tr
> fetch.max.bytes | 52428800 (50 MiB) | Soft cap per fetch response. The first batch is always returned so the consumer can make progress. | Lower it for memory-constrained consumers.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:200 — "DEFAULT_FETCH_MAX_BYTES = 50 * 1024 * 1024" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:196 — "will still be returned to ensure that the consumer can make progress"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:194-200 DEFAULT_FETCH_MAX_BYTES = 50*1024*1024 — "record batch will still be returned to ensure that the consumer can make progress"

### C0820 · k-tuning · L4 · tr
> max.partition.fetch.bytes | 1048576 | Per-partition cap per fetch (also soft). | Relevant when a few partitions are very hot.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:225 — "DEFAULT_MAX_PARTITION_FETCH_BYTES = 1 * 1024 * 1024"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:219-225 DEFAULT_MAX_PARTITION_FETCH_BYTES = 1*1024*1024 — "maximum amount of data per-partition", first batch still returned

### C0821 · k-tuning · L4 · tr
> max.poll.records | 500 | Records returned per poll(). It doesn't change fetching; records are cached and handed out incrementally. | Lower it when processing is slow, so each loop finishes inside max.poll.interval.ms (300000).
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:94 — "DEFAULT_MAX_POLL_RECORDS = 500" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:92 — "does not impact the underlying fetching behavior" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:629 — "300000,"
- pass 2: ✅ clients/.../consumer/ConsumerConfig.java:91-94 — "does not impact the underlying fetching behavior ... returns them incrementally", default 500; :627-629 max.poll.interval.ms 300000

### C0822 · k-tuning · L4 · h3
> Broker knobs
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0823 · k-tuning · L4 · tr
> num.network.threads | 3 | Threads that read requests from and write responses to sockets (per listener). | Raise it when NetworkProcessorAvgIdlePercent is low.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:152 — "NUM_NETWORK_THREADS_DEFAULT = 3" ; server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:153 — "each listener (except for controller listener)"
- pass 2: ✅ server/.../network/SocketServerConfigs.java:152-153 default 3 — "each listener (except for controller listener) creates its own thread pool"

### C0824 · k-tuning · L4 · tr
> num.io.threads | 8 | Request handler threads that process requests, which "may include disk I/O". | Raise it when RequestHandlerAvgIdlePercent is low and RequestQueueTimeMs grows.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/config/ServerConfigs.java:51 — "NUM_IO_THREADS_DEFAULT = 8" ; server-common/src/main/java/org/apache/kafka/server/config/ServerConfigs.java:52 — "which may include disk I/O"
- pass 2: ✅ server-common/.../ServerConfigs.java:51-52 default 8 — "processing requests, which may include disk I/O"

### C0825 · k-tuning · L4 · tr
> queued.max.requests | 500 | Request queue depth before network threads block. | Rarely. A full queue is a symptom; fix the slow stage behind it.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:144 — "QUEUED_MAX_REQUESTS_DEFAULT = 500" ; server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:145 — "before blocking the network threads"
- pass 2: ✅ SocketServerConfigs.java:144-145 default 500 — "number of queued requests allowed for data-plane, before blocking the network threads"

### C0826 · k-tuning · L4 · tr
> num.replica.fetchers | 1 | Fetcher threads per source broker for replication. | Followers lag (MaxLag growing) while the leader's disk and network are fine.
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:96 — "NUM_REPLICA_FETCHERS_DEFAULT = 1" ; server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:97 — "Number of fetcher threads used to replicate records from each source broker"
- pass 2: ✅ server/.../ReplicationConfigs.java:96-97 default 1 — "Number of fetcher threads used to replicate records from each source broker"

### C0827 · k-tuning · L4 · tr
> socket.send/receive.buffer.bytes | 102400 | TCP buffer sizes on the broker socket server. | High-latency links (cross-DC). The OS max socket buffer must allow it too.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:88 — "SOCKET_SEND_BUFFER_BYTES_DEFAULT = 100 * 1024" ; server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:92 — "SOCKET_RECEIVE_BUFFER_BYTES_DEFAULT = 100 * 1024" ; docs/operations/hardware-and-os.md:44 — "Max socket buffer size"
- pass 2: ✅ SocketServerConfigs.java:88,92 socket send/receive buffer default 100*1024 (OS-limit advice n/a)

### C0828 · k-tuning · L4 · tr
> num.recovery.threads.per.data.dir | 2 (was 1 before 4.0) | Threads for log recovery on startup and flushing at shutdown. | Faster restarts after an unclean shutdown, "at the expense of extra IO cycles".
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/config/ServerLogConfigs.java:147 — "NUM_RECOVERY_THREADS_PER_DATA_DIR_DEFAULT = 2" ; server-common/src/main/java/org/apache/kafka/server/config/ServerLogConfigs.java:148 — "log recovery at startup and flushing at shutdown" ; docs/getting-started/upgrade.md:314 — "at the expense of extra IO cycles"
- pass 2: ✅ ServerLogConfigs.java:147-148 default 2 — "log recovery at startup and flushing at shutdown"; upgrade.md:314 (4.0.0) — "changed from 1 to 2 ... at the expense of extra IO cycles"

### C0829 · k-tuning · L4 · h3
> Partitions: the parallelism you pay for
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0830 · k-tuning · L4 · p
> The partition count is the maximum parallelism of a consumer group, and each partition must fit entirely on one server. So more partitions means more parallel consumers and spreads the load across more brokers. But each partition's segments cost file descriptors and memory map areas. The OS docs recommend at least 100000 file descriptors for the broker as a starting point. Each segment uses 2 map areas (index and time index), and the docs warn that 50000 partitions on one broker means 100000 map areas, which "likely" crashes it with OutOfMemoryError (Map failed) at a typical vm.max_map_count of about 65535. Remember too that you can add partitions but never remove them, and adding them remaps keys.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:44 — "each partition must fit entirely on a single server" ; docs/operations/hardware-and-os.md:43 — "We recommend at least 100000 allowed file descriptors" ; docs/operations/hardware-and-os.md:45 — "each log segment uses 2 map areas" ; docs/operations/basic-kafka-operations.md:86 — "does not currently support reducing the number of partitions" ; docs/operations/basic-kafka-operations.md:63 — "Key Distribution Changes"
- pass 2: ✅ basic-kafka-operations.md:44 — "each partition must fit entirely on a single server ... maximum parallelism of your consumers"; hardware-and-os.md:43 — "at least 100000 allowed file descriptors"; :45 — "50000 partitions ... 100000 map areas and likely cause broker crash with OutOfMemoryError (Map failed)", ~65535; basic-kafka-operations.md:86 no reducing; :63 key mapping changes

### C0831 · k-tuning · L4 · h3
> OS and disks
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0832 · k-tuning · L4 · p
> Kafka leans on the page cache. The hardware docs give a back-of-the-envelope memory estimate: buffer about 30 seconds of writes, write_throughput × 30. Keep Kafka data on dedicated drives (not shared with application logs), mount with noatime, and prefer XFS. In the docs' comparison XFS gave "160ms vs. 250ms+" request local time against the best EXT4 configuration. Keep the default flush settings (no application fsync): durability comes from replication, and forcing fsync gives the OS less room to reorder writes and adds latency.
- pass 1: ✅ docs/operations/hardware-and-os.md:31 — "write_throughput*30" ; docs/operations/hardware-and-os.md:50 — "not sharing the same drives used for Kafka data with application logs" ; docs/operations/hardware-and-os.md:103 — "noatime" ; docs/operations/hardware-and-os.md:97 — "160ms vs. 250ms+" ; docs/operations/hardware-and-os.md:66 — "We recommend using the default flush settings" ; docs/operations/hardware-and-os.md:68 — "it can introduce latency as fsync"
- pass 2: ✅ hardware-and-os.md:31 — "buffer for 30 seconds ... write_throughput*30"; :50 not sharing drives with application logs; :97 — "160ms vs. 250ms+ for the best EXT4 configuration"; :103 noatime; :66-68 default flush settings, fsync "gives the OS less leeway to re-order writes"

### C0833 · k-tuning · L4 · div
> linger.msEveryone blames me for latency. I wait five lousy milliseconds!
- pass 1: n/a (fireside chat / humor)
- pass 2: n/a (dialogue/joke; 5 ms matches default)

### C0834 · k-tuning · L4 · div
> acks=allAt least you're predictable. I wait for the slowest in-sync follower, and nobody can tell me how long that takes.
- pass 1: n/a (fireside chat / humor)
- pass 2: n/a (dialogue)

### C0835 · k-tuning · L4 · div
> linger.msBut without me, you'd be doing that wait for a hundred tiny batches instead of one fat one.
- pass 1: n/a (fireside chat / humor)
- pass 2: n/a (dialogue)

### C0836 · k-tuning · L4 · div
> acks=allFine. Let's agree: under load we're a team, and at idle we're both pure overhead that buys safety and efficiency.
- pass 1: n/a (fireside chat / humor)
- pass 2: n/a (dialogue)

### C0837 · k-tuning · L4 · p
> Run the producer perf test twice against a local topic: once with defaults, once with linger.ms=50 batch.size=262144 compression.type=lz4. Which metric should improve, and which should get worse at light load?
- pass 1: n/a (exercise prompt / pedagogy)
- pass 2: n/a (exercise prompt)

### C0838 · k-tuning · L4 · pre
> bin/kafka-producer-perf-test.sh --bootstrap-server localhost:9092 --topic perf \ --num-records 500000 --record-size 200 --throughput -1 bin/kafka-producer-perf-test.sh --bootstrap-server localhost:9092 --topic perf \ --num-records 500000 --record-size 200 --throughput -1 \ --command-property linger.ms=50 batch.size=262144 compression.type=lz4
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:262 — "parser.addArgument("--num-records")" ; tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:270 — "payloadOptions.addArgument("--record-size")" ; tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:312 — "Set this to -1 to disable throttling" ; tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:324 — "parser.addArgument("--command-property")"
- pass 2: ✅ ProducerPerformance.java:240-324 --bootstrap-server/--topic/--num-records/--record-size/--throughput; --command-property nargs("+") — example "--command-property linger.ms=10 batch.size=32768"

### C0839 · k-tuning · L4 · p
> Records/sec and MB/s should go up (fewer, fuller, compressed batches). With --throughput set to a low rate instead of -1, average latency grows by up to the linger time, because batches wait to fill. Add --print-metrics and compare batch-size-avg, compression-rate-avg and record-queue-time-avg. The exact numbers depend on your machine.
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:350 — "parser.addArgument("--print-metrics")" ; clients/src/main/java/org/apache/kafka/clients/producer/internals/SenderMetricsRegistry.java:88 — "record-queue-time-avg" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:157 — "would add up to 50ms of latency" (exact numbers: stated as machine-dependent)
- pass 2: ✅ ProducerPerformance.java:350 --print-metrics; batch-size-avg, compression-rate-avg, record-queue-time-avg in SenderMetricsRegistry; latency effect per clients/.../producer/ProducerConfig.java:157 (numbers machine-dependent)

### C0840 · k-tuning · L4 · summary
> L4🔬 Go deeper: where the time goes inside the producer
- pass 1: n/a (heading)
- pass 2: n/a (summary heading)

### C0841 · k-tuning · L4 · p
> The producer's sender metrics (SenderMetricsRegistry) are your tuning dashboard. record-queue-time-avg is how long batches sit in the RecordAccumulator (dominated by linger.ms at light load). batch-size-avg shows whether batches actually fill. compression-rate-avg shows what compression buys. request-latency-avg is the network plus broker round trip, and produce-throttle-time-avg exposes quotas (chapter 20). If record-queue-time-avg is large while batch-size-avg is small, you're lingering for nothing. If bufferpool-wait-ratio is non-zero, buffer.memory is the wall. On the broker, compare LocalTimeMs against RemoteTimeMs for request=Produce to tell a slow disk from a slow follower.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/internals/SenderMetricsRegistry.java:81 — "batch-size-avg" ; clients/src/main/java/org/apache/kafka/clients/producer/internals/SenderMetricsRegistry.java:85 — "compression-rate-avg" ; docs/operations/monitoring.md:3089 — "request-latency-avg" ; docs/operations/monitoring.md:3237 — "bufferpool-wait-ratio" ; docs/operations/monitoring.md:728 — "non-zero for produce requests when ack=-1" (diagnostic interpretations are pedagogy)
- pass 2: ✅ SenderMetricsRegistry.java defines record-queue-time-avg, batch-size-avg, compression-rate-avg, request-latency-avg, produce-throttle-time-avg; bufferpool-wait-ratio is in BufferPool.java (producer metric, not SenderMetricsRegistry — wording lists it separately, acceptable); monitoring.md:711,724 LocalTimeMs/RemoteTimeMs request=Produce

### C0842 · k-tuning · L4 · li
> Tuning is a set of trades: latency vs throughput (linger.ms, batch.size, fetch.min.bytes), CPU vs bytes (compression), safety vs speed (acks).
- pass 1: n/a (summary of trades / pedagogy)
- pass 2: n/a (summary of trades)

### C0843 · k-tuning · L4 · li
> Defaults: linger.ms 5, batch.size 16384, compression.type none, acks all, max.poll.records 500, fetch.min.bytes 1.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:405 — "define(LINGER_MS_CONFIG, Type.LONG, 5" ; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:401 — "define(BATCH_SIZE_CONFIG, Type.INT, 16384" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:94 — "DEFAULT_MAX_POLL_RECORDS = 500" ; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:187 — "DEFAULT_FETCH_MIN_BYTES = 1"
- pass 2: ✅ clients/.../producer/ProducerConfig.java:405,401,397,391 (5, 16384, none, all); clients/.../consumer/ConsumerConfig.java:94 max.poll.records 500; clients/.../consumer/ConsumerConfig.java:187 fetch.min.bytes 1

### C0844 · k-tuning · L4 · li
> Diagnose with the request-time breakdown and thread idle percentages (ideally > 0.3) before adding threads.
- pass 1: ✅ docs/operations/monitoring.md:811 — "The average fraction of time the request handler threads are idle"
- pass 2: ✅ monitoring.md:685,776,815 — "ideally > 0.3"

### C0845 · k-tuning · L4 · li
> Partitions buy parallelism but cost file descriptors (≥100000 recommended) and 2 map areas per segment.
- pass 1: ✅ docs/operations/hardware-and-os.md:43 — "We recommend at least 100000 allowed file descriptors" ; docs/operations/hardware-and-os.md:45 — "each log segment uses 2 map areas"
- pass 2: ✅ hardware-and-os.md:43,45 — "at least 100000 allowed file descriptors", "each log segment uses 2 map areas"

### C0846 · k-tuning · L4 · li
> Page cache, dedicated disks, XFS, noatime, and no forced fsync.
- pass 1: ✅ docs/operations/hardware-and-os.md:103 — "noatime" ; docs/operations/hardware-and-os.md:66 — "We recommend using the default flush settings"
- pass 2: ✅ hardware-and-os.md:31,50,97,103,66
