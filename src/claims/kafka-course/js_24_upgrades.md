# Claims ledger — kafka-course.html — js_24_upgrades

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1600 · js:24. upgrades · L- · script
> The last ZooKeeper→KRaft bridge release: ZooKeeper clusters must migrate here before 4.x.
- pass 1: ✅ docs/operations/kraft.md:296 — "The last bridge release is Kafka 3.9"
- pass 2: ✅ docs/operations/kraft.md:296 — "In order to migrate from ZooKeeper to KRaft you need to use a bridge release. The last bridge release is Kafka 3.9."

### C1601 · js:24. upgrades · L- · script
> KIP-848 consumer protocol GA (group.version=1); new group coordinator.
- pass 1: ✅ docs/getting-started/upgrade.md:223 — "is now Generally Available (GA) in Apache Kafka 4.0" ; docs/getting-started/upgrade.md:222 — "brand-new group coordinator implementation"
- pass 2: ✅ docs/getting-started/upgrade.md:223 — "KIP-848 ... is now Generally Available (GA) in Apache Kafka 4.0"; GroupVersion.java:27 GV_1 at IBP_4_0_IV0

### C1602 · js:24. upgrades · L- · script
> ELR introduced (not on by default). MirrorMaker 1 removed.
- pass 1: ✅ docs/operations/eligible-leader-replicas.md:41 — "The ELR is not enabled by default for 4.0" ; docs/getting-started/upgrade.md:252 — "The original MirrorMaker (MM1) and related classes were removed"
- pass 2: ✅ docs/operations/eligible-leader-replicas.md:31 — "Starting from Apache Kafka 4.0 ... (ELR is enabled by default on new clusters starting 4.1)"; docs/getting-started/upgrade.md:252 MM1 removed

### C1603 · js:24. upgrades · L- · script
> linger.ms default 0 → 5; old protocol versions removed (clients/brokers ≥ 2.1).
- pass 1: ✅ docs/getting-started/upgrade.md:285 — "The default `linger.ms` changed from 0 to 5" ; docs/getting-started/upgrade.md:220 — "Old protocol API versions have been removed"
- pass 2: ✅ docs/getting-started/upgrade.md:285 — "The default linger.ms changed from 0 to 5 in Apache Kafka 4.0"; :220 brokers/clients "2.1 or higher"

### C1604 · js:24. upgrades · L- · script
> Queues for Kafka preview (enable with share.version=1).
- pass 1: ✅ docs/getting-started/upgrade.md:167 — "upgrade to `share.version=1`"
- pass 2: ✅ docs/getting-started/upgrade.md:167 — "Apache Kafka 4.1 ships with a preview of Queues for Kafka ... upgrade to share.version=1"

### C1605 · js:24. upgrades · L- · script
> ELR on by default for new clusters.
- pass 1: ✅ docs/getting-started/upgrade.md:173 — "will be enabled by default on the new clusters"
- pass 2: ✅ docs/getting-started/upgrade.md:173 — "ELR will be enabled by default on the new clusters" (4.1.0 notes)

### C1606 · js:24. upgrades · L- · script
> Streams rebalance protocol early access; static→dynamic controller quorum upgrade.
- pass 1: ✅ docs/getting-started/upgrade.md:181 — "Early Access for the Streams rebalance protocol" ; docs/operations/kraft.md:69 — "Apache Kafka 4.1 added support for upgrading a cluster from a static controller configuration"
- pass 2: ✅ docs/getting-started/upgrade.md:181 — "Early Access for the Streams rebalance protocol" (4.1); docs/operations/kraft.md:69 "Apache Kafka 4.1 added support for upgrading ... static ... to ... dynamic"

### C1607 · js:24. upgrades · L- · script
> flush() inside a callback is now prohibited; log.cleaner.enable deprecated.
- pass 1: ✅ docs/getting-started/upgrade.md:175 — "prohibits its use inside a callback" ; docs/getting-started/upgrade.md:172 — "The configuration `log.cleaner.enable` is deprecated"
- pass 2: ✅ docs/getting-started/upgrade.md:175 "The flush method now detects potential deadlocks and prohibits its use inside a callback"; :172 "log.cleaner.enable is deprecated"

### C1608 · js:24. upgrades · L- · script
> Streams rebalance protocol (KIP-1071) core GA. Avoid classic→streams migration on 4.2.0; fixed in 4.2.1.
- pass 1: ✅ docs/getting-started/upgrade.md:85 — "is now production-ready for its core feature set" ; docs/getting-started/upgrade.md:85 — "we recommend against doing migrations from classic to streams groups in 4.2.0" ; docs/getting-started/upgrade.md:64 — "Migrations from classic to streams groups are now safe starting with 4.2.1"
- pass 2: ✅ docs/getting-started/upgrade.md:85 — KIP-1071 "production-ready for its core feature set"; "we recommend against doing migrations from classic to streams groups in 4.2.0"; :64 fix in 4.2.1

### C1609 · js:24. upgrades · L- · script
> KIP-1147 CLI option renames (--command-property, --command-config…).
- pass 1: ✅ docs/getting-started/upgrade.md:99 — "The option `--command-property` is used for all command-line tools"
- pass 2: ✅ docs/getting-started/upgrade.md:94-112 — KIP-1147 "--command-property", "--command-config" (4.2.0 notes)

### C1610 · js:24. upgrades · L- · script
> metadata.version 4.3-IV0 (level 30) has metadata changes, so no downgrade after finalizing.
- pass 1: ✅ docs/getting-started/upgrade.md:39 — "IBP_4_3_IV0(30, "4.3", "IV0", true)"
- pass 2: ✅ MetadataVersion.java:125 — IBP_4_3_IV0(30, "4.3", "IV0", true); docs/getting-started/upgrade.md:39 "downgrade is not supported in this version"

