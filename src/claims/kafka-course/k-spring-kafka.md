# Claims ledger — kafka-course.html — k-spring-kafka

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1025 · k-spring-kafka · L4 · p
> Spring for Apache Kafka wraps the clients you just learned in a listener container: it runs the poll loop, calls your @KafkaListener method, commits offsets, catches exceptions, retries, dead-letters and shuts down cleanly. The patterns from the last chapter are all there, already built. The catch is that its defaults were chosen for "works out of the box", not for "correct for your topic". This chapter is pinned to Spring for Apache Kafka 4.1.1 (2026-08-20). That release builds against kafka-clients 4.2.1 and Spring Framework 7.0.9 (from its gradle/libs.versions.toml), and its quick tour lists Java 17 as the minimum.
- pass 1: ✅ spring-kafka-4.1.1/gradle/libs.versions.toml:9,20; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/quick-tour.adoc:~63 — "kafkaVersion = "4.2.1" / springVersion = "7.0.9" / Minimum Java version: 17"
- pass 2: ✅ gradle.properties:1 "version=4.1.1"; libs.versions.toml:9,20 "springVersion = 7.0.9" / "kafkaVersion = 4.2.1"; quick-tour.adoc:63 "Minimum Java version: 17"; github.com/spring-projects/spring-kafka/releases/tag/v4.1.1 "released this 20 Aug"

### C1026 · k-spring-kafka · L4 · p
> Your listener throws on a record because the payment gateway is down. With no error handler configured at all, how many times will Spring try that record before giving up — and what happens to it then?
- pass 1: n/a (brain-power question)
- pass 2: n/a (question; answer checked in C1038)

### C1027 · k-spring-kafka · L4 · h3
> The container: threads, partitions, commits
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1028 · k-spring-kafka · L4 · p
> A ConcurrentKafkaListenerContainerFactory builds a ConcurrentMessageListenerContainer per @KafkaListener. With concurrency n it creates n KafkaMessageListenerContainers, each with its own KafkaConsumer and thread — consumers are not thread-safe, so one consumer per thread. Kafka's group protocol then spreads the partitions. In the archive: n clerks from the same team, each holding whole ledger books. More clerks than books means idle clerks. The docs warn about a subtler version: with three topics of five partitions each and concurrency=15, you "see only five active consumers" under the classic RangeAssignor default. Size concurrency to your partition count (summed over instances) and check the assignor.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:161-170 — "container.setConcurrency(3) creates three KafkaMessageListenerContainer instances / only five active consumers"
- pass 2: ✅ kafka/receiving-messages/message-listener-container.adoc:162,169-170 — "setConcurrency(3) creates three KafkaMessageListenerContainer instances"; "you see only five active consumers"; "default ... is the RangeAssignor"; message-listeners.adoc:81 "Consumer object is not thread-safe" (archive analogy n/a)

### C1029 · k-spring-kafka · L4 · p
> Spring sets enable.auto.commit to false unless you set it (since 2.3), and commits for you according to the container's AckMode. The default is BATCH.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:208-210 — "The default AckMode is BATCH. / sets enable.auto.commit to false"
- pass 2: ✅ message-listener-container.adoc:208-209 — "Starting with version 2.3, the framework sets enable.auto.commit to false unless explicitly set"; "The default AckMode is BATCH"

### C1030 · k-spring-kafka · L4 · tr
> AckMode | When the container commits | Use it when
- pass 1: n/a (table header)
- pass 2: n/a (table header)

### C1031 · k-spring-kafka · L4 · tr
> BATCH (default) | After all records returned by one poll() have been processed | General purpose; fewer commits
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:217 — "Commit the offset when all the records returned by the poll() have been processed"
- pass 2: ✅ message-listener-container.adoc:217 — "BATCH: Commit the offset when all the records returned by the poll() have been processed" (use-it column is advice)

### C1032 · k-spring-kafka · L4 · tr
> RECORD | After the listener returns for each record | Slow, expensive records where re-processing a whole batch after a crash hurts; suggested for retry topics
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:216; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:14 — "Commit the offset when the listener returns / RECORD is suggested"
- pass 2: ✅ message-listener-container.adoc:216 — "RECORD: Commit the offset when the listener returns after processing the record"; retrytopic/how-the-pattern-works.adoc:14 "RECORD is suggested"

### C1033 · k-spring-kafka · L4 · tr
> TIME / COUNT / COUNT_TIME | After the poll's records are processed, if ackTime elapsed and/or ackCount records were received | High-volume topics where commit frequency matters
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:218-220 — "as long as the ackTime since the last commit has been exceeded"
- pass 2: ✅ message-listener-container.adoc:218-220 — "as long as the ackTime since the last commit has been exceeded"; COUNT_TIME "if either condition is true"

### C1034 · k-spring-kafka · L4 · tr
> MANUAL | Listener calls Acknowledgment.acknowledge(); then BATCH semantics | Hand-off to async work that completes later
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:221-222 — "The message listener is responsible to acknowledge() ... same semantics as BATCH"
- pass 2: ✅ message-listener-container.adoc:221-222 — "MANUAL: The message listener is responsible to acknowledge() ... same semantics as BATCH"

### C1035 · k-spring-kafka · L4 · tr
> MANUAL_IMMEDIATE | Immediately when acknowledge() is called | You need the commit to happen right at that point
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:223 — "Commit the offset immediately when the Acknowledgment.acknowledge() method is called"
- pass 2: ✅ message-listener-container.adoc:223 — "Commit the offset immediately when the Acknowledgment.acknowledge() method is called"

### C1036 · k-spring-kafka · L4 · p
> New in 4.1: @KafkaListener(ackMode = "MANUAL") overrides the factory's mode per listener, so you no longer need a second factory bean just for that. With transactions, offsets are sent to the transaction instead, and semantics equal RECORD or BATCH depending on the listener type.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/whats-new.adoc (x41-kafka-listener-ack-mode); spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:225 — "override the container factory's default acknowledgment mode"
- pass 2: ✅ whats-new.adoc:13 "ackMode attribute ... without creating separate container factory beans"; message-listener-container.adoc:225 "equivalent to RECORD or BATCH, depending on the listener type"

