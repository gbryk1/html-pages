# Claims ledger — kafka-course.html — k-request-pipeline

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0573 · k-request-pipeline · L3 · p
> Every produce, fetch, heartbeat and metadata call is a request that travels through the same assembly line inside the broker. When latency spikes, "Kafka is slow" is useless; "produce requests spend 900 ms in the request queue" tells you exactly which part of the line is jammed. This chapter gives you that vocabulary.
- pass 1: n/a (chapter motivation / pedagogy)
- pass 2: n/a — chapter intro (900 ms example illustrative)

### C0574 · k-request-pipeline · L3 · p
> Picture the archive branch's front desk. A doorman (the acceptor) lets visitors in and hands each to one of a few counter clerks (network processors). Clerks don't do the work — they read the form and drop it into an in-tray (the request queue). Behind the scenes, archivists (I/O handler threads) take forms from the tray, write in the ledgers or look things up, and put answers in each clerk's out-tray (response queue). Clerks hand the answers back. And some forms can't be answered yet — "tell me when three branches have a copy" — so they go to a waiting room: purgatory.
- pass 1: n/a (analogy)
- pass 2: n/a — front-desk analogy (maps to SocketServer/RequestChannel/KafkaRequestHandler/purgatory, verified below)

### C0575 · k-request-pipeline · L3 · p
> 1. Acceptor. One thread per listener accepts TCP connections and assigns each to a processor. The docs describe the model as "a single acceptor thread and N processor threads which handle a fixed number of connections each".
- pass 1: ✅ core/src/main/scala/kafka/network/SocketServer.scala:66; docs/implementation/network-layer.md:29 — "1 Acceptor thread per listener, that handles new connections."
- pass 2: ✅ docs/implementation/network-layer.md:28 — "a single acceptor thread and N processor threads which handle a fixed number of connections each"; SocketServer.scala:360 DataPlaneAcceptor per endpoint

### C0576 · k-request-pipeline · L3 · p
> 2. Network processors (num.network.threads, default 3 — per listener). Each runs a non-blocking selector, reads complete requests off its sockets, and puts them in the shared request queue. After reading a request from a connection it mutes that connection until the response is sent, which is how Kafka keeps per-connection ordering: one request per connection is being processed at a time on the broker.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:151-153; core/src/main/scala/kafka/network/SocketServer.scala:1040-1042 — "each listener (except for controller listener) creates its own thread pool / selector.mute(connectionId)"
- pass 2: ✅ SocketServerConfigs.java:152-153 — default 3, "each listener (except for controller listener) creates its own thread pool"; SocketServer.scala:1040-1041 sendRequest then selector.mute

### C0577 · k-request-pipeline · L3 · p
> 3. Request queue (queued.max.requests, default 500). When full, network threads block — they stop reading new requests. This is back-pressure, deliberately.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:143-145 — "The number of queued requests allowed for data-plane, before blocking the network threads"
- pass 2: ✅ SocketServerConfigs.java:144-145 — 500, "number of queued requests allowed for data-plane, before blocking the network threads"; RequestChannel.scala:353,380 ArrayBlockingQueue.put

### C0578 · k-request-pipeline · L3 · p
> 4. I/O handler threads (num.io.threads, default 8). They run KafkaApis.handle: validate, append to the log (page cache), or read for a fetch. This is where disk, CPU for decompression/validation, and lock contention show up.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/config/ServerConfigs.java:50-51; core/src/main/scala/kafka/server/KafkaApis.scala:152 — "NUM_IO_THREADS_DEFAULT = 8"
- pass 2: ✅ ServerConfigs.java:51-52 — 8, "threads that the server uses for processing requests, which may include disk I/O"; KafkaRequestHandler.scala:163 apis.handle

### C0579 · k-request-pipeline · L3 · p
> 5. Purgatory. If a request can't be completed right now, it's parked as a delayed operation and the I/O thread moves on. A produce with acks=all waits in the produce purgatory until the in-sync followers have fetched the data (or the request's timeout — the producer's request.timeout.ms, default 30 s — expires). A fetch waits in the fetch purgatory until fetch.min.bytes (default 1) are available or fetch.max.wait.ms (default 500) passes. That's how long-polling works without burning a thread per waiting consumer.
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/purgatory/DelayedProduce.java:122-131; core/src/main/scala/kafka/server/KafkaApis.scala:544; clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:187,210 — "timeout = produceRequest.timeout.toLong / DEFAULT_FETCH_MIN_BYTES = 1 / DEFAULT_FETCH_MAX_WAIT_MS = 500"
- pass 2: ✅ ProducerConfig.java:441-443 request.timeout.ms 30*1000; ConsumerConfig.java:187,210 fetch.min.bytes 1, fetch.max.wait.ms 500; monitoring.md:655-672 produce/fetch purgatory; DelayedOperationPurgatory.java:122 tryCompleteElseWatch

