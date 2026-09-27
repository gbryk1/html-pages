# Claims ledger — kafka-course.html — k-tiered-storage

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0649 · k-tiered-storage · L3 · p
> A Kafka broker's disk does two jobs at once: it's the hot write path, and it's the long-term archive. Most reads are tail reads served from the page cache; old data is touched rarely, for backfills and recovery — the docs make exactly this point. Yet keeping 90 days of data means buying 90 days of fast local disk, replicated three times, and moving all of it whenever you add a broker or replace a failed one.
- pass 1: ✅ docs/operations/tiered-storage.md:31 — "Older data is typically read from the disk for backfill or failure recovery purposes and is infrequent."
- pass 2: ✅ docs/operations/tiered-storage.md:31 — "Tail reads leverage OS's page cache ... Older data is typically read from the disk for backfill or failure recovery"; cost reasoning n/a

### C0650 · k-tiered-storage · L3 · p
> Tiered storage splits the two jobs. The local tier is the log you know. The remote tier is an external store (the docs mention HDFS or S3) that receives completed segments. In the archive: the branch keeps the last few volumes on the reading-room shelves; finished volumes are shipped to a central warehouse, and a card index says which warehouse box holds which lines.
- pass 1: ✅ docs/operations/tiered-storage.md:33 — "external storage systems, such as HDFS or S3, to store the completed log segments"
- pass 2: ✅ docs/operations/tiered-storage.md:33 — "remote tier uses external storage systems, such as HDFS or S3, to store the completed log segments"; analogy n/a

### C0651 · k-tiered-storage · L3 · p
> Which segments may safely be shipped to the warehouse? Think about the active segment, and about records that aren't committed yet (or belong to open transactions).
- pass 1: n/a (brain question)
- pass 2: n/a — thinking prompt

### C0652 · k-tiered-storage · L3 · h3
> The moving parts
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0653 · k-tiered-storage · L3 · p
> RemoteStorageManager (RSM) — a plugin interface that copies segment data and its indexes to the remote store, fetches them back, and deletes them (copyLogSegmentData, fetchLogSegment, fetchIndex, deleteLogSegmentData). Kafka ships no production implementation: you configure a vendor or open-source plugin with remote.log.storage.manager.class.name and .class.path.
- pass 1: ✅ docs/operations/tiered-storage.md:41; storage/api/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteStorageManager.java:101-160 — "Kafka server doesn't provide out-of-the-box implementation of RemoteStorageManager"
- pass 2: ✅ storage/.../RemoteStorageManager.java:101,114,145,160 copyLogSegmentData/fetchLogSegment/fetchIndex/deleteLogSegmentData; docs/operations/tiered-storage.md:41 — "Kafka server doesn't provide out-of-the-box implementation of RemoteStorageManager"

### C0654 · k-tiered-storage · L3 · p
> RemoteLogMetadataManager (RLMM) — keeps strongly consistent metadata about remote segments. The default, TopicBasedRemoteLogMetadataManager, stores it in the internal topic __remote_log_metadata; you must set remote.log.metadata.manager.listener.name so its internal clients know which listener to use.
- pass 1: ✅ docs/operations/tiered-storage.md:43; storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:71; storage/.../TopicBasedRemoteLogMetadataManagerConfig.java:44 — "remote.log.metadata.manager.listener.name is a mandatory property"
- pass 2: ✅ docs/operations/tiered-storage.md:43 — "strongly consistent semantics" ... "remote.log.metadata.manager.listener.name is a mandatory property"; TopicBasedRemoteLogMetadataManagerConfig.java:44 __remote_log_metadata

### C0655 · k-tiered-storage · L3 · p
> RemoteLogManager (RLM) — the broker component that runs per-partition tasks: a copy task on the leader, an expiration task that deletes remote segments past retention, and a follower task. Segments eligible for copying are those that are not the active segment and whose end offset is below the last stable offset — "as remote storage should contain only committed/acked messages". That answers the brain question.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManager.java:863,912-936,1141,1605 — "remote storage should contain only committed/acked messages"
- pass 2: ✅ storage/.../remote/storage/RemoteLogManager.java:863,1141,1605 RLMCopyTask/RLMExpirationTask/RLMFollowerTask; :914-917 — "remote storage should contain only committed/acked messages"

