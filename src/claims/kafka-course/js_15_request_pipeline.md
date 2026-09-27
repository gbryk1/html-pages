# Claims ledger — kafka-course.html — js_15_request_pipeline

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1486 · js:15. request pipeline · L- · script
> Press ▶ to send 60 produce requests through the broker.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1487 · js:15. request pipeline · L- · script
> Request queue full. Every I/O thread is stuck on the slow disk, requests pile up, and at the cap the network threads stop reading sockets — back-pressure reaches the clients.
- pass 1: ✅ server/src/main/java/org/apache/kafka/network/SocketServerConfigs.java:145 — "before blocking the network threads"
- pass 2: ✅ SocketServerConfigs.java:145 — "queued requests allowed for data-plane, before blocking the network threads"; RequestChannel.scala:380 requestQueue.put blocks

### C1488 · js:15. request pipeline · L- · script
> Disk is slow: each append now takes 6× longer. Watch the I/O threads fill up…
- pass 1: n/a (simulation step, labelled illustrative)
- pass 2: n/a — illustrative simulation parameter

### C1489 · js:15. request pipeline · L- · script
> Tick …. Network threads move requests into the queue; I/O threads append and park each acks=all request in purgatory until followers catch up.
- pass 1: ✅ docs/operations/monitoring.md:659,724 — "non-zero if ack=-1 is used"
- pass 2: ✅ SocketServer.scala:1040 sendRequest; monitoring.md:655-657 produce purgatory "non-zero if ack=-1 is used"

### C1490 · js:15. request pipeline · L- · script
> All 60 done — slowly. Queue time dominates (≈… illustrative ms): the fix is the disk (LocalTimeMs), not more network threads.
- pass 1: n/a (simulation result, labelled illustrative; LocalTimeMs meaning per docs/operations/monitoring.md:711)
- pass 2: ✅ mechanism: I/O-thread saturation shows as RequestQueueTimeMs (monitoring.md:698) with cause in LocalTimeMs (:711); number illustrative

### C1491 · js:15. request pipeline · L- · script
> All 60 answered. Queue time stays small; most of each request's life is RemoteTime — waiting in purgatory for the followers, which is expected with acks=all.
- pass 1: ✅ docs/operations/monitoring.md:724 — "Time the request waits for the follower … non-zero for produce requests when ack=-1"
- pass 2: ✅ monitoring.md:724-726 — RemoteTimeMs "Time the request waits for the follower… non-zero for produce requests when ack=-1"
