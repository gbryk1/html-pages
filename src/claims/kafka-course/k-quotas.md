# Claims ledger — kafka-course.html — k-quotas

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0757 · k-quotas · L4 · p
> Up to now your archive served one well-behaved city department. Real clusters are shared: payments, search, analytics and one intern's load test all write to the same branches. The official docs call the result "noisy neighbors": one tenant saturates the network or the request threads, and everyone else's latency goes through the roof.
- pass 1: ✅ docs/operations/multi-tenancy.md:31 — "noisy neighbors"
- pass 2: ✅ docs/operations/multi-tenancy.md:31 — "minimize potential collateral damage caused by \"noisy neighbors\""; :114 "network saturation, monopolize broker resources" (framing is pedagogy)

### C0758 · k-quotas · L4 · p
> In the ledger-archive picture, a quota is a counter rule: "each patron group may carry out at most X pages per second at this branch". A patron who goes faster isn't turned away. The clerk just says "sit down for a moment" and doesn't serve them until they're back under the limit. That is exactly how Kafka works: quotas throttle by delaying, not by rejecting (with one exception, below).
- pass 1: ✅ docs/design/design.md:509 — "returns a response with the delay immediately"
- pass 2: ✅ design/design.md:509 — "computes the amount of delay needed ... returns a response with the delay immediately" (analogy n/a; delay-not-reject confirmed, mutation-quota exception below)

### C0759 · k-quotas · L4 · h3
> What can you limit?
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0760 · k-quotas · L4 · p
> Kafka has two families of client quotas. Network bandwidth quotas are byte-rate thresholds: producer_byte_rate and consumer_byte_rate, in bytes/sec. Request rate quotas (request_percentage) cap the share of broker network and I/O thread time a group may use. A quota of n% means n% of one thread, so the total capacity is (num.io.threads + num.network.threads) × 100%. On top of those, controller_mutation_rate limits how fast a client may create topics, create partitions and delete topics, counted in partitions.
- pass 1: ✅ docs/design/design.md:465 — "Network bandwidth quotas define byte-rate" ; docs/design/design.md:503 — "represents `n%` of one thread" ; server-common/src/main/java/org/apache/kafka/server/config/QuotaConfig.java:98 — "accumulated by"
- pass 2: ✅ design.md:465-466,503 — "quota is out of a total capacity of ((num.io.threads + num.network.threads) * 100)%"; QuotaConfig.java:89-99 producer_byte_rate/request_percentage/controller_mutation_rate — "create topics ... create partitions ... delete topics ... accumulated by the number of partitions"

### C0761 · k-quotas · L4 · p
> There are also broker-side connection limits, which aren't tied to a client identity: max.connections.per.ip, max.connection.creation.rate, and a per-IP connection_creation_rate quota. All of them default to "unlimited" (Integer.MAX_VALUE).
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:111 — "MAX_CONNECTIONS_PER_IP_DEFAULT = Integer.MAX_VALUE" ; server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:127 — "MAX_CONNECTION_CREATION_RATE_DEFAULT = Integer.MAX_VALUE" ; server-common/src/main/java/org/apache/kafka/server/config/QuotaConfig.java:103 — "IP_CONNECTION_RATE_DEFAULT = Integer.MAX_VALUE"
- pass 2: ✅ SocketServerConfigs.java:111,127 MAX_CONNECTIONS_PER_IP_DEFAULT / MAX_CONNECTION_CREATION_RATE_DEFAULT = Integer.MAX_VALUE; QuotaConfig.java:93,103 connection_creation_rate, IP_CONNECTION_RATE_DEFAULT = Integer.MAX_VALUE

### C0762 · k-quotas · L4 · p
> The multi-tenancy docs claim that request-rate quotas often matter more than bandwidth quotas. How can a tenant hurt everyone else while moving hardly any bytes? (Think about thousands of tiny produce requests with linger.ms=0, or a consumer with fetch.max.wait.ms set near zero.)
- pass 1: ✅ docs/operations/multi-tenancy.md:116 — "bigger impact in multi-tenant clusters than setting incoming/outgoing network bandwidth quotas"
- pass 2: ✅ multi-tenancy.md:116 — "isolating users with request rate quotas has a bigger impact ... than setting incoming/outgoing network bandwidth quotas" (question itself is pedagogy)