### C0580 · k-request-pipeline · L3 · p
> 6. Response queue → processor → socket. Each processor has its own response queue; it writes the response and unmutes the connection.
- pass 1: ✅ core/src/main/scala/kafka/network/SocketServer.scala:829,1209-1214,945-947 — "private val responseQueue = new LinkedBlockingDeque / ChannelMuteEvent.RESPONSE_SENT"
- pass 2: ✅ SocketServer.scala:829 per-Processor responseQueue; :1077-1078 RESPONSE_SENT → tryUnmuteChannel

### C0581 · k-request-pipeline · L3 · figcaption
> A tick-based toy model: 3 network threads, 8 I/O threads and a request queue scaled down to 24 slots (real default 500). Service and replication times are illustrative; the metric names are the real JMX names under kafka.network:type=RequestMetrics.
- pass 1: n/a (figcaption, labelled illustrative; metric names per docs/operations/monitoring.md:685-750)
- pass 2: ✅ docs/operations/monitoring.md:685-750 — kafka.network:type=RequestMetrics,name=…; defaults 3/8/500 per SocketServerConfigs/ServerConfigs (toy numbers n/a)

### C0582 · k-request-pipeline · L3 · h3
> Reading the latency breakdown
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0583 · k-request-pipeline · L3 · p
> For each request type (Produce, FetchConsumer, FetchFollower, …) the broker publishes TotalTimeMs, split into:
- pass 1: ✅ docs/operations/monitoring.md:685 — "request={Produce|FetchConsumer|FetchFollower} … broken into queue, local, remote and response send time"
- pass 2: ✅ monitoring.md:685 — "TotalTimeMs,request={Produce|FetchConsumer|FetchFollower}… broken into queue, local, remote and response send time"

### C0584 · k-request-pipeline · L3 · tr
> RequestQueueTimeMs | waiting in the request queue | I/O threads saturated — check RequestHandlerAvgIdlePercent
- pass 1: ✅ docs/operations/monitoring.md:698,815 — "Time the request waits in the request queue / RequestHandlerAvgIdlePercent"
- pass 2: ✅ monitoring.md:698 "Time the request waits in the request queue"; :815 RequestHandlerAvgIdlePercent (diagnosis advice reasonable)

### C0585 · k-request-pipeline · L3 · tr
> LocalTimeMs | processing on the leader (append/read) | disk, page-cache misses, compression/validation cost, lock contention
- pass 1: ✅ docs/operations/monitoring.md:711 — "Time the request is processed at the leader"
- pass 2: ✅ monitoring.md:711 — "Time the request is processed at the leader" (causes list = advice)

### C0586 · k-request-pipeline · L3 · tr
> RemoteTimeMs | waiting in purgatory (followers for acks=all; data for fetches) | slow followers / ISR problems for produce; for consumer fetch it's mostly fetch.max.wait.ms and is normal
- pass 1: ✅ docs/operations/monitoring.md:724,672 — "Time the request waits for the follower / non-zero for produce requests when ack=-1"
- pass 2: ✅ monitoring.md:724 — "Time the request waits for the follower… non-zero for produce requests when ack=-1"; RequestChannel.scala:216 remote = responseComplete − localComplete (includes fetch long-poll)

### C0587 · k-request-pipeline · L3 · tr
> ResponseQueueTimeMs | waiting for the network thread | network threads busy — check NetworkProcessorAvgIdlePercent
- pass 1: ✅ docs/operations/monitoring.md:737,776 — "Time the request waits in the response queue / NetworkProcessorAvgIdlePercent"
- pass 2: ✅ monitoring.md:737 "Time the request waits in the response queue"; :776 NetworkProcessorAvgIdlePercent

### C0588 · k-request-pipeline · L3 · tr
> ResponseSendTimeMs | writing the response | slow clients / network, big fetch responses
- pass 1: ✅ docs/operations/monitoring.md:750 — "Time to send the response"
- pass 2: ✅ monitoring.md:750 — "Time to send the response"

