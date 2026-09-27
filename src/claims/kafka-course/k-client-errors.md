# Claims ledger — kafka-course.html — k-client-errors

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C0965 · k-client-errors · L4 · p
> Every Kafka application eventually meets three kinds of trouble: the network or cluster hiccups (a leader moves, an ISR shrinks), the data is bad (a record your code can't parse or can't process), or your own process misbehaves (too slow, killed mid-batch, fenced by a newer instance). Each needs a different reaction, and the most common production bug is treating all three the same way — usually with a try { … } catch (Exception e) { retry(); }.
- pass 1: n/a (pedagogy framing)
- pass 2: n/a — framing/opinion (three kinds of trouble; common-bug claim is advice)

### C0966 · k-client-errors · L4 · p
> A clerk of our city ledger archive is copying entries into ledger book 3 when the branch reports "the book's keeper just changed — try again". A minute later another entry is returned with "this entry is larger than the page allows". Should the clerk treat both the same way? Which one will never succeed no matter how many times it is retried?
- pass 1: n/a (analogy / brain-power question)
- pass 2: n/a — story prompt (maps to NotLeaderOrFollower vs RecordTooLarge, both classified correctly elsewhere)

### C0967 · k-client-errors · L4 · h3
> The producer: retriable vs non-retriable
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0968 · k-client-errors · L4 · p
> The Java client encodes the first split directly in its exception hierarchy. Every exception that extends org.apache.kafka.common.errors.RetriableException is, in the client's own words, "a transient exception that if retried may succeed". NotEnoughReplicasException, TimeoutException and the metadata errors (InvalidMetadataException and friends, such as NotLeaderOrFollowerException) live there. Everything else — RecordTooLargeException, InvalidTopicException, AuthorizationException, UnknownServerException — is non-retriable: the Callback javadoc calls them "fatal, the message will never be sent".
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/RetriableException.java:20; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/Callback.java:~33-51 — "A retriable exception is a transient exception that if retried may succeed / Non-Retriable exceptions (fatal, the message will never be sent)"
- pass 2: ✅ common/errors/RetriableException.java:20 — "a transient exception that if retried may succeed"; NotEnoughReplicas/Timeout extend RetriableException, NotLeaderOrFollower extends InvalidMetadataException; Callback.java:37 — "Non-Retriable exceptions (fatal, the message will never be sent)" lists InvalidTopic, RecordTooLarge, UnknownServer, Authorization

### C0969 · k-client-errors · L4 · p
> Here is the part many people miss: the producer already retries retriable errors for you. retries defaults to Integer.MAX_VALUE, and the real budget is time, not attempts: delivery.timeout.ms (default 120 000 ms = 2 minutes) is "an upper bound on the time to report success or failure after a call to send() returns". It covers the time the record waits in the accumulator, the wait for the broker's acknowledgement and all retries. Between attempts the client backs off exponentially from retry.backoff.ms (100 ms) up to retry.backoff.max.ms (1000 ms). Each single request is bounded by request.timeout.ms (30 000 ms), and the docs say delivery.timeout.ms should be ≥ request.timeout.ms + linger.ms.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:390,406,169-176; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/CommonClientConfigs.java:96-106; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:441-443 — "Integer.MAX_VALUE / 120 * 1000 / initial backoff value and will increase exponentially"
- pass 2: ✅ ProducerConfig.java:390,406,418-444 (retries MAX_VALUE, delivery 120*1000, request 30*1000), CommonClientConfigs.java:99,106 (100/1000); :169 — "An upper bound on the time to report success or failure after a call to send() returns"; :175 "greater than or equal to the sum of request.timeout.ms and linger.ms"