### C1037 · k-spring-kafka · L4 · h3
> Blocking retries: DefaultErrorHandler
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1038 · k-spring-kafka · L4 · p
> When the listener throws, the container calls a CommonErrorHandler, by default the DefaultErrorHandler. It remembers the failed record, seeks the partition back so the record (and everything after it) is redelivered by the next poll(), and waits according to a Spring BackOff. The default is FixedBackOff(0, 9): ten deliveries with no delay, then the record is logged at ERROR and skipped. That answers the brain-power question. Without a recoverer, the default ends by skipping and logging, which is quiet data loss.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:139,167; spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/listener/SeekUtils.java:57,63; KafkaMessageListenerContainer.java:1097 — "new FixedBackOff(0, DEFAULT_MAX_FAILURES - 1) / after ten failures, the failed record is logged"
- pass 2: ✅ KafkaMessageListenerContainer.java:1097 "common = new DefaultErrorHandler()"; SeekUtils.java:63 "new FixedBackOff(0, DEFAULT_MAX_FAILURES - 1)"; annotation-error-handling.adoc:138 "after ten failures, the failed record is logged (at the ERROR level)" ("quiet data loss" is commentary)

### C1039 · k-spring-kafka · L4 · p
> Two built-in guard rails matter:
- pass 1: n/a (lead-in)
- pass 2: n/a (lead-in)

### C1040 · k-spring-kafka · L4 · li
> Fatal exceptions skip retries. DeserializationException, MessageConversionException, ConversionException, MethodArgumentResolutionException, NoSuchMethodException and ClassCastException go straight to the recoverer on the first failure. Add your own with addNotRetryableExceptions(…), for example validation errors.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~220-235 — "exceptions that are considered fatal, by default, are"
- pass 2: ✅ annotation-error-handling.adoc:218-226,240 — lists DeserializationException ... ClassCastException; "the recoverer is invoked on the first failure"; handler.addNotRetryableExceptions(IllegalArgumentException.class)

### C1041 · k-spring-kafka · L4 · li
> Only RuntimeExceptions are handled. The docs warn that anything extending Error "bypass[es] the error handler entirely, causing the consumer to terminate immediately", and your health check may still say UP.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~243-250 — "bypass the error handler entirely, causing the consumer to terminate immediately"
- pass 2: ✅ annotation-error-handling.adoc:246-248 — "only processes exceptions that inherit from RuntimeException"; "bypass the error handler entirely, causing the consumer to terminate immediately"; "may report healthy status"

### C1042 · k-spring-kafka · L4 · p
> Blocking retries keep order: nothing behind the failing record in that partition is processed until it succeeds or is recovered. The price is a stalled partition and the poll-interval clock. The container sleeps between attempts, but the consumer must still poll() within max.poll.interval.ms (300 s by default) or it is kicked out of the group. For long back-offs, the docs point to ContainerPausingBackOffHandler, which "pauses the listener container until the back off time passes", for delays "longer than the max.poll.interval.ms".
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:119-123 — "pauses the listener container until the back off time passes"
- pass 2: ✅ annotation-error-handling.adoc:121-123 "default handler simply suspends the thread"; ContainerPausingBackOffHandler "pauses the listener container until the back off time passes" ... "longer than the max.poll.interval.ms"; ConsumerConfig.java:627-629 default 300000

### C1043 · k-spring-kafka · L4 · h3
> Dead letters: DeadLetterPublishingRecoverer
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1044 · k-spring-kafka · L4 · p
> Give the error handler a recoverer and exhausted records get somewhere to go. DeadLetterPublishingRecoverer publishes the failed record with a KafkaTemplate. By default it goes to <originalTopic>-dlt and the same partition, so the DLT "must have at least as many partitions as the original topic". (The suffix became -dlt in 3.3. Older apps used .DLT and must opt in to keep it.) Each dead letter carries headers such as KafkaHeaders.DLT_EXCEPTION_FQCN, DLT_EXCEPTION_MESSAGE, DLT_EXCEPTION_STACKTRACE, DLT_ORIGINAL_TOPIC, DLT_ORIGINAL_PARTITION, DLT_ORIGINAL_OFFSET and DLT_ORIGINAL_CONSUMER_GROUP, which is everything the last chapter said a replayable DLQ record needs.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:874-876,~897-911; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/appendix/change-history.adoc:193-196 — "<originalTopic>-dlt ... must have at least as many partitions"
- pass 2: ✅ annotation-error-handling.adoc:875-876,900-912 — "<originalTopic>-dlt ... same partition"; "must have at least as many partitions as the original topic"; headers listed; change-history.adoc:187-196 (3.3) "standardized to use the -dlt suffix ... retain .DLT ... opt-in"

### C1045 · k-spring-kafka · L4 · pre
> // Spring for Apache Kafka 4.1.1 (Spring Framework 7) import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries; import org.springframework.kafka.listener.*; import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory; @Configuration class KafkaErrorConfig { @Bean DeadLetterPublishingRecoverer recoverer(KafkaOperations<?, ?> template) { return new DeadLetterPublishingRecoverer(template); // -> orders-dlt, same partition } @Bean DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) { ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3); backOff.setInitialInterval(1_000L); backOff.setMultiplier(2.0); backOff.setMaxInterval(10_000L); // 1 s, 2 s, 4 s, then DLT DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff); handler.addNotRetryableExceptions(IllegalArgumentException.class); // validation: no retry return handler; } @Bean ConcurrentKafkaListenerContainerFactory<String, Order> kafkaListenerContainerFactory( ConsumerFactory<String, Order> consumerFactory, DefaultErrorHandler errorHandler) { var factory = new ConcurrentKafkaListenerContainerFactory<String, Order>(); factory.setConsumerFactory(consumerFactory); factory.setConcurrency(3); // <= partitions per instance factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD); factory.setCommonErrorHandler(errorHandler); return factory; } }
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/support/ExponentialBackOffWithMaxRetries.java:39,53-65; listener/DefaultErrorHandler.java:91; listener/DeadLetterPublishingRecoverer.java:121 — "signatures verified in source"
- pass 2: ✅ compile-plausible vs source: DeadLetterPublishingRecoverer.java:121 (KafkaOperations<?,?>) ctor, default resolver :76 topic+"-dlt" same partition; ExponentialBackOffWithMaxRetries.java:39,53-66 (int maxRetries, setters); ExceptionClassifier.java:151 addNotRetryableExceptions(Class...); ConcurrentKafkaListenerContainerFactory.setConcurrency(Integer), AbstractKafkaListenerContainerFactory setConsumerFactory/setCommonErrorHandler; 3 retries 1s/2s/4s correct. Minor: imports for @Configuration/@Bean, KafkaOperations, ConsumerFactory omitted (snippet)