### C0589 · k-request-pipeline · L3 · p
> The monitoring docs suggest both idle-percent gauges stay "ideally > 0.3". RequestChannel's RequestQueueSize and the purgatory gauges (PurgatorySize for Produce and Fetch) complete the picture — the docs note the produce purgatory is "non-zero if ack=-1 is used" and the fetch one's "size depends on fetch.wait.max.ms in the consumer".
- pass 1: ✅ docs/operations/monitoring.md:269,659,672,776,815 — "ideally > 0.3 / non-zero if ack=-1 is used / size depends on fetch.wait.max.ms in the consumer"
- pass 2: ✅ monitoring.md:780,819 "ideally > 0.3"; :269 RequestChannel RequestQueueSize; :657 "non-zero if ack=-1 is used"; :672 "size depends on fetch.wait.max.ms in the consumer"

### C0590 · k-request-pipeline · L3 · div
> High RemoteTimeMs on FetchConsumer — is my cluster broken?
- pass 1: n/a (Q&A question)
- pass 2: n/a — FAQ question

### C0591 · k-request-pipeline · L3 · div
> Probably not. An idle consumer's fetch sits in purgatory up to fetch.max.wait.ms waiting for data. That's by design. RemoteTime on Produce is the one to worry about: it's the time followers take to replicate for acks=all.
- pass 1: ✅ docs/operations/monitoring.md:672,724; core/src/main/scala/kafka/server/DelayedFetch.scala:55-66 — "size depends on fetch.wait.max.ms in the consumer"
- pass 2: ✅ monitoring.md:724,672 — fetch purgatory size depends on consumer max wait; RemoteTimeMs "non-zero for produce requests when ack=-1"

### C0592 · k-request-pipeline · L3 · div
> Should I just crank num.io.threads to 64?
- pass 1: n/a (Q&A question)
- pass 2: n/a — FAQ question (note: dynamic thread-pool updates are limited to currentSize/2…currentSize*2, broker-configs.md:162, so 8→64 is not one dynamic step)

### C0593 · k-request-pipeline · L3 · div
> Only if the threads are genuinely idle-starved and the bottleneck isn't the disk itself. More threads hammering a saturated disk just move the queueing from the request queue into the disk queue. Look at LocalTimeMs and disk metrics first. (And you couldn't jump 8 → 64 in one dynamic change anyway: thread-pool updates are limited to currentSize / 2 … currentSize * 2 per step.)
- pass 1: ✅ revised after pass-2 finding — C0593 (num.io.threads to 64 Q&A) — added: dynamic thread-pool updates limited to currentSize/2 … currentSize*2 per step, so 8 → 64 is not one change — docs/configuration/broker-configs.md:162 "Updates are restricted to the range currentSize / 2 to currentSize 
- pass 2: ✅ server/.../DynamicThreadPool.java:30,50-53 — num.io.threads "value should be at least half ... should not be greater than double the current value"

### C0594 · k-request-pipeline · L3 · p
> Producer p99 latency jumped from 20 ms to 800 ms. Triage in five steps:
- pass 1: n/a (workflow prompt)
- pass 2: n/a — exercise framing

### C0595 · k-request-pipeline · L3 · li
> Look at TotalTimeMs,request=Produce on the brokers: if it's low, the delay is client-side (batching, linger.ms, max.block.ms, network).
- pass 1: n/a (operational advice (triage heuristic))
- pass 2: n/a — triage advice; metric name valid (monitoring.md:685)

### C0596 · k-request-pipeline · L3 · li
> Split it: which of Queue / Local / Remote / ResponseQueue / ResponseSend dominates?
- pass 1: ✅ docs/operations/monitoring.md:685 — "broken into queue, local, remote and response send time"
- pass 2: ✅ monitoring.md:698-750 — the five component metrics exist

### C0597 · k-request-pipeline · L3 · li
> Queue high → RequestHandlerAvgIdlePercent near 0 → I/O threads saturated; check what they're doing (Local).
- pass 1: n/a (operational advice (triage heuristic))
- pass 2: ✅ monitoring.md:698,815 — RequestQueueTimeMs; RequestHandlerAvgIdlePercent (advice)

### C0598 · k-request-pipeline · L3 · li
> Remote high → followers slow: check follower fetch TotalTimeMs,request=FetchFollower, ISR shrinks, the network between brokers.
- pass 1: n/a (operational advice (triage heuristic))
- pass 2: ✅ monitoring.md:685 TotalTimeMs,request=FetchFollower exists; advice n/a