### C0656 · k-tiered-storage · L3 · figcaption
> Simplified: one partition on its leader; segment sizes, times and the disk meter are illustrative. In reality uploads run on remote.log.manager.copier.thread.pool.size threads and remote reads on remote.log.reader.threads.
- pass 1: n/a (figcaption, labelled illustrative; thread configs per storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:118-121,150-152)
- pass 2: ✅ storage/.../RemoteLogManagerConfig.java:118,150 remote.log.manager.copier.thread.pool.size, remote.log.reader.threads; figure numbers n/a

### C0657 · k-tiered-storage · L3 · h3
> Two retention clocks
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0658 · k-tiered-storage · L3 · p
> A tiered topic (remote.storage.enable=true) has two retention settings. local.retention.ms / local.retention.bytes decide how long segments stay on the broker; retention.ms / retention.bytes decide how long the data exists at all (in the remote tier). The local values default to -2, meaning "use the full retention" — so you must set them explicitly to get any benefit, and they must not exceed the total retention. Crucially, a local segment becomes eligible for deletion only after it has been uploaded.
- pass 1: ✅ docs/operations/tiered-storage.md:47-56,109; storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:159-169 — "Default value is -2, it represents log.retention.ms value is to be used"
- pass 2: ✅ clients/.../TopicConfig.java:89-96 — "Default value is -2 ... effective value should always be less than or equal to retention.ms"; docs/operations/tiered-storage.md:109 "eligible for deletion only after it gets uploaded"

### C0659 · k-tiered-storage · L3 · h3
> Reading old data
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0660 · k-tiered-storage · L3 · p
> Consumers don't change at all. When a fetch asks for an offset that's no longer local, the leader serves it from the remote store: the read runs on the remote-reader thread pool (remote.log.reader.threads, default 10) and the fetch waits in a dedicated DelayedRemoteFetch purgatory (up to remote.fetch.max.wait.ms, default 500). Expect higher latency for these cold reads and budget for the object store's request costs.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:150-152,203-206; core/src/main/scala/kafka/server/ReplicaManager.scala:62,195-199 — "Size of the thread pool that is allocated for handling remote log reads."
- pass 2: ✅ storage/.../RemoteLogManagerConfig.java:152 DEFAULT_REMOTE_LOG_READER_THREADS = 10; :206 DEFAULT_REMOTE_FETCH_MAX_WAIT_MS = 500; core/.../ReplicaManager.scala:195-199 DelayedRemoteFetch purgatory; latency/cost advice n/a

### C0661 · k-tiered-storage · L3 · p
> An object-store outage fills local disks. Since local deletion waits for a successful upload, a broken plugin, bad credentials or an unreachable bucket means segments pile up locally no matter what local.retention.ms says. Alert on remote copy lag and errors, and size local disks with headroom for an outage.
- pass 1: ✅ docs/operations/tiered-storage.md:109 — "a local log segment is eligible for deletion only after it gets uploaded to remote"
- pass 2: ✅ storage/.../UnifiedLog.java:1857-1864 isSegmentEligibleForDeletion requires offset <= highestOffsetInRemoteStorage; docs/operations/tiered-storage.md:109; alerting advice n/a

### C0662 · k-tiered-storage · L3 · p
> Limitations listed in the 4.3 docs: no support for compacted topics; tiered storage must be disabled on every topic before disabling it at the broker level; admin actions need clients 3.0+; no support for segments missing a producer snapshot file (topics created before 2.8.0).
- pass 1: ✅ docs/operations/tiered-storage.md:170-177 — "No support for compacted topics"
- pass 2: ✅ docs/operations/tiered-storage.md:174-177 — "No support for compacted topics" ... "clients from version 3.0 onwards" ... "topic is created before v2.8.0"

