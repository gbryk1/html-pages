# Claims ledger — kafka-course.html — k-rebalance

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0401 · k-rebalance · L2 · p
> In Part I a consumer group was a team of clerks sharing ledger books: each book goes to exactly one clerk, and each clerk keeps a bookmark (the committed offset). The hard part isn't reading. It's changing who reads what when a clerk arrives, leaves or collapses, without two clerks reading the same book and without a book left unattended. That handover is a rebalance, and Kafka has two very different protocols for it.
- pass 1: n/a (analogy / intro)
- pass 2: n/a — analogy/intro

### C0402 · k-rebalance · L2 · h3
> The group coordinator and __consumer_offsets
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0403 · k-rebalance · L2 · p
> Every group has a group coordinator: a broker-side component on the broker that leads one partition of the internal topic __consumer_offsets. Which partition? abs(groupId.hashCode()) % offsets.topic.num.partitions (default 50 partitions, replication factor 3). A consumer finds its coordinator with a FindCoordinator request, then sends all group traffic (joins, heartbeats, offset commits) there. Commits and group state are written as records into that partition, so they're replicated like any other data, and a new coordinator can rebuild them by replaying the partition after a failover.
- pass 1: ✅ group-coordinator/.../GroupCoordinatorService.java:423-427; GroupCoordinatorConfig.java:114,123; core/.../KafkaApis.scala:1266,1286-1290 — "Utils.abs(groupId.hashCode()) % numPartitions"; coordinator = partition leader; docs/implementation/distribution.md:33
- pass 2: ✅ GroupCoordinatorService.java:427 — "Utils.abs(groupId.hashCode()) % numPartitions"; GroupCoordinatorConfig.java:114/123 — OFFSETS_TOPIC_PARTITIONS_DEFAULT = 50, REPLICATION_FACTOR_DEFAULT = 3

### C0404 · k-rebalance · L2 · h3
> Classic protocol: JoinGroup / SyncGroup
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0405 · k-rebalance · L2 · p
> The classic protocol (the client default in 4.3: group.protocol=classic) puts the brains in the client:
- pass 1: ✅ clients/.../consumer/ConsumerConfig.java:115-117 — "The default value is <code>classic</code>"
- pass 2: ✅ clients/.../ConsumerConfig.java:115 — "DEFAULT_GROUP_PROTOCOL = GroupProtocol.CLASSIC"

### C0406 · k-rebalance · L2 · li
> Something changes (a member joins or leaves, a member misses heartbeats, subscriptions change). The coordinator moves the group to PreparingRebalance and every member must send JoinGroup.
- pass 1: ✅ group-coordinator/.../classic/ClassicGroupState.java:40,69-71 — "join group from a new member => PREPARING_REBALANCE"; "member failure detected"
- pass 2: ✅ group-coordinator/.../classic/ClassicGroupState.java:69-71 — "join group from new member… leave group… member failure detected => PREPARING_REBALANCE"

### C0407 · k-rebalance · L2 · li
> The coordinator waits for members to rejoin, picks one as the group leader and gives it everyone's subscriptions.
- pass 1: ✅ clients/.../consumer/ConsumerPartitionAssignor.java:54 — "Subscription sent to the leader"
- pass 2: ✅ ClassicGroupState.java:55 — "some members have joined by the timeout => COMPLETING_REBALANCE"; leader receives member metadata in JoinGroup response (classic protocol)

### C0408 · k-rebalance · L2 · li
> The leader runs the client-side assignor (partition.assignment.strategy) and sends the plan in SyncGroup. The coordinator hands each member its share.
- pass 1: ✅ ConsumerPartitionAssignor.java:75-76; SyncGroupRequest.json:17 — "assignment as provided by the leader in assign()"
- pass 2: ✅ ConsumerConfig.java:446 partition.assignment.strategy (client-side); leader's SyncGroup carries assignments, coordinator returns each member's share (SyncGroupRequest.json apiKey 14)

