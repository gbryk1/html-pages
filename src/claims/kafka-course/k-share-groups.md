# Claims ledger — kafka-course.html — k-share-groups

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0685 · k-share-groups · L3 · p
> Consumer groups give you ordering and scale by handing each partition to exactly one member. That's perfect for ordered streams — and awkward for a work queue. Want 50 workers on a topic with 8 partitions? Tough: 42 sit idle. One slow record blocks everything behind it in that partition. And there's no built-in notion of "this record failed three times, stop trying".
- pass 1: n/a (motivation / pedagogy (consumer-group assignment per docs/design/design.md:248))
- pass 2: n/a — motivation (one partition per member per docs/design/design.md:254 contrast); "42 idle" arithmetic illustrative

### C0686 · k-share-groups · L3 · p
> Share groups are a second kind of group for exactly those workloads. They were previewed in 4.1 and declared production-ready in 4.2. The design docs list the differences: consumers cooperatively consume and a partition may be assigned to several consumers; there can be more consumers than partitions; records are acknowledged individually (optimised for batches); and delivery attempts are counted, so unprocessable records can be handled automatically. The price: no ordering guarantee between records handed to different consumers.
- pass 1: ✅ docs/getting-started/upgrade.md:84,167; docs/design/design.md:252-257 — "The number of consumers in a share group can exceed the number of partitions"
- pass 2: ✅ docs/getting-started/upgrade.md:167 "4.1 ships with a preview", docs/getting-started/upgrade.md:84 "production-ready in Apache Kafka 4.2"; docs/design/design.md:254-257 differences listed verbatim; ordering loss inherent to shared partitions

### C0687 · k-share-groups · L3 · p
> In the archive analogy: instead of assigning each clerk a whole book, the clerks share the book, and there's a sign-out sheet next to it. A clerk signs out a few lines, works on them, and ticks them "done" — or hands them back. If a clerk vanishes, their sign-outs expire and the lines go back into the pool. The sheet also counts how often each line was signed out; after too many attempts the line is stamped "unprocessable" and set aside.
- pass 1: n/a (analogy)
- pass 2: n/a — analogy (consistent with docs/design/design.md:265-277)

### C0688 · k-share-groups · L3 · figcaption
> Simplified: one partition, 2 records per fetch, lock timeout compressed to a second or two. Delivery limit 5 = the default group.share.delivery.count.limit. The small number on each record is its delivery count; SPSO/SPEO are the share-partition start/end offsets.
- pass 1: n/a (figcaption, labelled simplified; limit 5 per group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:49)
- pass 2: ✅ group-coordinator/.../share/ShareGroupConfig.java:49 SHARE_GROUP_DELIVERY_COUNT_LIMIT_DEFAULT = 5; figure numbers n/a

### C0689 · k-share-groups · L3 · h3
> Acquisition locks and the record lifecycle
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0690 · k-share-groups · L3 · p
> When a share consumer fetches, the broker hands it available records and marks them acquired with a time-limited acquisition lock — 30 s by default (share.record.lock.duration.ms group config, broker default group.share.record.lock.duration.ms). While acquired, no other member of the group gets them. The holder then does one of:
- pass 1: ✅ docs/design/design.md:265-267; group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:60-61 — "By default, the lock duration is 30 seconds"
- pass 2: ✅ docs/design/design.md:265-267 — "time-limited acquisition lock" ... "By default, the lock duration is 30 seconds ... share.record.lock.duration.ms"; group-coordinator/.../share/ShareGroupConfig.java:60-61 broker 30000

### C0691 · k-share-groups · L3 · li
> ACCEPT — processed successfully; the record becomes acknowledged.
- pass 1: ✅ docs/design/design.md:269; clients/src/main/java/org/apache/kafka/clients/consumer/AcknowledgeType.java:27 — "Acknowledge successful processing of the record."
- pass 2: ✅ docs/design/design.md:269 "Acknowledge successful processing"; clients/.../AcknowledgeType.java:27 ACCEPT; RecordState ACKNOWLEDGED

### C0692 · k-share-groups · L3 · li
> RELEASE — give it back for another delivery attempt.
- pass 1: ✅ docs/design/design.md:270 — "Release the record, making it available for another delivery attempt."
- pass 2: ✅ docs/design/design.md:270 — "Release the record, making it available for another delivery attempt."

