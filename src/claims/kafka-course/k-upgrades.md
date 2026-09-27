# Claims ledger — kafka-course.html — k-upgrades

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0922 · k-upgrades · L4 · p
> Upgrading a Kafka cluster changes two separate things, and most upgrade mistakes come from mixing them up. The first is the software version: the jars on each node. The second is the feature levels the whole cluster has agreed to use: metadata.version and a handful of feature flags, stored as records in the KRaft metadata log.
- pass 1: ✅ docs/getting-started/upgrade.md:38 — "Once the cluster's behavior and performance have been verified, finalize the upgrade by running `bin/kafka-features.sh"
- pass 2: ✅ docs/getting-started/upgrade.md:37-39 — software roll, then "finalize the upgrade by running bin/kafka-features.sh ... upgrade --release-version 4.3"; feature levels in metadata log (FeatureControlManager.java)

### C0923 · k-upgrades · L4 · p
> In the archive, the software is each branch's clerks and furniture. The feature level is the edition of the city rulebook that the council has formally adopted. You retrain the clerks branch by branch, and the old rulebook stays in force while you do. Only when every branch can handle the new edition does the council vote it in. After that vote, some new rules write records the old clerks can't read, so there's no going back.
- pass 1: n/a (pedagogy / analogy)
- pass 2: n/a (analogy)

### C0924 · k-upgrades · L4 · h3
> The rolling upgrade, per the 4.3 docs
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0925 · k-upgrades · L4 · li
> Check the preconditions. 4.x is KRaft-only. Brokers must already run KRaft with software and metadata at least 3.3.x. KRaft clusters older than 3.3.x should go to 3.9.x first. ZooKeeper clusters must migrate to KRaft using a bridge release, and "the last bridge release is Kafka 3.9".
- pass 1: ✅ docs/getting-started/upgrade.md:33 — "broker upgrades to 4.3.0 (and higher) require KRaft mode" ; docs/operations/kraft.md:296 — "The last bridge release is Kafka 3.9"
- pass 2: ✅ docs/getting-started/upgrade.md:33 — "require KRaft mode and the software and metadata versions must be at least 3.3.x"; "upgrading to 3.9.x"; docs/operations/kraft.md:296 "The last bridge release is Kafka 3.9."

### C0926 · k-upgrades · L4 · li
> Roll the binaries: stop one broker, update the code, restart it, and repeat. The cluster still speaks the old metadata.version, so a mixed-version cluster is fine.
- pass 1: ✅ docs/getting-started/upgrade.md:37 — "Upgrade the brokers one at a time: shut down the broker, update the code, and restart it" (old MV in effect until finalize: C0929 source)
- pass 2: ✅ docs/getting-started/upgrade.md:37 — "Upgrade the brokers one at a time: shut down the broker, update the code, and restart it."

### C0927 · k-upgrades · L4 · li
> Verify behavior and performance at your leisure. This is your cheap rollback window: just reinstall the old binaries.
- pass 1: ✅ docs/getting-started/upgrade.md:38 — "Once the cluster's behavior and performance have been verified" ("cheap rollback" = inference: MV not yet changed)
- pass 2: ✅ docs/getting-started/upgrade.md:37-38 — verify "behavior and performance" before finalizing; metadata.version unchanged until finalize, so old binaries still compatible

### C0928 · k-upgrades · L4 · li
> Finalize: bin/kafka-features.sh --bootstrap-server localhost:9092 upgrade --release-version 4.3
- pass 1: ✅ docs/getting-started/upgrade.md:38 — "upgrade --release-version 4.3`"
- pass 2: ✅ docs/getting-started/upgrade.md:38 — "bin/kafka-features.sh --bootstrap-server localhost:9092 upgrade --release-version 4.3"