### C0409 · k-rebalance · L2 · p
> That's a global synchronization barrier: nobody gets a new assignment until everyone has joined. How painful it is depends on the rebalance flavour:
- pass 1: ✅ docs/operations/consumer-rebalance-protocol.md:32 — new protocol "no longer relies on a global synchronization barrier"
- pass 2: ✅ docs/operations/consumer-rebalance-protocol.md — new protocol "no longer relies on a global synchronization barrier" (i.e. classic does)

### C0410 · k-rebalance · L2 · li
> Eager (e.g. RangeAssignor, RoundRobinAssignor, StickyAssignor): each consumer must revoke all its partitions before joining. The whole group stops. That's the famous "stop-the-world" rebalance.
- pass 1: ✅ ConsumerPartitionAssignor.java:250-252; CooperativeStickyAssignor.java class doc — "EAGER… revoke all its owned partitions"; "StickyAssignor follows the eager rebalancing protocol"
- pass 2: ✅ StickyAssignor/RangeAssignor/RoundRobinAssignor use the default EAGER supportedProtocols (ConsumerPartitionAssignor); only CooperativeStickyAssignor.java:68 returns COOPERATIVE

### C0411 · k-rebalance · L2 · li
> Cooperative (CooperativeStickyAssignor): consumers keep their partitions while rebalancing. The assignor withholds partitions that must move; their owners revoke just those, and a second rebalance gives them to the new owner. Everyone else keeps working.
- pass 1: ✅ ConsumerPartitionAssignor.java:254-258 — "retain its currently owned partitions… reassigned… in the next rebalance event"
- pass 2: ✅ CooperativeStickyAssignor.java:68 — "RebalanceProtocol.COOPERATIVE, RebalanceProtocol.EAGER"; incremental two-round revocation (KIP-429)

### C0412 · k-rebalance · L2 · p
> The default partition.assignment.strategy is [RangeAssignor, CooperativeStickyAssignor]. That means eager Range by default, but you can switch to cooperative with a single rolling bounce that removes RangeAssignor from the list.
- pass 1: ✅ ConsumerConfig.java:163-164 — "default assignor is [RangeAssignor, CooperativeStickyAssignor]… single rolling bounce"
- pass 2: ✅ ConsumerConfig.java:163 — "default assignor is [RangeAssignor, CooperativeStickyAssignor]… single rolling bounce that removes the RangeAssignor"

### C0413 · k-rebalance · L2 · h3
> The new consumer protocol (KIP-848)
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0414 · k-rebalance · L2 · p
> KIP-848 is GA since Kafka 4.0 and enabled by default on the server. Clients opt in with group.protocol=consumer. It moves the brains to the broker and removes the barrier:
- pass 1: ✅ consumer-rebalance-protocol.md:31,38,66 — "4.0… GA"; "automatically enabled on the server"; "must be set to `consumer`"
- pass 2: ✅ docs/operations/consumer-rebalance-protocol.md — "Starting from Apache Kafka 4.0… (KIP-848) is Generally Available"; "automatically enabled on the server since Apache Kafka 4.0"

### C0415 · k-rebalance · L2 · li
> Members send a periodic ConsumerGroupHeartbeat (with MemberEpoch, subscribed topics or a regex, and the partitions they own). There's no JoinGroup/SyncGroup.
- pass 1: ✅ clients/.../message/ConsumerGroupHeartbeatRequest.json — fields MemberEpoch, SubscribedTopicNames/Regex, TopicPartitions
- pass 2: ✅ ConsumerGroupHeartbeatRequest.json — fields MemberEpoch, SubscribedTopicNames, SubscribedTopicRegex, TopicPartitions (apiKey 68)

### C0416 · k-rebalance · L2 · li
> The coordinator computes a target assignment with a server-side assignor (group.consumer.assignors: uniform by default, or range).
- pass 1: ✅ consumer-rebalance-protocol.md:47-49; GroupCoordinatorConfig.java:214-222 — "`uniform` and `range`… `uniform` is the default"
- pass 2: ✅ consumer-rebalance-protocol.md — "`uniform` and `range` assignors are provided by default… `uniform` is the default one"