### C0693 · k-share-groups · L3 · li
> REJECT — unprocessable; archive it, never deliver again.
- pass 1: ✅ docs/design/design.md:271 — "Reject the record, indicating it's unprocessable"
- pass 2: ✅ docs/design/design.md:271 — "Reject the record, indicating it's unprocessable and preventing further delivery attempts"

### C0694 · k-share-groups · L3 · li
> RENEW — still working on it; extend the lock.
- pass 1: ✅ docs/design/design.md:272 — "Renew the record, extending the delivery attempt"
- pass 2: ✅ docs/design/design.md:272 — "Renew the record, extending the delivery attempt"; AcknowledgeType.java:36 RENEW

### C0695 · k-share-groups · L3 · li
> nothing — the lock expires and the record becomes available again.
- pass 1: ✅ docs/design/design.md:273 — "the lock is automatically released when its duration elapses"
- pass 2: ✅ docs/design/design.md:273 — "Do nothing, in which case the lock is automatically released when its duration elapses."

### C0696 · k-share-groups · L3 · p
> Each acquisition bumps the record's delivery count. When a record would become available again but has already reached the limit (share.delivery.count.limit, broker default group.share.delivery.count.limit = 5), it's archived instead. That's your built-in poison-pill guard. In 4.3.1, archived simply means "no more delivery attempts": dead-letter queues for share groups (KIP-1191) exist only as groundwork in the source — a ShareGroupDLQ interface with a no-op implementation and the ARCHIVING record state — with no configuration and nothing actually written to a DLQ topic yet.
- pass 1: ❌ fixed in part file — ShareGroupDLQ.java + NoOpShareGroupDLQManager.java (KIP-1191 groundwork); GroupConfig share.delivery.count.limit; group.share.delivery.count.limit default 5
- pass 2: ✅ server/.../share/fetch/InFlightState.java:163-164 AVAILABLE && deliveryCount >= maxDeliveryCount → ARCHIVED; group-coordinator/.../share/ShareGroupConfig.java:49 default 5; server-common/.../share/dlq/ShareGroupDLQ.java + NoOpShareGroupDLQManager only (no non-test callers, no DLQ config found)

### C0697 · k-share-groups · L3 · p
> The broker also caps in-flight work per partition: group.share.partition.max.record.locks (default 2000 acquired records). Hit it and fetches return nothing until locks are released or time out.
- pass 1: ✅ docs/design/design.md:277; group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:36-37 — "fetching operations will temporarily yield no further records"
- pass 2: ✅ docs/design/design.md:277 — "fetching operations will temporarily yield no further records"; group-coordinator/.../share/ShareGroupConfig.java:36-37 default 2000

### C0698 · k-share-groups · L3 · h3
> Where the state lives
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0699 · k-share-groups · L3 · p
> For each share-partition (group × topic-partition) the partition leader keeps the in-flight window between the share-partition start offset (SPSO) — everything below is done — and the share-partition end offset (SPEO). The durable version of this is written by the share coordinator to the internal topic __share_group_state (50 partitions, RF 3, min ISR 2 by default): a snapshot has the start offset plus state batches of (firstOffset, lastOffset, deliveryState, deliveryCount), where the state is 0 Available, 2 Acked or 4 Archived. Membership and assignment are handled by the group coordinator, with a heartbeat protocol (ShareGroupHeartbeat) similar to KIP-848.
- pass 1: ✅ core/src/main/java/kafka/server/share/SharePartition.java:279-285; share-coordinator/src/main/resources/common/message/ShareSnapshotValue.json:20-50; share-coordinator/.../ShareCoordinatorConfig.java:37-47 — "The delivery state - 0:Available,2:Acked,4:Archived."
- pass 2: ✅ share-coordinator/.../ShareSnapshotValue.json:29,38-46 StartOffset + StateBatches, "0:Available,2:Acked,4:Archived"; ShareCoordinatorConfig.java:38,42,47 50/3/2; ShareGroupHeartbeatRequest.json exists