### C1046 · k-spring-kafka · L4 · h3
> Poison pills: ErrorHandlingDeserializer
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1047 · k-spring-kafka · L4 · p
> Remember that deserialization happens inside poll(), before Spring sees anything, so "Spring has no way to handle the problem". The fix is to wrap your real deserializer in ErrorHandlingDeserializer. If the delegate fails, it returns null and puts a DeserializationException (with the raw bytes) in a header. The container sees the header, skips your listener and calls the error handler. Because DeserializationException is on the fatal list, the record goes straight to the recoverer. Paired with DeadLetterPublishingRecoverer, the DLT record gets the original bytes back as its value. Configure the delegate with ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS (spring.deserializer.value.delegate.class). In 4.x, the Jackson 3 JacksonJsonDeserializer replaces the deprecated Jackson 2 JsonDeserializer.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/serdes.adoc:522-545; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~920 — "Spring has no way to handle the problem, because it occurs before the poll() returns"
- pass 2: ✅ kafka/serdes.adoc:524,527,539 — "Spring has no way to handle the problem"; "returns a null value and a DeserializationException in a header that contains the cause and the raw bytes"; ErrorHandlingDeserializer.java:64 "spring.deserializer.value.delegate.class"; annotation-error-handling.adoc:924 DLPR "will restore the record value()"; JsonDeserializer.java:68 @Deprecated(forRemoval=true, since="4.0"); change-history.adoc:112 JacksonJsonSerializer/Deserializer replaces JsonSerializer/Deserializer

### C1048 · k-spring-kafka · L4 · pre
> props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class); props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class); props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.Order"); props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.example");
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/support/serializer/ErrorHandlingDeserializer.java:64; JacksonJsonDeserializer.java:83,88 — "spring.deserializer.value.delegate.class / VALUE_DEFAULT_TYPE / TRUSTED_PACKAGES"
- pass 2: ✅ ErrorHandlingDeserializer.java:64 VALUE_DESERIALIZER_CLASS; JacksonJsonDeserializer.java:83,88 VALUE_DEFAULT_TYPE, TRUSTED_PACKAGES — same pattern as serdes.adoc:546-552

### C1049 · k-spring-kafka · L4 · h3
> Non-blocking retries: @RetryableTopic and @DltHandler
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1050 · k-spring-kafka · L4 · p
> The alternative to stalling a partition is to move the failing record aside. With @RetryableTopic on a @KafkaListener, a failure forwards the record to a retry topic with a back-off timestamp. The retry topic's consumer pauses that partition until the record is due, then processes it again. After the last attempt, the record goes to the DLT, handled by a @DltHandler method (or a default consumer "which only logs"). The framework creates the topics and containers for you.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:4-8; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/retry-config.adoc:54 — "forwarded to a retry topic with a back off timestamp / only logs the consumption"
- pass 2: ✅ retrytopic/how-the-pattern-works.adoc:4-6,9 — "forwarded to a retry topic with a back off timestamp"; "pauses the consumption for that topic's partition"; retry-config.adoc:54 "default consumer is created which only logs"

### C1051 · k-spring-kafka · L4 · pre
> @RetryableTopic(attempts = "4", backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 10000)) @KafkaListener(topics = "payments", groupId = "payments-svc") void onPayment(Payment p) { gateway.charge(p); } // may throw @DltHandler void onDeadPayment(Payment p, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) { alerts.raise(p, topic); } // creates payments-retry-1000, payments-retry-2000, payments-retry-4000, payments-dlt
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:8; spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/annotation/BackOff.java:86,116,146; KafkaHeaders.java:85 — "main-topic-retry-1000, main-topic-retry-2000, main-topic-retry-4000"
- pass 2: ✅ how-the-pattern-works.adoc:8 — "1000ms with a multiplier of 2 and 4 max attempts ... main-topic-retry-1000, -retry-2000, -retry-4000 and main-topic-dlt"; annotation/BackOff.java:86,116,146 delay/maxDelay/multiplier attributes exist