### C0417 · k-rebalance · L2 · li
> Each member is reconciled individually through heartbeat responses: first "revoke these", then "you now own these". A partition is only given to its new owner once the old owner has confirmed it released it. That's what keeps two clerks off the same book.
- pass 1: ✅ group-coordinator/.../modern/MemberState.java:34-45 — UNREVOKED_PARTITIONS; "waits on some partitions which have not been revoked by their previous owners"
- pass 2: ✅ modern/MemberState.java:35-45 — UNREVOKED_PARTITIONS "must revoke some partitions"; UNRELEASED_PARTITIONS "waits on some partitions which have not been revoked by their previous owners"

### C0418 · k-rebalance · L2 · li
> Heartbeat interval and session timeout become broker configs (group.consumer.heartbeat.interval.ms 5 000, group.consumer.session.timeout.ms 45 000). The client configs session.timeout.ms, heartbeat.interval.ms and partition.assignment.strategy can't be used with it.
- pass 1: ✅ GroupCoordinatorConfig.java:184,196; consumer-rebalance-protocol.md:40-43,72-76 — 45000 / 5000; "no longer usable"
- pass 2: ✅ GroupCoordinatorConfig.java:184/196 — 45000 / 5000; consumer-rebalance-protocol.md — "no longer usable: heartbeat.interval.ms, session.timeout.ms, partition.assignment.strategy"

### C0419 · k-rebalance · L2 · figcaption
> Heuristic animation: the step timing is illustrative, not measured. Real durations depend on how long members take to finish poll(), heartbeat intervals and your rebalance listener code. The final assignments shown are one valid outcome; real assignors may pick different partitions.
- pass 1: n/a (figcaption labelled heuristic/illustrative)
- pass 2: n/a — figure caption, labelled heuristic

### C0420 · k-rebalance · L2 · h3
> Failure detection: two different clocks
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0421 · k-rebalance · L2 · p
> A consumer can be "dead" in two ways. Crashed: heartbeats stop, and after the session timeout (session.timeout.ms, default 45 s, classic; group.consumer.session.timeout.ms, default 45 s, new protocol) the coordinator removes it. Until then, its partitions are not read by anyone. Stuck: the process heartbeats happily, but your code hasn't called poll() for more than max.poll.interval.ms (default 300 000 ms = 5 min). The consumer is considered failed, leaves the group, and a rebalance hands its partitions to others. When the slow code finally commits, it gets a commit failure because it no longer owns the partitions.
- pass 1: ✅ ConsumerConfig.java:436-438,627-629; CommonClientConfigs.java:192-198,208-214; KafkaConsumer.java:141-146 — 45000 / 300000; "proactively leave the group… CommitFailedException"
- pass 2: ✅ ConsumerConfig.java:436-443 session.timeout.ms 45000; :627-629 max.poll.interval.ms 300000; GroupCoordinatorConfig.java:184 group.consumer.session.timeout.ms 45000; late commit → CommitFailedException

### C0422 · k-rebalance · L2 · div
> Rolling restarts trigger rebalances for every pod. Can I avoid that?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0423 · k-rebalance · L2 · div
> Yes: static membership. Give each instance a stable group.instance.id. The coordinator then recognises a restarted member as the same one and doesn't reassign its partitions, as long as it comes back before the session timeout. Duplicate ids are fenced with FencedInstanceIdException. Streams apps set one id per instance.
- pass 1: ✅ docs/design/design.md:123-131; CommonClientConfigs.java:196-198 — "no rebalance will be triggered"; FencedInstanceIdException; reassigned after session timeout
- pass 2: ✅ group.instance.id static membership (KIP-345); clients/.../errors/FencedInstanceIdException.java exists; partitions kept until session timeout

### C0424 · k-rebalance · L2 · div
> Can I switch an existing group to the new protocol without downtime?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0425 · k-rebalance · L2 · div
> Yes. Roll consumers with group.protocol=consumer. When the first one joins, the group converts from Classic to Consumer and interoperates with the remaining classic members, provided the classic assignor doesn't embed custom metadata. The docs map RangeAssignor → range and the sticky/round-robin ones → uniform.
- pass 1: ✅ consumer-rebalance-protocol.md:56-61,89 — "converted from `Classic` to `Consumer`… assignor that does not embed custom metadata"
- pass 2: ✅ consumer-rebalance-protocol.md — "converted from Classic to Consumer… only possible when the classic group uses an assignor that does not embed custom metadata"; mapping table Range→range, (Coop)Sticky/RoundRobin→uniform

