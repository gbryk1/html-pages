# Claims ledger — kafka-course.html — k-transactions

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0490 · k-transactions · L3 · p
> The idempotent producer from Part II solved one problem: a retry of the same batch to the same partition doesn't create a duplicate. That's it. It says nothing about two different partitions, nothing about a producer that crashes and restarts, and nothing about the consumer offset you want to move at the same time as you write your output.
- pass 1: ✅ docs/design/design.md:193 — "the broker assigns each producer an ID and deduplicates messages using a sequence number"
- pass 2: ✅ docs/design/design.md:196 — "idempotent delivery option which guarantees that resending will not result in duplicate entries"; transactions are the separate feature for multi-partition atomic writes (same para)

### C0491 · k-transactions · L3 · p
> Think about the classic "read → process → write" loop. You read record 41 from orders, write a result to invoices, then commit offset 42. Crash between the write and the commit, and on restart you read 41 again and write a second invoice. Commit first, crash before the write, and the invoice is never written. Both orders are wrong. You need the output write and the offset move to be one atomic act.
- pass 1: ✅ docs/design/design.md:199-200 — "crashes after processing messages but before saving its position"
- pass 2: ✅ docs/design/design.md:204-205 — save position then process = "at-most-once"; process then save = "at-least-once" (mechanism matches)

### C0492 · k-transactions · L3 · p
> In the city ledger archive, your clerk copies lines from the "orders" book into two other books ("invoices" and "audit") and then moves the bookmark in the orders book. All three books may live in different branches. How would you make sure a reader never sees the invoice without the audit line, and never sees either if the clerk faints halfway? You can't erase ink — the books are append-only.
- pass 1: n/a (analogy / brain question)
- pass 2: n/a — ledger analogy / brain-teaser question

### C0493 · k-transactions · L3 · p
> Kafka's answer: write everything immediately, decide visibility later. The records go into the logs straight away, flagged as "part of transaction X". When the producer commits, a tiny control record — a commit or abort marker — is appended to every partition that took part. Readers that ask for isolation.level=read_committed hold back transactional records until they see the marker, and skip the ones that end up aborted. In ledger terms: the clerk writes in pencil-grey ink, and at the end a notary stamps "VALID" or "VOID" in every book touched.
- pass 1: ✅ docs/design/design.md:229 — "transaction marker records are written to indicate the outcome of the transaction"
- pass 2: ✅ docs/design/design.md:230 — "when the transaction commits or aborts, transaction marker records are written to indicate the outcome"; ConsumerConfig ISOLATION_LEVEL_DOC: read_committed withholds until completed (analogy part n/a)

### C0494 · k-transactions · L3 · p
> A transactional producer is an idempotent producer with a name. You set transactional.id (setting it implies enable.idempotence). That name is stable across restarts — that's the whole point. It's hashed onto one partition of the internal topic __transaction_state (50 partitions by default, replication factor 3, min ISR 2), and the leader of that partition acts as the producer's transaction coordinator. The coordinator keeps the transaction's state machine and writes every state change to __transaction_state, so a new coordinator can pick up after a failover.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:358; transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionLogConfig.java:32-45; core/src/main/scala/kafka/coordinator/transaction/TransactionStateManager.scala:447 — "If a TransactionalId is configured, enable.idempotence is implied / PARTITIONS_DEFAULT = 50 / Utils.abs(transactionalId.hashCode) %"
- pass 2: ✅ ProducerConfig.java:358 — "If a TransactionalId is configured, enable.idempotence is implied"; TransactionLogConfig.java:32/40/45 — partitions 50, RF 3, min ISR 2; TransactionStateManager.scala:447 partitionFor hash

