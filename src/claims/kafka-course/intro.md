# Claims ledger — kafka-course.html — intro

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0001 · intro · L1 · p
> Most Kafka tutorials teach you the API: create a producer, call send(), write a poll loop, done. That gets you to "it works on my laptop". It does not tell you why a consumer group freezes for thirty seconds, why a broker that lost its disk did not lose your data, or why acks=all is still not enough on its own. Those answers live one layer down, in the mechanisms. This guide is about that layer.
- pass 1: n/a (framing/pedagogy)
- pass 2: n/a — framing/pedagogy; mechanisms covered later

### C0002 · intro · L1 · p
> We will keep one picture in our heads the whole way through. Imagine a city's ledger archive. Every event in the city is written, in order, as a numbered line in a bound ledger book. Nobody ever erases or rewrites a line; they only add new lines at the end. The archive has several branches (buildings), each holding some of the books, and every important book has photocopies in other branches in case one burns down. A city council keeps the official minutes of which branch holds which book. Teams of clerks read the books and keep a bookmark of how far they got. That is Kafka: records, partitions, brokers, replicas, the KRaft controller quorum, consumer groups and committed offsets.
- pass 1: n/a (analogy)
- pass 2: n/a — analogy overview

### C0003 · intro · L1 · p
> The guide has levels. Chapters and blocks carry a chip — L1 essentials, L2 practitioner, L3 expert internals, L4 mastery (operations and application design). Choose your depth with the switch in the bottom-left corner; deeper material hides until you want it. The map below jumps anywhere and ticks off what you've read.
- pass 1: n/a (page navigation)
- pass 2: n/a — UI hint

### C0004 · intro · L1 · figcaption
> A map, not a simulation: each station is a chapter. Real systems overlap these steps (batching, pipelining, many partitions at once); the failover path is shown in one step here and is explained in chapters on replication, KRaft and leader epochs.
- pass 1: n/a (caption: simplifications)
- pass 2: n/a — figure caption, labelled as a map not a simulation

### C0005 · intro · L1 · p
> Facts are checked against Apache Kafka 4.3.1 (released 25 June 2026, the newest release at the time of writing) and Spring for Apache Kafka 4.1.1 (August 2026). The 4.x line changed a lot of "classic" Kafka knowledge, so older blog posts can mislead you:
- pass 1: ✅ kafka.apache.org/community/downloads — "4.3.1 Released June 25, 2026"; spring-kafka GitHub releases — "v4.1.1 published 2026-08-20"
- pass 2: ✅ gradle.properties:17 version=4.3.1; spring-kafka-4.1.1/gradle.properties:1 version=4.1.1; dates per downloads page (4.3.1 Jun 25 2026)

### C0006 · intro · L1 · tr
> 4.0 (Mar 2025) | ZooKeeper mode removed — KRaft only. New group coordinator and the next-generation consumer rebalance protocol (KIP-848) GA. Transactions server-side defense (KIP-890). Eligible Leader Replicas (KIP-966 part 1). Producer linger.ms default 0 → 5. Old protocol API versions removed (brokers and clients must be ≥ 2.1).
- pass 1: ✅ downloads — "4.0.0 Released March 18, 2025"; ../kafka-4.3.1-src/docs/getting-started/upgrade.md:219-224,284 — "ZooKeeper mode has been removed", "new group coordinator", "KIP-848 ... GA", "KIP-890", "KIP-966 Part 1", "default linger.ms changed from 0 to 5"; :218 "brokers are version 2.1 or higher"
- pass 2: ✅ docs/getting-started/upgrade.md:220-225,285 — "ZooKeeper mode has been removed"; "KIP-848 ... Generally Available"; "brand-new group coordinator"; "linger.ms changed from 0 to 5"; clients/brokers "2.1 or higher"

### C0007 · intro · L1 · tr
> 4.1 (Sep 2025) | Queues for Kafka (share groups, KIP-932) in preview. ELR enabled by default on new clusters. Streams rebalance protocol (KIP-1071) in early access.
- pass 1: ✅ downloads — "4.1.0 Released September 2, 2025"; ../kafka-4.3.1-src/docs/getting-started/upgrade.md 4.1.0 — "preview of Queues for Kafka (KIP-932)", "ELR ... enabled by default on the new clusters", "Early Access for the Streams rebalance protocol"
- pass 2: ✅ docs/getting-started/upgrade.md:167,173,181 — "ships with a preview of Queues for Kafka (KIP-932)"; "ELR will be enabled by default on the new clusters"; "Early Access for the Streams rebalance protocol"

### C0008 · intro · L1 · tr
> 4.2 (Feb 2026) | Share groups production-ready. Streams rebalance protocol production-ready for its core feature set. Dynamic KRaft controllers can auto-join the voter set (controller.quorum.auto.join.enable).
- pass 1: ✅ downloads — "4.2.0 Released February 17, 2026"; ../kafka-4.3.1-src/docs/getting-started/upgrade.md 4.2.0 — "Queues for Kafka ... production-ready", "Streams Rebalance Protocol ... production-ready for its core feature set", "controller.quorum.auto.join.enable ... automatically join the cluster's voter set"
- pass 2: ✅ docs/getting-started/upgrade.md:84,85,121 — "KIP-932 is production-ready in Apache Kafka 4.2"; KIP-1071 "production-ready for its core feature set"; controller.quorum.auto.join.enable (defaults to false)

### C0009 · intro · L1 · tr
> 4.3 (May 2026) | Cordoning of log directories (KIP-1066). Newly added followers on tiered-storage clusters can skip already-uploaded data (opt-in follower.fetch.last.tiered.offset.enable, default false). New share-group limits (share.delivery.count.limit and friends).
- pass 1: ✅ revised after pass-2 finding — C0009 intro 4.3 row → upgrade.md:55 "a newly added follower replica that has no local data" ; default false
- pass 2: ✅ docs/getting-started/upgrade.md:50,52,55 — "Support for cordoning log directories ... KIP-1066"; "`follower.fetch.last.tiered.offset.enable` (default: `false`)"; "`share.delivery.count.limit`, ..."

### C0010 · intro · L1 · p
> Always read the upgrade notes of your exact version.
- pass 1: n/a (advice)
- pass 2: n/a — advice

### C0011 · intro · L1 · p
> Kafka only ever appends to files. It never updates a record in place and, by default, does not even force data to disk on each write. So how can it be faster than a database, survive a broker crash without losing writes it acknowledged, and offer exactly-once processing? Keep the question in mind — the finale answers it.
- pass 1: ✅ design.md:64,335 — "All data is immediately written to a persistent log ... not necessarily flushed to disk"; ServerLogConfigs.java:101 LOG_FLUSH_INTERVAL_MESSAGES_DEFAULT = Long.MAX_VALUE
- pass 2: ✅ docs/design/design.md:335 — "we do not want to require the use of fsync on every write"; append-only log per docs/implementation/log.md:39