### C0763 · k-quotas · L4 · h3
> Who is "a client"? Users, client-ids and precedence
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0764 · k-quotas · L4 · p
> A quota applies to a group of clients. The identity is the authenticated user principal, the application-chosen client.id, or the pair (user, client-id). Every connection in a quota group shares the quota. If (user=test-user, client-id=test-client) gets 10 MB/s, all producer instances with that pair split those 10 MB/s together. For a given connection, the most specific matching quota wins, in this order:
- pass 1: ✅ docs/design/design.md:478 — "shared across all producer instances" ; docs/design/design.md:478 — "the most specific quota matching the connection is applied"
- pass 2: ✅ design.md:476-478 — "if (user=\"test-user\", client-id=\"test-client\") has a produce quota of 10MB/sec, this is shared across all producer instances"

### C0765 · k-quotas · L4 · li
> matching user and client-id
- pass 1: ✅ docs/design/design.md:486 — "matching user and client-id quotas"
- pass 2: ✅ design.md:486 — "matching user and client-id quotas"

### C0766 · k-quotas · L4 · li
> matching user, default client-id
- pass 1: ✅ docs/design/design.md:487 — "matching user and default client-id quotas"
- pass 2: ✅ design.md:487 — "matching user and default client-id quotas"

### C0767 · k-quotas · L4 · li
> matching user
- pass 1: ✅ docs/design/design.md:488 — "matching user quota"
- pass 2: ✅ design.md:488 — "matching user quota"

### C0768 · k-quotas · L4 · li
> default user, matching client-id
- pass 1: ✅ docs/design/design.md:489 — "default user and matching client-id quotas"
- pass 2: ✅ design.md:489 — "default user and matching client-id quotas"

### C0769 · k-quotas · L4 · li
> default user, default client-id
- pass 1: ✅ docs/design/design.md:490 — "default user and default client-id quotas"
- pass 2: ✅ design.md:490 — "default user and default client-id quotas"

### C0770 · k-quotas · L4 · li
> default user
- pass 1: ✅ docs/design/design.md:491 — "default user quota"
- pass 2: ✅ design.md:491 — "default user quota"

### C0771 · k-quotas · L4 · li
> matching client-id
- pass 1: ✅ docs/design/design.md:489 — "matching client-id quota"
- pass 2: ✅ design.md:492 — "matching client-id quota"

### C0772 · k-quotas · L4 · li
> default client-id
- pass 1: ✅ docs/design/design.md:487 — "default client-id quota"
- pass 2: ✅ design.md:493 — "default client-id quota"

### C0773 · k-quotas · L4 · p
> Quota overrides are written to the KRaft metadata log. Every broker reads them and they take effect immediately, so you never need a rolling restart to re-tune a tenant. By default there is no quota at all: clients get unlimited throughput until you set one.
- pass 1: ✅ docs/design/design.md:482 — "quota overrides are written to the metadata log" ; docs/operations/basic-kafka-operations.md:690 — "By default, clients receive an unlimited quota"
- pass 2: ✅ design.md:482 — "quota overrides are written to the metadata log ... read by all brokers and are effective immediately"; QuotaConfig.java:87 QUOTA_BYTES_PER_SECOND_DEFAULT = Long.MAX_VALUE

### C0774 · k-quotas · L4 · figcaption
> Illustrative numbers: quota 10 MB/s per broker, one 2-second window. The real broker measures rates over quota.window.num (default 11) samples of quota.window.size.seconds (default 1 s), and the delay formula is the one in QuotaUtils.throttleTime. The latency effect in "Remove all quotas" is qualitative.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/config/QuotaConfig.java:42 — "NUM_QUOTA_SAMPLES_DEFAULT = 11" ; server-common/src/main/java/org/apache/kafka/server/config/QuotaConfig.java:52 — "QUOTA_WINDOW_SIZE_SECONDS_DEFAULT = 1"
- pass 2: ✅ QuotaConfig.java:32,41 quota.window.num default 11; :44,52 quota.window.size.seconds default 1; QuotaUtils.java:41 throttleTime (rest labelled illustrative)

### C0775 · k-quotas · L4 · h3
> How throttling actually works
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0776 · k-quotas · L4 · p
> When a group goes over its quota, the broker computes how long the client must stay silent for its average rate to drop back under the limit. It returns the response immediately, with that delay in the ThrottleTimeMs field (a fetch response carries no data in that case). Then it mutes the channel: it stops reading from that client's socket until the delay is over. A modern client sees a non-zero throttle time and holds back on its own. An old or misbehaving client that keeps sending anyway still gets nowhere, because its requests sit unread in a muted socket. The docs describe this as blocking the client "from both sides".
- pass 1: ✅ docs/design/design.md:509 — "returns a response with the delay immediately" ; clients/src/main/resources/common/message/ProduceResponse.json:82 — "ThrottleTimeMs" ; docs/design/design.md:509 — "Therefore, requests from a throttled client are effectively blocked from both sides"
- pass 2: ✅ design.md:509 — "In case of a fetch request, the response will not contain any data. Then, the broker mutes the channel ... effectively blocked from both sides"