### C0495 · k-transactions · L3 · p
> The API is five calls: initTransactions() once, then per transaction beginTransaction(), send()…, optionally sendOffsetsToTransaction(offsets, consumer.groupMetadata()), then commitTransaction() or abortTransaction(). On the consumer side you set isolation.level=read_committed and enable.auto.commit=false — the offsets travel inside the transaction instead.
- pass 1: ✅ docs/design/design.md:225 — "must include isolation.level=read_committed and enable.auto.commit=false"
- pass 2: ✅ docs/design/design.md:226 — "The consumer configuration must include isolation.level=read_committed and enable.auto.commit=false"; TransactionManager.java:433 sendOffsetsToTransaction(offsets, groupMetadata)

### C0496 · k-transactions · L3 · figcaption
> Simplified: one coordinator, two output partitions, offsets and epochs are illustrative. The flow shown is transaction version 2 (no separate AddPartitionsToTxn / AddOffsetsToTxn calls from the client); the red bar is the last stable offset (LSO) of invoices-0.
- pass 1: n/a (figcaption, labelled illustrative; TV2 flow per clients/src/main/java/org/apache/kafka/clients/producer/internals/TransactionManager.java:433-469)
- pass 2: ✅ TransactionManager.java:466 (TV2: partition only tracked locally) and :433 — "In transaction V2, the client will skip sending AddOffsetsToTxn"; LSO per ConsumerConfig.java:352 (illustrative numbers n/a)

### C0497 · k-transactions · L3 · h3
> What happens on commit
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0498 · k-transactions · L3 · p
> The coordinator's state machine moves Empty → Ongoing → PrepareCommit → CompleteCommit (or PrepareAbort → CompleteAbort). The moment PrepareCommit is durably written to __transaction_state, the decision is final — even if the coordinator dies, the next one will finish the job. Then it sends WriteTxnMarkers requests to the leaders of every partition in the transaction (including the __consumer_offsets partition holding your group's offsets). Once all markers are written, it records CompleteCommit.
- pass 1: ✅ transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionState.java:39-82; clients/src/main/resources/common/message/WriteTxnMarkersRequest.json:28-35 — "PREPARE_COMMIT … COMPLETE_COMMIT / The result of the transaction to write to the partitions"
- pass 2: ✅ TransactionState.java:94-102 — VALID_PREVIOUS_STATES: PREPARE_COMMIT←ONGOING, COMPLETE_COMMIT←PREPARE_COMMIT, COMPLETE_ABORT←PREPARE_ABORT; WriteTxnMarkersRequest.json apiKey 27 "The transaction markers to be written"

### C0499 · k-transactions · L3 · p
> On the reading side, every partition tracks a last stable offset (LSO): the point before which every transactional record has been decided. A read_committed consumer only gets records below the LSO, and the fetch response carries a list of AbortedTransactions so the client can drop records from aborted ones. That's why one long-running open transaction stalls every read_committed consumer of that partition — even consumers that don't care about that producer. Records behind the open transaction are withheld until it finishes.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:352-354; clients/src/main/resources/common/message/FetchResponse.json:75,102 — "read_committed consumers will not be able to read up to the high watermark when there are in flight transactions"
- pass 2: ✅ ConsumerConfig.java:352-354 — "only return messages up to the last stable offset (LSO)… messages appearing after… ongoing transactions will be withheld"; FetchResponse.json:102 AbortedTransactions

### C0500 · k-transactions · L3 · p
> The open transaction blocks everyone. Since read_committed consumers stop at the LSO, a producer that begins a transaction and then hangs holds back the whole partition until the coordinator aborts it after transaction.timeout.ms (producer default 60000 ms; it can't exceed the broker's transaction.max.timeout.ms, default 15 minutes). Keep transactions short, and alert on consumer lag that sits exactly at the LSO while the high watermark keeps moving.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:351-352,534; transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionStateManagerConfig.java:33 — "coordinator proactively aborts it / 60000 / TimeUnit.MINUTES.toMillis(15)"
- pass 2: ✅ ProducerConfig.java:534 default 60000; :351 "before the coordinator proactively aborts it"; TransactionStateManagerConfig.java:33 TRANSACTIONS_MAX_TIMEOUT_MS_DEFAULT = 15 minutes (alert advice n/a)

