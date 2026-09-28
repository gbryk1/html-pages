# Token spend report — Kafka internals guide session (25–28 Sep 2026)

Source: the session transcripts in `~/.claude/projects/-Users-gbryk-cc-html-tools/` (main session 06af6bef…,
its 17 subagents, and the other session 78db266a… that ran in the same account just before this one).
Output-token counts in transcripts are logged before streaming finishes, so they are undercounted; the text that was
written (~1.3 MB of HTML/JS/notes) is roughly 350k output tokens.

## 1. Totals

| Who | API calls | Avg context per call | Cache writes | Cache reads | Weighted share* |
|---|---|---|---|---|---|
| 5 author subagents (Parts I–V) | 681 | ~200k (peaks 308k–401k) | 9.6M | 127.4M | **51%** |
| 12 verifier subagents (pass 2 + round 2) | 669 | ~90k | 1.9M | 58.7M | 17% |
| main session (me) | ~220 | ~235k | 1.7M | 50.3M | 16% |
| other session 78db266a + its 10 subagents | 347 | ~140k | 1.8M | 46.3M | 16% |

\* input-equivalent proxy: input 1×, cache write 1.25×, cache read 0.1×, output 5× (API price ratios). The plan's
limit accounting per token type is not published; use the shares, not the absolute numbers.
About 280M tokens were re-read from cache in total, 236M of them in this session.

## 2. Timeline of the limits (local time)

| When | What happened |
|---|---|
| 25 Sep 17:34–22:25 | Other session (Claude Code guide validation) uses the same 5-hour window. |
| 22:35 | 5 Opus authors launched in parallel → session limit within ~15 min (window already spent). |
| 22:51 | Reset. All 5 resumed → each resume re-writes its context → limit again ~23:10. |
| 26 Sep 03:51 | Reset, resume → limit ~04:10. |
| 08:51 | Reset, resume → **weekly limit** reached. |
| 27 Sep 18:00 | Weekly reset. Pass 1 by authors + 6 verifiers in parallel → session limit ~18:25. |
| 23:00 | Reset; remaining verifiers, fixes, round 2 → finished. |

Each 5-hour window after a reset was used up in ~20 minutes.

## 3. Why so fast — root causes, ranked

1. **Every tool call re-sends the whole context.** Authors averaged ~200k tokens per call and peaked at 300–400k.
   An author with 150 calls re-read ~35M tokens. HTML output itself was a small share of the spend.
2. **Authors were reused for four more jobs** (finish after limit, pass-1 ledger fill, two fix batches, a missing
   Streams chapter). Every job added to the same context, so it only grew.
3. **Cold resumes after rate-limit pauses:** 29 of them. The prompt cache (1 h TTL) had expired, so each resume
   re-wrote 250–370k tokens: ~7.9M cache-write tokens. Cache writes are the most expensive input type.
4. **Too much parallelism for a subscription window:** 5 authors, then 6 verifiers, all on Opus, at once.
5. **A second session was running in the same account**, so the first window was nearly empty when this work started.
6. **Authors ran their own headless browser QA** (test pages, Chromium runs, screenshots), which duplicated the
   final checks and added tool output to their contexts.
7. **Main context growth:** ~35 screenshots (~1.5k tokens each), 17 subagent reports, a 30 KB template read, and about
   25 calls debugging `check-page.cjs` timeouts on tall mobile figures (a script limitation, now fixed).
8. **Ad-hoc tooling:** `carry.py`, export/merge scripts and many small probe scripts written in-session, because the
   skill had no ledger carry/export/merge.
9. **Everything ran on Opus.** Checking and lookup work (pass 2, pass-1 fill, fixes) doesn't need it.

## 4. Proposed skill changes (`skill-proposal/`, diff in `skill-proposal.diff`)

**SKILL.md**
- New **Token budget** section with three budgets: `lean` (token-saving), `standard` (default) and `max`. The budget
  sets:
  - authoring: `lean` = you write it yourself; `standard` = ≤ 3 authors; `max` = one per part;
  - concurrency cap: 1 / 3 / 5;
  - verifier model and batch size: `sonnet` at ~300 / ~200 claims, `max` inherits the model at ~150;
  - quiz size and screenshot rounds.
- Deep mode asks the budget next to Depth and Extras, and recommends `lean` for 15+ chapters.
- Seven rules that apply to every budget:
  1. never resume a big idle agent;
  2. one job per subagent, and authors fill pass 1 while their context is warm;
  3. no browser QA loops inside authors;
  4. cap tool output;
  5. Sonnet for checking;
  6. watch concurrency and other sessions;
  7. keep the main context lean.
- Step 8 uses the new ledger commands. `description` and `argument-hint` mention the budget:
  `[simple|deep] [lean|standard|max] <topic>`.
- Two new pitfalls: tall mobile figures, and resuming after a limit.

**reference.md**
- §15 gets budget rules for authors: capped output, 1–3 Write calls, fill pass 1 before returning, ≤ 15-line report.
- New §16 has the measured spend table and planning numbers.

**scripts/extract-claims.cjs**
- `--carry <oldDir>` keeps the verdicts of every claim whose text didn't change. It carried 1653/1653 on this page.
- `--export <ledger> <out> [--open-only]` writes verifier copies with pass 1 hidden; `--open-only` gives the round-2 set.
- `--merge <verifierDir> <ledger>` copies pass-2 verdicts back by ID.
- Headings and `<summary>` labels are pre-marked n/a. That was 120 of 1653 claims here (7%) that verifiers no
  longer have to touch.

**scripts/check-page.cjs**
- Figures taller than the viewport get a temporarily taller viewport for their screenshot. Before this, the mobile
  run timed out (the page now passes with it).

## 5. Expected effect

Estimate, not a measurement:
- **`lean` on a page of this size:**
  - one authoring context instead of five growing ones;
  - no cold resumes;
  - Sonnet verifiers run one after another;
  - no in-session tooling.

  Expect well under half of the spend above, and no window used up in 20 minutes. The trade-off is a longer
  wall-clock time.
- **`standard`:** most of the saving comes from rules 1–3 alone. Together they remove the 29 cold resumes and stop
  author contexts from growing past ~200k.