### C0426 · k-rebalance · L2 · div
> Is classic going away?
- pass 1: n/a (question)
- pass 2: n/a — FAQ question

### C0427 · k-rebalance · L2 · div
> The documented plan (KIP-1274): in 5.0 KafkaConsumer defaults to the consumer protocol, and in 6.0 it only supports it, while brokers keep supporting classic for older clients.
- pass 1: ✅ consumer-rebalance-protocol.md:95-100 — "5.0: defaults to Consumer protocol… 6.0: only supports Consumer… broker still supports Classic"
- pass 2: ✅ consumer-rebalance-protocol.md:95-100 — "5.0: KafkaConsumer defaults to Consumer protocol… 6.0: only supports Consumer… broker still supports Classic"

### C0428 · k-rebalance · L2 · tr
> group.protocol (client) | classic | Choose classic or KIP-848 consumer protocol | Set consumer for new apps on 4.x brokers
- pass 1: ✅ ConsumerConfig.java:115-117,653-656 — default classic (use-when = advice)
- pass 2: ✅ ConsumerConfig.java:115 default classic; advice n/a

### C0429 · k-rebalance · L2 · tr
> partition.assignment.strategy | [Range, CooperativeSticky] | Client-side assignors (classic only), by preference | Drop Range to get cooperative rebalancing
- pass 1: ✅ ConsumerConfig.java:152-164,446-448; consumer-rebalance-protocol.md:76 — List.of(RangeAssignor, CooperativeStickyAssignor)
- pass 2: ✅ ConsumerConfig.java:448 — "List.of(RangeAssignor.class, CooperativeStickyAssignor.class)"; not usable with consumer protocol (rebalance docs)

### C0430 · k-rebalance · L2 · tr
> session.timeout.ms | 45000 | No heartbeat for this long → member removed (classic) | Faster failure detection vs false positives
- pass 1: ✅ ConsumerConfig.java:436-438; CommonClientConfigs.java:208-214 — 45000; "remove this client from the group"
- pass 2: ✅ ConsumerConfig.java:436-438 — session.timeout.ms default 45000

### C0431 · k-rebalance · L2 · tr
> heartbeat.interval.ms | 3000 | Heartbeat period (classic) | Keep well below the session timeout
- pass 1: ✅ ConsumerConfig.java:441-443 — 3000
- pass 2: ✅ ConsumerConfig.java:441-443 — heartbeat.interval.ms default 3000

### C0432 · k-rebalance · L2 · tr
> max.poll.interval.ms | 300000 | Max gap between poll() calls before the member is considered failed | Raise for slow batch processing, or lower max.poll.records
- pass 1: ✅ ConsumerConfig.java:627-629; CommonClientConfigs.java:192-195 — 300000; "maximum delay between invocations of poll()"
- pass 2: ✅ ConsumerConfig.java:627-629 — max.poll.interval.ms default 300000

### C0433 · k-rebalance · L2 · tr
> group.instance.id | null | Static membership id | Rolling deploys, stateful consumers
- pass 1: ✅ ConsumerConfig.java:430-432; design.md:128 — default null; static membership
- pass 2: ✅ group.instance.id default null (ConsumerConfig GROUP_INSTANCE_ID_CONFIG); static membership

### C0434 · k-rebalance · L2 · tr
> group.consumer.heartbeat.interval.ms (broker) | 5000 | Heartbeat period dictated to KIP-848 members | Rarely
- pass 1: ✅ GroupCoordinatorConfig.java:196; consumer-rebalance-protocol.md:40-42 — 5000
- pass 2: ✅ GroupCoordinatorConfig.java:196 — "CONSUMER_GROUP_HEARTBEAT_INTERVAL_MS_DEFAULT = 5000"

