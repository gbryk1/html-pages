# Claims ledger — kafka-course.html — js_17_tiered_storage

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1508 · js:17. tiered storage · L- · script
> Each box is a segment (offsets inside). Press ⏩ to let segments roll, upload and age out of local disk.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1509 · js:17. tiered storage · L- · script
> Hour …: the active segment rolled; the copy task uploaded every closed segment below the LSO; segments older than local.retention.ms and already uploaded were deleted locally.
- pass 1: ✅ storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManager.java:912-936; docs/operations/tiered-storage.md:109 — "Segment is not the active segment and … end-offset is less than the last-stable-offset"
- pass 2: ✅ storage/.../remote/storage/RemoteLogManager.java:914-931 copy non-active segments below LSO; UnifiedLog.java:1864 local deletion only when uploaded; docs/operations/tiered-storage.md:109

### C1510 · js:17. tiered storage · L- · script
> Steady state: the broker keeps only the recent segments, the remote store keeps everything up to retention.ms. The active segment is never uploaded.
- pass 1: ✅ docs/operations/tiered-storage.md:56,110; storage/src/main/java/org/apache/kafka/server/log/remote/storage/RemoteLogManager.java:914 — "when segments exceed this time, the segments in remote storage will be deleted"
- pass 2: ✅ storage/.../remote/storage/RemoteLogManager.java:933 "Discard the last active segment"; docs/operations/tiered-storage.md:110 remote deleted after retention.ms

### C1511 · js:17. tiered storage · L- · script
> Offset 0 is still local — served from the broker's disk/page cache like any fetch. Press ⏩ a few times and read again.
- pass 1: ✅ docs/operations/tiered-storage.md:31 — "Tail reads leverage OS's page cache"
- pass 2: ✅ ReplicaManager.scala:1904 — only offsets below localLogStartOffset go remote

### C1512 · js:17. tiered storage · L- · script
> Offset 0 is only in the remote tier. The leader parks the fetch as a DelayedRemoteFetch, a remote-reader thread pulls the segment (using cached remote indexes) and the consumer gets its data — slower, but no client change needed.
- pass 1: ✅ core/src/main/scala/kafka/server/ReplicaManager.scala:62,195-199; storage/.../RemoteLogManagerConfig.java:90-93,150-152 — "DelayedRemoteFetch / index files fetched from remote storage"
- pass 2: ✅ ReplicaManager.scala:1625-1642 DelayedRemoteFetch; storage/.../RemoteLogManagerConfig.java:150 reader threads; storage/.../RemoteLogManagerConfig.java:90-93 remote index cache

### C1513 · js:17. tiered storage · L- · script
> Offset 0 is gone everywhere — neither local nor uploaded.
- pass 1: n/a (UI edge-case message)
- pass 2: ✅ RLMExpirationTask (storage/.../remote/storage/RemoteLogManager.java:1141) deletes remote segments past retention.ms/bytes

### C1514 · js:17. tiered storage · L- · script
> The object store is unreachable (expired credentials, network, bucket policy…).
- pass 1: n/a (scenario setup, illustrative)
- pass 2: n/a — scenario setup

### C1515 · js:17. tiered storage · L- · script
> Hour …: uploads fail, so no segment is eligible for local deletion — … segments on local disk.
- pass 1: ✅ docs/operations/tiered-storage.md:109 — "eligible for deletion only after it gets uploaded to remote"
- pass 2: ✅ UnifiedLog.java:1857-1864 — not eligible for deletion until uploaded; counts n/a

### C1516 · js:17. tiered storage · L- · script
> Local disk is filling up even though local.retention.ms says 2 h: local deletion only happens after a successful upload. Alert on copy lag and errors before this becomes a disk-full outage.
- pass 1: ✅ docs/operations/tiered-storage.md:109 — "eligible for deletion only after it gets uploaded to remote (alerting advice = opinion)"
- pass 2: ✅ docs/operations/tiered-storage.md:109 + UnifiedLog.java:1864; alert advice n/a