### C0501 · k-transactions · L3 · h3
> Zombie fencing — why the id must be stable
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0502 · k-transactions · L3 · p
> Imagine instance A of your service stalls in a long GC pause. The platform thinks it's dead and starts instance B with the same transactional.id. B calls initTransactions(); the coordinator bumps the producer epoch for that id and aborts whatever A left open. When A wakes up and tries to write with the old epoch, the partition leader rejects it with INVALID_PRODUCER_EPOCH (surfaced as InvalidProducerEpochException, which asks the app to abort); as soon as A talks to the coordinator again — to commit or abort — it gets the fatal ProducerFencedException and must close. The epoch is the notary's seal: only the newest seal is honoured. Without a stable id there's nothing to fence on, and A and B would happily write duplicates side by side.
- pass 1: ✅ revised after pass-2 finding — C0502 (zombie fencing paragraph) + C0532 bullet + C1478 figure + quiz C1272 — stale-epoch produce now "rejected with INVALID_PRODUCER_EPOCH (InvalidProducerEpochException, app aborts)"; fatal ProducerFencedException only on the zombie's next coordinator call (
- pass 2: ✅ AddPartitionsToTxnManager.java:172-175, InvalidProducerEpochException.java javadoc "user should abort the ongoing transaction"; TransactionManager.java:1792-1795 EndTxn → fatal PRODUCER_FENCED

### C0503 · k-transactions · L3 · div
> So the consumer is transactional too?
- pass 1: n/a (Q&A question)
- pass 2: n/a — FAQ question

### C0504 · k-transactions · L3 · div
> No. Only the producer has transactional.id. The trick is that the consumer's position is itself just a record in __consumer_offsets, so the producer writes it inside the transaction via sendOffsetsToTransaction. The consumer is merely configured to read only committed data.
- pass 1: ✅ docs/design/design.md:213 — "it is only the producer which is transactional"
- pass 2: ✅ docs/design/design.md:216 — "it is only the producer which is transactional… able to make transactional updates to the consumer's position"; :226 "Only the producer has the transactional.id configuration"

### C0505 · k-transactions · L3 · div
> Does "exactly-once" cover my database write in the processing step?
- pass 1: n/a (Q&A question)
- pass 2: n/a — FAQ question

### C0506 · k-transactions · L3 · div
> No. The guarantee covers reading from Kafka, writing to Kafka, and moving offsets in Kafka. Side effects outside Kafka (an HTTP call, a DB insert) can still happen twice after an abort and retry — they need their own idempotency or must store offsets in the same place as their output.
- pass 1: ✅ docs/design/design.md:205-207 — "Exactly-once delivery for other destination systems generally requires cooperation with such systems"
- pass 2: ✅ docs/design/design.md:210 — "letting the consumer store its offset in the same place as its output"; :212 "Exactly-once delivery for other destination systems generally requires cooperation"

### C0507 · k-transactions · L3 · div
> What about read_uncommitted consumers?
- pass 1: n/a (Q&A question)
- pass 2: n/a — FAQ question

### C0508 · k-transactions · L3 · div
> That's the default isolation.level. They read up to the high watermark and see records of open and aborted transactions. Transactions only protect readers who opt in.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:350-354; docs/design/design.md:223 — "If set to read_uncommitted (the default), consumer.poll() will return all messages"
- pass 2: ✅ ConsumerConfig.java:350,357 — "read_uncommitted (the default), consumer.poll() will return all messages, even transactional messages which have been aborted"