### C0663 · k-tiered-storage · L3 · p
> Turning it off per topic (KRaft): remote.log.copy.disable=true makes remote data read-only (then set local.retention.* to match total retention or -2, or the disk can fill), or remote.storage.enable=false,remote.log.delete.on.disable=true deletes all remote data.
- pass 1: ✅ docs/operations/tiered-storage.md:142-160 — "local retention policies will not be applied anymore, and that might … cause unexpected disk full"
- pass 2: ✅ docs/operations/tiered-storage.md:142-159 — "remote.storage.enable=true,remote.log.copy.disable=true"; local.retention.* same as retention or "-2"; "remote.storage.enable=false,remote.log.delete.on.disable=true"

### C0664 · k-tiered-storage · L3 · p
> New in 4.3: the dynamic broker config follower.fetch.last.tiered.offset.enable (default false, KIP-1023). With it, a newly added follower that has no local data skips straight to the leader's earliest pending-upload offset instead of re-fetching data already in remote storage — the upgrade notes say this "reduces bootstrap time significantly for large tiered-storage topics".
- pass 1: ✅ docs/getting-started/upgrade.md:55; server/src/main/java/org/apache/kafka/server/config/ReplicationConfigs.java:143-145 — "reduces bootstrap time significantly for large tiered-storage topics"
- pass 2: ✅ docs/getting-started/upgrade.md:55 — "new dynamic broker configuration follower.fetch.last.tiered.offset.enable (default: false)" ... "reduces bootstrap time significantly"; ReplicationConfigs.java:145

### C0665 · k-tiered-storage · L3 · tr
> remote.log.storage.system.enable (broker) | false | Turns on tiered storage in the broker | Cluster-wide opt-in; needs an RSM plugin
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:55-58; docs/operations/tiered-storage.md:39 — "DEFAULT_REMOTE_LOG_STORAGE_SYSTEM_ENABLE = false"
- pass 2: ✅ storage/.../RemoteLogManagerConfig.java:55,58 DEFAULT_REMOTE_LOG_STORAGE_SYSTEM_ENABLE = false; docs/operations/tiered-storage.md:39

### C0666 · k-tiered-storage · L3 · tr
> remote.log.storage.manager.class.name / .class.path | none | Your RSM plugin (Kafka ships none for production) | Always, when enabled
- pass 1: ✅ docs/operations/tiered-storage.md:41; storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:60-63 — "Users must configure remote.log.storage.manager.class.name and remote.log.storage.manager.class.path"
- pass 2: ✅ docs/operations/tiered-storage.md:41 — "Users must configure remote.log.storage.manager.class.name and remote.log.storage.manager.class.path"

### C0667 · k-tiered-storage · L3 · tr
> remote.log.metadata.manager.listener.name | none | Listener for the default topic-based RLMM clients | Mandatory with the default RLMM
- pass 1: ✅ docs/operations/tiered-storage.md:43 — "mandatory property to specify which listener the clients … use"
- pass 2: ✅ storage/.../RemoteLogManagerConfig.java:256-257 default null; docs/operations/tiered-storage.md:43 — "mandatory property" with default internal-topic RLMM

### C0668 · k-tiered-storage · L3 · tr
> remote.storage.enable (topic) | false | Tier this topic | Long-retention, append-only (non-compacted) topics
- pass 1: ✅ docs/operations/tiered-storage.md:47,174 — "By default it is set to false / No support for compacted topics"
- pass 2: ✅ storage/.../LogConfig.java:136 DEFAULT_REMOTE_STORAGE_ENABLE = false; docs/operations/tiered-storage.md:174 no compacted topics

### C0669 · k-tiered-storage · L3 · tr
> local.retention.ms / local.retention.bytes | -2 (= use retention.ms / retention.bytes) | How much stays on local disk | Set to hours/days to actually shrink local disk
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:159-169 — "Default value is -2"
- pass 2: ✅ LogConfig.java:139-140 DEFAULT_LOCAL_RETENTION_* = -2 "derived from RetentionMs/Bytes"

### C0670 · k-tiered-storage · L3 · tr
> remote.log.reader.threads | 10 | Threads serving remote reads | Many consumers backfilling from old offsets
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:150-152 — "DEFAULT_REMOTE_LOG_READER_THREADS = 10"
- pass 2: ✅ storage/.../RemoteLogManagerConfig.java:152 DEFAULT_REMOTE_LOG_READER_THREADS = 10

