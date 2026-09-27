# Claims ledger — kafka-course.html — k-cluster

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0058 · k-cluster · L1 · p
> One archive building can't hold every ledger in a big city, and one fire would destroy everything. So the city runs several branches. In Kafka, a branch is a broker: a server process that stores partitions and serves reads and writes for them. A group of brokers working together is a cluster.
- pass 1: ✅ docs/getting-started/introduction.md:65 — "Kafka is run as a cluster of one or more servers … called the brokers" (fire = analogy)
- pass 2: ✅ docs/design/design.md:285 / docs/getting-started/introduction.md:83 — brokers host partitions; cluster of brokers; analogy parts n/a

### C0059 · k-cluster · L1 · p
> The partitions of a topic are spread across the brokers. Each partition also has copies (replicas, chapter 5) on several brokers, and exactly one of those copies is the leader. All writes for a partition go to its leader. There are normally many more partitions than brokers, and Kafka spreads leadership around so each broker leads its fair share.
- pass 1: ✅ docs/design/design.md:285 — "single leader … All writes go to the leader … leaders are evenly distributed among brokers"
- pass 2: ✅ docs/design/design.md:285 — "each partition in Kafka has a single leader ... All writes go to the leader"; docs/operations/basic-kafka-operations.md:108 preferred replicas balance leadership

### C0060 · k-cluster · L1 · h3
> Finding the right branch: bootstrap + metadata
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0061 · k-cluster · L1 · p
> A client (producer or consumer) needs to know which broker leads which partition. It learns this in two steps:
- pass 1: n/a (pedagogy lead-in)
- pass 2: n/a — lead-in

### C0062 · k-cluster · L1 · li
> Bootstrap. You configure bootstrap.servers, a list like host1:9092,host2:9092. It's used for the first connection (and again to re-bootstrap if every known broker becomes unreachable, the default metadata.recovery.strategy=rebootstrap). It doesn't need to list every broker, but the docs recommend listing more than one in case one is down.
- pass 1: ✅ revised after pass-2 finding — C0062/C0083 k-cluster bootstrap → CommonClientConfigs.java:231-242 metadata.recovery.strategy default rebootstrap
- pass 2: ✅ CommonClientConfigs.java:47-51,242 — "we recommend including more than one server"; DEFAULT_METADATA_RECOVERY_STRATEGY = REBOOTSTRAP

### C0063 · k-cluster · L1 · li
> Metadata. The client asks any broker for metadata. Every broker can answer: which brokers exist, and for each partition which broker is the leader, which brokers hold replicas, and which replicas are in sync.
- pass 1: ✅ docs/design/design.md:125 + clients/src/main/resources/common/message/MetadataResponse.json:50-89 — "all Kafka nodes can answer a request for metadata"; LeaderId/ReplicaNodes/IsrNodes
- pass 2: ✅ docs/design/design.md:125 — "all Kafka nodes can answer a request for metadata"; MetadataResponse.json has Brokers, LeaderId, ReplicaNodes, IsrNodes

### C0064 · k-cluster · L1 · p
> Then the client connects directly to the leader of each partition it needs. There's no router or load balancer in the middle. In archive terms: you walk into any branch and read the notice board ("Payments book 1 is kept at branch B2"), then you walk to B2 yourself. This is a deliberate design choice. No extra hop, and no central bottleneck in the data path.
- pass 1: ✅ docs/design/design.md:125 — "directly to the broker that is the leader for the partition without any intervening routing tier"
- pass 2: ✅ docs/design/design.md:125 — "directly to the broker that is the leader for the partition without any intervening routing tier"

### C0065 · k-cluster · L1 · h3
> Who keeps the notice board up to date? The controllers
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0066 · k-cluster · L1 · p
> Somebody has to decide which broker leads which partition and notice when a broker dies. In Kafka 4.x that's the job of the controllers. They form a small quorum and use a Raft-based protocol called KRaft. In the analogy, they're the city council that keeps the official registry. Brokers register with the controller and send it regular heartbeats. If no heartbeat arrives within broker.session.timeout.ms (default 9000 ms), the broker is considered offline, and the controller picks a new leader for its partitions from the replicas that are in sync.
- pass 1: ✅ docs/design/design.md:289-296,364 + raft/.../KRaftConfigs.java:44 — "heartbeats to the controller … broker.session.timeout.ms"; "BROKER_SESSION_TIMEOUT_MS_DEFAULT = 9000"; "electing one of the remaining members of the ISR"
- pass 2: ✅ docs/design/design.md:296 — "If the controller fails to receive a heartbeat before ... broker.session.timeout.ms expires, then the node is considered offline"; raft/.../KRaftConfigs.java:44 default 9000