### C0509 · k-transactions · L3 · h3
> KIP-890 and transaction version 2
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0510 · k-transactions · L3 · p
> The original protocol had a gap: a produce request that arrived late (say, a retry delayed in the network) could land after a transaction finished and silently become part of the next one, or linger as a "hanging" transaction that pins the LSO forever. KIP-890 ("transactions server-side defense") closes this in two steps. First, partition leaders verify with the coordinator that a partition really is part of an ongoing transaction before accepting transactional data (transaction.partition.verification.enable, default true). Second, with transaction.version=2 (TV2), the producer epoch is bumped at the end of every transaction, so a straggler from transaction N carries a stale epoch and is rejected by construction.
- pass 1: ✅ docs/operations/transaction-protocol.md:31; transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionLogConfig.java:52-53 — "duplicates are not written as part of the next transaction / TRANSACTION_PARTITION_VERIFICATION_ENABLE_DEFAULT = true"
- pass 2: ✅ docs/operations/transaction-protocol.md:31 — "producer epoch is bumped on every transaction… duplicates are not written as part of the next transaction"; TransactionLogConfig.java:53 TRANSACTION_PARTITION_VERIFICATION_ENABLE_DEFAULT = true

### C0511 · k-transactions · L3 · p
> TV2 also trims round trips. The client no longer sends AddPartitionsToTxn itself: the partition leader adds the partition to the transaction on the server side while handling the produce request. sendOffsetsToTransaction likewise skips the separate AddOffsetsToTxn call and goes straight to TxnOffsetCommit. The server side has been on since 4.0 and is controlled by the transaction.version feature; 4.x producers switch to it at the start of their next transaction once they learn the cluster supports it — no restart needed. Downgrades are safe too.
- pass 1: ✅ docs/operations/transaction-protocol.md:33-43; clients/src/main/java/org/apache/kafka/clients/producer/internals/TransactionManager.java:433-469 — "Producer clients starting 4.0 and above will use the new transactional protocol"
- pass 2: ✅ docs/operations/transaction-protocol.md:35-41 — "automatically enabled on the server since Apache Kafka 4.0… producer clients do not need to be restarted… Downgrades are safe"; TransactionManager.java:433,466

### C0512 · k-transactions · L3 · p
> On a local 4.3 cluster, check which transaction version the cluster runs, then list the transactions the coordinators know about and describe one. Which commands?
- pass 1: n/a (exercise prompt)
- pass 2: n/a — exercise prompt

### C0513 · k-transactions · L3 · pre
> bin/kafka-features.sh --bootstrap-server localhost:9092 describe bin/kafka-transactions.sh --bootstrap-server localhost:9092 list bin/kafka-transactions.sh --bootstrap-server localhost:9092 describe --transactional-id billing-1 # watch only committed data: bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic invoices \ --isolation-level read_committed --from-beginning
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/FeatureCommand.java:139; tools/src/main/java/org/apache/kafka/tools/TransactionsCommand.java:395-404,468; tools/src/main/java/org/apache/kafka/tools/consumer/ConsoleConsumerOptions.java:189 — "addParser("describe") / "--transactional-id" / accepts("isolation-level""
- pass 2: ✅ tools/.../TransactionsCommand.java:395,404,468 (describe --transactional-id, list); FeatureCommand describe subparser (tools/FeatureCommand.java:139); ConsoleConsumerOptions.java:189 "isolation-level"

### C0514 · k-transactions · L3 · p
> Look for transaction.version in the describe output. If a partition's read_committed consumers are stuck, kafka-transactions.sh find-hanging --broker-id 1 looks for hanging transactions and abort --topic … --partition … --start-offset … aborts one by hand.
- pass 1: ✅ tools/src/main/java/org/apache/kafka/tools/TransactionsCommand.java:101-126,545-553 — "return "find-hanging" / --broker-id / --start-offset"
- pass 2: ✅ TransactionsCommand.java:545-553 find-hanging "--broker-id"; :101-126 abort "--topic" "--partition" "--start-offset"