### C1052 · k-spring-kafka · L4 · p
> Defaults to know: attempts = "3" (the number of attempts made before the message goes to the DLT), a fixed back-off of 1000 ms, retry suffix -retry, DLT suffix -dlt, numPartitions = "1" and replicationFactor = "-1" (broker default) for auto-created topics, and SameIntervalTopicReuseStrategy.SINGLE_TOPIC. So a fixed back-off uses one …-retry topic. (4.1 changed RetryTopicConfigurationBuilder's default to SINGLE_TOPIC too, to match the annotation.) In 4.0 the @Backoff from Spring Retry became Spring Kafka's own @BackOff, because Spring Retry was dropped in favour of Spring Framework 7's core retry support.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/annotation/RetryableTopic.java:59,67,116,126,200; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/whats-new.adoc (x41-retry-topic-builder-default); spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/appendix/change-history.adoc:126-160 — "attempts() default "3" / SINGLE_TOPIC"
- pass 2: ✅ RetryableTopic.java:59,116,126,168,175,200 — attempts "3", numPartitions "1", replicationFactor "-1" (broker default), -retry/-dlt (RetryTopicConstants:32,37), SINGLE_TOPIC; RetryableTopic.java:62 "fixed backOff of DEFAULT_DELAY ms" (1000); whats-new.adoc "changed from MULTIPLE_TOPICS to SINGLE_TOPIC"; change-history.adoc:127-138 Spring Retry removed, @Backoff moved to @BackOff

### C1053 · k-spring-kafka · L4 · p
> Two hard limits from the docs: non-blocking retries are not supported with batch listeners, and they cannot combine with container transactions. And the big caveat, in the docs' own capitals: "IMPORTANT: By using this strategy you lose Kafka's ordering guarantees for that topic." Record 18 overtakes the retried record 17. That is exactly what the figure's toggle shows.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic.adoc:12,14; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "Non-blocking retries are not supported with Batch Listeners"
- pass 2: ✅ retrytopic.adoc:12,14 — "not supported with Batch Listeners"; "cannot combine with Container Transactions"; how-the-pattern-works.adoc:12 "you lose Kafka's ordering guarantees for that topic"

### C1054 · k-spring-kafka · L4 · figcaption
> Simplified: one partition, three attempts (blocking: FixedBackOff(1000L, 2L); non-blocking: @RetryableTopic defaults — 3 attempts, fixed 1 s, single -retry topic). Timing is compressed and illustrative; real retry topics pause the partition until the record is due.
- pass 1: n/a (figcaption; labelled simplified/illustrative (defaults match RetryableTopic.java))
- pass 2: ✅ annotation-error-handling.adoc:167 FixedBackOff(1000L, 2L) = "3 delivery attempts"; @RetryableTopic defaults attempts 3, 1000 ms, SINGLE_TOPIC (RetryableTopic.java:59,200); rest labelled illustrative

### C1055 · k-spring-kafka · L4 · div
> DefaultErrorHandlerI keep your ledger in order. Nothing leaves the desk until the bad page is sorted.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a (dialogue/analogy)

### C1056 · k-spring-kafka · L4 · div
> @RetryableTopicAnd the whole desk waits while the payment gateway is down for ten minutes. I put the bad page in a tray and keep the queue moving.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a (dialogue/analogy)

### C1057 · k-spring-kafka · L4 · div
> DefaultErrorHandlerThen the refund for that same account gets processed before the charge. Enjoy explaining that.
- pass 1: n/a (fireside dialogue)
- pass 2: n/a (dialogue/analogy)

### C1058 · k-spring-kafka · L4 · div
> @RetryableTopicFair. So: me for independent events (emails, notifications, idempotent upserts), you for per-key state machines. And both of us can work together — the docs let you retry some exceptions blocking first, then hand over to me.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/retry-topic-combine-blocking.adoc:3-5 — "use both blocking and non-blocking retries in conjunction"
- pass 2: ✅ retrytopic/retry-topic-combine-blocking.adoc:5-7 — blocking retries for chosen exceptions before non-blocking (usage advice n/a)

### C1059 · k-spring-kafka · L4 · p
> That combination is real: extend RetryTopicConfigurationSupport and override configureBlockingRetries(…) to retry chosen exceptions in place first (the docs' example: a database hiccup that "would likely trigger errors on the next records as well"), with non-blocking retries after that. There's a global list of fatal exceptions for retry topics too; those skip retries and go straight to the DLT.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/retry-topic-combine-blocking.adoc:5-8; retrytopic/features.adoc:121 — "would likely trigger errors on the next records as well"
- pass 2: ✅ retry-topic-combine-blocking.adoc:5,7 — "would likely trigger errors on the next records as well"; "override the configureBlockingRetries method ... extends RetryTopicConfigurationSupport"; retrytopic/features.adoc:121 "global list of fatal exceptions ... sent to the DLT without any retries"

### C1060 · k-spring-kafka · L4 · h3
> Transactions
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1061 · k-spring-kafka · L4 · p
> Give the DefaultKafkaProducerFactory a transactionIdPrefix and it keeps a cache of transactional producers (transactional.id = prefix + n). The prefix "must be unique per instance". With Spring Boot, setting spring.kafka.producer.transaction-id-prefix auto-configures a KafkaTransactionManager and wires it into the listener container. The container then starts a transaction before calling your listener, your KafkaTemplate sends join it, and on success it sends the consumed offsets with sendOffsetsToTransaction() before committing. That's the consume-transform-produce pattern from Part III, done for you. On an exception it rolls back and repositions the consumer. Repeated failures go to the DefaultAfterRollbackProcessor, the transactional twin of DefaultErrorHandler. Spring Kafka 3.0+ only supports EOSMode.V2 (KIP-447 fencing). The docs are careful to scope exactly-once: the read → process → write sequence completes once; "the read and process have at least once semantics". Side effects outside Kafka (an HTTP call, an email) can still happen twice.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/transactions.adoc:15-28; kafka/exactly-once.adoc:4-20 — "transactionIdPrefix must be unique per instance / read and process have at least once semantics"
- pass 2: ✅ kafka/transactions.adoc:17-22,28 — "transactional.id ... is transactionIdPrefix + n"; "must be unique per instance"; Boot auto-configures KafkaTransactionManager; exactly-once.adoc:5-16 "sendOffsetsToTransaction()", "rolled back and the consumer is repositioned", "read and process have at least once semantics", "3.0 and later only supports EOSMode.V2"; DefaultAfterRollbackProcessor per annotation-error-handling.adoc:143

### C1062 · k-spring-kafka · L4 · p
> For a database write plus a Kafka send, annotate the method @Transactional with your DataSourceTransactionManager. The KafkaTemplate synchronizes with it, and the DB commits first, then Kafka. That is two transactions, not one atomic one. If the second commit fails, the docs say the exception reaches the caller and you "should take remedial action … to compensate". For outside-world side effects, pair transactions with idempotent consumers.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/transactions.adoc:55-72 — "Applications should take remedial action, if necessary, to compensate"
- pass 2: ✅ transactions.adoc:53-72 — DataSourceTransactionManager; "database transaction will commit followed by the Kafka transaction"; "exception will be thrown to the caller"; "take remedial action, if necessary, to compensate"

### C1063 · k-spring-kafka · L4 · h3
> Sending: KafkaTemplate returns a CompletableFuture
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1064 · k-spring-kafka · L4 · p
> Every send(…) returns CompletableFuture<SendResult<K, V>>. Attach whenComplete((result, ex) -> …) for non-blocking handling. The Throwable can be cast to KafkaProducerException, whose getFailedProducerRecord() gives you the record. If you must block, the docs recommend get() "with a timeout". By default the template has a LoggingProducerListener that only logs errors, so a send you never check fails silently apart from a log line. The producer-side rules from the last chapter still apply underneath: keep completion handlers short, and move slow work to your own executor (e.g. whenCompleteAsync(…, executor)).
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/sending-messages.adoc:160-182; spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/core/KafkaProducerException.java:53 — "using the method with a timeout is recommended"
- pass 2: ✅ kafka/sending-messages.adoc:27-37,160,171,179,181 — send returns CompletableFuture<SendResult<K,V>>; whenComplete; "Throwable can be cast to a KafkaProducerException"; KafkaProducerException.java:53 getFailedProducerRecord(); "using the method with a timeout is recommended"; "LoggingProducerListener, which logs errors" (executor advice is guidance)

### C1065 · k-spring-kafka · L4 · h3
> Share consumers (Kafka queues) in Spring
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1066 · k-spring-kafka · L4 · p
> Spring Kafka 4.0 added early-access share consumers. The docs say that since 4.1, "backed by Apache Kafka 4.2, share consumers are promoted to full production support". You use a DefaultShareConsumerFactory, a ShareKafkaListenerContainerFactory and a plain @KafkaListener(containerFactory = "shareKafkaListenerContainerFactory"). Acknowledgement follows ContainerProperties.ShareAckMode (new in 4.1, replacing a boolean):
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/kafka-queues.adoc:4-5; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/appendix/change-history.adoc:93-98; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/whats-new.adoc (x41-share-ack-mode) — "share consumers are promoted to full production support"
- pass 2: ✅ kafka/kafka-queues.adoc:4-5,14,236-260 — "As of Spring for Apache Kafka 4.1, backed by Apache Kafka 4.2, share consumers are promoted to full production support"; change-history.adoc:95 4.0 "early access support"; whats-new.adoc:20 boolean replaced by ShareAckMode enum

### C1067 · k-spring-kafka · L4 · li
> EXPLICIT (default): the container sends ACCEPT when your listener returns. On an exception, a ShareConsumerRecordRecoverer decides ACCEPT, RELEASE or REJECT, and the default is REJECTING.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/kafka-queues.adoc:449-455 — "On listener exceptions, the ShareConsumerRecordRecoverer decides the outcome ... default: REJECT"
- pass 2: ✅ kafka-queues.adoc:451-453 — "container sends ACCEPT ... ShareConsumerRecordRecoverer decides the outcome (ACCEPT, RELEASE, or REJECT; default: REJECT)"; ContainerProperties.java:352 default EXPLICIT; ShareConsumerRecordRecoverer.java:30 "REJECTING (log and REJECT, default)"

### C1068 · k-spring-kafka · L4 · li
> MANUAL: your listener takes a ShareAcknowledgment and must call exactly one of acknowledge(), release(), reject(). Unacknowledged records block the next poll. renew() (KIP-1222) extends the lock for slow work.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/kafka-queues.adoc:479-484; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/whats-new.adoc (x41-share-acknowledgment-renew) — "must call exactly once with a terminal operation / Subsequent polls are blocked"
- pass 2: ✅ kafka-queues.adoc:481-483 — "must call exactly once with a terminal operation (acknowledge(), release(), or reject()). Subsequent polls are blocked"; whats-new.adoc renew() "extend the acquisition lock ... (KIP-1222, Kafka 4.2)"

### C1069 · k-spring-kafka · L4 · li
> IMPLICIT: maps to share.acknowledgement.mode=implicit; every acquired record is accepted "regardless of processing outcome".
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/kafka-queues.adoc:526-530 — "automatically accepts all acquired records regardless of processing outcome"
- pass 2: ✅ kafka-queues.adoc:528-530 — "automatically accepts all acquired records regardless of processing outcome"; "maps directly to setting share.acknowledgement.mode=implicit"

### C1070 · k-spring-kafka · L4 · p
> Since 4.1, undeserializable records are caught at poll level and REJECTed, so a poison pill can't kill the thread. Current limits: no batch listeners and no message converters for share consumers. The broker's delivery-count limit (the share.delivery.count.limit group config, broker default 5 via group.share.delivery.count.limit) is your built-in "poison pill" cutoff, and there is no ordering promise to lose.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/whats-new.adoc (x41-share-consumer-error-handling); spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/kafka-queues.adoc:980-981; kafka-4.3.1-src/docs/getting-started/upgrade.md:52; kafka-4.3.1-src/group-coordinator/src/main/java/org/apache/kafka/coordinator/group/modern/share/ShareGroupConfig.java:48-49 — "undeserializable records are REJECTed / SHARE_GROUP_DELIVERY_COUNT_LIMIT_DEFAULT = 5"
- pass 2: ✅ whats-new.adoc (x41-share-consumer-error-handling) "undeserializable records are REJECTed and the next poll proceeds"; kafka-queues.adoc:980-981 "No Message Converters", "No Batch Listeners"; Kafka GroupConfig.java:66 "share.delivery.count.limit"; ShareGroupConfig.java:48-49 group.share.delivery.count.limit default 5

### C1071 · k-spring-kafka · L4 · h3
> Observability and testing
- pass 1: n/a (heading)
- pass 2: n/a (heading)

### C1072 · k-spring-kafka · L4 · p
> If Micrometer is on the classpath with a single MeterRegistry, the listener container and KafkaTemplate record timers automatically: spring.kafka.listener with tags name, result (success/failure) and exception, and spring.kafka.template for sends. Set observationEnabled to true on ContainerProperties and KafkaTemplate to switch to Micrometer Observation (see the Micrometer Tracing reference for tracing). MicrometerProducerListener/MicrometerConsumerListener bind the clients' own metrics (KafkaClientMetrics). One 4.1.1 behaviour change: KafkaAdmin now remembers a failed cluster-id lookup for clusterIdRetryInterval (default 5 minutes). Previously, an observation-enabled container could block on every record while the cluster id was unavailable.
- pass 1: ✅ revised after pass-2 finding — C1072 spring micrometer → kafka/micrometer.adoc:123 "Starting with version 4.1.1, a failed describeCluster attempt is remembered"
- pass 2: ✅ spring-kafka-docs .../kafka/micrometer.adoc:7-16,32,101,123 — "spring.kafka.listener" tags name/result/exception; "Set observationEnabled to true"; whats-new.adoc:105 "clusterIdRetryInterval ... defaults to 5 minutes"

### C1073 · k-spring-kafka · L4 · p
> For tests, @EmbeddedKafka starts an in-JVM broker. Since 4.0 that's only EmbeddedKafkaKraftBroker, KRaft only; the ZooKeeper variant and the JUnit 4 EmbeddedKafkaRule are gone. The docs recommend a single broker across tests with a different topic per test, via Spring's context caching. For tests closer to production (real broker image, real networking), the Testcontainers Kafka module's org.testcontainers.kafka.KafkaContainer runs the apache/kafka or apache/kafka-native images, and getBootstrapServers() gives you the address to point your factories at.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/testing.adoc:29-35,~250; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/appendix/change-history.adoc:11-33; https://java.testcontainers.org/modules/kafka/ — "only the EmbeddedKafkaKraftBroker implementation is now available / KafkaContainer works with apache/kafka"
- pass 2: ✅ testing.adoc:32,84,248-249 — "only the EmbeddedKafkaKraftBroker implementation is now available"; "no longer supports JUnit 4"; "single broker instance ... different topic for each test"; change-history.adoc:20,27 ZK broker and EmbeddedKafkaRule removed; java.testcontainers.org/modules/kafka "org.testcontainers.kafka.KafkaContainer supports apache/kafka and apache/kafka-native", kafka.getBootstrapServers()

### C1074 · k-spring-kafka · L4 · p
> Best-practices checklist for a Spring Kafka 4.1 service:
- pass 1: n/a (checklist lead-in)
- pass 2: n/a (lead-in)

### C1075 · k-spring-kafka · L4 · li
> Pick an ordering stance per topic. Per-key state (balances, order status) → blocking DefaultErrorHandler + DLT. Independent events → @RetryableTopic. Truly queue-like work → share consumers.
- pass 1: n/a (opinion / recommendation)
- pass 2: n/a (advice; mechanisms per C1042/C1053)

### C1076 · k-spring-kafka · L4 · li
> Always configure a recoverer. The default ends with "log and skip". Use DeadLetterPublishingRecoverer and create the -dlt topic with ≥ the source's partition count.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:139,874-876 — "after ten failures, the failed record is logged / must have at least as many partitions"
- pass 2: ✅ annotation-error-handling.adoc:138,876 — default logs after ten failures; DLT "must have at least as many partitions"

### C1077 · k-spring-kafka · L4 · li
> Wrap deserializers in ErrorHandlingDeserializer so poison pills become dead letters, not crash loops.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/serdes.adoc:525-529 — "container's ErrorHandler is called with the failed ConsumerRecord"
- pass 2: ✅ serdes.adoc:524-527; annotation-error-handling.adoc:221 DeserializationException fatal → recoverer on first failure

### C1078 · k-spring-kafka · L4 · li
> Classify exceptions: addNotRetryableExceptions(…) for validation/business errors. Retry only what can heal.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~232-240 — "add more exception types to the not-retryable category"
- pass 2: n/a (advice; addNotRetryableExceptions exists, ExceptionClassifier.java:151)

### C1079 · k-spring-kafka · L4 · li
> Bound blocking back-off well below max.poll.interval.ms, or use ContainerPausingBackOffHandler.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:121-122 — "useful when the delays are longer than the max.poll.interval.ms"
- pass 2: ✅ annotation-error-handling.adoc:122-123 ContainerPausingBackOffHandler "useful when the delays are longer than the max.poll.interval.ms" (rest is advice)

### C1080 · k-spring-kafka · L4 · li
> Keep concurrency ≤ partitions, and watch the assignor for multi-topic listeners.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:166-170 — "only five active consumers ... other 10 consumers being idle"
- pass 2: n/a (advice; see C1028)

### C1081 · k-spring-kafka · L4 · li
> Make listeners idempotent. Every path here is at-least-once, except Kafka-to-Kafka writes in a container transaction.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/exactly-once.adoc:14-16 — "The read and process have at least once semantics"
- pass 2: ✅ exactly-once.adoc:11-14 — EOS covers the read→process→write sequence; "read and process have at least once semantics" (advice part n/a)

### C1082 · k-spring-kafka · L4 · li
> Handle the CompletableFuture of every KafkaTemplate.send that matters, or use transactions.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/sending-messages.adoc:160 — "LoggingProducerListener, which logs errors and does nothing"
- pass 2: n/a (advice; see C1064)

### C1083 · k-spring-kafka · L4 · li
> Unique transactionIdPrefix per instance when using transactions. Don't combine them with retry topics.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/transactions.adoc:26; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic.adoc:14 — "must be unique per instance / cannot combine with Container Transactions"
- pass 2: ✅ transactions.adoc:22 "must be unique per instance"; retrytopic.adoc:14 "cannot combine with Container Transactions"

### C1084 · k-spring-kafka · L4 · li
> Monitor the DLT (size, age), spring.kafka.listener failure timers and consumer lag. A DLT nobody reads is a slower way of losing the data.
- pass 1: n/a (operational advice)
- pass 2: n/a (advice; spring.kafka.listener timer per micrometer.adoc:11)

### C1085 · k-spring-kafka · L4 · li
> Test with @EmbeddedKafka for speed, and Testcontainers for real-broker behaviour.
- pass 1: n/a (advice)
- pass 2: n/a (advice)

### C1086 · k-spring-kafka · L4 · tr
> Anti-pattern | What goes wrong | Do instead
- pass 1: n/a (table header)
- pass 2: n/a (table header)

### C1087 · k-spring-kafka · L4 · tr
> No recoverer on DefaultErrorHandler | After 10 fast attempts the record is logged and skipped. Silent loss. | DeadLetterPublishingRecoverer + DLT alerting
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:139 — "after ten failures, the failed record is logged (at the ERROR level)"
- pass 2: ✅ SeekUtils.java:63 FixedBackOff(0, 9) = 10 attempts; annotation-error-handling.adoc:138 "after ten failures, the failed record is logged"

### C1088 · k-spring-kafka · L4 · tr
> FixedBackOff.UNLIMITED_ATTEMPTS for everything | One poison record stalls the partition forever | Bounded attempts; unlimited only for known-transient exceptions via setBackOffFunction
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:141,~285-290 — "FixedBackOff.UNLIMITED_ATTEMPTS causes (effectively) infinite retries / setBackOffFunction"
- pass 2: ✅ annotation-error-handling.adoc:141,280-284 — "FixedBackOff.UNLIMITED_ATTEMPTS causes (effectively) infinite retries"; handler.setBackOffFunction((record, ex) -> ...) (stall is consequence of blocking retries)

### C1089 · k-spring-kafka · L4 · tr
> Long blocking back-off (minutes) | Exceeds max.poll.interval.ms; rebalance; duplicates | ContainerPausingBackOffHandler or retry topics
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:121-122; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/clients/consumer/CommitFailedException.java:30 — "delays are longer than the max.poll.interval.ms"
- pass 2: ✅ annotation-error-handling.adoc:121-123; max.poll.interval.ms exceeded → member leaves group (ConsumerConfig default 300000); fixes are ContainerPausingBackOffHandler or retry topics

### C1090 · k-spring-kafka · L4 · tr
> @RetryableTopic on per-key state updates | Later records overtake the retried one: lost ordering | Blocking retries, or park the whole key
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "you lose Kafka's ordering guarantees for that topic"
- pass 2: ✅ how-the-pattern-works.adoc:12 "you lose Kafka's ordering guarantees for that topic" (fix column is advice)

### C1091 · k-spring-kafka · L4 · tr
> Catching and swallowing exceptions in the listener | Container commits; error handler never sees it | Let it throw (or throw a classified exception)
- pass 1: n/a (reasoning / advice)
- pass 2: ✅ annotation-error-handling.adoc:110 — only an exception thrown to the container reaches error handling; a swallowed exception is a normal return, so the offset is committed per AckMode

### C1092 · k-spring-kafka · L4 · tr
> Throwing an Error subclass for business failures | Bypasses the error handler; consumer terminates | Extend RuntimeException
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~243-250 — "ensure that it is extended from RuntimeException"
- pass 2: ✅ annotation-error-handling.adoc:246-247 — "Exceptions inheriting from Error bypass the error handler entirely, causing the consumer to terminate"

### C1093 · k-spring-kafka · L4 · tr
> DLT with fewer partitions than the source | Default resolver targets the same partition number, which doesn't exist | Match partition counts or supply a destination resolver
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:874-876 — "must have at least as many partitions as the original topic"
- pass 2: ✅ DeadLetterPublishingRecoverer.java:76 default resolver uses cr.partition(); annotation-error-handling.adoc:876, :867 optional BiFunction destination resolver

### C1094 · k-spring-kafka · L4 · tr
> concurrency > partitions | Idle threads, false sense of scale | Add partitions (plan it) or use share consumers
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:166-170 — "other 10 consumers being idle"
- pass 2: ✅ message-listener-container.adoc:169 idle consumers beyond partitions (fix column advice)

### C1095 · k-spring-kafka · L4 · tr
> Ignoring the KafkaTemplate future | Failed sends only show up in the logs | whenComplete handling or transactions
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/sending-messages.adoc:160 — "LoggingProducerListener, which logs errors"
- pass 2: ✅ sending-messages.adoc:160 default LoggingProducerListener "logs errors and does nothing when the send is successful"

### C1096 · k-spring-kafka · L4 · tr
> Same transactionIdPrefix on every replica | Instances fence each other (ProducerFencedException) | Unique prefix per instance
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/transactions.adoc:26; kafka-4.3.1-src/clients/src/main/java/org/apache/kafka/common/errors/ProducerFencedException.java — "transactionIdPrefix must be unique per instance"
- pass 2: ✅ transactions.adoc:22,110 "must be unique on each instance"; a duplicate transactional.id is fenced by the broker on initTransactions (ProducerFencedException, per Kafka transactional semantics)

### C1097 · k-spring-kafka · L4 · p
> You deployed the payments listener above. After a gateway outage, you want to inspect what landed in the DLT, including the headers Spring added. Which command shows them on a local 4.3 cluster, and which header tells you the original offset?
- pass 1: n/a (exercise prompt)
- pass 2: n/a (question)

### C1098 · k-spring-kafka · L4 · pre
> bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic payments-dlt --from-beginning \ --formatter-property print.headers=true --formatter-property print.offset=true
- pass 1: ✅ kafka-4.3.1-src/tools/src/main/java/org/apache/kafka/tools/consumer/DefaultMessageFormatter.java:65,77 — "print.offset / print.headers"
- pass 2: ✅ Kafka ConsoleConsumerOptions.java:139 "formatter-property"; DefaultMessageFormatter.java:65,77 print.offset, print.headers; DLT name payments-dlt per RetryTopicConstants.java:37 "-dlt"

### C1099 · k-spring-kafka · L4 · p
> print.headers and print.offset are DefaultMessageFormatter properties, passed with --formatter-property (4.2 deprecated the old --property flag in its favour). The original position is in the kafka_dlt-original-offset header, whose constant is KafkaHeaders.DLT_ORIGINAL_OFFSET. Next to it are …-original-topic, …-original-partition and the kafka_dlt-exception-* headers.
- pass 1: ✅ kafka-4.3.1-src/docs/getting-started/upgrade.md:~96; spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/support/KafkaHeaders.java:32,210 — "--property ... deprecated in favor of --formatter-property / dlt-original-offset"
- pass 2: ✅ kafka-4.3.1 docs/getting-started/upgrade.md:81,96 (Notable changes in 4.2.0) "--property ... is deprecated in favor of --formatter-property"; KafkaHeaders.java:32,210 PREFIX "kafka_" + "dlt-original-offset"

### C1100 · k-spring-kafka · L4 · tr
> Symptom | Likely cause | Fix
- pass 1: n/a (table header)
- pass 2: n/a (table header)

### C1101 · k-spring-kafka · L4 · tr
> Listener stops consuming, app reports healthy | An Error (not RuntimeException) escaped the listener | Throw runtime exceptions; add a container-stopped event alert
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~243-247; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/events.adoc:21 — "applications may report healthy status despite having terminated consumers"
- pass 2: ✅ annotation-error-handling.adoc:246-248 Error terminates consumer, "may report healthy status"; kafka/events.adoc:21,25 ConsumerStoppedEvent/ContainerStoppedEvent exist for alerting

### C1102 · k-spring-kafka · L4 · tr
> Same record retried ~10 times fast, then vanishes | Default FixedBackOff(0, 9), default logging recoverer | Configure back-off and DeadLetterPublishingRecoverer
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:139,167 — "instead of the default configuration (FixedBackOff(0L, 9))"
- pass 2: ✅ SeekUtils.java:63; annotation-error-handling.adoc:138

### C1103 · k-spring-kafka · L4 · tr
> DLT publish fails: partition doesn't exist | DLT has fewer partitions than the source | Recreate DLT with enough partitions or custom resolver
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:874-876 — "must have at least as many partitions"
- pass 2: ✅ annotation-error-handling.adoc:875-876 same partition, DLT "must have at least as many partitions"

### C1104 · k-spring-kafka · L4 · tr
> Retries never happen for ClassCastException | It's on the default fatal list | Intended; fix the type mapping, or reclassify deliberately
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~220-230 — "ClassCastException"
- pass 2: ✅ annotation-error-handling.adoc:226 ClassCastException in default fatal list; ExceptionClassifier.java:66

### C1105 · k-spring-kafka · L4 · tr
> Out-of-order updates after enabling @RetryableTopic | Non-blocking retries give up ordering | Blocking retries for that topic
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "you lose Kafka's ordering guarantees"
- pass 2: ✅ how-the-pattern-works.adoc:12

### C1106 · k-spring-kafka · L4 · summary
> L4🔬 Go deeper: what the container actually does on an exception
- pass 1: n/a (heading)
- pass 2: n/a (summary heading)

### C1107 · k-spring-kafka · L4 · p
> In KafkaMessageListenerContainer's consumer loop, a listener exception is wrapped in ListenerExecutionFailedException and passed to CommonErrorHandler.handleRemaining(…) for record listeners. The DefaultErrorHandler (a FailedRecordProcessor) uses a FailedRecordTracker (a per-thread map of topic-partition → failed record and its offset) to count attempts and pick the BackOff, then either recovers the record or calls SeekUtils to seek each affected partition back to its first unprocessed offset. The next poll() re-fetches them. SeekUtils.DEFAULT_MAX_FAILURES is 10, hence DEFAULT_BACK_OFF = new FixedBackOff(0, DEFAULT_MAX_FAILURES - 1). Since 2.9, seekAfterError=false keeps the unprocessed records in memory and redelivers them after a paused poll() instead of seeking. resetStateOnExceptionChange (true by default for record listeners) restarts the back-off when the exception type changes. In 4.1, FailedBatchProcessor gained setBackOffFunction and setResetStateOnExceptionChange for batch listeners, defaulting to false there for compatibility.
- pass 1: ❌ fixed in part file — FailedRecordTracker.java:51 per-thread map TopicPartition → FailedRecord
- pass 2: ✅ KafkaMessageListenerContainer.java:3120-3142,3240 (ListenerExecutionFailedException; handleRemaining when seeksAfterHandling, else handleOne); DefaultErrorHandler.java:53,168 extends FailedBatchProcessor→FailedRecordProcessor, SeekUtils.seekOrRecover; FailedRecordTracker.java:51 Map<Thread, Map<TopicPartition, FailedRecord>>; SeekUtils.java:57,63; annotation-error-handling.adoc:131-135 (2.9 seekAfterError=false, "single paused poll()"), :296-300 resetStateOnExceptionChange true since 2.9; whats-new.adoc x41-back-off-function default false in FailedBatchProcessor

### C1108 · k-spring-kafka · L4 · p
> For batch listeners, throw BatchListenerFailedException with the failing index. The handler commits the records before it, retries from the failing one and, once retries are exhausted, recovers only that record. Any other exception from a batch listener falls back to retrying the complete batch.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:128-129,171-181 — "perform seeks so that all the remaining records (including the failed record) will be redelivered"
- pass 2: ✅ annotation-error-handling.adoc:176-184,380-386,130 — commits records before the index, retries remaining, "only the failed record is sent to the DLT"; non-BatchListenerFailedException fallback = Retrying Complete Batches

### C1109 · k-spring-kafka · L4 · li
> Container = poll loop + commits + error handling; concurrency = consumers/threads, useful only up to the partition count.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:161-170 — "creates three KafkaMessageListenerContainer instances"
- pass 2: ✅ message-listener-container.adoc:162-170 (summary)

### C1110 · k-spring-kafka · L4 · li
> Default AckMode is BATCH; 4.1 lets @KafkaListener(ackMode=…) override it per listener.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/receiving-messages/message-listener-container.adoc:208; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/whats-new.adoc (x41-kafka-listener-ack-mode) — "The default AckMode is BATCH"
- pass 2: ✅ message-listener-container.adoc:208; whats-new.adoc:13

### C1111 · k-spring-kafka · L4 · li
> DefaultErrorHandler default = 10 attempts, no delay, then log and skip. Always add a DeadLetterPublishingRecoverer (<topic>-dlt, same partition).
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:139,874-876 — "after ten failures ... logged / <originalTopic>-dlt"
- pass 2: ✅ SeekUtils.java:63; annotation-error-handling.adoc:138,875

### C1112 · k-spring-kafka · L4 · li
> ErrorHandlingDeserializer turns poison pills into dead letters; deserialization errors are fatal (no retries).
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/serdes.adoc:525-529; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/annotation-error-handling.adoc:~222 — "DeserializationException"
- pass 2: ✅ serdes.adoc:524-527; annotation-error-handling.adoc:221

### C1113 · k-spring-kafka · L4 · li
> Blocking retries keep order but stall the partition. @RetryableTopic keeps flowing but loses ordering, and doesn't work with batch listeners or container transactions.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic.adoc:12,14; spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/retrytopic/how-the-pattern-works.adoc:12 — "not supported with Batch Listeners / cannot combine with Container Transactions"
- pass 2: ✅ how-the-pattern-works.adoc:12; retrytopic.adoc:12,14

### C1114 · k-spring-kafka · L4 · li
> Transactions: unique transactionIdPrefix per instance, EOS V2 only. Exactly-once covers read-process-write to Kafka, not other side effects.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/exactly-once.adoc:14-20; transactions.adoc:26 — "only supports EOSMode.V2"
- pass 2: ✅ transactions.adoc:22; exactly-once.adoc:11-16

### C1115 · k-spring-kafka · L4 · li
> Share consumers are production-ready in 4.1 (on Kafka 4.2+) with ShareAckMode EXPLICIT/MANUAL/IMPLICIT.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/kafka/kafka-queues.adoc:4-5; spring-kafka-4.1.1/spring-kafka/src/main/java/org/springframework/kafka/listener/ContainerProperties.java:126,352 — "promoted to full production support"
- pass 2: ✅ kafka-queues.adoc:5; ContainerProperties.java:126 ShareAckMode EXPLICIT/MANUAL/IMPLICIT

### C1116 · k-spring-kafka · L4 · li
> Test with @EmbeddedKafka (KRaft only) and Testcontainers' KafkaContainer.
- pass 1: ✅ spring-kafka-4.1.1/spring-kafka-docs/src/main/antora/modules/ROOT/pages/testing.adoc:29-35; https://java.testcontainers.org/modules/kafka/ — "only the EmbeddedKafkaKraftBroker implementation is now available"
- pass 2: ✅ testing.adoc:32; Testcontainers Kafka module docs