### C0777 · k-quotas · L4 · p
> Why per broker and not cluster-wide? A cluster-wide limit would need the brokers to share their quota usage with each other in real time. The design docs say bluntly that this "can be harder to get right than the quota implementation itself!" So a 10 MB/s quota means 10 MB/s at each broker. A tenant whose partitions sit on six brokers can move up to six times that in total.
- pass 1: ✅ docs/design/design.md:507 — "This can be harder to get right than the quota implementation itself"
- pass 2: ✅ design.md:507 — "would require a mechanism to share client quota usage among all the brokers. This can be harder to get right than the quota implementation itself!"; per-broker quota (design.md:499)

### C0778 · k-quotas · L4 · div
> So a throttled producer never gets an error?
- pass 1: n/a (question heading)
- pass 2: n/a (FAQ question)

### C0779 · k-quotas · L4 · div
> For byte-rate and request quotas, no. It just slows down, and you'll see it in produce-throttle-time-avg / fetch-throttle-time-avg on the client and in the broker's throttle-time attribute. The exception is controller_mutation_rate: once the strict mutation quota is exhausted, operations above it are rejected with THROTTLING_QUOTA_EXCEEDED. The Java admin client retries them by default (CreateTopicsOptions.retryOnQuotaViolation is true).
- pass 1: ❌ fixed in part file — CreateTopicsOptions.java:28 retryOnQuotaViolation default true; error THROTTLING_QUOTA_EXCEEDED (multi-tenancy.md)
- pass 2: ✅ Errors.java:374 THROTTLING_QUOTA_EXCEEDED(89); ControllerMutationQuotaManager.java:88-89 strict quota; CreateTopicsOptions.java:28 retryOnQuotaViolation = true; SenderMetricsRegistry.java:123 produce-throttle-time-avg; FetchMetricsRegistry.java:106 fetch-throttle-time-avg; monitoring.md:832 throttle-time attribute

### C0780 · k-quotas · L4 · div
> Why are samples so short (1 s)? Wouldn't a long window be smoother?
- pass 1: n/a (question heading)
- pass 2: n/a (FAQ question)

### C0781 · k-quotas · L4 · div
> The design docs argue the opposite. Large windows (for example 10 × 30 s) let a client burst for a long time and then punish it with a long stall. Many small windows catch a violation fast and correct it with short delays.
- pass 1: ✅ docs/design/design.md:511 — "30 windows of 1 second each"
- pass 2: ✅ design.md:511 — "large measurement windows (for e.g. 10 windows of 30 seconds each) leads to large bursts of traffic followed by long delays"

### C0782 · k-quotas · L4 · p
> On your local 4.3 cluster, give the client-id loadtest a producer quota of 1 MB/s, describe it, then run the perf tool with that client id and watch the throughput settle.
- pass 1: n/a (exercise prompt / pedagogy)
- pass 2: n/a (exercise prompt)

### C0783 · k-quotas · L4 · pre
> bin/kafka-configs.sh --bootstrap-server localhost:9092 --alter \ --add-config 'producer_byte_rate=1048576' --entity-type clients --entity-name loadtest bin/kafka-configs.sh --bootstrap-server localhost:9092 --describe \ --entity-type clients --entity-name loadtest bin/kafka-producer-perf-test.sh --bootstrap-server localhost:9092 --topic perf \ --num-records 200000 --record-size 1000 --throughput -1 \ --command-property client.id=loadtest
- pass 1: ✅ docs/operations/basic-kafka-operations.md:695 — "--entity-type clients --entity-name clientA" ; tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:312 — "Set this to -1 to disable throttling" ; tools/src/main/java/org/apache/kafka/tools/ProducerPerformance.java:324 — "parser.addArgument("--command-property")"
- pass 2: ✅ basic-kafka-operations.md:718-732 kafka-configs.sh --alter --add-config ... --entity-type clients; ProducerPerformance.java:240-324 --bootstrap-server, --topic, --num-records, --record-size, --throughput, --command-property