### C0515 · k-transactions · L3 · tr
> transactional.id (producer) | null | Enables transactions; stable identity used for fencing | Any consume–transform–produce or multi-partition atomic write; one id per input shard/instance
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:356-360,537-542 — "If no TransactionalId is provided, then the producer is limited to idempotent delivery"
- pass 2: ✅ ProducerConfig.java:537-539 — TRANSACTIONAL_ID_CONFIG default null; :357 enables transactional delivery across sessions (usage advice n/a)

### C0516 · k-transactions · L3 · tr
> transaction.timeout.ms (producer) | 60000 | Coordinator aborts a transaction open longer than this | Lower it so a hung producer unblocks read_committed readers sooner
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:351,532-536 — "remain open before the coordinator proactively aborts it"
- pass 2: ✅ ProducerConfig.java:532-534 — transaction.timeout.ms default 60000; :351 "before the coordinator proactively aborts it"

### C0517 · k-transactions · L3 · tr
> isolation.level (consumer) | read_uncommitted | read_committed → only decided data up to the LSO | Always for downstream of transactional producers
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:349-357 — "consumer.poll() will only return messages up to the last stable offset (LSO)"
- pass 2: ✅ ConsumerConfig.java:357 — DEFAULT_ISOLATION_LEVEL = READ_UNCOMMITTED; :352 read_committed "up to the last stable offset (LSO)"

### C0518 · k-transactions · L3 · tr
> transaction.max.timeout.ms (broker) | 900000 (15 min) | Upper bound a producer may request | Rarely; raise only for long batch jobs
- pass 1: ✅ transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionStateManagerConfig.java:32-33 — "TRANSACTIONS_MAX_TIMEOUT_MS_DEFAULT = (int) TimeUnit.MINUTES.toMillis(15)"
- pass 2: ✅ TransactionStateManagerConfig.java:33-35 — default 15 minutes = 900000; "maximum allowed timeout for transactions"

### C0519 · k-transactions · L3 · tr
> transactional.id.expiration.ms (broker) | 604800000 (7 d) | Idle transactional ids are expired from the coordinator | Many short-lived ids (dynamic ids per job)
- pass 1: ✅ transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionStateManagerConfig.java:38-39 — "TRANSACTIONAL_ID_EXPIRATION_MS_DEFAULT = (int) TimeUnit.DAYS.toMillis(7)"
- pass 2: ✅ TransactionStateManagerConfig.java:39-40 — 7 days = 604800000; "wait without receiving any transaction status updates… before expiring its transactional id"

### C0520 · k-transactions · L3 · tr
> transaction.state.log.replication.factor / .min.isr / .num.partitions | 3 / 2 / 50 | Durability and spread of __transaction_state | Dev clusters with < 3 brokers must lower the RF
- pass 1: ✅ transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/TransactionLogConfig.java:31-45; clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:360 — "REPLICATION_FACTOR_DEFAULT = 3 / MIN_ISR_DEFAULT = 2 / PARTITIONS_DEFAULT = 50"
- pass 2: ✅ TransactionLogConfig.java:32/40/45 — 50 / 3 / 2; ProducerConfig.java:360 "for development you can change this, by adjusting… transaction.state.log.replication.factor"

### C0521 · k-transactions · L3 · summary
> L3🔬 Go deeper: coordinator internals and the TV2 code path
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C0522 · k-transactions · L3 · p
> Finding the coordinator. TransactionStateManager.partitionFor(transactionalId) is Utils.abs(transactionalId.hashCode) % transactionTopicPartitionCount. The client discovers it with FindCoordinator (key type transaction), then calls InitProducerId, which returns the producer id and a fresh epoch.
- pass 1: ✅ core/src/main/scala/kafka/coordinator/transaction/TransactionStateManager.scala:447; clients/src/main/resources/common/message/FindCoordinatorRequest.json:38-39; InitProducerIdResponse.json:38-40 — "(group, transaction, share) / ProducerId / ProducerEpoch"
- pass 2: ✅ core/.../TransactionStateManager.scala:447 — "Utils.abs(transactionalId.hashCode) % transactionTopicPartitionCount"; FindCoordinator key type transaction; InitProducerId returns ProducerId/ProducerEpoch