### C0700 · k-share-groups · L3 · div
> Consumer groupOne partition, one owner. You get strict order and a single bookmark per partition. Cheap and simple.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue/pedagogy

### C0701 · k-share-groups · L3 · div
> Share groupAnd your 42 extra workers stare at the wall. I let them all pull from the same partitions and track every record separately.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue/pedagogy

### C0702 · k-share-groups · L3 · div
> Consumer groupTracking every record costs state. And you scramble the order.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue/pedagogy

### C0703 · k-share-groups · L3 · div
> Share groupRight — use me when records are independent jobs, processed one at a time. If order matters, you're still the one to call.
- pass 1: ✅ docs/getting-started/upgrade.md:84 — "Use share groups in cases where records are processed one at a time"
- pass 2: n/a — dialogue/advice

### C0704 · k-share-groups · L3 · tr
> share.record.lock.duration.ms (group) / group.share.record.lock.duration.ms (broker) | 30000 (broker bounds 15000–60000) | Acquisition lock length | Match to your worst normal processing time; RENEW for outliers
- pass 1: ✅ group-coordinator/src/main/java/org/apache/kafka/coordinator/group/GroupConfig.java:64,187-190; group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:60-69 — "SHARE_GROUP_MIN_RECORD_LOCK_DURATION_MS_DEFAULT = 15000"
- pass 2: ✅ group-coordinator/.../GroupConfig.java:64; group-coordinator/.../share/ShareGroupConfig.java:61,65,69 — default 30000, min 15000, max 60000 (bounds are themselves configurable broker configs); advice n/a

### C0705 · k-share-groups · L3 · tr
> share.delivery.count.limit (group, new in 4.3) / group.share.delivery.count.limit | 5 (broker bounds 2–10) | Attempts before a record is archived | Lower for fail-fast, higher for flaky downstreams
- pass 1: ✅ docs/getting-started/upgrade.md:52; group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:48-57 — "SHARE_GROUP_DELIVERY_COUNT_LIMIT_DEFAULT = 5"
- pass 2: ✅ docs/getting-started/upgrade.md:52 "New group configs ... share.delivery.count.limit, share.partition.max.record.locks" (4.3.0); group-coordinator/.../share/ShareGroupConfig.java:49,53,57 — 5 / max 10 / min 2

### C0706 · k-share-groups · L3 · tr
> share.partition.max.record.locks (group, new in 4.3) / group.share.partition.max.record.locks | 2000 (broker bounds 100–4000) | Max acquired records per share-partition | Many consumers on few partitions need more in-flight
- pass 1: ✅ docs/getting-started/upgrade.md:52; group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:36-45 — "SHARE_GROUP_PARTITION_MAX_RECORD_LOCKS_DEFAULT = 2000"
- pass 2: ✅ docs/getting-started/upgrade.md:52; group-coordinator/.../share/ShareGroupConfig.java:37,41,45 — 2000 / max 4000 / min 100

### C0707 · k-share-groups · L3 · tr
> share.auto.offset.reset (group) | latest | Where a new share group starts | earliest to process the backlog
- pass 1: ✅ group-coordinator/src/main/java/org/apache/kafka/coordinator/group/GroupConfig.java:70-72 — "SHARE_AUTO_OFFSET_RESET_DEFAULT = ShareGroupAutoOffsetResetStrategy.LATEST"
- pass 2: ✅ group-coordinator/.../GroupConfig.java:70-71 SHARE_AUTO_OFFSET_RESET_DEFAULT = LATEST

### C0708 · k-share-groups · L3 · tr
> share.isolation.level (group) | read_uncommitted | Whether to deliver records of open/aborted transactions | read_committed downstream of transactional producers
- pass 1: ✅ group-coordinator/src/main/java/org/apache/kafka/coordinator/group/GroupConfig.java:80-81 — "SHARE_ISOLATION_LEVEL_DEFAULT = IsolationLevel.READ_UNCOMMITTED"
- pass 2: ✅ group-coordinator/.../GroupConfig.java:80-81 SHARE_ISOLATION_LEVEL_DEFAULT = READ_UNCOMMITTED