### C0599 · k-request-pipeline · L3 · li
> ResponseQueue/Send high → network threads (NetworkProcessorAvgIdlePercent) or huge responses.
- pass 1: n/a (operational advice (triage heuristic))
- pass 2: ✅ monitoring.md:737,750,776 — ResponseQueueTimeMs/ResponseSendTimeMs, NetworkProcessorAvgIdlePercent

### C0600 · k-request-pipeline · L3 · p
> Your broker runs with defaults. Name the three thread/queue settings of this chapter and say which one you'd check first when RequestQueueTimeMs rises. Then read a metric from the command line.
- pass 1: n/a (exercise prompt)
- pass 2: n/a — exercise prompt

### C0601 · k-request-pipeline · L3 · p
> num.network.threads (3), queued.max.requests (500), num.io.threads (8). Rising queue time means requests wait for an I/O thread — check RequestHandlerAvgIdlePercent and LocalTimeMs before touching num.io.threads. Thread counts are dynamically updatable broker configs:
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:144,152; server-common/.../ServerConfigs.java:51; core/src/main/scala/kafka/server/DynamicBrokerConfig.scala:627; server/src/main/java/org/apache/kafka/server/config/DynamicBrokerConfig.java:60 — "NUM_IO_THREADS_CONFIG / NUM_NETWORK_THREADS_CONFIG in reconfigurable sets"
- pass 2: ✅ SocketServerConfigs.java:144,152 / ServerConfigs.java:51 (500, 3, 8); broker-configs.md:162-165 — thread pools "may be updated dynamically at cluster-default level" (range currentSize/2…*2)

### C0602 · k-request-pipeline · L3 · pre
> bin/kafka-configs.sh --bootstrap-server localhost:9092 --entity-type brokers \ --entity-default --alter --add-config num.io.threads=16 bin/kafka-jmx.sh --jmx-url service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi \ --object-name kafka.server:type=KafkaRequestHandlerPool,name=RequestHandlerAvgIdlePercent
- pass 1: ✅ core/src/main/scala/kafka/admin/ConfigCommand.scala:45; tools/src/main/java/org/apache/kafka/tools/JmxTool.java:305,332 — "accepts("object-name" / accepts("jmx-url""
- pass 2: ✅ broker-configs.md:162 (cluster-default dynamic update, 8→16 = 2× allowed); JmxTool.java:305,332 --object-name, --jmx-url; KafkaRequestHandler.scala:206 RequestHandlerAvgIdlePercent

### C0603 · k-request-pipeline · L3 · p
> (The JMX tool needs the broker started with JMX_PORT=9999.)
- pass 1: ✅ bin/kafka-run-class.sh:207-208 — "-Dcom.sun.management.jmxremote.port=$JMX_PORT"
- pass 2: ✅ bin/kafka-run-class.sh:207-208 — "if [ $JMX_PORT ]… -Dcom.sun.management.jmxremote.port=$JMX_PORT"

### C0604 · k-request-pipeline · L3 · summary
> L3🔬 Go deeper: the classes behind the stages
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C0605 · k-request-pipeline · L3 · p
> kafka.network.SocketServer creates an Acceptor per listener and a pool of Processors. A processor does requestChannel.sendRequest(req) and then selector.mute(connectionId); the connection is unmuted on ChannelMuteEvent.RESPONSE_SENT (or later if a quota throttle is active — Part IV).
- pass 1: ✅ core/src/main/scala/kafka/network/SocketServer.scala:66,1040-1042,945-961 — "requestChannel.sendRequest(req) / selector.mute(connectionId) / THROTTLE_ENDED"
- pass 2: ✅ core/.../SocketServer.scala:458 Acceptor, :1040-1041 "requestChannel.sendRequest(req); selector.mute(connectionId)", :947-962 RESPONSE_SENT / THROTTLE_ENDED → tryUnmuteChannel

### C0606 · k-request-pipeline · L3 · p
> kafka.network.RequestChannel holds the bounded request queue (queueSize = queued.max.requests). KafkaRequestHandler threads (the pool is KafkaRequestHandlerPool) poll it and call KafkaApis.handle, which dispatches on the API key.
- pass 1: ✅ core/src/main/scala/kafka/network/RequestChannel.scala:343; core/src/main/scala/kafka/server/KafkaRequestHandler.scala:88; KafkaApis.scala:152 — "class RequestChannel(val queueSize: Int"
- pass 2: ✅ RequestChannel.scala:343,353 — class RequestChannel(val queueSize…), ArrayBlockingQueue(queueSize); KafkaRequestHandler.scala:114,163,223 receiveRequest → apis.handle; KafkaApis.scala:169 apiKey match

