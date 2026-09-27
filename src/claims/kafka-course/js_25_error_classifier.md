# Claims ledger — kafka-course.html — js_25_error_classifier

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1622 · js:25. error classifier · L- · script
> Client retries for you. It extends RetriableException, so the producer's Sender re-enqueues the batch while delivery.timeout.ms (120 s) has time left. With idempotence on (default), a retry can't create a duplicate.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasException.java:22; Sender.java:876-882; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:339-340 — "ensure that exactly one copy of each message is written"
- pass 2: ✅ NotEnoughReplicasException extends RetriableException; Sender.java:876-882 canRetry while delivery timeout not reached; ENABLE_IDEMPOTENCE_DOC "exactly one copy of each message"

### C1623 · js:25. error classifier · L- · script
> Client retries for you. It is an InvalidMetadataException (a RefreshRetriableException), so the client refreshes metadata, finds the new leader and retries. You never see it unless the time budget runs out.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/NotLeaderOrFollowerException.java:27; InvalidMetadataException.java:22; Sender.java:713 — "extends InvalidMetadataException / extends RefreshRetriableException"
- pass 2: ✅ NotLeaderOrFollowerException extends InvalidMetadataException extends RefreshRetriableException; Sender.java:713-730 metadata.requestUpdate on InvalidMetadataException

### C1624 · js:25. error classifier · L- · script
> Your code decides. Technically retriable, but reaching your callback usually means delivery.timeout.ms is spent (or send() already waited max.block.ms for metadata/buffer space). Park the record durably and alert; don't resend blindly (you'd reorder it and bypass idempotence).
- pass 1: ✅ revised after pass-2 finding — C1624: same caveat in error-classifier narration for TimeoutException (part-5.js), and in quiz "why" for C1307 (part-5.quiz.js) — source: KafkaProducer.java:1049-1061
- pass 2: ✅ KafkaProducer.java:1049-1061 — ApiException in doSend "callback.onCompletion(nullMetadata, e)"; BufferExhaustedException extends TimeoutException; delivery.timeout.ms 120000

### C1625 · js:25. error classifier · L- · script
> Your code decides — never retry. Non-retriable: the record will never fit. The producer stays usable; dead-letter or split/compress the payload and fix the producer.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/Callback.java:~35-40 — "Non-Retriable exceptions (fatal, the message will never be sent): RecordTooLargeException"
- pass 2: ✅ RecordTooLargeException extends ApiException, listed under Callback.java:37 "fatal, the message will never be sent"; not a producer-fatal state for non-transactional producer

### C1626 · js:25. error classifier · L- · script
> Fatal. An AuthorizationException: listed as non-retriable in the Callback javadoc and as a fatal error for idempotent/transactional producers. Retrying can't fix ACLs — stop and alert.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/Callback.java:~42-44; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:927-929; TopicAuthorizationException.java:22 — "AuthorizationException are considered fatal errors"
- pass 2: ✅ Callback.java:37-46 lists AuthorizationException as non-retriable; KafkaProducer.java:919-929 fatal for transactional and idempotent producers

### C1627 · js:25. error classifier · L- · script
> Fatal for this producer. A newer instance with the same transactional.id took over. It extends ApplicationRecoverableException; abortTransaction() can't fix it — close() the producer (and usually this instance steps down).
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/ProducerFencedException.java:25; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:917-921 — "the only option left is to call close()"
- pass 2: ✅ ProducerFencedException extends ApplicationRecoverableException; KafkaProducer.java:919-922 "only option left is to call close()"

### C1628 · js:25. error classifier · L- · script
> Your code decides: quarantine + seek. A poison pill thrown from poll(). The consumer is fine, but its position stays on the bad record. Send keyBuffer()/valueBuffer() to a DLQ, then seek(topicPartition(), offset() + 1).
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/CompletedFetch.java:266-268,344 — "please seek past the record to continue consumption"
- pass 2: ✅ CompletedFetch.java:266-289 position stays on bad record; RecordDeserializationException accessors

### C1629 · js:25. error classifier · L- · script
> Your code decides: don't retry the commit. The group rebalanced (usually max.poll.interval.ms exceeded) and your partitions may belong to someone else. The records will be re-delivered — make processing idempotent, and poll more often.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/CommitFailedException.java:20-36 — "assigned the partitions to another member"
- pass 2: ✅ CommitFailedException.java javadoc + default message (max.poll.interval.ms); records re-delivered to new owner

### C1630 · js:25. error classifier · L- · script
> Not an error. Another thread called consumer.wakeup() — the one thread-safe method. Catch it, rethrow only if you weren't closing, and close() in finally.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/KafkaConsumer.java:445-481 — "wakeup(), which can safely be used from an external thread"
- pass 2: ✅ KafkaConsumer.java:445-471 wakeup() only thread-safe method; pattern rethrow if !closed, close() in finally

### C1631 · js:25. error classifier · L- · script
> Pick an exception and press ▶ Classify.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint

### C1632 · js:25. error classifier · L- · script
> poll() #…: RecordDeserializationException at offset 42 — caught, logged, polled again… the position never moved.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/CompletedFetch.java:266-268 — "use the last record to do deserialization again (offset 42 illustrative)"
- pass 2: n/a — simulation narration (offset 42 illustrative); mechanism matches CompletedFetch cachedRecordException

### C1633 · js:25. error classifier · L- · script
> Stuck forever. The fetch keeps the bad record as the next one to return, so catching and re-polling spins on offset 42 while lag grows. Fix: quarantine the bytes, then consumer.seek(e.topicPartition(), e.offset() + 1).
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/CompletedFetch.java:266-268,344 — "please seek past the record to continue consumption"
- pass 2: ✅ CompletedFetch.java:266-268 — "we should use the last record to do deserialization again"; fix via seek(topicPartition(), offset()+1)