### C0435 · k-rebalance · L2 · tr
> group.consumer.session.timeout.ms (broker) | 45000 | Session timeout for KIP-848 members | Faster failure detection cluster-wide
- pass 1: ✅ GroupCoordinatorConfig.java:184 — 45000
- pass 2: ✅ GroupCoordinatorConfig.java:184 — "CONSUMER_GROUP_SESSION_TIMEOUT_MS_DEFAULT = 45000"

### C0436 · k-rebalance · L2 · tr
> group.consumer.assignors (broker) | uniform, range | Server-side assignors; first is default | Custom server-side assignor
- pass 1: ✅ GroupCoordinatorConfig.java:214-222; consumer-rebalance-protocol.md:47-50 — Uniform, Range; "first assignor in the list"
- pass 2: ✅ GroupCoordinatorConfig.java:215-222 — built-ins UniformAssignor, RangeAssignor; "The first one in the list is considered as the default assignor"

### C0437 · k-rebalance · L2 · tr
> group.initial.rebalance.delay.ms (broker) | 3000 | Delay the first classic rebalance of an empty group so more members can join | Tests (set 0) or large groups starting together
- pass 1: ✅ GroupCoordinatorConfig.java:171-173 — "wait for more consumers to join a new group before performing the first rebalance"; 3000
- pass 2: ✅ GroupCoordinatorConfig.java:171-173 — "wait for more consumers to join a new group before performing the first rebalance"; default 3000

### C0438 · k-rebalance · L2 · p
> Start two console consumers in the same group, one classic and one with the new protocol (in two terminals, one command each), then describe the group. What type does the group report, and what happens to the second consumer?
- pass 1: n/a (exercise prompt)
- pass 2: n/a — exercise prompt

### C0439 · k-rebalance · L2 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic rb-demo --partitions 6 bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic rb-demo --group g1 \ --command-property group.protocol=consumer bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic rb-demo --group g1 bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list --type bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group g1 --members --verbose bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group g1 --state
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ tools ConsoleConsumerOptions.java:99 "command-property"; ConsumerGroupCommandOptions.java:191-193 --type optional arg with --list; --members/--verbose/--state exist

### C0440 · k-rebalance · L2 · p
> The first consumer creates a Consumer-type group. The classic member still joins: the coordinator supports mixed groups so you can migrate with a rolling restart, and it serves the classic member through the classic JoinGroup/SyncGroup APIs. --list --type shows the group's TYPE (Consumer). --members alone lists both members with a #PARTITIONS count (6 split between them); add --verbose to see the actual assigned partitions. --state shows the coordinator, assignment strategy, state and member count. Stop one consumer and describe again: its partitions move to the other.
- pass 1: ✅ revised after pass-2 finding — C0440 — --members shows a #PARTITIONS count; command now uses --members --verbose for actual assignments — ConsumerGroupCommand.java:432 "\"CLIENT-ID\", \"#PARTITIONS\""; :440 if (verbose)
- pass 2: ✅ GroupCoordinatorConfig.java:228 migration policy default BIDIRECTIONAL; ConsumerGroupCommand.java:434,441 "#PARTITIONS", verbose adds "CURRENT-ASSIGNMENT"; :523-529 "COORDINATOR (ID)","ASSIGNMENT-STRATEGY","STATE","#MEMBERS"

### C0441 · k-rebalance · L3 · summary
> L3🔬 Go deeper: inside the new group coordinator
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0442 · k-rebalance · L3 · p
> Since 4.0 the coordinator is the Java group-coordinator module. GroupCoordinatorService routes each request to a coordinator shard for the right __consumer_offsets partition (partitionFor = Utils.abs(groupId.hashCode()) % numPartitions). GroupMetadataManager holds ClassicGroup and ConsumerGroup objects. Classic groups move through states like PreparingRebalance → CompletingRebalance → Stable.
- pass 1: ✅ GroupCoordinatorService.java:416-427; coordinator-common/.../runtime/CoordinatorShard.java; ClassicGroupState.java:59,74 — partitionFor; PreparingRebalance/CompletingRebalance
- pass 2: ✅ GroupCoordinatorService.java:423-427 partitionFor = Utils.abs(groupId.hashCode()) % numPartitions; classic/ClassicGroupState.java:59/74/90 PreparingRebalance, CompletingRebalance, Stable

