# Claims ledger — kafka-course.html — k-kraft

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0330 · k-kraft · L2 · p
> Who decides which branch holds which book, which photocopy is current, which branches are open today? In our city, the council does, and it doesn't send letters around. It keeps minutes: a numbered, append-only record of every decision. Branches read the minutes and update their own copy of the registry. That's KRaft. Since Kafka 4.0 it's the only mode; ZooKeeper is gone.
- pass 1: n/a (analogy; ZooKeeper removal ✅ docs/operations/kraft.md:294-296)
- pass 2: ✅ docs/operations/kraft.md:294 — "migrate from ZooKeeper to KRaft you need to use a bridge release. The last bridge release is Kafka 3.9" (analogy n/a)

### C0331 · k-kraft · L2 · p
> Concretely, cluster metadata (topics, partitions, leaders, ISRs, broker registrations, configs, ACLs, feature levels) lives in a single-partition internal topic called __cluster_metadata. Every change is a record in that log: TopicRecord, PartitionRecord, PartitionChangeRecord, RegisterBrokerRecord, BrokerRegistrationChangeRecord, ConfigRecord, FeatureLevelRecord and so on. The current state of the cluster is simply "replay the log from the start" (or from a snapshot).
- pass 1: ✅ revised after pass-2 finding — C0331/C0340 — FenceBrokerRecord → BrokerRegistrationChangeRecord (fenced = FENCE) — metadata/.../controller/ReplicationControlManager.java:1406-1408 "new BrokerRegistrationChangeRecord()… setFenced(BrokerRegistrationFencingChange.FENCE.value())"
- pass 2: ✅ metadata/src/main/resources/common/metadata/*.json — TopicRecord, PartitionRecord, PartitionChangeRecord, RegisterBrokerRecord, BrokerRegistrationChangeRecord, ConfigRecord, FeatureLevelRecord all exist

### C0332 · k-kraft · L2 · h3
> Roles: controllers and brokers
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0333 · k-kraft · L2 · p
> Each server sets process.roles to broker, controller, or broker,controller ("combined", fine for development, not recommended for critical deployments). Controllers form the metadata quorum: one is the active controller (the Raft leader), the others are hot standbys that replicate the log. You typically run 3 or 5. A majority must be alive, so 3 tolerate 1 failure and 5 tolerate 2 (the docs' rule: 2N + 1 controllers survive N failures).
- pass 1: ✅ docs/operations/kraft.md:33-47,289 — "Combined mode is not recommended in critical deployment"; "With 3 controllers… tolerate 1"
- pass 2: ✅ docs/operations/kraft.md:32-47,289 — "Combined mode is not recommended in critical deployment environments"; "3 or 5"; "withstand N concurrent failures ... 2N + 1 controllers"

### C0334 · k-kraft · L2 · p
> Brokers are observers: they replicate the metadata log too but never vote. In kafka-metadata-quorum.sh describe --status you literally see CurrentVoters (controllers) and CurrentObservers (brokers).
- pass 1: ✅ docs/operations/kraft.md:230-242; raft/.../KafkaRaftClient.java:133-135 — "CurrentVoters… CurrentObservers"; voters "take part in elections"
- pass 2: ✅ KafkaRaftClient.java:134-136 — "distinguishes between voters and observers. Voters ... are the only ones who take part in elections"; kraft.md describe --status shows CurrentVoters / CurrentObservers

### C0335 · k-kraft · L2 · figcaption
> Simplified: one record per step (a real CreateTopics writes a TopicRecord plus one PartitionRecord per partition, usually in one batch), no snapshots, and voters/observers fetch continuously rather than in visible rounds. The "commit" line is the metadata log's high watermark: an entry is committed once a majority of voters have it. Timings are illustrative; the relevant defaults are named in the text.
- pass 1: n/a (figcaption, simplification labelled; records ✅ metadata/*.json)
- pass 2: ✅ (labelled simplified/illustrative) QuorumController.java:168 durable before completion; KafkaRaftClient commit = majority high watermark

### C0336 · k-kraft · L2 · h3
> Raft, Kafka-style: pull, not push
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0337 · k-kraft · L2 · p
> Textbook Raft has the leader push entries to followers. KRaft's implementation (KafkaRaftClient) describes itself as a "Kafkaesque version of the Raft protocol". Leader election is more or less pure Raft (a Vote request carrying the candidate's last log offset), but replication is driven by followers fetching, using the same Fetch API that partition replicas use (Chapter 10). Because nobody pushes, a new leader sends BeginQuorumEpoch so voters learn who to fetch from. A voter that hasn't fetched successfully from the leader for controller.quorum.fetch.timeout.ms (default 2 000 ms) becomes prospective and runs a pre-vote (KIP-996: a Vote request with PreVote=true, which isn't persisted). Only if a majority grants it does it become a candidate and start a real election.
- pass 1: ✅ revised after pass-2 finding — C0337/C1421 (+ quorum config row, L3 state list) — fetch timeout → Prospective + pre-vote (Vote with PreVote=true, not persisted), candidate only after majority grants — raft/.../QuorumState.java:68 "Prospective: After expiration of the fetch timeout"; KafkaRa
- pass 2: ✅ KafkaRaftClient.java:129-150,3326-3328 — "Kafkaesque version of the Raft protocol ... replication is driven by replica fetching"; BeginQuorumEpoch; VoteRequest.json PreVote "(not persisted)"

### C0338 · k-kraft · L2 · p
> An entry is committed once a majority of voters have it. That's the metadata log's high watermark. The active controller only completes a request once its records are durable in the metadata log, so a decision it has acknowledged can't be "un-made" by a controller failover.
- pass 1: ❌ fixed in part file — QuorumController.java:166-169 events complete after records are committed to the metadata log
- pass 2: ✅ QuorumController.java:168-169 — "future associated with each operation will not be completed until the results of the operation have been made durable to the metadata log"

### C0339 · k-kraft · L2 · h3
> Broker liveness: heartbeats and fencing
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0340 · k-kraft · L2 · p
> When a broker starts, it registers with the active controller (a RegisterBrokerRecord lands in the log) and then sends a heartbeat every broker.heartbeat.interval.ms (default 2 000 ms). If the controller gets no heartbeat within broker.session.timeout.ms (default 9 000 ms), the broker's lease has expired. The controller fences it (a BrokerRegistrationChangeRecord with fenced = FENCE) and moves leadership of its partitions to other ISR members. The design docs define "alive" exactly like this: an active session with the controller, plus keeping up with the leader when acting as a follower.
- pass 1: ✅ revised after pass-2 finding — C0331/C0340 — FenceBrokerRecord → BrokerRegistrationChangeRecord (fenced = FENCE) — metadata/.../controller/ReplicationControlManager.java:1406-1408 "new BrokerRegistrationChangeRecord()… setFenced(BrokerRegistrationFencingChange.FENCE.value())"
- pass 2: ✅ raft/.../KRaftConfigs.java:40,44 — heartbeat 2000, session 9000; design.md:291-296 "active session ... broker.session.timeout.ms"; ClusterControlManager.java:618 FENCE

### C0341 · k-kraft · L2 · div
> Does the metadata log grow forever?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0342 · k-kraft · L2 · div
> No. Nodes periodically write a snapshot of the full state (…-epoch.checkpoint files in __cluster_metadata-0). A snapshot is generated after metadata.log.max.record.bytes.between.snapshots (default 20 MiB) of new records or metadata.log.max.snapshot.interval.ms (default 1 hour). A node that's far behind receives a snapshot via FetchSnapshot instead of replaying everything.
- pass 1: ✅ raft/.../MetadataLogConfig.java:38-44; KafkaRaftClient.java:162-166; kraft.md:256 — "20 * 1024 * 1024"; "TimeUnit.HOURS.toMillis(1)"; FetchSnapshot
- pass 2: ✅ MetadataLogConfig.java:38-45 — 20 * 1024 * 1024 bytes / TimeUnit.HOURS.toMillis(1); kraft.md:57 …-0000000001.checkpoint in __cluster_metadata-0; KafkaRaftClient.java:162 FetchSnapshot when behind log start

### C0343 · k-kraft · L2 · div
> Do clients talk to controllers?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0344 · k-kraft · L2 · div
> Normal producers and consumers don't. They ask brokers for metadata, and brokers answer from their locally replayed copy. Admin tools can target controllers directly with --bootstrap-controller.
- pass 1: ✅ metadata/.../KRaftMetadataCache.java:65; core/.../metadata/KRaftMetadataCachePublisher.scala:25; kraft.md:78 --bootstrap-controller
- pass 2: ✅ docs/design/design.md:125 — "all Kafka nodes can answer a request for metadata"; kraft.md add/remove-controller examples use --bootstrap-controller

### C0345 · k-kraft · L2 · div
> Why must I run kafka-storage.sh format before first start?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0346 · k-kraft · L2 · div
> Safety. If a majority of controllers could silently auto-format empty directories, a leader could be elected with missing committed metadata. Formatting explicitly stamps the cluster id (and, for dynamic quorums, the initial voter set) into the storage.
- pass 1: ✅ docs/operations/kraft.md:113-115 — "auto-formatting can sometimes obscure an error condition… elected with missing committed data"
- pass 2: ✅ docs/operations/kraft.md:117 — "If a majority of the controllers were able to start with an empty log directory, a leader might be able to be elected with missing committed data"

### C0347 · k-kraft · L2 · h3
> Static vs dynamic quorums (KIP-853)
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0348 · k-kraft · L2 · p
> Older clusters list every controller in controller.quorum.voters: a static quorum, where membership can't change without a full reconfiguration. With kraft.version 1 (release 4.1+ for upgrades of existing clusters), quorums are dynamic: nodes find the quorum through controller.quorum.bootstrap.servers (like bootstrap.servers for clients), and you add or remove controllers online with kafka-metadata-quorum.sh add-controller / remove-controller. The quorum is formatted as dynamic when controller.quorum.voters is absent and you format with --standalone, --initial-controllers or --no-initial-controllers.
- pass 1: ✅ docs/operations/kraft.md:69,75,102,159-161,189,195-221 — static lists all controllers; dynamic via bootstrap.servers; add/remove-controller
- pass 2: ✅ docs/operations/kraft.md:64-78,151-176 — "Apache Kafka 4.1 added support for upgrading ... static ... to dynamic"; "formatted as dynamic if controller.quorum.voters is not present, and one of --standalone, --initial-controllers, or --no-initial-controllers"

### C0349 · k-kraft · L2 · tr
> process.roles | (required) | broker, controller or both | Separate roles in production; combined only for dev
- pass 1: ✅ raft/.../KRaftConfigs.java:73; kraft.md:41,288 — process.roles NO_DEFAULT_VALUE; combined "should be avoided in critical deployment"
- pass 2: ✅ KRaftConfigs.java:73 NO_DEFAULT_VALUE, ValidList.in("broker","controller"); kraft.md:288 — "should be set to either broker or controller but not both"

### C0350 · k-kraft · L2 · tr
> controller.quorum.bootstrap.servers | empty | Where to find the quorum (dynamic quorums) | All new clusters; set on every broker and controller
- pass 1: ✅ raft/.../QuorumConfig.java:67-72; kraft.md:65,159 — "List.of()"; "Every broker and controller must set"
- pass 2: ✅ QuorumConfig.java:72 DEFAULT_QUORUM_BOOTSTRAP_SERVERS = List.of(); kraft.md:65 — "Every broker and controller must set the controller.quorum.bootstrap.servers property"

### C0351 · k-kraft · L2 · tr
> controller.quorum.voters | empty | Fixed voter list (static quorums) | Only legacy static clusters (deprecated by kraft.version 1)
- pass 1: ✅ QuorumConfig.java:58-65; kraft.md:102 — "KRaft version 1 deprecated the `controller.quorum.voters` property"
- pass 2: ✅ QuorumConfig.java:65 DEFAULT_QUORUM_VOTERS = List.of(); kraft.md:99 — "KRaft version 1 deprecated the controller.quorum.voters property"

### C0352 · k-kraft · L2 · tr
> broker.heartbeat.interval.ms | 2000 | Broker → controller heartbeat period | Rarely changed
- pass 1: ✅ raft/.../KRaftConfigs.java:39-41 — "BROKER_HEARTBEAT_INTERVAL_MS_DEFAULT = 2000"
- pass 2: ✅ KRaftConfigs.java:40-41 — "BROKER_HEARTBEAT_INTERVAL_MS_DEFAULT = 2000 ... length of time in milliseconds between broker heartbeats"

### C0353 · k-kraft · L2 · tr
> broker.session.timeout.ms | 9000 | Broker lease length without heartbeats before fencing | Trade faster failover vs false fencing on GC pauses/networks
- pass 1: ✅ KRaftConfigs.java:43-45 — "9000"; "that a broker lease lasts if no heartbeats are made"
- pass 2: ✅ KRaftConfigs.java:44-45 — "9000 ... length of time in milliseconds that a broker lease lasts if no heartbeats are made"

### C0354 · k-kraft · L2 · tr
> controller.quorum.fetch.timeout.ms | 2000 | Voter without a successful fetch → becomes prospective (pre-vote), then candidate; leader without majority fetches → resigns | Rarely; flaky networks
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ QuorumConfig.java:80-83 — "Maximum time a leader can go without receiving valid fetch ... from a majority ... before resigning"; KafkaRaftClient.java:3327 prospective

### C0355 · k-kraft · L2 · tr
> controller.quorum.election.timeout.ms | 1000 | Max wait before triggering a new election | Rarely
- pass 1: ✅ QuorumConfig.java:74-77 — "Maximum time… before triggering a new election"; 1_000
- pass 2: ✅ QuorumConfig.java:75-77 — "Maximum time in milliseconds to wait without being able to fetch from the leader before triggering a new election" = 1_000

### C0356 · k-kraft · L2 · tr
> metadata.log.max.record.bytes.between.snapshots | 20971520 | New bytes since last snapshot before a new snapshot | Very large clusters with heavy metadata churn
- pass 1: ✅ MetadataLogConfig.java:40-44 — "METADATA_SNAPSHOT_MAX_NEW_RECORD_BYTES = 20 * 1024 * 1024"
- pass 2: ✅ MetadataLogConfig.java:40-42 — 20 * 1024 * 1024 = 20971520; "maximum number of bytes in the log between the latest snapshot and the high-watermark"

### C0357 · k-kraft · L2 · p
> On your quickstart node, create a topic and then find its TopicRecord in the metadata log. Before running: what do you expect LeaderId and the number of CurrentVoters to be on a single combined node?
- pass 1: n/a (exercise prompt)
- pass 2: n/a — exercise prompt

### C0358 · k-kraft · L2 · pre
> bin/kafka-metadata-quorum.sh --bootstrap-server localhost:9092 describe --status bin/kafka-metadata-quorum.sh --bootstrap-server localhost:9092 describe --replication bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic orders --partitions 3 bin/kafka-dump-log.sh --cluster-metadata-decoder \ --files /tmp/kraft-combined-logs/__cluster_metadata-0/00000000000000000000.log | grep -A2 orders
- pass 1: ✅ tools/.../MetadataQuorumCommand.java "--status", "--replication"; DumpLogSegments.java:853 cluster-metadata-decoder; kraft.md:230,250
- pass 2: ✅ docs/operations/kraft.md:250 — "kafka-dump-log.sh --cluster-metadata-decoder --files metadata_log_dir/__cluster_metadata-0/00000000000000000000.log"; describe --status/--replication; log.dirs=/tmp/kraft-combined-logs (config/server.properties:73)

### C0359 · k-kraft · L2 · p
> On a single combined node, LeaderId is that node's id (1 in the quickstart config), there is exactly one voter and no observers (the combined node is listed once, as the voter). The dump shows a TOPIC_RECORD payload with "name":"orders" and a topic id, followed by one PARTITION_RECORD per partition with its replicas, ISR, leader and epochs. That's the council minute for "create orders". HighWatermark in --status is the metadata log's commit point.
- pass 1: ✅ revised after pass-2 finding — C0359 — "the node also appears as a broker" → "exactly one voter and no observers (the combined node is listed once, as the voter)" — per pass-2 verifier; docs/operations/kraft.md:237-242 (voters vs observers listing)
- pass 2: ✅ config/server.properties:19-22 — "process.roles=broker,controller", "node.id=1"; DumpLogSegments.java:743 type = MetadataRecordType name (TOPIC_RECORD)

### C0360 · k-kraft · L3 · summary
> L3🔬 Go deeper: from minute to local registry
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0361 · k-kraft · L3 · p
> The active controller is a QuorumController (metadata module). It handles admin requests on a single event thread, turns them into metadata records, and appends them through the Raft client. Broker liveness is tracked by BrokerHeartbeatManager; the broker side of registration and heartbeats is BrokerLifecycleManager. The quorum's own state machine (unattached, prospective, candidate, leader, follower…) lives in QuorumState, and the protocol in KafkaRaftClient. Its APIs are Vote, BeginQuorumEpoch, EndQuorumEpoch (graceful resignation), Fetch and FetchSnapshot.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ metadata/.../controller/QuorumController.java, BrokerHeartbeatManager.java; server/.../BrokerLifecycleManager.java; raft QuorumState/ProspectiveState; KafkaRaftClient.java:138-165 lists Vote, BeginQuorumEpoch, EndQuorumEpoch, Fetch, FetchSnapshot

### C0362 · k-kraft · L3 · p
> On every node, a MetadataLoader reads committed batches and builds a MetadataDelta on top of the previous immutable MetadataImage. Publishers then apply the new image, for example to make the local ReplicaManager become leader or follower for partitions. The leader's appends linger up to controller.quorum.append.linger.ms (default 25 ms) to batch writes.
- pass 1: ✅ metadata/.../image/loader/MetadataLoader.java, MetadataDelta.java, MetadataImage.java; core/.../BrokerMetadataPublisher.scala:148; QuorumConfig.java:90-94 — "replicaManager.applyDelta(topicsDelta, newImage)"; linger 25
- pass 2: ✅ MetadataLoader.java:63 — "follows changes provided by a RaftClient, and packages them into metadata deltas and images"; QuorumConfig.java:90-94 controller.quorum.append.linger.ms = 25

### C0363 · k-kraft · L3 · p
> Storage bootstrap: kafka-storage.sh format --standalone writes meta.properties and a bootstrap snapshot 00000000000000000000-0000000000.checkpoint containing KRaftVersionRecord and VotersRecord. With --initial-controllers, that VotersRecord lists all controllers, and the value must be identical on every controller.
- pass 1: ✅ docs/operations/kraft.md:122-125,143 — "meta.properties… 00000000000000000000-0000000000.checkpoint… KRaftVersionRecord and VotersRecord"
- pass 2: ✅ docs/operations/kraft.md:125,143 — "create a snapshot at 00000000000000000000-0000000000.checkpoint with ... KRaftVersionRecord and VotersRecord"; "important that the value of this flag is the same in all of the controllers"

### C0364 · k-kraft · L2 · li
> All cluster metadata is a replicated log, __cluster_metadata. State = replay records (or load a snapshot).
- pass 1: ✅ Topic.java:30; kraft.md:256,264 — __cluster_metadata; snapshot checkpoints
- pass 2: ✅ C0331 evidence (__cluster_metadata replay / snapshots)

### C0365 · k-kraft · L2 · li
> Controllers vote (Raft, majority quorum: 3 → 1 failure, 5 → 2). Brokers are observers that fetch and apply.
- pass 1: ✅ kraft.md:47; KafkaRaftClient.java:133-135 — "With 3 controllers… 1… with 5… 2"; voters vs observers
- pass 2: ✅ kraft.md:47; KafkaRaftClient.java:134-136

### C0366 · k-kraft · L2 · li
> Kafka's Raft is pull-based: followers fetch. BeginQuorumEpoch announces a new leader. Commit = majority of voters.
- pass 1: ✅ KafkaRaftClient.java:128-150 — "replication is driven by replica fetching"; BeginQuorumEpoch
- pass 2: ✅ KafkaRaftClient.java:129-151

### C0367 · k-kraft · L2 · li
> Brokers heartbeat every 2 s; no heartbeat for broker.session.timeout.ms (9 s) → fenced, and leaders move.
- pass 1: ✅ KRaftConfigs.java:39-45; design.md:364 — 2000 / 9000; controller elects new leaders
- pass 2: ✅ KRaftConfigs.java:40,44; design.md:296

### C0368 · k-kraft · L2 · li
> Dynamic quorums (KIP-853, kraft.version ≥ 1) use controller.quorum.bootstrap.servers and support add/remove-controller.
- pass 1: ✅ kraft.md:75,157-161,195-221 — "Dynamic controller cluster was added in `kraft.version=1`"; add/remove-controller
- pass 2: ✅ kraft.md:151-176 (KIP-853, kraft.version ≥ 1, add-controller/remove-controller)