### C0523 · k-transactions · L3 · p
> States. org.apache.kafka.coordinator.transaction.TransactionState defines EMPTY, ONGOING, PREPARE_COMMIT, PREPARE_ABORT, COMPLETE_COMMIT, COMPLETE_ABORT, DEAD, PREPARE_EPOCH_FENCE. PREPARE_EPOCH_FENCE is the path taken when the coordinator must abort an open transaction and fence its producer epoch — both when a new InitProducerId arrives for the id and when abortTimedOutTransactions aborts a transaction that exceeded its timeout.
- pass 1: ✅ revised after pass-2 finding — C0523 (transactions 🔬 States) — PREPARE_EPOCH_FENCE also used by abortTimedOutTransactions, not only on a new InitProducerId — core/src/main/scala/kafka/coordinator/transaction/TransactionCoordinator.scala:1051 "Some(txnMetadata.prepareFenceProducerEpoch())"
- pass 2: ✅ transaction-coordinator/.../TransactionState.java:39-82 eight states; TransactionCoordinator.scala:284 (InitProducerId) and :1051 (abortTimedOutTransactions) call prepareFenceProducerEpoch()

### C0524 · k-transactions · L3 · p
> Markers. TransactionMarkerChannelManager batches WriteTxnMarkers requests per destination broker. Each marker carries ProducerId, ProducerEpoch, TransactionResult (true = COMMIT), CoordinatorEpoch and, from request v2, a TransactionVersion field.
- pass 1: ✅ clients/src/main/resources/common/message/WriteTxnMarkersRequest.json:28-46; core/src/main/scala/kafka/coordinator/transaction/TransactionMarkerChannelManager.scala — "TransactionVersion … versions 2+"
- pass 2: ✅ TransactionMarkerChannelManager.scala:114,203 (TxnMarkerQueue per destination broker); WriteTxnMarkersRequest.json — ProducerId, ProducerEpoch, TransactionResult "(false = ABORT, true = COMMIT)", CoordinatorEpoch, TransactionVersion "versions": "2+"