### C0067 · k-cluster · L1 · p
> For learning you can run one process that is both broker and controller. The quickstart's config/server.properties sets process.roles=broker,controller. In production they're usually separate machines. Kafka 4.x has no ZooKeeper at all. Chapter 9 covers KRaft in depth.
- pass 1: ✅ config/server.properties:19; docs/operations/kraft.md:41,288; docs/getting-started/upgrade.md:33 — "process.roles=broker,controller"; "Combined mode can be used in development environments"; "ZooKeeper mode has been removed"
- pass 2: ✅ config/server.properties:19 — "process.roles=broker,controller"; docs/getting-started/upgrade.md:221 "ZooKeeper mode has been removed"

### C0068 · k-cluster · L1 · figcaption
> Simplified: one topic, one replica shown per broker line, controller drawn as a single box (really a quorum). Timing is illustrative. The error name and the refresh behavior are real: the producer treats NOT_LEADER_OR_FOLLOWER as a "metadata is stale" error and requests a metadata update before retrying.
- pass 1: ✅ clients/.../producer/internals/Sender.java:713-730 — "Received invalid metadata error … Going to request metadata update now" (timing labelled illustrative)
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/internals/Sender.java:713-730 — InvalidMetadataException (incl. NOT_LEADER_OR_FOLLOWER) → "metadata.requestUpdate(false)" then retry

### C0069 · k-cluster · L1 · div
> ClientWhy can't I just talk to one broker and let it forward my request? Much simpler for me.
- pass 1: n/a (fireside dialogue question)
- pass 2: n/a — dialogue prompt

### C0070 · k-cluster · L1 · div
> BrokerSimpler for you, but every byte would cross the network twice, and the forwarding broker would become everyone's bottleneck. I'd rather tell you where the leader is and let you go there yourself.
- pass 1: ✅ docs/design/design.md:125 — "without any intervening routing tier" (bottleneck reasoning = pedagogy)
- pass 2: n/a — dialogue rationale; direct-to-leader design per docs/design/design.md:125

### C0071 · k-cluster · L1 · div
> ClientAnd if the leader moves while I'm not looking?
- pass 1: n/a (fireside dialogue question)
- pass 2: n/a — dialogue prompt

### C0072 · k-cluster · L1 · div
> BrokerThen the old leader says "not me anymore" and you refresh your metadata. You also refresh it every metadata.max.age.ms (default 5 minutes) anyway, so you notice new brokers and partitions.
- pass 1: ✅ clients/.../producer/ProducerConfig.java:447 + clients/.../CommonClientConfigs.java:64 — "5 * 60 * 1000"; "proactively discover any new brokers or partitions"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:447 — METADATA_MAX_AGE default "5 * 60 * 1000"; clients/src/main/java/org/apache/kafka/clients/CommonClientConfigs.java:64 "proactively discover any new brokers or partitions"

### C0073 · k-cluster · L1 · p
> Describe the topic you created in chapter 1:
- pass 1: n/a (exercise lead-in)
- pass 2: n/a — lead-in

### C0074 · k-cluster · L1 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic payments
- pass 1: ✅ tools/.../TopicCommand.java:720,734,735 — accepts("bootstrap-server"), ("describe"), ("topic")
- pass 2: ✅ docs/getting-started/quickstart.md:109 — "bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092"

### C0075 · k-cluster · L1 · p
> Find the Leader:, Replicas: and Isr: columns for each partition. On a single-node cluster, what must they all say, and why?
- pass 1: ✅ TopicCommand.java:341-345 — "\tLeader: " … "\tReplicas: " … "\tIsr: "
- pass 2: n/a — exercise question

### C0076 · k-cluster · L1 · p
> Every partition has the same leader, replica list and ISR: the one node's ID (with the quickstart config, 1). With one broker there's nowhere else to put a leader or a copy. On a three-broker cluster with --replication-factor 3 you'd see three IDs under Replicas and the leaders spread across the brokers.
- pass 1: ✅ config/server.properties:22 — "node.id=1" (single broker ⇒ same leader/replicas/ISR)
- pass 2: ✅ config/server.properties:22 — "node.id=1"; TopicCommand.java:765 --replication-factor