### C1611 · js:24. upgrades · L- · script
> Cordoning log directories (KIP-1066); kafka-streams-scala deprecated.
- pass 1: ✅ docs/getting-started/upgrade.md:50 — "Support for cordoning log directories" ; docs/getting-started/upgrade.md:48 — "The `kafka-streams-scala` library is deprecated as of Kafka 4.3"
- pass 2: ✅ docs/getting-started/upgrade.md:50 — "Support for cordoning log directories ... KIP-1066"; :48 "kafka-streams-scala library is deprecated as of Kafka 4.3"

### C1612 · js:24. upgrades · L- · script
> Removes group.coordinator.rebalance.protocols: protocols controlled only by feature versions.
- pass 1: ✅ docs/getting-started/upgrade.md:51 — "In Kafka 5.0, all protocols will always be enabled"
- pass 2: ✅ docs/getting-started/upgrade.md:51 — "In Kafka 5.0, all protocols will always be enabled and controlled solely by feature versions"

### C1613 · js:24. upgrades · L- · script
> Removes kafka-streams-scala, the KIP-1147 deprecated CLI options, MX4J support, --max-partition-memory-bytes.
- pass 1: ✅ docs/getting-started/upgrade.md:48 — "will be removed in Kafka 5.0" ; docs/getting-started/upgrade.md:94 — "The deprecated options will be removed in Kafka 5.0" ; docs/getting-started/upgrade.md:91 — "MX4J library" ; docs/getting-started/upgrade.md:83 — "--max-partition-memory-bytes"
- pass 2: ✅ docs/getting-started/upgrade.md:48, :94 (KIP-1147 "will be removed in Kafka 5.0"), :91 MX4J, :83 --max-partition-memory-bytes — all "removed in Kafka 5.0"

### C1614 · js:24. upgrades · L- · script
> Allowlist connector override policy becomes the default; PrincipalConnectorClientConfigOverridePolicy removed.
- pass 1: ✅ docs/getting-started/upgrade.md:134 — "From Kafka 5.0.0, this will be the default" ; docs/getting-started/upgrade.md:134 — "is now deprecated and will be removed in Kafka 5.0.0"
- pass 2: ✅ docs/getting-started/upgrade.md:134 — "From Kafka 5.0.0, this will be the default ... PrincipalConnectorClientConfigOverridePolicy ... will be removed in Kafka 5.0.0"

### C1615 · js:24. upgrades · L- · script
> A three-broker cluster on 4.2 software, metadata.version=4.2-IV1. Press ▶.
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: ✅ MetadataVersion.java:121 IBP_4_2_IV1 exists (4.2 latest); scenario illustrative

### C1616 · js:24. upgrades · L- · script
> Rolling: stop broker …, install 4.3, restart. The others keep serving; leadership moves away and back.
- pass 1: ✅ docs/operations/basic-kafka-operations.md:95 — "It will migrate any partitions the server is the leader for to other replicas prior to shutting down" ; docs/operations/basic-kafka-operations.md:108 — "By default the Kafka cluster will try to restore leadership to the preferred replicas"
- pass 2: ✅ docs/getting-started/upgrade.md:37 — "Upgrade the brokers one at a time: shut down the broker, update the code, and restart it."

### C1617 · js:24. upgrades · L- · script
> Finalized. metadata.version is now 4.3-IV0. The other feature flags already had their 4.3 defaults (none of them has a 4.3-specific level), so only the metadata level moved.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/StreamsVersion.java:27 — "SV_1(1, MetadataVersion.IBP_4_2_IV1" ; server-common/src/main/java/org/apache/kafka/server/common/Feature.java:190 — "public FeatureVersion defaultVersion" (no *Version enum has an IBP_4_3 bootstrap level)
- pass 2: ✅ all *Version.java bootstrap levels ≤ IBP_4_2_IV1 (StreamsVersion.java:27 highest), so Feature.defaultVersion (Feature.java:190-199) gives same levels for 4.3

### C1618 · js:24. upgrades · L- · script
> Upgraded and finalized. Now someone wants to go back to 4.2…
- pass 1: n/a (labelled illustrative figure narration)
- pass 2: n/a (narration)

### C1619 · js:24. upgrades · L- · script
> Refusing to perform the requested downgrade because it might delete metadata information.
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:411 — "Refusing to perform the requested downgrade because it might delete metadata information"
- pass 2: ✅ FeatureControlManager.java:411 — "Refusing to perform the requested downgrade because it might delete metadata information."

### C1620 · js:24. upgrades · L- · script
> Unsafe metadata downgrade is not supported in this version.
- pass 1: ✅ metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:406 — "Unsafe metadata downgrade is not supported in this version"
- pass 2: ✅ FeatureControlManager.java:406 — "Unsafe metadata downgrade is not supported in this version."

### C1621 · js:24. upgrades · L- · script
> One-way door. 4.3-IV0 has metadata changes, so the controller refuses a safe downgrade, and unsafe downgrade isn’t implemented in 4.3.1. Finalize only after you’re sure.
- pass 1: ✅ docs/getting-started/upgrade.md:39 — "IBP_4_3_IV0(30, "4.3", "IV0", true)" ; metadata/src/main/java/org/apache/kafka/controller/FeatureControlManager.java:406 — "Unsafe metadata downgrade is not supported in this version"
- pass 2: ✅ MetadataVersion.java:125; FeatureControlManager.java:399-411 (checkIfMetadataChanged → refuse; unsafe unsupported)