### C0443 · k-rebalance · L3 · p
> For KIP-848, TargetAssignmentBuilder computes the desired assignment for a new group epoch, and CurrentAssignmentBuilder is "the reconciliation engine": per member it walks the states STABLE, UNREVOKED_PARTITIONS (the member must revoke some partitions before moving to the next epoch) and UNRELEASED_PARTITIONS (the member moved to the new epoch but waits for partitions that previous owners haven't released yet). A member with a stale epoch gets FENCED_MEMBER_EPOCH: "the member must abandon all its partitions and rejoin".
- pass 1: ✅ modern/consumer/CurrentAssignmentBuilder.java doc; modern/TargetAssignmentBuilder.java; MemberState.java:32-45; Errors.java:398 — "reconciliation engine"; "must abandon all its partitions and rejoin"
- pass 2: ✅ modern/consumer/CurrentAssignmentBuilder.java:37 — "encapsulates the reconciliation engine"; MemberState.java:32-45; GroupMetadataManager.java:1639 — "The member must abandon all its partitions and rejoin."

### C0444 · k-rebalance · L3 · p
> Wire APIs: classic uses FindCoordinator (key 10), JoinGroup (11), Heartbeat (12), LeaveGroup (13) and SyncGroup (14); the new protocol uses ConsumerGroupHeartbeat (68) with fields MemberEpoch, InstanceId, RebalanceTimeoutMs, SubscribedTopicNames/SubscribedTopicRegex (RE2J, evaluated on the server), ServerAssignor and TopicPartitions.
- pass 1: ✅ clients/.../message/*Request.json:17 — apiKey 10/11/12/13/14/68; consumer-rebalance-protocol.md:68 RE2J server-side
- pass 2: ✅ message JSON apiKeys: FindCoordinator 10, JoinGroup 11, Heartbeat 12, LeaveGroup 13, SyncGroup 14, ConsumerGroupHeartbeat 68; fields MemberEpoch, InstanceId, RebalanceTimeoutMs, SubscribedTopicNames/Regex, ServerAssignor, TopicPartitions; RE2J server-side per rebalance docs

### C0445 · k-rebalance · L2 · li
> The coordinator for a group lives on the leader of __consumer_offsets partition abs(hash(groupId)) % 50.
- pass 1: ✅ GroupCoordinatorService.java:427; GroupCoordinatorConfig.java:114; KafkaApis.scala:1286-1290
- pass 2: ✅ GroupCoordinatorService.java:427; OFFSETS_TOPIC_PARTITIONS_DEFAULT = 50 (default; configurable)

### C0446 · k-rebalance · L2 · li
> Classic = JoinGroup/SyncGroup with a client-side assignor and a global barrier. Eager revokes everything; cooperative revokes only what moves (two rounds).
- pass 1: ✅ ConsumerPartitionAssignor.java:250-258; consumer-rebalance-protocol.md:32
- pass 2: ✅ summary of C0406-C0411 (sources as there)

### C0447 · k-rebalance · L2 · li
> KIP-848 (group.protocol=consumer) = heartbeat-driven, server-side assignor, per-member incremental reconciliation, no barrier.
- pass 1: ✅ consumer-rebalance-protocol.md:32,47,66
- pass 2: ✅ consumer-rebalance-protocol.md — "fully incremental design, which no longer relies on a global synchronization barrier"

### C0448 · k-rebalance · L2 · li
> Crash → partitions idle until the session timeout (45 s). Slow poll() → out after max.poll.interval.ms (5 min).
- pass 1: ✅ ConsumerConfig.java:438,629; CommonClientConfigs.java:192-214
- pass 2: ✅ session.timeout.ms 45000; max.poll.interval.ms 300000 (ConsumerConfig.java:436/627)

### C0449 · k-rebalance · L2 · li
> group.instance.id (static membership) avoids rebalances on quick restarts.
- pass 1: ✅ docs/design/design.md:123 — "Group membership remains unchanged… no rebalance will be triggered"
- pass 2: ✅ static membership via group.instance.id avoids rebalances on restarts within session timeout (KIP-345)