### C0607 · k-request-pipeline · L3 · p
> DelayedOperationPurgatory (server-common) keeps delayed operations watched by keys (e.g. a partition) and a hierarchical TimingWheel in a SystemTimer for expiry. tryCompleteElseWatch tries to finish the operation immediately and otherwise registers it; events like "HW advanced" call checkAndComplete for the key.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/purgatory/DelayedOperationPurgatory.java:122; server-common/.../timer/TimingWheel.java:23; core/src/main/scala/kafka/server/ReplicaManager.scala:305-306 — "Hierarchical Timing Wheels / delayedProducePurgatory.checkAndComplete"
- pass 2: ✅ server-common/.../DelayedOperationPurgatory.java:21,51,122 SystemTimer, tryCompleteElseWatch, checkAndComplete; server-common/.../timer/TimingWheel.java

### C0608 · k-request-pipeline · L3 · p
> DelayedProduce completes when every partition is satisfied: this broker is no longer leader, the replica is gone, or enough replicas have caught up (or an error). DelayedFetch lists its completion cases A–H in the source, including "the accumulated bytes … exceeds the minimum bytes", the fetch offset not being on the last segment, leadership change and "a diverging epoch was found" (next chapter). fetch.purgatory.purge.interval.requests and producer.purgatory.purge.interval.requests (both 1000) control how often completed entries are purged.
- pass 1: ✅ server/.../purgatory/DelayedProduce.java:122-131; core/src/main/scala/kafka/server/DelayedFetch.scala:55-66; server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:106-111 — "Case G: The accumulated bytes … exceeds the minimum bytes / PURGE_INTERVAL_REQUESTS_DEFAULT = 1000"
- pass 2: ✅ server/.../DelayedProduce.java:124-132 Cases A–C; core/.../DelayedFetch.scala:58-65 Cases A–H ("exceeds the minimum bytes", "not on the last segment", "A diverging epoch was found"); ReplicationConfigs.java:105-111 both 1000

### C0609 · k-request-pipeline · L3 · li
> Acceptor → network processors (3) → request queue (500) → I/O threads (8) → purgatory if needed → response queue → processor.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:144,152; ServerConfigs.java:51 — "summary of C0575–C0580"
- pass 2: ✅ SocketServerConfigs.java:144,152; ServerConfigs.java:51; SocketServer.scala flow (acceptor → processor → RequestChannel → handler → responseQueue)

### C0610 · k-request-pipeline · L3 · li
> A full request queue blocks network threads: back-pressure, by design.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:145 — "before blocking the network threads"
- pass 2: ✅ SocketServerConfigs.java:145 — "before blocking the network threads"

### C0611 · k-request-pipeline · L3 · li
> Connections are muted while a request is in flight on the broker, preserving per-connection order.
- pass 1: ✅ core/src/main/scala/kafka/network/SocketServer.scala:1040-1042,945-947 — "selector.mute(connectionId) … RESPONSE_SENT"
- pass 2: ✅ SocketServer.scala:1040-1041,1077-1078 — mute after sendRequest, unmute on RESPONSE_SENT

### C0612 · k-request-pipeline · L3 · li
> Purgatory parks acks=all produces and long-poll fetches without holding a thread.
- pass 1: ✅ server/.../purgatory/DelayedProduce.java:37-38; core/src/main/scala/kafka/server/DelayedFetch.scala:8-10 — "A delayed produce operation … watched in the produce operation purgatory"
- pass 2: ✅ DelayedOperationPurgatory.java:122 tryCompleteElseWatch; monitoring.md:655-672 produce (ack=-1) and fetch purgatory

### C0613 · k-request-pipeline · L3 · li
> Diagnose with TotalTimeMs = Queue + Local + Remote + ResponseQueue + ResponseSend, plus the two idle-percent gauges (> 0.3).
- pass 1: ✅ docs/operations/monitoring.md:685,776,815 — "broken into queue, local, remote and response send time / ideally > 0.3"
- pass 2: ✅ RequestChannel.scala:213-220 — queue+local+remote+responseQueue+responseSend telescopes to endTimeNanos − startTimeNanos = totalTimeMs; monitoring.md:780,819 "ideally > 0.3"
