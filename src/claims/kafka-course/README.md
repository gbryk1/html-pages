# Claims ledger — kafka-course.html

Every factual claim extracted from the rendered page (`skills/animated-knowledge-page/scripts/extract-claims.cjs`,
one file per chapter; `js_*` files hold figure narrations from the inline script). Checked in September 2026
against Apache Kafka 4.3.1 (docs and source), Spring for Apache Kafka 4.1.1 (docs and source), the KIPs cited
in the page, and current vendor docs (RabbitMQ, Pulsar, Redpanda, AWS Kinesis, Google Cloud Pub/Sub).

- **pass 1** — the chapter authors' own check, recorded per claim (source file:line and quote).
  Cheat-sheet rows cite the chapter note they restate.
- **pass 2** — independent verification: 11 fresh verifier subagents that never saw the pass-1 lines, then one
  re-verification round on the 66 claims rewritten after pass 2.
  Final: 1257 ✅, 396 n/a (pedagogy, analogies, UI text, numbers labelled illustrative), 0 open.
  `extract-claims.cjs --status` prints ALL CLAIMS VALIDATED TWICE.

Notable corrections found by pass 2: the high watermark cannot advance while the ISR is below
`min.insync.replicas` (4.3.1 `Partition.maybeIncrementLeaderHW`); KRaft elections start with a KIP-996 pre-vote;
fencing is a `BrokerRegistrationChangeRecord`, not `FenceBrokerRecord`; a stale-epoch transactional write gets
`INVALID_PRODUCER_EPOCH` before the fatal `ProducerFencedException`; Redpanda does not implement KIP-890 server side.

Claim IDs are positional and change when the page is re-extracted; match on the quoted text.