### C0929 · k-upgrades · L4 · p
> Finalizing is where the one-way doors are. Every MetadataVersion carries a boolean that says whether it changes the metadata format. IBP_4_3_IV0(30, "4.3", "IV0", true) does, so after finalizing 4.3 you cannot downgrade the metadata. IBP_4_2_IV1(29, "4.2", "IV1", false) doesn't, which is why the 4.2 docs say downgrade is supported. A downgrade is possible only if no version between current and target has metadata changes.
- pass 1: ✅ docs/getting-started/upgrade.md:39 — "IBP_4_3_IV0(30, "4.3", "IV0", true)" ; docs/getting-started/upgrade.md:76 — "IBP_4_2_IV1(29, "4.2", "IV1", false)" ; docs/getting-started/upgrade.md:39 — "a downgrade is only possible if there are no metadata changes in the versions between"
- pass 2: ✅ docs/getting-started/upgrade.md:39 — "IBP_4_3_IV0(30, "4.3", "IV0", true) means this version has metadata changes"; :76 IBP_4_2_IV1(29, ..., false) "downgrade is supported"; "only possible if there are no metadata changes in the versions between"

### C0930 · k-upgrades · L4 · li
> kraft.version = 1
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/KRaftVersion.java:26 — "KRAFT_VERSION_1(1, MetadataVersion.IBP_3_9_IV0)" (starting state = defaults for a cluster at 4.2)
- pass 2: ✅ KRaftVersion.java:26,30 — KRAFT_VERSION_1(1, IBP_3_9_IV0), LATEST_PRODUCTION

### C0931 · k-upgrades · L4 · li
> group.version = 1
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/GroupVersion.java:27 — "GV_1(1, MetadataVersion.IBP_4_0_IV0"
- pass 2: ✅ GroupVersion.java:27,31 — GV_1(1, IBP_4_0_IV0), LATEST_PRODUCTION = GV_1

### C0932 · k-upgrades · L4 · li
> transaction.version = 2
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/TransactionVersion.java:31 — "TV_2(2, MetadataVersion.IBP_4_0_IV2"
- pass 2: ✅ TransactionVersion.java:31,35 — TV_2(2, IBP_4_0_IV2), LATEST_PRODUCTION = TV_2

### C0933 · k-upgrades · L4 · li
> eligible.leader.replicas.version = 1
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/EligibleLeaderReplicasVersion.java:27 — "ELRV_1(1, MetadataVersion.IBP_4_1_IV0"
- pass 2: ✅ EligibleLeaderReplicasVersion.java:27,31 — ELRV_1, LATEST_PRODUCTION

### C0934 · k-upgrades · L4 · li
> share.version = 1
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/ShareVersion.java:28 — "SV_1(1, MetadataVersion.IBP_4_2_IV0"
- pass 2: ✅ ShareVersion.java:28,32 — SV_1(1, IBP_4_2_IV0), LATEST_PRODUCTION

### C0935 · k-upgrades · L4 · li
> streams.version = 1
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/StreamsVersion.java:27 — "SV_1(1, MetadataVersion.IBP_4_2_IV1"
- pass 2: ✅ StreamsVersion.java:27,32 — SV_1(1, IBP_4_2_IV1), LATEST_PRODUCTION

### C0936 · k-upgrades · L4 · li
> metadata.version 4.3-IV0 (level 30) has metadata changes, so no downgrade after finalizing.
- pass 1: ✅ docs/getting-started/upgrade.md:39 — "IBP_4_3_IV0(30, "4.3", "IV0", true)"
- pass 2: ✅ MetadataVersion.java:125 — IBP_4_3_IV0(30, "4.3", "IV0", true); docs/getting-started/upgrade.md:39 "downgrade is not supported in this version"

### C0937 · k-upgrades · L4 · li
> group.coordinator.rebalance.protocols deprecated; share.delivery.count.limit as a group config.
- pass 1: ✅ docs/getting-started/upgrade.md:51 — "The `group.coordinator.rebalance.protocols` configuration is deprecated" ; docs/getting-started/upgrade.md:52 — "New group configs have been introduced: `share.delivery.count.limit`"
- pass 2: ✅ docs/getting-started/upgrade.md:51 — "group.coordinator.rebalance.protocols configuration is deprecated"; :52 "New group configs ... share.delivery.count.limit"