### C0671 · k-tiered-storage · L3 · tr
> remote.log.manager.copy.max.bytes.per.second | Long.MAX_VALUE | Upload throttle | Uploads compete with replication/clients for bandwidth
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:171-175 — "DEFAULT_REMOTE_LOG_MANAGER_COPY_MAX_BYTES_PER_SECOND = Long.MAX_VALUE"
- pass 2: ✅ storage/.../RemoteLogManagerConfig.java:171-175 remote.log.manager.copy.max.bytes.per.second default Long.MAX_VALUE

### C0672 · k-tiered-storage · L3 · tr
> follower.fetch.last.tiered.offset.enable | false | Empty new follower skips already-tiered data (4.3) | Adding brokers / reassigning big tiered partitions
- pass 1: ✅ docs/getting-started/upgrade.md:55; server/.../ReplicationConfigs.java:145 — "default: false"
- pass 2: ✅ ReplicationConfigs.java:143-145 default false — "an empty follower skips offsets up to the last tiered offset"; upgrade.md:41-55 (4.3.0 notes)

### C0673 · k-tiered-storage · L3 · p
> The official quick start uses the test-only LocalTieredStorage plugin (built from the source tree) to simulate a remote store in a local directory. After configuring the broker, how do you create a tiered topic that keeps only a second locally, push enough data to roll segments, and prove offset 0 is served from "remote"?
- pass 1: n/a (exercise prompt; LocalTieredStorage per docs/operations/tiered-storage.md:60)
- pass 2: ✅ docs/operations/tiered-storage.md:60-70 — "LocalTieredStorage implemented for integration test"; built via ./gradlew :storage:testJar; local.retention.ms=1000 in :115

### C0674 · k-tiered-storage · L3 · pre
> bin/kafka-topics.sh --create --topic tieredTopic --bootstrap-server localhost:9092 \ --config remote.storage.enable=true --config local.retention.ms=1000 --config retention.ms=3600000 \ --config segment.bytes=1048576 --config file.delete.delay.ms=1000 bin/kafka-producer-perf-test.sh --bootstrap-server localhost:9092 --topic tieredTopic \ --num-records 1200 --record-size 1024 --throughput -1 ls /tmp/kafka-remote-storage/kafka-tiered-storage/ # uploaded .log/.index/.timeindex/... files bin/kafka-console-consumer.sh --topic tieredTopic --from-beginning --max-messages 1 \ --bootstrap-server localhost:9092 --formatter-property print.offset=true
- pass 1: ✅ docs/operations/tiered-storage.md:114-139 — "--config remote.storage.enable=true --config local.retention.ms=1000"
- pass 2: ✅ docs/operations/tiered-storage.md:114-116,122,128,139 — commands match verbatim (listing shown under a partition subdirectory tieredTopic-0-<id>)

### C0675 · k-tiered-storage · L3 · p
> These are the docs' own commands. The local segment disappears only after its upload; the consumer read of offset 0 then has to go to the remote tier.
- pass 1: ✅ docs/operations/tiered-storage.md:109,125,136 — "make sure it will successfully fetch offset 0 from the remote storage"
- pass 2: ✅ docs/operations/tiered-storage.md:109 "eligible for deletion only after it gets uploaded"; :136 "fetch offset 0 from the remote storage"

### C0676 · k-tiered-storage · L3 · summary
> L3🔬 Go deeper: tasks, metadata and the fetch path
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C0677 · k-tiered-storage · L3 · p
> org.apache.kafka.server.log.remote.storage.RemoteLogManager schedules RLMCopyTask (leader: candidateLogSegments() skips the active segment and anything whose next segment starts beyond the LSO), RLMExpirationTask (enforces retention.ms/bytes on remote data) and RLMFollowerTask. Tasks run every remote.log.manager.task.interval.ms (30 s).
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManager.java:863,912-936,1141,1605; storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:128-131 — "Interval at which remote log manager runs the scheduled tasks"
- pass 2: ✅ storage/.../remote/storage/RemoteLogManager.java:914-931 candidateLogSegments adds segment only if next segment baseOffset <= LSO, discards active; :1141,1605; storage/.../RemoteLogManagerConfig.java:131 DEFAULT_REMOTE_LOG_MANAGER_TASK_INTERVAL_MS = 30 * 1000L