### C0709 · k-share-groups · L3 · tr
> share.acknowledgement.mode (consumer) | implicit | implicit: next poll/commit accepts the batch; explicit: call acknowledge() per record | explicit when you need RELEASE/REJECT per record
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:375-381,692-694 — "delivery is acknowledged implicitly on the next call to poll or commit"
- pass 2: ✅ clients/.../ConsumerConfig.java:375-381,694 default IMPLICIT — "acknowledged implicitly on the next call to poll or commit"; explicit must use acknowledge()

### C0710 · k-share-groups · L3 · p
> Start two console share consumers in the same share group on a 1-partition topic, make one reject everything, set the group's delivery limit, and inspect the group. Commands?
- pass 1: n/a (exercise prompt)
- pass 2: n/a — lab prompt

### C0711 · k-share-groups · L3 · pre
> bin/kafka-console-share-consumer.sh --bootstrap-server localhost:9092 --topic jobs --group workers bin/kafka-console-share-consumer.sh --bootstrap-server localhost:9092 --topic jobs --group workers --reject bin/kafka-configs.sh --bootstrap-server localhost:9092 --alter --entity-type groups --entity-name workers \ --add-config share.delivery.count.limit=3,share.auto.offset.reset=earliest bin/kafka-share-groups.sh --bootstrap-server localhost:9092 --describe --group workers --members bin/kafka-share-groups.sh --bootstrap-server localhost:9092 --describe --group workers --offsets
- pass 1: ✅ tools/.../consumer/ConsoleShareConsumerOptions.java:60,144,160; tools/.../consumer/group/ShareGroupCommandOptions.java:104-140; core/src/main/scala/kafka/admin/ConfigCommand.scala:57 — "--entity-type groups --entity-name <group> / accepts("reject""
- pass 2: ✅ tools/.../consumer/ConsoleShareConsumerOptions.java:60,144,160 --topic/--reject/--group; core/.../ConfigCommand.scala:73,191 entity-type groups; ShareGroupCommandOptions.java:104,114,136,138 --group/--describe/--members/--offsets

### C0712 · k-share-groups · L3 · p
> Both consumers receive records from the same partition. On a cluster with fewer than 3 brokers, first set share.coordinator.state.topic.replication.factor and share.coordinator.state.topic.min.isr to 1 — the upgrade notes call this out.
- pass 1: ✅ docs/getting-started/upgrade.md:79; docs/design/design.md:254 — "set the broker configurations share.coordinator.state.topic.replication.factor and … min.isr to 1"
- pass 2: ✅ docs/getting-started/upgrade.md:79 — "fewer than 3 brokers, you must set ... share.coordinator.state.topic.replication.factor and ...min.isr to 1"; docs/design/design.md:254

### C0713 · k-share-groups · L3 · summary
> L3🔬 Go deeper: share-partition internals
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C0714 · k-share-groups · L3 · p
> The broker-side state machine is org.apache.kafka.server.share.fetch.RecordState: AVAILABLE, ACQUIRED, ACKNOWLEDGED, ARCHIVING, ARCHIVED (ARCHIVING per KIP-1191, the share-group dead-letter-queue KIP; org.apache.kafka.server.share.dlq.ShareGroupDLQ defines reasons such as DELIVERY_COUNT_EXCEEDED and CLIENT_REJECT, but only NoOpShareGroupDLQManager exists and the broker does not call it in 4.3.1). In-flight ranges are InFlightBatches holding an InFlightState with the delivery count, member id and an AcquisitionLockTimerTask. The poison-pill rule is literally: if (newState == AVAILABLE && ops != DECREASE && deliveryCount >= maxDeliveryCount) newState = ARCHIVED.
- pass 1: ❌ fixed in part file — RecordState.java enum AVAILABLE, ACQUIRED, ACKNOWLEDGED, ARCHIVING, ARCHIVED; ShareGroupDLQ reasons DELIVERY_COUNT_EXCEEDED, CLIENT_REJECT
- pass 2: ✅ server/.../share/fetch/RecordState.java:27-31 (ARCHIVING "Per KIP-1191"); ShareGroupDLQ.java:36-37 CLIENT_REJECT, DELIVERY_COUNT_EXCEEDED; InFlightBatch.java, server/.../share/fetch/InFlightState.java:45-52 deliveryCount/memberId/AcquisitionLockTimerTask; server/.../share/fetch/InFlightState.java:163 rule verbatim