### C0784 · k-quotas · L4 · p
> Throughput should hover near 1 MB/s instead of whatever your laptop can push. --entity-default instead of --entity-name sets the default for all client-ids. (--bootstrap-server and --command-property are the 4.2+ spellings from KIP-1147. The older --producer-props still works but is deprecated.)
- pass 1: ✅ docs/operations/basic-kafka-operations.md:713 — "--entity-default_ option" ; docs/getting-started/upgrade.md:101 — "`--producer-props` is deprecated in favor of `--command-property`"
- pass 2: ✅ basic-kafka-operations.md:713 — "specifying --entity-default option instead of --entity-name"; ProducerPerformance.java:632 — "--producer-props has been deprecated ... Use --command-property instead"; upgrade.md:94 (4.2.0 notes) KIP-1147

### C0785 · k-quotas · L4 · p
> Replication throttles set by kafka-reassign-partitions.sh --throttle are a different mechanism: leader.replication.throttled.rate / follower.replication.throttled.rate plus per-topic lists of throttled replicas. Forget to run --verify after the reassignment and the throttle stays behind, quietly throttling normal replication. Set it too low, below max(BytesInPerSec), and the move never finishes.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:605 — "Failure to do so could cause regular replication traffic to be throttled" ; docs/operations/basic-kafka-operations.md:675 — "max(BytesInPerSec) > throttle"
- pass 2: ✅ basic-kafka-operations.md:620-631 leader/follower.replication.throttled.rate + per-topic throttled.replicas; :668 remove via --verify; :675 — "max(BytesInPerSec) > throttle" means no progress

### C0786 · k-quotas · L4 · p
> Onboarding a new tenant onto a shared cluster, the way the multi-tenancy docs recommend:
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (intro to list)