### C0970 · k-client-errors · L4 · p
> So when your callback receives an exception, the client has already done what it could. A TimeoutException in your callback is technically "retriable", but it usually means the whole two-minute budget is gone. The other case is that send() already waited max.block.ms for metadata or buffer space, and doSend() reports that timeout through the callback too. Re-sending it blindly in the callback is how you get unbounded memory growth and records reordered behind newer ones. The right reaction is almost always: record the failure somewhere durable (a local outbox, a log, a metric + alert) and let a human or a controlled process decide.
- pass 1: ✅ revised after pass-2 finding — C0970: callback TimeoutException "usually" means delivery.timeout.ms spent; added the max.block.ms-in-send() case reported via callback (part-5.html) — source: kafka-4.3.1-src/clients/.../producer/KafkaProducer.java:1049-1061 (ApiException → callback + FutureF
- pass 2: ✅ KafkaProducer.java:1049-1061 — timeout from waitOnMetadata/buffer (ApiException) reported "callback.onCompletion(nullMetadata, e)"; ProducerConfig.java:406 delivery.timeout.ms 120*1000 (advice part n/a)

### C0971 · k-client-errors · L4 · tr
> Config | Default (4.3.1) | What it does | Use it when
- pass 1: n/a (table header)
- pass 2: n/a — table header

### C0972 · k-client-errors · L4 · tr
> delivery.timeout.ms | 120000 | Total time budget per record from send() returning to success/failure, including retries | The one knob to tune how long to keep trying; raise it to ride out longer leader elections, lower it for latency-sensitive paths that have their own fallback
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:406,169-176 — "define(DELIVERY_TIMEOUT_MS_CONFIG, Type.INT, 120 * 1000 (use-when column = advice)"
- pass 2: ✅ ProducerConfig.java:406 — define(DELIVERY_TIMEOUT_MS_CONFIG, Type.INT, 120 * 1000; :170 "limits the total time that a record will be delayed prior to sending … time allowed for retriable send failures" (use-when column is advice)

### C0973 · k-client-errors · L4 · tr
> retries | 2147483647 | Max retry attempts for transient errors | Leave unset — the docs say to "prefer to leave this config unset and instead use delivery.timeout.ms". 0 disables idempotence (or throws if idempotence is explicitly on)
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:390,285,601-606 — "prefer to leave this config unset and instead use delivery.timeout.ms"
- pass 2: ✅ ProducerConfig.java:390 Integer.MAX_VALUE; :285 — "Users should generally prefer to leave this config unset and instead use delivery.timeout.ms"; :601-606 throws ConfigException if idempotence explicitly set, else "Idempotence will be disabled"

### C0974 · k-client-errors · L4 · tr
> request.timeout.ms | 30000 | How long the client waits for one request's response before retrying or failing | Rarely changed; keep it well below delivery.timeout.ms
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:441-443 — "REQUEST_TIMEOUT_MS_CONFIG, Type.INT, 30 * 1000"
- pass 2: ✅ ProducerConfig.java:441-443 — REQUEST_TIMEOUT_MS_CONFIG, Type.INT, 30 * 1000 (advice column n/a)

### C0975 · k-client-errors · L4 · tr
> max.block.ms | 60000 | How long send() may block waiting for metadata and buffer space before failing | Lower it if a blocking send() would stall a request thread
- pass 1: ❌ fixed in part file — ProducerConfig MAX_BLOCK_MS default 60000; KafkaProducer.java:1049-1061 timeout delivered as ApiException to callback/future
- pass 2: ✅ ProducerConfig.java:435-437 60 * 1000; :206 — "For send() this timeout bounds the total time waiting for both metadata fetch and buffer allocation"

### C0976 · k-client-errors · L4 · tr
> retry.backoff.ms / retry.backoff.max.ms | 100 / 1000 | Exponential back-off between retries | Rarely changed
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/CommonClientConfigs.java:96-106 — "DEFAULT_RETRY_BACKOFF_MS = 100L / DEFAULT_RETRY_BACKOFF_MAX_MS = 1000L"
- pass 2: ✅ CommonClientConfigs.java:99,106 — DEFAULT_RETRY_BACKOFF_MS = 100L; DEFAULT_RETRY_BACKOFF_MAX_MS = 1000L (exponential backoff per KIP-580)

### C0977 · k-client-errors · L4 · tr
> enable.idempotence | true | Broker de-duplicates retried batches; ordering preserved for up to 5 in-flight requests | Keep it on; it is what makes automatic retries safe (Part II, idempotent producer)
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:527-529,271-276 — "if enable.idempotence is set to true, ordering will be preserved"
- pass 2: ✅ ProducerConfig.java:527-529 default true; :339-341 — "exactly one copy of each message is written … max.in.flight … less than or equal to 5 (with message ordering preserved"

### C0978 · k-client-errors · L4 · p
> Some errors are thrown from send() itself, not passed to the callback: a SerializationException ("if the key or value are not valid objects given the configured serializers"), an InterruptException, and IllegalStateException after close(). If you only look at the callback, those bypass your error handling. Wrap the call, not just the result. Note also that send() can block for up to max.block.ms (metadata fetch plus buffer allocation). That timeout arrives as a TimeoutException in the callback/future, after your thread has already waited.
- pass 1: ❌ fixed in part file — KafkaProducer.send javadoc "@throws SerializationException", "InterruptException", "IllegalStateException"; TimeoutException via callback (KafkaProducer.java:1049-1061)
- pass 2: ✅ KafkaProducer.java:951-954 — "@throws SerializationException If the key or value are not valid objects given the configured serializers"; IllegalStateException "when send is invoked after producer has been closed"; doSend: waitOnMetadata TimeoutException is an ApiException → callback + FutureFailure

### C0979 · k-client-errors · L4 · p
> Since Kafka 4.1, calling producer.flush() inside a callback throws a KafkaException: "KafkaProducer.flush() invocation inside a callback is not permitted because it may lead to deadlock." Callbacks run on the producer's I/O thread; the javadoc asks you to keep them fast and hand expensive work to your own Executor.
- pass 1: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:175; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:1218-1219 — "flush() invocation inside a callback is not permitted because it may lead to deadlock"
- pass 2: ✅ KafkaProducer.java:1219 — "KafkaProducer.flush() invocation inside a callback is not permitted because it may lead to deadlock."; upgrade.md:175 under 4.1.0 notable changes; KafkaProducer.java:942-944 Executor advice

### C0980 · k-client-errors · L4 · pre
> // Kafka clients 4.3.1 — a producer callback that classifies instead of blindly retrying producer.send(record, (metadata, e) -> { if (e == null) return; // acknowledged if (e instanceof RetriableException) { // the client already retried until delivery.timeout.ms ran out outbox.park(record, e); // durable, replayable later } else if (e instanceof RecordTooLargeException) { deadLetters.record(record, e); // will never fit: don't retry } else { fatal.set(e); // auth, unknown server error…: stop, alert } });
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/RecordTooLargeException.java:26; RetriableException.java — "code sample; class names verified (outbox/deadLetters/fatal are placeholders)"
- pass 2: ✅ code consistent with 4.3.1 hierarchy (RetriableException, RecordTooLargeException extends ApiException non-retriable); outbox/deadLetters/fatal are illustrative app objects

### C0981 · k-client-errors · L4 · p
> The transactional and idempotent producer adds one more category. ProducerFencedException (a newer instance with the same transactional.id took over — Part III) extends ApplicationRecoverableException, whose javadoc says the error "is fatal to the producer, and the application needs to restart the producer after handling the error". The send() javadoc lists the transactional errors that abortTransaction() cannot fix — ProducerFencedException, OutOfOrderSequenceException, UnsupportedVersionException, AuthorizationException — and says "the only option left is to call close()". Any other failed transactional send surfaces at commitTransaction(); you abort and carry on.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/ProducerFencedException.java:25; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:911-923 — "then the only option left is to call close()"
- pass 2: ✅ ProducerFencedException extends ApplicationRecoverableException; ApplicationRecoverableException.java:18 — "is fatal to the producer, and the application needs to restart the producer after handling the error"; KafkaProducer.java:919-922 — "the only option left is to call close()"; :913-915 abort and continue

### C0982 · k-client-errors · L4 · h3
> The consumer: poison pills, lost commits, wake-ups
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0983 · k-client-errors · L4 · p
> A poison pill is a record your consumer can't deserialize — someone wrote JSON into an Avro topic, or a producer shipped a new schema early. The consumer deserializes inside poll(), so the failure surfaces as a RecordDeserializationException thrown from poll(). Its message ends with the key sentence: "If needed, please seek past the record to continue consumption." The position does not move past the bad record by itself, so a loop that just logs and polls again hits the same record forever. Since Kafka 3.8 (KIP-1036) the exception carries everything you need to quarantine it: topicPartition(), offset(), origin() (KEY or VALUE), keyBuffer(), valueBuffer(), headers(), timestamp().
- pass 1: ✅ revised after pass-2 finding — C0983: "Since Kafka 3.9" → "Since Kafka 3.8 (KIP-1036)" for RecordDeserializationException accessors (part-5.html; notes) — source: Apache Kafka 3.8.0 release announcement / KIP-1036 (pass-2 verifier)
- pass 2: ✅ clients/.../internals/CompletedFetch.java:344 — "If needed, please seek past the record to continue consumption."; RecordDeserializationException.java:88-116 origin()/keyBuffer()/valueBuffer()/headers()/timestamp(); KIP-1036 in AK 3.8.0 release announcement

### C0984 · k-client-errors · L4 · pre
> // Kafka clients 4.3.1 — quarantine a poison pill and move on while (running) { ConsumerRecords<String, Order> records; try { records = consumer.poll(Duration.ofMillis(500)); } catch (RecordDeserializationException e) { dlqProducer.send(new ProducerRecord<ByteBuffer, ByteBuffer>( "orders-dlq", null, e.keyBuffer(), e.valueBuffer(), e.headers())); consumer.seek(e.topicPartition(), e.offset() + 1); // step over it continue; } for (ConsumerRecord<String, Order> r : records) process(r); // must be idempotent consumer.commitSync(); }
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/RecordDeserializationException.java; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerRecord.java:109 — "ProducerRecord(String topic, Integer partition, K key, V value, Iterable<Header> headers)"
- pass 2: ✅ API calls match 4.3.1: RecordDeserializationException.keyBuffer()/valueBuffer()/headers()/topicPartition()/offset(); ProducerRecord(topic, partition, key, value, headers) ctor exists; seek(tp, offset+1). (dlqProducer must use ByteBufferSerializer — implied)

### C0985 · k-client-errors · L4 · p
> The second classic is CommitFailedException. Its javadoc explains it exactly: the commit failed "with an unrecoverable error. This can happen when a group rebalance completes before the commit could be successfully applied" — the partitions may already belong to another member. The default message even names the usual cause: the time between poll() calls exceeded max.poll.interval.ms (default 300 000 ms), so fix it "either by increasing max.poll.interval.ms or by reducing the maximum size of batches returned in poll() with max.poll.records" (default 500). Do not retry the commit: the records you processed will be re-delivered to the new owner. That is why consumers must be idempotent.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/CommitFailedException.java:20-36; ConsumerConfig.java:94,627-629 — "increasing max.poll.interval.ms or by reducing the maximum size of batches"
- pass 2: ✅ CommitFailedException.java:21-23 — "with an unrecoverable error. This can happen when a group rebalance completes before the commit could be successfully applied"; default msg "either by increasing max.poll.interval.ms or by reducing the maximum size of batches returned in poll() with max.poll.records"; ConsumerConfig.java:629 300000, :94 DEFAULT_MAX_POLL_RECORDS = 500

### C0986 · k-client-errors · L4 · p
> RebalanceInProgressException from commitSync() is milder: the javadoc suggests completing the rebalance with poll() and reconsidering the commit afterwards, with the caveat that your assignment may have changed.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/KafkaConsumer.java:930-935 — "complete the rebalance by calling poll(Duration) and commit can be reconsidered afterwards"
- pass 2: ✅ KafkaConsumer.java:929-933 — "complete the rebalance by calling poll(Duration) and commit can be reconsidered afterwards … assigned partitions may have changed"

### C0987 · k-client-errors · L4 · p
> Finally, WakeupException is not an error at all — it's the only thread-safe way to interrupt a consumer. KafkaConsumer "is NOT thread-safe", except for wakeup(), which makes the blocked call throw WakeupException. The javadoc pattern: set a closed flag, call consumer.wakeup() from the shutdown hook, catch WakeupException in the poll loop, rethrow it only if you weren't closing, and close() in finally. Thread interrupts also work, but the javadoc discourages them "since they may cause a clean shutdown of the consumer to be aborted".
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/KafkaConsumer.java:440-497 — "The Kafka consumer is NOT thread-safe / may cause a clean shutdown of the consumer to be aborted"
- pass 2: ✅ KafkaConsumer.java:441-495 — "The Kafka consumer is NOT thread-safe"; wakeup() exception; pattern closed flag/if (!closed.get()) throw e/finally close(); "since they may cause a clean shutdown of the consumer to be aborted"

### C0988 · k-client-errors · L4 · figcaption
> Classification follows the 4.3.1 exception hierarchy and javadocs (Callback, KafkaProducer.send, KafkaConsumer). Real handling depends on your delivery guarantees; offsets 40–44 in the loop demo are illustrative.
- pass 1: n/a (figcaption; offsets labelled illustrative)
- pass 2: n/a — figure caption; sources named are correct

### C0989 · k-client-errors · L4 · h3
> Dead-letter queues and idempotent consumers
- pass 1: n/a (heading)
- pass 2: n/a — heading

### C0990 · k-client-errors · L4 · p
> Kafka's plain consumer has no built-in dead-letter queue (DLQ): a consumer group tracks one offset per partition, not a per-record fate. A DLQ in a consumer-group app is a pattern you build — a separate topic you produce the failed record to, with headers describing why, before you commit past it. Kafka Connect has one built in for sink connectors (errors.deadletterqueue.topic.name with errors.tolerance=all), and since 4.2 Kafka Streams has one too (errors.dead.letter.queue.topic.name, KIP-1034). Spring for Apache Kafka gives you one for listeners (next chapter). Share groups (Part III) take a different route: they count delivery attempts per record, and a rejected record gets no further delivery attempts.
- pass 1: ✅ kafka-4.3.1-src/docs/kafka-connect/user-guide.md:402,432-435; kafka-4.3.1-src/docs/streams/upgrade-guide.md:148; kafka-4.3.1-src/docs/design/design.md:271 — "errors.dead.letter.queue.topic.name / preventing further delivery attempts for that record"
- pass 2: ✅ kafka-connect/user-guide.md:402,432-435 errors.deadletterqueue.topic.name + errors.tolerance=all; streams/upgrade-guide.md:148 (section "Streams API changes in 4.2.0") — "errors.dead.letter.queue.topic.name … KIP-1034"; AcknowledgeType.java:33 REJECT — "do not release it for another delivery attempt"

### C0991 · k-client-errors · L4 · p
> A good DLQ record is replayable: the original bytes (not a toString()), the original topic/partition/offset, the exception class and message, a timestamp, and the consumer group. Someone will fix the bug and want to push those records back through — design for that day.
- pass 1: n/a (opinion / design advice)
- pass 2: n/a — design advice

### C0992 · k-client-errors · L4 · p
> Because the consumer gives you at-least-once delivery (the KafkaConsumer javadoc: "each record will likely be delivered one time but in failure cases could be duplicated"), processing must tolerate duplicates. Common designs: a natural idempotent write (upsert by business key), a processed-ids table updated in the same database transaction as the effect, or storing the consumed offset alongside the result and seeking to it on assignment (the javadoc's "storing offsets outside Kafka" pattern). For Kafka-to-Kafka pipelines, transactions (Part III) make the read-process-write cycle exactly-once.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/KafkaConsumer.java:254-256,327 — "each record will likely be delivered one time but in failure cases could be duplicated"
- pass 2: ✅ KafkaConsumer.java:254-255 — "each record will likely be delivered one time but in failure cases could be duplicated"; :327 "Storing Offsets Outside Kafka"

### C0993 · k-client-errors · L4 · div
> RetryGive me one more go. The database was just restarting, I'll get through this time.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue

### C0994 · k-client-errors · L4 · div
> OrderingFine — but while you sit on record 17, records 18, 19 and 20 with the same key wait behind you. If you step aside to a retry topic, 18 overtakes you and the account balance goes negative.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue (illustrative record numbers)

### C0995 · k-client-errors · L4 · div
> RetrySo I either block the whole partition or give up order?
- pass 1: n/a (fireside dialogue)
- pass 2: n/a — dialogue

### C0996 · k-client-errors · L4 · div
> OrderingFor that key, yes. Block briefly for transient faults, send true poison to a DLQ fast, and if the key's history matters, park all later records of that key too.
- pass 1: n/a (fireside dialogue / advice)
- pass 2: n/a — dialogue/advice

### C0997 · k-client-errors · L4 · p
> That dialogue is the central trade-off of this Part. Inside a partition, order is only preserved if you process strictly in sequence. Any retry that lets later records go first (a retry topic, a DLQ you replay later, a share group redelivery) breaks per-key order. Blocking retries keep order but stall every key in the partition and risk max.poll.interval.ms. Decide per topic which one you can afford.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "you lose Kafka's ordering guarantees (rest = reasoning from per-partition order)"
- pass 2: n/a — trade-off analysis/advice (mechanism consistent with per-partition ordering and max.poll.interval.ms)

### C0998 · k-client-errors · L4 · div
> Should I set retries=0 so I can handle failures myself?
- pass 1: n/a (question)
- pass 2: n/a — question

### C0999 · k-client-errors · L4 · div
> Almost never. retries=0 disables idempotence (the config check logs "Idempotence will be disabled because retries is set to 0"), and your own resend creates a new record that the broker can't de-duplicate. Tune delivery.timeout.ms instead.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:601-606 — "Idempotence will be disabled because {} is set to 0."
- pass 2: ✅ ProducerConfig.java:606 — log.info("Idempotence will be disabled because {} is set to 0.", RETRIES_CONFIG)

### C1000 · k-client-errors · L4 · div
> Is enable.auto.commit=true at-least-once?
- pass 1: n/a (question)
- pass 2: n/a — question

### C1001 · k-client-errors · L4 · div
> Only if you finish processing every record from a poll() before the next poll() or close(). Hand records to another thread and the auto-commit can run ahead of what you've processed — the javadoc says this "results in missing records".
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/KafkaConsumer.java:257-261 — "it is possible for the committed offset to get ahead of the consumed position, which results in missing records"
- pass 2: ✅ KafkaConsumer.java:257-260 — "you must consume all data returned from each call to poll(Duration) before any subsequent calls … results in missing records"

### C1002 · k-client-errors · L4 · div
> Can I catch CommitFailedException and just commit again?
- pass 1: n/a (question)
- pass 2: n/a — question

### C1003 · k-client-errors · L4 · div
> No. The partitions may already be someone else's. Let the records be re-processed by the new owner and make processing idempotent.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/CommitFailedException.java:20-24 — "some of the partitions may have already been assigned to another member"
- pass 2: ✅ CommitFailedException.java:23-24 — "cannot generally be retried because some of the partitions may have already been assigned to another member"

### C1004 · k-client-errors · L4 · p
> Make a poison pill on a local 4.3 cluster and watch a consumer choke on it:
- pass 1: n/a (exercise prompt)
- pass 2: n/a — exercise prompt

### C1005 · k-client-errors · L4 · pre
> bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic ints --partitions 1 bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic ints > hello bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ints --from-beginning \ --formatter-property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
- pass 1: ✅ kafka-4.3.1-src/tools/src/main/java/org/apache/kafka/tools/consumer/ConsoleConsumerOptions.java:139,163; DefaultMessageFormatter.java:97 — "formatter-property / from-beginning / value.deserializer"
- pass 2: ✅ ConsoleConsumerOptions.java:139 --formatter-property; DefaultMessageFormatter.java:97 reads value.deserializer; kafka-topics --create/--partitions valid

### C1006 · k-client-errors · L4 · p
> What happens? Which flag of kafka-console-consumer.sh keeps it running, and why is that flag the console-tool version of a DLQ-less "skip"?
- pass 1: n/a (exercise question)
- pass 2: n/a — exercise question

### C1007 · k-client-errors · L4 · p
> "hello" is 5 bytes, and IntegerDeserializer throws SerializationException: Size of data received by IntegerDeserializer is not 4, so the tool stops. Add --skip-message-on-error ("If there is an error when processing a message, skip it instead of halt") and it logs the error and continues. Skipping without recording the record anywhere is data loss by design. Acceptable for a console tool, not for an application: there you would quarantine the bytes first. (The console consumer reads raw bytes and deserializes in its formatter, so the error surfaces there. In your own consumer with a value deserializer configured, the same bytes cause a RecordDeserializationException from poll().)
- pass 1: ✅ ConsoleConsumerOptions.java:173-174; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/serialization/IntegerDeserializer.java:30; ConsoleConsumer.java:120-128 — "skip it instead of halt / Size of data received by IntegerDeserializer is not 4"
- pass 2: ✅ IntegerDeserializer.java:30 — "Size of data received by IntegerDeserializer is not 4"; ConsoleConsumerOptions.java:173 — "If there is an error when processing a message, skip it instead of halt."; ConsoleConsumer.java:124-125 logs and skips

### C1008 · k-client-errors · L4 · tr
> Symptom | Likely cause | Fix
- pass 1: n/a (table header)
- pass 2: n/a (table header)

### C1009 · k-client-errors · L4 · tr
> Consumer logs the same deserialization error forever; lag grows on one partition | Poison pill; the loop catches and re-polls without seeking | seek(tp, offset+1) after quarantining the bytes; or an error-handling deserializer
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/CompletedFetch.java:266-268,344 — "please seek past the record to continue consumption"
- pass 2: ✅ CompletedFetch.java:266-289 — re-deserializes lastRecord while cachedRecordException set; fix via seek (consistent with C0983)

### C1010 · k-client-errors · L4 · tr
> CommitFailedException, duplicates downstream | Processing a batch took longer than max.poll.interval.ms; member evicted | Lower max.poll.records, speed up processing, or raise max.poll.interval.ms; make processing idempotent
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/CommitFailedException.java:30-36 — "increasing max.poll.interval.ms or by reducing ... max.poll.records"
- pass 2: ✅ CommitFailedException default message (max.poll.interval.ms / max.poll.records)

### C1011 · k-client-errors · L4 · tr
> Callbacks report TimeoutException after ~2 minutes | delivery.timeout.ms exhausted (under-min-ISR partition, unreachable leader) | Fix the cluster side (Part IV troubleshooting); park records durably; alert
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:169-174; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/NotEnoughReplicasException.java:22 — "upper bound on the time to report success or failure (causes = troubleshooting heuristic)"
- pass 2: ✅ ProducerConfig.java:406 120 s default; NotEnoughReplicas/leader errors retriable until delivery timeout (fix column is advice)

### C1012 · k-client-errors · L4 · tr
> Throughput collapses after adding logging to the callback | Slow work on the producer I/O thread | Hand work to your own executor; keep callbacks trivial
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/Callback.java:20-21; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:~938-941 — "callback will generally execute in the background I/O thread so it should be fast"
- pass 2: ✅ KafkaProducer.java:942-945 — "callbacks will generally execute in the I/O thread … should be reasonably fast or they will delay the sending"

### C1013 · k-client-errors · L4 · tr
> Shutdown hangs or loses the last commit | Thread interrupt instead of wakeup(); no close() in finally | Use the javadoc wakeup() pattern
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/KafkaConsumer.java:440-497 — "we discourage their use since they may cause a clean shutdown of the consumer to be aborted"
- pass 2: ✅ KafkaConsumer.java:494-496 — interrupts "may cause a clean shutdown of the consumer to be aborted"; pattern uses close() in finally

### C1014 · k-client-errors · L4 · summary
> L4🔬 Go deeper: where the client decides "retriable"
- pass 1: n/a (heading)
- pass 2: n/a — summary heading

### C1015 · k-client-errors · L4 · p
> Broker errors travel as numeric error codes in each response. The client maps each code to an exception class through org.apache.kafka.common.protocol.Errors, and whether that class extends RetriableException decides the producer's path. In the producer's Sender, a batch that fails with a retriable error goes back into the RecordAccumulator if its delivery deadline hasn't passed; otherwise the batch completes exceptionally and every record's callback fires with that exception. Metadata-type errors extend InvalidMetadataException, which also triggers a metadata refresh before the retry — that's how a leader move becomes invisible to you.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/internals/Sender.java:692-699,713,876-882 — "!batch.hasReachedDeliveryTimeout(...) && ... instanceof RetriableException"
- pass 2: ✅ Sender.java:876-882 canRetry — !hasReachedDeliveryTimeout && attempts<retries && instanceof RetriableException; :699 reenqueueBatch; :713-730 InvalidMetadataException → metadata.requestUpdate

### C1016 · k-client-errors · L4 · p
> The 4.x clients also have intermediate classes in the hierarchy: RefreshRetriableException ("requiring a refresh … before retrying"), ApplicationRecoverableException (restart the producer) as abstract bases, plus the concrete TransactionAbortableException (abort the transaction and continue). Classify on these base classes, not on long lists of leaf exceptions, and your handler keeps working when new leaves are added.
- pass 1: ✅ revised after pass-2 finding — C1016: RefreshRetriableException/ApplicationRecoverableException described as abstract bases, TransactionAbortableException as concrete (part-5.html) — source: kafka-4.3.1-src/clients/.../common/errors/RefreshRetriableException.java, ApplicationRecoverableExce
- pass 2: ✅ clients/.../errors/RefreshRetriableException.java:20-24 — "requiring a refresh ... before retrying"; ApplicationRecoverableException.java:20 "needs to restart the producer"; TransactionAbortableException concrete

### C1017 · k-client-errors · L4 · p
> On the consumer side, deserialization happens in CompletedFetch (ShareCompletedFetch for share consumers): newRecordDeserializationException(…) builds the exception with the raw key/value ByteBuffers and headers, and the fetch keeps the bad record as the next one to return, so the same failure repeats until you seek.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/CompletedFetch.java:266-268,336-344; ShareCompletedFetch.java imports RecordDeserializationException — "use the last record to do deserialization again"
- pass 2: ✅ CompletedFetch.java:321-344 and ShareCompletedFetch.java:330-345 newRecordDeserializationException(origin, …, record, e, headers); CompletedFetch.java:266-268 "we should use the last record to do deserialization again"

### C1018 · k-client-errors · L4 · li
> Producer: RetriableExceptions are retried automatically until delivery.timeout.ms (120 s); leave retries at its default and tune the timeout.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java:390,406,285 — "prefer to leave this config unset and instead use delivery.timeout.ms"
- pass 2: ✅ ProducerConfig.java:285,406 + Sender.canRetry

### C1019 · k-client-errors · L4 · li
> An exception in your callback means the client is done trying. Park or dead-letter the record instead of re-sending it blindly.
- pass 1: n/a (advice)
- pass 2: n/a — advice (see C0970 caveat)

### C1020 · k-client-errors · L4 · li
> ProducerFencedException, OutOfOrderSequenceException, UnsupportedVersionException and AuthorizationException are fatal for a transactional producer: close() it. Don't flush() inside callbacks (throws since 4.1).
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java:917-923; kafka-4.3.1-src/docs/getting-started/upgrade.md:175 — "the only option left is to call close() / prohibits its use inside a callback"
- pass 2: ✅ KafkaProducer.java:919-922 (close() only option); :1219 flush-in-callback KafkaException, upgrade.md 4.1.0

### C1021 · k-client-errors · L4 · li
> Poison pill → RecordDeserializationException from poll(); quarantine keyBuffer()/valueBuffer(), then seek(offset + 1).
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/CompletedFetch.java:344 — "please seek past the record to continue consumption"
- pass 2: ✅ CompletedFetch.java:344 — "please seek past the record to continue consumption"; RecordDeserializationException.keyBuffer()/valueBuffer()

### C1022 · k-client-errors · L4 · li
> CommitFailedException = you were rebalanced out (usually slow processing). Don't retry the commit; make processing idempotent.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/CommitFailedException.java:20-36 — "group has already rebalanced"
- pass 2: ✅ CommitFailedException.java:20-24 and default message

### C1023 · k-client-errors · L4 · li
> wakeup() + WakeupException is the thread-safe shutdown path.
- pass 1: ✅ kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/KafkaConsumer.java:445-447 — "wakeup(), which can safely be used from an external thread"
- pass 2: ✅ KafkaConsumer.java:445-447 — "The only exception to this rule is wakeup(), which can safely be used from an external thread"

### C1024 · k-client-errors · L4 · li
> Retries that let later records overtake break per-key order. Blocking retries keep order but stall the partition.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "you lose Kafka's ordering guarantees for that topic"
- pass 2: n/a — analysis/advice