### C0715 · k-share-groups · L3 · p
> Protocol: ShareGroupHeartbeat (membership/assignment), ShareFetch (fetch + piggy-backed acknowledgements, with share sessions capped by group.share.max.share.sessions, 2000), ShareAcknowledge, and the coordinator-facing InitializeShareGroupState, ReadShareGroupState, WriteShareGroupState, DeleteShareGroupState. Share fetches that must wait use a ShareFetch purgatory (monitoring exposes PurgatorySize,delayedOperation=ShareFetch).
- pass 1: ✅ clients/src/main/resources/common/message/ShareFetchRequest.json:33,58; group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:76-77; docs/operations/monitoring.md:6620 — "AcknowledgementBatches / SHARE_GROUP_MAX_SHARE_SESSIONS_DEFAULT = 2000"
- pass 2: ✅ clients/.../message/ ShareGroupHeartbeat/ShareFetch/ShareAcknowledge/Initialize/Read/Write/DeleteShareGroupState*.json; ShareFetchRequest.json:58 AcknowledgementBatches; group-coordinator/.../share/ShareGroupConfig.java:77 max.share.sessions 2000; docs/operations/monitoring.md:6620 PurgatorySize,delayedOperation=ShareFetch

### C0716 · k-share-groups · L3 · p
> Feature flag: share.version — SV_1 is bound to metadata version IBP_4_2_IV0 and is the latest production level. 4.3 deprecates group.coordinator.rebalance.protocols; in 5.0 group types are controlled only by feature versions (group.version, streams.version, share.version).
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/ShareVersion.java:28-32; docs/getting-started/upgrade.md:51 — "SV_1(1, MetadataVersion.IBP_4_2_IV0"
- pass 2: ✅ server-common/.../ShareVersion.java:28,32 — "SV_1(1, MetadataVersion.IBP_4_2_IV0" / LATEST_PRODUCTION = SV_1; docs/getting-started/upgrade.md:51 rebalance.protocols deprecated, 5.0 controlled by group.version, streams.version, share.version

### C0717 · k-share-groups · L3 · li
> Share groups (GA in 4.2) let many consumers share partitions; more consumers than partitions is fine; ordering across consumers is not guaranteed.
- pass 1: ✅ docs/getting-started/upgrade.md:84; docs/design/design.md:254-255 — "partitions may be assigned to multiple consumers"
- pass 2: ✅ docs/getting-started/upgrade.md:84 production-ready in 4.2; docs/design/design.md:254-255

### C0718 · k-share-groups · L3 · li
> Records are acquired with a time-limited lock (30 s default) and individually accepted, released, rejected or renewed.
- pass 1: ✅ docs/design/design.md:265-273 — "time-limited acquisition lock"
- pass 2: ✅ docs/design/design.md:265-273; group-coordinator/.../share/ShareGroupConfig.java:61 30000

### C0719 · k-share-groups · L3 · li
> Delivery count limit (default 5) archives poison records automatically; 4.3 made it a per-group config.
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/share/fetch/InFlightState.java:163-164; docs/getting-started/upgrade.md:52 — "New group configs have been introduced: share.delivery.count.limit"
- pass 2: ✅ group-coordinator/.../share/ShareGroupConfig.java:49 default 5; server/.../share/fetch/InFlightState.java:163; docs/getting-started/upgrade.md:52 share.delivery.count.limit new group config in 4.3

### C0720 · k-share-groups · L3 · li
> Durable state: SPSO + state batches in __share_group_state, written by the share coordinator.
- pass 1: ✅ share-coordinator/src/main/resources/common/message/ShareSnapshotValue.json:26-50; docs/getting-started/upgrade.md:79 — "__share_group_state"
- pass 2: ✅ ShareSnapshotValue.json:29,38; clients/.../internals/Topic.java:29 __share_group_state

### C0721 · k-share-groups · L3 · li
> Use them for independent jobs processed one at a time; keep consumer groups for ordered streams.
- pass 1: ✅ docs/getting-started/upgrade.md:84 — "Use share groups in cases where records are processed one at a time, rather than as part of an ordered stream."
- pass 2: n/a — advice