### C0787 · k-quotas · L4 · li
> Give them a namespace with a hierarchical prefix, for example acme.infosec., and turn off auto.create.topics.enable (but don't rely on that alone).
- pass 1: ✅ docs/operations/multi-tenancy.md:77 — "you should not rely solely on this option"
- pass 2: ✅ multi-tenancy.md:57 "acme.infosec.telemetry.logins" hierarchical names; :77 — "auto.create.topics.enable=false ... you should not rely solely on this option"

### C0788 · k-quotas · L4 · li
> Grant prefixed ACLs: kafka-acls.sh --add --allow-principal User:Alice --producer --resource-pattern-type prefixed --topic acme.infosec.
- pass 1: ✅ docs/operations/multi-tenancy.md:107 — "--resource-pattern-type prefixed --topic acme.infosec."
- pass 2: ✅ multi-tenancy.md:102-107 — "--add --allow-principal User:Alice --producer --resource-pattern-type prefixed --topic acme.infosec."

### C0789 · k-quotas · L4 · li
> Set per-user quotas for bytes and request_percentage, plus controller_mutation_rate if they create topics programmatically.
- pass 1: ✅ docs/operations/multi-tenancy.md:116 — "controller_mutation_rate"
- pass 2: ✅ multi-tenancy.md:116 per-user client quotas, request rate quotas, controller_mutation_rate

### C0790 · k-quotas · L4 · li
> Set retention.ms/retention.bytes on their topics and alert on kafka.log:type=Log,name=Size.
- pass 1: ✅ docs/operations/multi-tenancy.md:83 — "retention.bytes (size) and retention.ms (time)" ; docs/operations/monitoring.md:984 — "kafka.log:type=Log,name=Size"
- pass 2: ✅ multi-tenancy.md:83 retention.bytes / retention.ms; monitoring.md:984 — "kafka.log:type=Log,name=Size,topic=...,partition=..."

### C0791 · k-quotas · L4 · li
> Graph their throttle-time. Throttling that never stops means they need a bigger contract, not a silent raise.
- pass 1: n/a (opinion / operational advice)
- pass 2: n/a (operational advice)

### C0792 · k-quotas · L4 · summary
> L4🔬 Go deeper: the delay formula and the ThrottledChannel queue
- pass 1: n/a (heading)
- pass 2: n/a (summary heading)

### C0793 · k-quotas · L4 · p
> ClientQuotaManager records each request's bytes (or thread time) in a per-group Sensor with a rate Quota. When recording breaks the bound, the metrics library throws QuotaViolationException, and QuotaUtils.throttleTime computes
- pass 1: ✅ server/src/main/java/org/apache/kafka/server/quota/ClientQuotaManager.java:339 — "catch (QuotaViolationException e)" ; server-common/src/main/java/org/apache/kafka/server/quota/QuotaUtils.java:41 — "public static long throttleTime"
- pass 2: ✅ ClientQuotaManager.java:25,339 catches QuotaViolationException from sensor record; QuotaUtils.java:41 throttleTime

### C0794 · k-quotas · L4 · pre
> throttleTimeMs = (observed − bound) / bound × windowSize
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/quota/QuotaUtils.java:44 — "difference / e.bound()"
- pass 2: ✅ QuotaUtils.java:42-44 — "difference / e.bound() * windowSize(e.metric(), timeMs)" with difference = value − bound

### C0795 · k-quotas · L4 · p
> using "the precise window used by the rate calculation". That is the time the client must stay idle for the average to fall back to the bound. ClientQuotaManager.throttle(...) then records the value in the throttle-time sensor and puts a ThrottledChannel in a delay queue. A ThrottledChannelReaper thread takes it out when the time is up and notifies the socket server that throttling is done, so the channel is unmuted. ControllerMutationQuotaManager specialises this: its quota is strict ("does not accept any mutations once the quota is exhausted") for request versions that support it, and permissive for older ones. You can replace the whole policy with client.quota.callback.class (a ClientQuotaCallback), for example to assign quotas per tenant group instead of per principal.
- pass 1: ❌ fixed in part file — ClientQuotaManager.java throttledChannel.notifyThrottlingDone(); ThrottledChannel.java:50-55
- pass 2: ✅ QuotaUtils.java:43 — "Use the precise window used by the rate calculation"; ClientQuotaManager.java:393-403 throttleTimeSensor().record, delayQueue.add(throttledChannel); :277-294 ThrottledChannelReaper notifyThrottlingDone; ControllerMutationQuotaManager.java:88-89,170-171 strict since version else permissive; QuotaConfig.java:54 client.quota.callback.class

### C0796 · k-quotas · L4 · li
> Quotas: producer_byte_rate, consumer_byte_rate, request_percentage, controller_mutation_rate, plus IP connection limits. Default: unlimited.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/config/QuotaConfig.java:92 — "CONTROLLER_MUTATION_RATE_OVERRIDE_CONFIG = "controller_mutation_rate"" ; docs/operations/basic-kafka-operations.md:690 — "By default, clients receive an unlimited quota"
- pass 2: ✅ QuotaConfig.java:87-103 defaults Long.MAX_VALUE / Integer.MAX_VALUE; SocketServerConfigs.java:111,127

### C0797 · k-quotas · L4 · li
> Entities: user, client-id, or (user, client-id). The most specific match wins, and all connections in the group share one quota.
- pass 1: ✅ docs/design/design.md:478 — "the most specific quota matching the connection is applied"
- pass 2: ✅ design.md:478 — "most specific quota matching the connection is applied. All connections of a quota group share the quota"

### C0798 · k-quotas · L4 · li
> Enforcement is per broker, by delaying: the response carries ThrottleTimeMs and the channel is muted. Mutation quotas reject with THROTTLING_QUOTA_EXCEEDED.
- pass 1: ✅ docs/design/design.md:499 — "This quota is defined on a per-broker basis" ; docs/design/design.md:509 — "the broker mutes the channel" ; server/src/main/java/org/apache/kafka/server/quota/ControllerMutationQuotaManager.java:162 — "rejected with a THROTTLING_QUOTA_EXCEEDED error"
- pass 2: ✅ design.md:507-509 per-broker, delay + mute; Errors.java:374 THROTTLING_QUOTA_EXCEEDED for strict mutation quota

### C0799 · k-quotas · L4 · li
> Changes live in the metadata log and apply immediately, with no restart.
- pass 1: ✅ docs/design/design.md:482 — "effective immediately"
- pass 2: ✅ design.md:482 — "effective immediately ... without having to do a rolling restart"

### C0800 · k-quotas · L4 · li
> Watch throttle-time (broker) and *-throttle-time-avg (client).
- pass 1: ✅ docs/operations/monitoring.md:832 — "throttle-time indicates" ; clients/src/main/java/org/apache/kafka/clients/producer/internals/SenderMetricsRegistry.java:123 — "produce-throttle-time-avg"
- pass 2: ✅ monitoring.md:832 throttle-time; SenderMetricsRegistry.java:123 / FetchMetricsRegistry.java:106 *-throttle-time-avg
