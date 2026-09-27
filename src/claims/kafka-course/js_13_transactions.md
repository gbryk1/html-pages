# Claims ledger — kafka-course.html — js_13_transactions

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1458 · js:13. transactions · L- · script
> Press ▶ to run one consume–transform–produce transaction.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1459 · js:13. transactions · L- · script
> beginTransaction() is local: the client just marks itself "in transaction". Nothing is sent yet.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/internals/TransactionManager.java:348-353 — "transitionTo(State.IN_TRANSACTION)"
- pass 2: ✅ clients/.../TransactionManager.java:348-352 — beginTransaction() just "transitionTo(State.IN_TRANSACTION)", no request sent

### C1460 · js:13. transactions · L- · script
> send() to invoices-0 and audit-3. Under TV2 each partition leader adds its partition to the transaction on the server side; the coordinator moves to Ongoing. The records are in the logs already, but pending.
- pass 1: ✅ docs/operations/transaction-protocol.md:43; clients/src/main/java/org/apache/kafka/clients/producer/internals/TransactionManager.java:466-469 — "only sending a single call to add partitions on the server side"
- pass 2: ✅ TransactionManager.java:466-469 — V2: partition only tracked locally; transaction-protocol.md:45 "single call to add partitions on the server side"; coordinator EMPTY→ONGOING (TransactionState.java:96)

### C1461 · js:13. transactions · L- · script
> Another, non-transactional producer writes offset 3. A read_committed reader still stops at the LSO (offset 2): everything after the first open transactional record is withheld.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:352-353 — "any messages appearing after messages belonging to ongoing transactions will be withheld"
- pass 2: ✅ ConsumerConfig.java:353 — "any messages appearing after messages belonging to ongoing transactions will be withheld" (offsets illustrative n/a)

### C1462 · js:13. transactions · L- · script
> sendOffsetsToTransaction() writes the consumer group's new offset into __consumer_offsets — as part of the same transaction (TxnOffsetCommit).
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/internals/TransactionManager.java:433-438; docs/design/design.md:203 — "the client will skip sending AddOffsetsToTxn before sending txnOffsetCommit"
- pass 2: ✅ TransactionManager.java:433-437 — V2 skips AddOffsetsToTxn, uses txnOffsetCommitHandler; design.md:208 offset written in same transaction

### C1463 · js:13. transactions · L- · script
> … → the coordinator durably writes … to __transaction_state. From here the outcome is decided, even if the coordinator crashes.
- pass 1: ✅ transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionState.java:53-62 — "PREPARE_COMMIT / PREPARE_ABORT states persisted by the coordinator"
- pass 2: ✅ TransactionState.java:97 PREPARE_COMMIT←ONGOING, written to __transaction_state; coordinator failover completes pending markers (TransactionStateManager loadTransactionMetadata)

### C1464 · js:13. transactions · L- · script
> WriteTxnMarkers: a … control record is appended to every partition in the transaction, including the offsets partition.
- pass 1: ✅ clients/src/main/resources/common/message/WriteTxnMarkersRequest.json:28-41; docs/design/design.md:229 — "The transaction markers to be written."
- pass 2: ✅ WriteTxnMarkersRequest.json — markers per topic/partition; offsets partition is part of the transaction (design.md:208)

### C1465 · js:13. transactions · L- · script
> CompleteCommit. The LSO jumps past the marker; the reader now gets 0–3 (markers are never handed to the application). With TV2 the epoch was bumped for the next transaction (5 → 6).
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/internals/CompletedFetch.java:230,377; docs/operations/transaction-protocol.md:31 — "if (!currentBatch.isControlBatch()) / the producer epoch is bumped on every transaction"
- pass 2: ✅ Consumer skips control batches; transaction-protocol.md:31 "producer epoch is bumped on every transaction" (numbers illustrative n/a)

### C1466 · js:13. transactions · L- · script
> CompleteAbort. Records A and B stay in the log (append-only!) but read_committed readers skip them using the aborted-transaction list in the fetch response. The offset move was aborted too; the consumer does not rewind by itself, so after the app seeks back to the committed offset (or restarts) the input is re-read.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ clients/.../message/FetchResponse.json AbortedTransactions + TxnOffsetCommit pending offsets dropped on abort; consumer position is client-side, not rewound — mechanism correct

### C1467 · js:13. transactions · L- · script
> Instance A (epoch 5) opens a transaction and writes A to invoices-0 — then freezes in a long GC pause.
- pass 1: n/a (scenario setup, illustrative)
- pass 2: n/a — illustrative scenario setup

### C1468 · js:13. transactions · L- · script
> Instance B starts with the same transactional.id and calls initTransactions(). The coordinator bumps the epoch to 6 and must fence the old incarnation first.
- pass 1: ✅ transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionState.java:82; docs/design/design.md:225 — "PREPARE_EPOCH_FENCE"
- pass 2: ✅ TransactionCoordinator.scala:278-284 — ONGOING: "abort the current ongoing txn first… ensure that the epoch is bumped" via prepareFenceProducerEpoch

### C1469 · js:13. transactions · L- · script
> A's open transaction is aborted with an ABORT marker, so it cannot pin the LSO.
- pass 1: ✅ docs/design/design.md:225 — "causes any in-flight transaction from the previous instance to abort"
- pass 2: ✅ TransactionState.java:98 PREPARE_ABORT←PREPARE_EPOCH_FENCE; abort marker written, LSO released

### C1470 · js:13. transactions · L- · script
> Zombie A wakes up and sends with epoch 5…
- pass 1: n/a (scenario step, illustrative)
- pass 2: n/a — illustrative scenario step

### C1471 · js:13. transactions · L- · script
> Rejected: stale epoch. The partition leader refuses the write with INVALID_PRODUCER_EPOCH (A's client sees InvalidProducerEpochException); when A next calls the coordinator to commit or abort, it gets the fatal ProducerFencedException. Only the newest epoch for a transactional.id may write — no duplicates from the zombie.
- pass 1: ✅ revised after pass-2 finding — source in fixes logs (fixes-N.md) for this chapter
- pass 2: ✅ server/.../AddPartitionsToTxnManager.java:172-175 — "Producers expect to handle INVALID_PRODUCER_EPOCH"; TransactionManager.java:1792-1795 EndTxn → "fatalError(Errors.PRODUCER_FENCED.exception())"
