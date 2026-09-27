# Claims ledger — kafka-course.html — js_27_alternatives_chooser

Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative

### C1645 · js:27. alternatives chooser · L- · script
> Many independent reader groups (fan-out)
- pass 1: n/a (UI label)
- pass 2: n/a — chooser label

### C1646 · js:27. alternatives chooser · L- · script
> Per-message ack + automatic poison limit
- pass 1: n/a (UI label)
- pass 2: n/a — chooser label

### C1647 · js:27. alternatives chooser · L- · script
> Brokers without local message storage
- pass 1: n/a (UI label)
- pass 2: n/a — chooser label

### C1648 · js:27. alternatives chooser · L- · script
> ✓ meets every ticked requirement
- pass 1: n/a (UI label)
- pass 2: n/a — UI legend

### C1649 · js:27. alternatives chooser · L- · script
> Tick what your workload needs. The chooser scores six options on the facts in the table above.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI hint (heuristic)

### C1650 · js:27. alternatives chooser · L- · script
> Nothing ticked yet — every option "fits" an empty wish list. Tick at least one requirement.
- pass 1: n/a (UI hint)
- pass 2: n/a — UI message

### C1651 · js:27. alternatives chooser · L- · script
> Fits all …: …. Now weigh what the chooser ignores — team skills, what you already run, cost, licensing.
- pass 1: n/a (UI template / opinion)
- pass 2: n/a — UI template/advice

### C1652 · js:27. alternatives chooser · L- · script
> No option meets all …. Closest (…/…): …. Drop or relax the ✗ requirement — or combine two systems.
- pass 1: n/a (UI template)
- pass 2: n/a — UI template/advice

### C1653 · js:27. alternatives chooser · L- · script
> Nobody does everything. Exchange routing (RabbitMQ), the Kafka API (Kafka, Redpanda) and disk-less brokers (Pulsar) live in different products, and inside Kafka you choose per group between ordering (consumer groups) and elastic per-record work (share groups). Pick the two or three requirements that really matter.
- pass 1: ✅ https://www.rabbitmq.com/docs/queues; https://pulsar.apache.org/docs/next/concepts-architecture-overview/; https://docs.redpanda.com/current/get-started/intro-to-events/; kafka-4.3.1-src/docs/getting-started/upgrade.md:84 — "exchanges / stateless broker / Kafka API compatible / rather than as part of an ordered stream"
- pass 2: ✅ RabbitMQ exchanges (docs/queues), Kafka API for Kafka/Redpanda (Redpanda docs "compatible with the Kafka API"), Pulsar stateless brokers + BookKeeper (architecture overview), share groups unordered (upgrade.md:84)