### C0938 · k-upgrades · L4 · li
> group.*.assignment.interval.ms (default 1 s); follower.fetch.last.tiered.offset.enable; ListOffsets v11.
- pass 1: ✅ docs/getting-started/upgrade.md:54 — "These default to an interval of 1 second" ; docs/getting-started/upgrade.md:55 — "follower.fetch.last.tiered.offset.enable" ; docs/getting-started/upgrade.md:56 — "extended to version 11"
- pass 2: ✅ docs/getting-started/upgrade.md:54 — assignment.interval.ms configs "default to an interval of 1 second"; :55 follower.fetch.last.tiered.offset.enable; :56 "ListOffsets API has been extended to version 11"

### C0939 · k-upgrades · L4 · li
> Cordoning log directories (KIP-1066); kafka-streams-scala deprecated.
- pass 1: ✅ docs/getting-started/upgrade.md:50 — "Support for cordoning log directories" ; docs/getting-started/upgrade.md:48 — "The `kafka-streams-scala` library is deprecated as of Kafka 4.3"
- pass 2: ✅ docs/getting-started/upgrade.md:50 — "Support for cordoning log directories ... KIP-1066"; :48 "kafka-streams-scala library is deprecated as of Kafka 4.3"

### C0940 · k-upgrades · L4 · figcaption
> Feature levels come from MetadataVersion and the *Version enums in server-common (4.3.1). The refusal text is quoted from FeatureControlManager. Controllers are left out; in a real cluster you roll them too. 5.0 is not released; its card lists only removals announced in the 4.x upgrade notes.
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:411 — "Refusing to perform the requested downgrade" ; docs/getting-started/upgrade.md:48 — "will be removed in Kafka 5.0" (simplification stated)
- pass 2: ✅ server-common/src/main/java/org/apache/kafka/server/common/*Version.java; FeatureControlManager.java:406,411 refusal texts; 5.0 items all from upgrade.md deprecation notes

### C0941 · k-upgrades · L4 · h3
> Feature flags: one switch per protocol
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0942 · k-upgrades · L4 · p
> Since KIP-584, metadata.version isn't the only versioned feature. Each big protocol change has its own flag, so it can be enabled, and in most cases downgraded, independently. --release-version X sets all features to their defaults for that release. The tool's own help says "3.9-IV0 will set metadata.version=21 and kraft.version=1". --feature name=level changes just one.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/Feature.java:30 — "KIP-584" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:154 — "The release version to update all features to" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:154 — "3.9-IV0 will set metadata.version=21 and kraft.version=1" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:158 — "A feature upgrade we should perform, in feature=level format"
- pass 2: ✅ FeatureCommand.java:154 — "3.9-IV0 will set metadata.version=21 and kraft.version=1"; :153,157 --release-version / --feature

### C0943 · k-upgrades · L4 · tr
> metadata.version | 3.3-IV3 (min) … 4.3-IV0 = level 30 (latest production) | n/a | Gates metadata record formats; one-way when "metadata changes" = true.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/MetadataVersion.java:144 — "MINIMUM_VERSION = IBP_3_3_IV3" ; server-common/src/main/java/org/apache/kafka/server/common/MetadataVersion.java:154 — "LATEST_PRODUCTION = IBP_4_3_IV0" ; server-common/src/main/java/org/apache/kafka/server/common/MetadataVersion.java:125 — "IBP_4_3_IV0(30"
- pass 2: ✅ MetadataVersion.java:50,125,144,154 — MINIMUM_VERSION = IBP_3_3_IV3; LATEST_PRODUCTION = IBP_4_3_IV0 (level 30, metadata changes true)

### C0944 · k-upgrades · L4 · tr
> kraft.version | 1 → KIP-853 dynamic controller quorum | 3.9-IV0 | Static→dynamic upgrade of existing quorums is supported since 4.1; afterwards, replace controller.quorum.voters with controller.quorum.bootstrap.servers.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/KRaftVersion.java:25 — "// Version 1 enables KIP-853." ; docs/operations/kraft.md:69 — "Apache Kafka 4.1 added support for upgrading a cluster from a static controller configuration" ; docs/operations/kraft.md:102 — "remove the `controller.quorum.voters` property and add the `controller.quorum.bootstrap.servers`"
- pass 2: ✅ KRaftVersion.java:26 KRAFT_VERSION_1(1, IBP_3_9_IV0); docs/operations/kraft.md:69 "Apache Kafka 4.1 added support for upgrading a cluster from a static controller configuration to a dynamic"; :102 remove controller.quorum.voters, add controller.quorum.bootstrap.servers (note kraft.md:75 loosely says "release-version 4.1")

### C0945 · k-upgrades · L4 · tr
> group.version | 1 → KIP-848 consumer rebalance protocol | 4.0-IV0 | Once groups use it, cluster downgrade only to ≥ 3.4.1.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/GroupVersion.java:26 — "// Version 1 enables the consumer rebalance protocol (KIP-848)." ; docs/getting-started/upgrade.md:223 — "can only be downgraded to version 3.4.1 or newer"
- pass 2: ✅ GroupVersion.java:27 GV_1 at IBP_4_0_IV0; docs/getting-started/upgrade.md:223 — "once the new protocol is used by consumer groups, the cluster can only be downgraded to version 3.4.1 or newer"

### C0946 · k-upgrades · L4 · tr
> transaction.version | 1 → flexible transactional state records; 2 → epoch bump per transaction (KIP-890) | 4.0-IV2 | Downgrades are safe; producers switch on their next transaction.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/TransactionVersion.java:28 — "// Version 1 enables flexible transactional state records. (KIP-890)" ; server-common/src/main/java/org/apache/kafka/server/common/TransactionVersion.java:30 — "// Version 2 enables epoch bump per transaction and optimizations. (KIP-890)" ; docs/operations/transaction-protocol.md:39 — "Downgrades are safe to perform"
- pass 2: ✅ TransactionVersion.java:28-31 — "flexible transactional state records", "epoch bump per transaction" (KIP-890); docs/operations/transaction-protocol.md:39 "Downgrades are safe ... on the first transaction after"

### C0947 · k-upgrades · L4 · tr
> eligible.leader.replicas.version | 1 → ELR (KIP-966) | 4.1-IV0 (depends on MV ≥ 4.0-IV1) | Enabling moves min.insync.replicas to cluster level; downgrade to 0 is safe.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/EligibleLeaderReplicasVersion.java:27 — "IBP_4_0_IV1.featureLevel()" ; docs/operations/eligible-leader-replicas.md:54 — "The previously set `min.insync.replicas` value at the broker-level config will be removed" ; docs/operations/eligible-leader-replicas.md:43 — "eligible.leader.replicas.version=0"
- pass 2: ✅ EligibleLeaderReplicasVersion.java:27 — ELRV_1(1, IBP_4_1_IV0, depends on IBP_4_0_IV1); docs/operations/eligible-leader-replicas.md:43 "Downgrades are safe ... eligible.leader.replicas.version=0"; :51-54 cluster-level min.insync.replicas

### C0948 · k-upgrades · L4 · tr
> share.version | 1 → share groups (KIP-932) | 4.2-IV0 | In 4.1 (preview) you had to enable it explicitly.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/ShareVersion.java:28 — "SV_1(1, MetadataVersion.IBP_4_2_IV0" ; docs/getting-started/upgrade.md:167 — "upgrade to `share.version=1`"
- pass 2: ✅ ShareVersion.java:26-28 — "This was a preview in 4.1 which required enabling explicitly." SV_1 at IBP_4_2_IV0

### C0949 · k-upgrades · L4 · tr
> streams.version | 1 → Streams rebalance protocol (KIP-1071) | 4.2-IV1 | upgrade --feature streams.version=1 / downgrade --feature streams.version=0.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/StreamsVersion.java:27 — "SV_1(1, MetadataVersion.IBP_4_2_IV1" ; docs/streams/developer-guide/streams-rebalance-protocol.md:88 — "upgrade --feature streams.version=1" ; docs/streams/developer-guide/streams-rebalance-protocol.md:93 — "downgrade --feature streams.version=0"
- pass 2: ✅ StreamsVersion.java:27 SV_1 at IBP_4_2_IV1; docs/streams/developer-guide/streams-rebalance-protocol.md:88,93 "upgrade --feature streams.version=1" / "downgrade --feature streams.version=0"

### C0950 · k-upgrades · L4 · p
> On a local 4.3 cluster: (a) list the finalized features, (b) see what --release-version 4.1 would map to without touching anything, (c) rehearse an upgrade with no side effects.
- pass 1: n/a (exercise prompt / pedagogy)
- pass 2: n/a (exercise prompt)

### C0951 · k-upgrades · L4 · pre
> bin/kafka-features.sh --bootstrap-server localhost:9092 describe bin/kafka-features.sh --bootstrap-server localhost:9092 version-mapping --release-version 4.1 bin/kafka-features.sh --bootstrap-server localhost:9092 upgrade --release-version 4.3 --dry-run
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:139 — "subparsers.addParser("describe")" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:202 — "subparsers.addParser("version-mapping")" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:160 — "upgradeParser.addArgument("--dry-run")"
- pass 2: ✅ FeatureCommand.java:139,148,160,202,207 — describe, upgrade --release-version --dry-run, version-mapping --release-version

### C0952 · k-upgrades · L4 · p
> describe prints each feature's SupportedMinVersion, SupportedMaxVersion and FinalizedVersionLevel. Use --bootstrap-controller localhost:9093 to ask the controllers directly, and --node-id (4.2+) to pick a node. feature-dependencies --feature eligible.leader.replicas.version=1 shows that ELR needs a sufficiently new metadata.version. For brand-new clusters, kafka-storage.sh format accepts --release-version and --feature too.
- pass 1: ✅ docs/operations/kraft.md:80 — "SupportedMinVersion: 0  SupportedMaxVersion: 1  FinalizedVersionLevel: 0" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:83 — "addArgument("--bootstrap-controller")" ; docs/getting-started/upgrade.md:136 — "Added an optional `--node-id` flag" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:214 — "subparsers.addParser("feature-dependencies")" ; core/src/main/scala/kafka/tools/StorageTool.scala:333 — "formatParser.addArgument("--release-version"" ; core/src/main/scala/kafka/tools/StorageTool.scala:338 — "formatParser.addArgument("--feature""
- pass 2: ✅ FeatureCommand.java:251 "SupportedMinVersion ... SupportedMaxVersion ... FinalizedVersionLevel"; :83 --bootstrap-controller; :141 --node-id (docs/getting-started/upgrade.md:136 added in 4.2); :214-219 feature-dependencies --feature; StorageTool.scala:333,338 format --release-version / --feature

### C0953 · k-upgrades · L4 · p
> 4.0 removed old protocol API versions (KIP-896). Brokers and Java clients (including Streams and Connect) must both be at least 2.1 before either side goes to 4.0, and non-Apache clients need checking separately. Clients and Streams need Java 11+, and brokers, Connect and tools need Java 17+. Upgrading the cluster can break an ancient client that nobody remembers owning.
- pass 1: ✅ docs/getting-started/upgrade.md:220 — "Old protocol API versions have been removed" ; docs/getting-started/upgrade.md:305 — "brokers, connect and tools now require Java 17"
- pass 2: ✅ docs/getting-started/upgrade.md:220 — "ensure brokers are version 2.1 or higher before upgrading Java clients ... Similarly ... clients ... 2.1 or higher before upgrading brokers to 4.0" (KIP-896); :305 clients/Streams Java 11, brokers/connect/tools Java 17

### C0954 · k-upgrades · L4 · p
> Why can't you just "undo" 4.3's metadata? Because after finalizing, the controller may write records in a format that older code can't parse, and a downgrade would have to drop them. Ask for it anyway and FeatureControlManager answers "Refusing to perform the requested downgrade because it might delete metadata information." Even with downgrade --unsafe, the answer in 4.3.1 is "Unsafe metadata downgrade is not supported in this version." A code comment points to KAFKA-13896 for a future implementation. The practical rule: finalize only after you're sure you won't need the old binaries.
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:411 — "Refusing to perform the requested downgrade because it might delete metadata information" ; metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:406 — "Unsafe metadata downgrade is not supported in this version" ; metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:409 — "KAFKA-13896"
- pass 2: ✅ FeatureControlManager.java:404-411 — "Unsafe metadata downgrade is not supported in this version." / "Refusing to perform the requested downgrade because it might delete metadata information." / "(KAFKA-13896)"; FeatureCommand.java:179 --unsafe

### C0955 · k-upgrades · L4 · h3
> What 5.0 will take away
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0956 · k-upgrades · L4 · p
> 5.0 isn't out, but the 4.x notes already name what it removes. Clean these up now: group.coordinator.rebalance.protocols (in 5.0 all protocols are always on, and group.version/streams.version/share.version alone control them), the kafka-streams-scala library, the CLI options deprecated by KIP-1147 (--producer-props, --consumer.config, --property…, replaced by --command-property, --command-config, --formatter-property, --reader-property), --max-partition-memory-bytes in the console producer, MX4J support, the misspelled PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG constant, the tag-less app-info metric names, and PrincipalConnectorClientConfigOverridePolicy (the new allowlist policy becomes the default).
- pass 1: ✅ docs/getting-started/upgrade.md:51 — "In Kafka 5.0, all protocols will always be enabled" ; docs/getting-started/upgrade.md:48 — "The `kafka-streams-scala` library is deprecated" ; docs/getting-started/upgrade.md:94 — "The deprecated options will be removed in Kafka 5.0" ; docs/getting-started/upgrade.md:83 — "--max-partition-memory-bytes" ; docs/getting-started/upgrade.md:91 — "MX4J library" ; docs/getting-started/upgrade.md:92 — "PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG" ; docs/getting-started/upgrade.md:122 — "The AppInfo metrics will deprecate the following metric names" ; docs/getting-started/upgrade.md:134 — "From Kafka 5.0.0, this will be the default"
- pass 2: ✅ docs/getting-started/upgrade.md:51 (rebalance.protocols; "controlled solely by feature versions"), :48 kafka-streams-scala, :83 --max-partition-memory-bytes, :91 MX4J, :92 PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG, KIP-1147 list :94-112 (--producer-props, --consumer.config, --property → --command-property/--command-config/--formatter-property/--reader-property), app-info tag-less names, :134 PrincipalConnectorClientConfigOverridePolicy / allowlist default in 5.0 — all "will be removed in Kafka 5.0"

### C0957 · k-upgrades · L4 · summary
> L4🔬 Go deeper: how a feature's default is chosen
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C0958 · k-upgrades · L4 · p
> Every feature level is an enum constant with a bootstrap metadata version, for example GV_1(1, MetadataVersion.IBP_4_0_IV0, Map.of()), and optional dependencies, like ELRV_1(1, IBP_4_1_IV0, Map.of("metadata.version", IBP_4_0_IV1.featureLevel())). Feature.defaultVersion(mv) walks the levels and picks the highest one whose bootstrap version is ≤ mv. That's what --release-version and kafka-storage format use, and what version-mapping prints. Feature.validateVersion rejects levels whose dependencies aren't met. The 4.3.1 source already defines IBP_4_4_IV0(31, …), but LATEST_PRODUCTION is IBP_4_3_IV0. Versions past that are for testing only, and MINIMUM_VERSION is IBP_3_3_IV3. The broker exposes the active level as kafka.server:type=MetadataLoader,name=CurrentMetadataVersion.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/GroupVersion.java:27 — "GV_1(1, MetadataVersion.IBP_4_0_IV0, Map.of())" ; server-common/src/main/java/org/apache/kafka/server/common/EligibleLeaderReplicasVersion.java:27 — "ELRV_1(1, MetadataVersion.IBP_4_1_IV0" ; server-common/src/main/java/org/apache/kafka/server/common/Feature.java:190 — "public FeatureVersion defaultVersion" ; tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:314 — "feature.defaultLevel(" ; core/src/main/scala/kafka/tools/StorageTool.scala:197 — "feature.defaultLevel(metadataVersion)" ; server-common/src/main/java/org/apache/kafka/server/common/Feature.java:163 — "public static void validateVersion" ; server-common/src/main/java/org/apache/kafka/server/common/MetadataVersion.java:133 — "IBP_4_4_IV0(31" ; server-common/src/main/java/org/apache/kafka/server/common/MetadataVersion.java:154 — "LATEST_PRODUCTION = IBP_4_3_IV0" ; docs/operations/monitoring.md:2104 — "name=CurrentMetadataVersion"
- pass 2: ✅ GroupVersion.java:27 "GV_1(1, MetadataVersion.IBP_4_0_IV0, Map.of())"; EligibleLeaderReplicasVersion.java:27 (dependency key is MetadataVersion.FEATURE_NAME = metadata.version); Feature.java:163-199 validateVersion/defaultVersion; MetadataVersion.java:127-133 IBP_4_4_IV0(31) "unstable", :144,154; monitoring.md:2104 kafka.server:type=MetadataLoader,name=CurrentMetadataVersion

### C0959 · k-upgrades · L4 · li
> Two steps: roll binaries (reversible), then kafka-features.sh upgrade --release-version X (maybe not reversible).
- pass 1: ✅ docs/getting-started/upgrade.md:39 — "Note that cluster metadata downgrade is not supported in this version since it has metadata changes"
- pass 2: ✅ docs/getting-started/upgrade.md:37-39 — roll code, then kafka-features.sh upgrade --release-version; downgrade only without metadata changes

### C0960 · k-upgrades · L4 · li
> 4.x needs KRaft and ≥ 3.3.x; ZooKeeper clusters migrate via the 3.9 bridge first.
- pass 1: ✅ docs/getting-started/upgrade.md:33 — "broker upgrades to 4.3.0 (and higher) require KRaft mode" ; docs/operations/kraft.md:296 — "The last bridge release is Kafka 3.9"
- pass 2: ✅ docs/getting-started/upgrade.md:33; docs/operations/kraft.md:296 — "The last bridge release is Kafka 3.9."

### C0961 · k-upgrades · L4 · li
> Feature flags: group.version (KIP-848), transaction.version (KIP-890), eligible.leader.replicas.version (ELR), share.version, streams.version, kraft.version.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/Feature.java:50 — "STREAMS_VERSION(StreamsVersion.FEATURE_NAME"
- pass 2: ✅ server-common/.../common: GroupVersion, TransactionVersion, EligibleLeaderReplicasVersion, ShareVersion, StreamsVersion, KRaftVersion FEATURE_NAME constants

### C0962 · k-upgrades · L4 · li
> 4.3-IV0 has metadata changes: no metadata downgrade, and unsafe downgrade isn't supported.
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:406 — "Unsafe metadata downgrade is not supported in this version"
- pass 2: ✅ MetadataVersion.java:125; FeatureControlManager.java:406 — "Unsafe metadata downgrade is not supported in this version."

### C0963 · k-upgrades · L4 · li
> Start removing 5.0-deprecated configs, CLI flags and metric names today.
- pass 1: n/a (advice)
- pass 2: n/a (advice)