### C0077 · k-cluster · L2 · p
> Staying in sync with reality. Clients refresh metadata in two ways: periodically (metadata.max.age.ms, default 300000 ms, to discover new brokers or partitions proactively) and on demand when a broker returns a "you're talking to the wrong node" error. Errors like NOT_LEADER_OR_FOLLOWER (error code 6) and UNKNOWN_TOPIC_OR_PARTITION extend InvalidMetadataException, which is a retriable exception. The producer retries the batch after the refresh, so your application normally never sees them.
- pass 1: ✅ ProducerConfig.java:447; common/protocol/Errors.java:192; common/errors/NotLeaderOrFollowerException.java:27, UnknownTopicOrPartitionException.java:26, InvalidMetadataException.java:22 — "extends InvalidMetadataException"; "extends RefreshRetriableException"
- pass 2: ✅ clients/.../common/errors/NotLeaderOrFollowerException.java:27 "extends InvalidMetadataException"; InvalidMetadataException extends RefreshRetriableException extends RetriableException; Errors.java:192 NOT_LEADER_OR_FOLLOWER(6

### C0078 · k-cluster · L3 · summary
> L3🔬 Go deeper: what's inside a MetadataResponse
- pass 1: n/a (summary heading)
- pass 2: n/a — summary heading

### C0079 · k-cluster · L3 · p
> From MetadataResponse.json in the protocol definitions: a list of Brokers (NodeId, Host, Port, Rack), the ClusterId, a ControllerId, and per topic its Name, TopicId and Partitions. Each partition entry has PartitionIndex, LeaderId, LeaderEpoch, ReplicaNodes, IsrNodes and OfflineReplicas. On the broker, KafkaApis.handleTopicMetadataRequest answers from a local metadata cache (KRaftMetadataCache). That cache is fed by the controllers' metadata log, which is why any broker can answer.
- pass 1: ✅ MetadataResponse.json:50-89; core/.../KafkaApis.scala:880; core/.../BrokerServer.scala:209 — "handleTopicMetadataRequest"; "new KRaftMetadataCache"
- pass 2: ✅ clients/src/main/resources/common/message/MetadataResponse.json — Brokers(NodeId,Host,Port,Rack), ClusterId, ControllerId, TopicId, PartitionIndex, LeaderId, LeaderEpoch, ReplicaNodes, IsrNodes, OfflineReplicas; KafkaApis.scala:880 handleTopicMetadataRequest; metadata/.../KRaftMetadataCache.java

### C0080 · k-cluster · L3 · p
> Skipping a round-trip (KIP-951). Since ProduceResponse v10, a NOT_LEADER_OR_FOLLOWER response can carry tagged fields CurrentLeader (leader id + epoch) and NodeEndpoints. In the producer's Sender, that hint updates the partition's leader right away, and a normal metadata update is still requested. The LeaderEpoch number prevents confusion between an old and a new leader. Chapter 16 explains it.
- pass 1: ✅ clients/src/main/resources/common/message/ProduceResponse.json:38; Sender.java:722-730 — "Version 10 adds 'CurrentLeader' and 'NodeEndpoints' as tagged fields (KIP-951)"; "metadata.requestUpdate(false)"
- pass 2: ✅ common/message/ProduceResponse.json:73,84 — CurrentLeader & NodeEndpoints "versions": "10+", "taggedVersions": "10+"; clients/src/main/java/org/apache/kafka/clients/producer/internals/Sender.java:639-644,725-730 updatePartitionLeadership + requestUpdate

### C0081 · k-cluster · L1 · li
> A broker stores partitions and serves requests; several brokers form a cluster.
- pass 1: ✅ docs/getting-started/introduction.md:65 — "cluster of one or more servers … brokers"
- pass 2: ✅ docs/design/design.md:285 — brokers host partition replicas

### C0082 · k-cluster · L1 · li
> Each partition has one leader broker; writes go to the leader. Leadership is spread across brokers.
- pass 1: ✅ docs/design/design.md:285 — "All writes go to the leader … leaders are evenly distributed"
- pass 2: ✅ docs/design/design.md:285 — "single leader ... All writes go to the leader"

### C0083 · k-cluster · L1 · li
> bootstrap.servers is for first contact (and re-bootstrap); the client then gets metadata from any broker.
- pass 1: ✅ revised after pass-2 finding — C0062/C0083 k-cluster bootstrap → CommonClientConfigs.java:231-242 metadata.recovery.strategy default rebootstrap
- pass 2: ✅ CommonClientConfigs.java:47-51,231-233 — "used to establish the initial connection"; rebootstrap "repeats the bootstrap process using bootstrap.servers"

### C0084 · k-cluster · L1 · li
> Clients talk directly to partition leaders. No proxy or router sits in the data path.
- pass 1: ✅ docs/design/design.md:125 — "without any intervening routing tier"
- pass 2: ✅ docs/design/design.md:125 — "without any intervening routing tier"

### C0085 · k-cluster · L1 · li
> KRaft controllers track broker liveness (heartbeats, broker.session.timeout.ms 9 s) and pick new leaders.
- pass 1: ✅ design.md:296 + KRaftConfigs.java:44 — "BROKER_SESSION_TIMEOUT_MS_DEFAULT = 9000"
- pass 2: ✅ raft/.../KRaftConfigs.java:44 — "BROKER_SESSION_TIMEOUT_MS_DEFAULT = 9000"; docs/design/design.md:296

### C0086 · k-cluster · L1 · li
> A stale client gets NOT_LEADER_OR_FOLLOWER, refreshes metadata and retries automatically.
- pass 1: ✅ Sender.java:713-730 + Errors.java:192 — "Going to request metadata update now"
- pass 2: ✅ clients/src/main/java/org/apache/kafka/clients/producer/internals/Sender.java:713-730 — requestUpdate on InvalidMetadataException; retriable