### C0678 · k-tiered-storage · L3 · p
> For each uploaded segment the RSM receives the log file plus offset index, time index, transaction index, producer snapshot and a leader-epoch checkpoint — visible in the quick-start listing as .index, .timeindex, .snapshot, .leader_epoch_checkpoint files. The RLMM records segment lifecycle events in __remote_log_metadata (default 50 partitions, RF 3, retention -1 = unlimited).
- pass 1: ✅ storage/api/src/main/java/org/apache/kafka/server/log/remote/storage/LogSegmentData.java:31-36; docs/operations/tiered-storage.md:128-133; storage/.../TopicBasedRemoteLogMetadataManagerConfig.java:54-56 — "private final Path producerSnapshotIndex"
- pass 2: ✅ storage/.../LogSegmentData.java:31-36 logSegment/offsetIndex/timeIndex/transactionIndex(optional)/producerSnapshotIndex/leaderEpochIndex; docs/operations/tiered-storage.md:129-133; TopicBasedRemoteLogMetadataManagerConfig.java:54-56 50 partitions, RF 3, retention -1

### C0679 · k-tiered-storage · L3 · p
> On the fetch path, ReplicaManager detects an offset below the local log start, creates a remote read and parks the fetch as DelayedRemoteFetch in its own purgatory; that purgatory's purge interval is 0 so completed results can be garbage-collected immediately. Remote index files are cached locally (remote.log.index.file.cache.total.size.bytes, 1 GiB).
- pass 1: ✅ core/src/main/scala/kafka/server/ReplicaManager.scala:195-199; storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:90-93 — "purgeInterval is set to 0 to release the references"
- pass 2: ✅ core/.../ReplicaManager.scala:1904 offset < localLogStartOffset; :195 "purgeInterval is set to 0 to release the references ... immediately for GC"; storage/.../RemoteLogManagerConfig.java:93 1024*1024*1024L

### C0680 · k-tiered-storage · L3 · li
> Two tiers: local segments on the broker, completed segments in a remote store via an RSM plugin (none shipped for production).
- pass 1: ✅ docs/operations/tiered-storage.md:33,41 — "doesn't provide out-of-the-box implementation"
- pass 2: ✅ docs/operations/tiered-storage.md:33,41

### C0681 · k-tiered-storage · L3 · li
> Only non-active segments below the LSO are uploaded; local copies are deleted only after a successful upload.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManager.java:912-936; docs/operations/tiered-storage.md:109 — "Segment is not the active segment"
- pass 2: ✅ storage/.../remote/storage/RemoteLogManager.java:914-917 not active & below LSO; UnifiedLog.java:1864 local deletion needs highestOffsetInRemoteStorage

### C0682 · k-tiered-storage · L3 · li
> local.retention.* defaults to -2 (= full retention) — set it, or nothing shrinks.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:159-169 — "Default value is -2"
- pass 2: ✅ LogConfig.java:139-140 -2; TopicConfig.java:89-90

### C0683 · k-tiered-storage · L3 · li
> Old-offset reads are transparent to clients but slower: remote reader threads + DelayedRemoteFetch.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManagerConfig.java:150-152,203-206 — "remote log reads"
- pass 2: ✅ storage/.../RemoteLogManagerConfig.java:150-152 reader thread pool; ReplicaManager.scala:1642 DelayedRemoteFetch

### C0684 · k-tiered-storage · L3 · li
> Not for compacted topics. 4.3 adds follower.fetch.last.tiered.offset.enable for faster new followers.
- pass 1: ✅ docs/operations/tiered-storage.md:174; docs/getting-started/upgrade.md:55 — "No support for compacted topics"
- pass 2: ✅ docs/operations/tiered-storage.md:174; upgrade.md:55 (4.3.0 notable changes)