### C0525 · k-transactions · L3 · p
> TV2 on the client. In TransactionManager.maybeAddPartition, when isTransactionV2Enabled() is true, the partition is only tracked locally — no AddPartitionsToTxn request is queued. sendOffsetsToTransaction uses txnOffsetCommitHandler directly, skipping AddOffsetsToTxn. The broker side, when a produce arrives for a partition not yet in the transaction, asks the coordinator to add it and, on CONCURRENT_TRANSACTIONS (the previous transaction's markers still being written), retries with add.partitions.to.txn.retry.backoff.ms (default 20) up to add.partitions.to.txn.retry.backoff.max.ms (default 100). The docs note this can show up as higher produce latency while end-to-end transaction latency stays the same.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/producer/internals/TransactionManager.java:433-469; docs/operations/transaction-protocol.md:43-47; transaction-coordinator/src/main/java/org/apache/kafka/coordinator/transaction/AddPartitionsToTxnConfig.java:29-35 — "the client will skip sending AddOffsetsToTxn / RETRY_BACKOFF_MS_DEFAULT = 20"
- pass 2: ✅ TransactionManager.java:466-469 (V2 tracks locally), :433-437 txnOffsetCommitHandler; AddPartitionsToTxnConfig.java:30,35 max 100 / backoff 20; transaction-protocol.md:47 "This can result in higher produce latencies… end to end latency… does not increase"

### C0526 · k-transactions · L3 · p
> Feature levels. TransactionVersion: TV_0 original, TV_1 flexible transactional state records, TV_2 "epoch bump per transaction and optimizations"; LATEST_PRODUCTION = TV_2.
- pass 1: ✅ server-common/src/main/java/org/apache/kafka/server/common/TransactionVersion.java:26-35 — "Version 2 enables epoch bump per transaction and optimizations"
- pass 2: ✅ server-common/.../TransactionVersion.java:26-35 — "Version 2 enables epoch bump per transaction and optimizations"; LATEST_PRODUCTION = TV_2

### C0527 · k-transactions · L3 · p
> Error classes. The docs group the transactional producer's exceptions into: retriable (handled internally), refresh-retriable, abortable (abort the transaction and rewind the consumer), application-recoverable (restart the producer), invalid-configuration, and generic KafkaException.
- pass 1: ❌ fixed in part file — operations/transaction-protocol.md exception categories (grammar fix only)
- pass 2: ✅ docs/design/design.md:233-238 — RetriableException, RefreshRetriableException, AbortableException, ApplicationRecoverableException ("must include restarting the producer"), InvalidConfigurationException, KafkaException

### C0528 · k-transactions · L3 · li
> Transactional records go into the logs immediately; commit/abort markers decide visibility later.
- pass 1: ✅ docs/design/design.md:229 — "transaction marker records are written to indicate the outcome"
- pass 2: ✅ docs/design/design.md:230 — records "marked as being part of the transactions, and then when the transaction commits or aborts, transaction marker records are written"

### C0529 · k-transactions · L3 · li
> The coordinator is the leader of one __transaction_state partition, chosen by hashing transactional.id.
- pass 1: ✅ core/src/main/scala/kafka/coordinator/transaction/TransactionStateManager.scala:447 — "Utils.abs(transactionalId.hashCode) % transactionTopicPartitionCount"
- pass 2: ✅ TransactionStateManager.scala:447 partitionFor(transactionalId) hash % partition count; leader of that __transaction_state partition is the coordinator

### C0530 · k-transactions · L3 · li
> read_committed consumers stop at the LSO; one long-open transaction stalls the partition for them.
- pass 1: ✅ clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java:352-354 — "messages appearing after messages belonging to ongoing transactions will be withheld"
- pass 2: ✅ ConsumerConfig.java:352-354 — "read_committed consumers will not be able to read up to the high watermark when there are in flight transactions"

### C0531 · k-transactions · L3 · li
> Offsets are committed inside the transaction with sendOffsetsToTransaction — that's the exactly-once part.
- pass 1: ✅ docs/design/design.md:203,218 — "write the offset to Kafka in the same transaction as the output topics"
- pass 2: ✅ docs/design/design.md:208 — "we can write the offset to Kafka in the same transaction as the output topics"

### C0532 · k-transactions · L3 · li
> Stable transactional.id + epoch bump = zombie fencing (stale produces get INVALID_PRODUCER_EPOCH; the zombie's next coordinator call gets ProducerFencedException).
- pass 1: ✅ revised after pass-2 finding — C0502 (zombie fencing paragraph) + C0532 bullet + C1478 figure + quiz C1272 — stale-epoch produce now "rejected with INVALID_PRODUCER_EPOCH (InvalidProducerEpochException, app aborts)"; fatal ProducerFencedException only on the zombie's next coordinator call (
- pass 2: ✅ AddPartitionsToTxnManager.java:172-175 (INVALID_PRODUCER_EPOCH to producer); TransactionManager.java coordinator handlers → fatalError(PRODUCER_FENCED)

### C0533 · k-transactions · L3 · li
> TV2 (KIP-890): epoch bumped every transaction, server-side AddPartitions, fewer round trips, no restart needed to adopt it.
- pass 1: ✅ docs/operations/transaction-protocol.md:31-43 — "the producer epoch is bumped on every transaction / A producer will not upgrade mid-transaction"
- pass 2: ✅ docs/operations/transaction-protocol.md:31-43 — epoch bumped every transaction; "single call to add partitions on the server side"; "do not need to be restarted"
