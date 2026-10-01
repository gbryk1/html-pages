---
name: animated-knowledge-page
description: "Build an animated course, interactive guide, scrollytelling explainer or 'from zero to expert' knowledge page (\"animowany kurs\", \"strona wiedzy\", \"przewodnik\", Head First style) on one technical topic, tool or product. Two modes: simple (build what was asked) and deep (plan first, user picks how deep, multi-level content), and three token budgets: lean (token-saving), standard, max. Produces a single self-contained HTML file with scroll-triggered SVG/HTML/Canvas diagrams, Head First boxes, a quiz and a cheat sheet. Every claim is validated twice (by the author against sources, then by an independent verifier), and the page is verified headless."
argument-hint: "[simple|deep] [lean|standard|max] <topic>"
---

# Animated Knowledge Page

Builds one self-contained `.html` file that teaches a concept, system or tool through scroll-triggered, clickable
diagrams and Head First-style prose. The content can be flat or multi-level (parts → chapters → deeper blocks), and
the reader picks a reading depth.

| File | What it is |
|---|---|
| [templates/shell.html](templates/shell.html) | Starter page: design system, parts/levels/depth switch/course map, helpers, an example figure, and the quiz |
| [reference.md](reference.md) | Patterns, box catalogue, figure archetypes, multi-level contract, validation protocol, subagent contracts, browser setup |
| [scripts/check-page.cjs](scripts/check-page.cjs) | Smoke test: light, dark, mobile, reduced motion, and depth 1 |
| [scripts/click-through.cjs](scripts/click-through.cjs) | Clicks every control and fails on any JS error |
| [scripts/extract-claims.cjs](scripts/extract-claims.cjs) | Builds the claims ledger for double validation; `--status` is the ship gate |

## Two modes

Take the mode (and the budget, see Token budget) from the arguments or the request ("quick", "simple" → simple; "deep", "thorough", "complete guide",
"zero to expert" → deep). If it's unclear, ask once with AskUserQuestion: **Simple** — "build what I asked, now",
or **Deep** — "plan first, choose the depth, multi-level".

| | Simple | Deep |
|---|---|---|
| Scope | exactly what the user asked for | proposed by you, and the user chooses how deep |
| Plan | internal, not shown | a written plan, shown and approved before building |
| Structure | flat chapters (levels optional) | parts → chapters → levels L1/L2/L3 (+ "go deeper" blocks) |
| Validation | **double**: author + independent verifier | **double**, with parallel verifiers when the ledger is large |
| Subagents | usually none, except the pass-2 verifier | research fan-out, verifiers, and optionally chapter authors (see Parallel work) |

There is **no chapter limit** in either mode. Each chapter explains one mechanism, and the topic decides how many chapters there are.

## Token budget

Subscription limits run out on *context re-reads*, not on the HTML you write. Every subagent turn re-sends its whole
context: an author at 300k tokens pays ~300k per tool call, and a resumed agent whose prompt cache expired (idle > 1 h,
e.g. after a rate-limit pause) re-writes its whole context from scratch. Pick a budget together with the mode:

| | `lean` (token-saving) | `standard` (default) | `max` |
|---|---|---|---|
| When | the user mentions limits, cost, "save tokens", "cheap", or is on a subscription plan with a large (15+ chapter) page | normal requests | the user explicitly wants the fastest wall-clock build |
| Authoring | you write the page yourself, one part at a time; no author subagents | ≤ 3 author subagents, each ≤ ~6 chapters | one author per part |
| Concurrent subagents | 1 | ≤ 3 | ≤ 5 |
| Research | grep on disk yourself, capped output | one fact-sheet subagent per big source area, `model: "sonnet"` | same, per area |
| Pass-2 verifiers | `model: "sonnet"`, ~300 claims each, run one after another | `model: "sonnet"`, ~200 claims each | inherit model, ~150 each |
| Quiz | 1 question per chapter | 1–3 | 2–3 |
| Screenshot rounds | one full round, then only the figures you changed | two full rounds | as needed |

In deep mode, add the budget as a third question next to Depth and Extras (recommend `lean` for 15+ chapters).
Double validation stays mandatory in every budget; only who does it and on which model changes.

Rules for every budget (measured on a 27-chapter deep page, see reference §16):
1. **Never resume a big idle subagent.** After a limit pause or > 1 h idle, a resume re-writes its whole context
   (29 such re-writes cost ~8M tokens on the Kafka guide). Apply small fixes yourself (`sed`/Edit) or spawn a *fresh*
   agent with a short brief and only the files it needs.
2. **One job per subagent, then it ends.** Authors write their part *and* fill pass 1 in the same warm run: they
   assemble a test page, run `extract-claims.cjs` on it, fill `pass 1` from their notes, and return. Your final
   extraction carries those lines over with `--carry`. Don't bring authors back for pass 1 or fix rounds.
3. **Authors don't run browser QA loops.** They run `node --check` and at most one `click-through.cjs`; screenshots
   and layout fixes are yours, once, for the whole page.
4. **Cap every tool output.** `grep -n -m 20`, `cut -c1-200`, `| head -40`; never `cat` a doc page or the template;
   keep sources on disk and quote lines. Tell subagents the same in their brief.
5. **Cheaper models for checking.** Verifiers, pass-1 fillers, fixers and fact-sheet researchers get `model: "sonnet"`;
   keep the inherited model for authoring and assembly.
6. **Watch concurrency and other sessions.** Parallel agents share one account-wide window: five parallel authors
   exhausted a 5-hour window in ~1.5 h, and a second Claude session in the same account was consuming the same window.
   If a limit hits, wait for the reset and restart with fresh, small briefs (rule 1) rather than resuming everything.
7. **Keep your own context lean.** Read the template once (or only the parts you edit), never read subagent transcripts,
   ask subagents for ≤ 15-line reports, and use the ledger tools (`--export`, `--merge`, `--carry`) instead of
   ad-hoc scripts.

## Hard constraints

- One HTML file: inline `<style>` and `<script>`, vanilla JS, SVG, HTML and Canvas. No framework, no build step, no network dependencies.
- Light and dark themes come from `prefers-color-scheme`. There must be no horizontal overflow at 390px. Reduced motion shows the end
  states. The page is readable without JS. On multi-level pages, depth 1 must also be clean.
- **Nothing ships unvalidated.** Every factual claim (text, tables, code, captions, quiz, figure narrations) passes
  two independent checks. Invented numbers are labelled "illustrative" on the page, and each figcaption says what the simulation simplifies.

## Steps

0. **Frame.** Pick the mode (above). Write in the language of the user's request unless told otherwise, and write any
   repo index or README entry in the repo's language. Find the target path and the ship conventions: the pages folder,
   the index, and any generator that copies sources. If there is no repo, use the working directory.
   Done when you know the mode, language, audience, target path and ship steps.

1. **Survey the sources.** Find the authoritative sources: official docs (download `llms.txt` and markdown pages into
   scratch; reference §10), specs, source code, papers. In deep mode, map the whole topic landscape here, because you
   can't propose depth tiers for what you haven't surveyed.
   Done when you have a list of the source files or URLs and a topic map.

2. **Scope.**
   - *Simple:* list the chapters that cover the request (one mechanism each, in dependency order), each with its
     takeaway, the misconception it kills, a figure spec (what the reader clicks, what changes, the break-it action,
     the reset) and its boxes. Don't show this to the user; continue.
   - *Deep:* **ask how deep to go**, with AskUserQuestion and cumulative tiers (reference §13). Each option's
     `preview` lists the parts and chapters that tier adds, for example:
     1. Essentials,
     2. + Practitioner,
     3. + Expert / internals,
     4. + Edge cases, operations and comparisons.

     Add a second multi-select question for optional extras, such as hands-on exercises, a troubleshooting part,
     comparisons with alternatives, or internals deep dives. Then write the **plan** to scratch as `<page>.plan.md`:
     parts → chapters → level (L1/L2/L3) → takeaway, misconception, figure spec, boxes, "go deeper" blocks, the
     claims each chapter will make with their sources, and the quiz topics. Show a compact outline and ask the user to
     approve or adjust it. **Don't build before approval.**

   Done when (simple) every chapter has a figure spec, or (deep) the user has approved the plan.

3. **Validation pass 1: the author, while writing** (reference §11). Check every command, flag, default, number, path
   and *behavior* against a source *before* it goes into the page. Keep notes (claim → source → quote) in the plan or
   in scratch. Anything you can't source gets softened into a clearly-marked opinion, or dropped.
   Done when every planned claim has a source.

4. **Start from the shell.** Copy `templates/shell.html` to the target and fill in the `{{…}}` placeholders.
   Multi-level content uses the structure contract (reference §12): `section.part[data-level]`,
   `section.chapter[data-level][data-title]`, `[data-level]` blocks, `details.deeper`, `#map` and `LEVELS`. The TOC,
   chapter numbers, level chips and course map are generated for you. For a flat page, delete the parts and the
   level attributes, and the depth switch disappears. Per-figure CSS goes in the marked slot, with **prefixed class names**.

5. **Write the chapters: prose and boxes.** Use conversational second person, one idea per paragraph, and humor that
   serves the point. Each chapter gets 1–3 boxes (reference §3) and ends with a 📌 summary or ✏️ exercise. Tool
   guides get **Option | What | When** tables. Expert material goes into L2/L3 blocks or `details.deeper`, so a
   beginner can read at depth 1 without gaps: never make an L1 chapter depend on L2 content.

6. **Build one figure per mechanism chapter** (reference §4–8). Use an IIFE with `registerFig(el,{first,enter,leave})`,
   `await` sequences with one `.status` sentence per step, buttons with `data-act`, a break-it action, `↺ Reset`,
   `makeQueue()` and a `runId`. Each action starts from the state its narration assumes. Prefer HTML-grid figures on
   mobile. Use deterministic data. Build large pages in parts, concatenate them, and run `node --check` on the script.

7. **Quiz and cheat sheet.** Write one or more questions per chapter, where the wrong options are the chapter's
   misconceptions, and tag deeper-level questions in the text ("(L3)"). The cheat sheet has the columns
   parameter/command | default/what | when. The 🎓 box answers chapter 0's question.

8. **Validation pass 2: independent verification** (reference §11). Extract the ledger:
   ```bash
   node <skill-dir>/scripts/extract-claims.cjs page.html "$SCRATCH/claims" --split   # one file per chapter
   node <skill-dir>/scripts/extract-claims.cjs --export "$SCRATCH/claims" "$SCRATCH/verify"   # pass-1 hidden, for verifiers
   node <skill-dir>/scripts/extract-claims.cjs --merge  "$SCRATCH/verify" "$SCRATCH/claims"   # verdicts back by ID
   # after fixing the page: re-extract and keep every verdict whose text didn't change
   node <skill-dir>/scripts/extract-claims.cjs page.html "$SCRATCH/claims2" --split --carry "$SCRATCH/claims"
   node <skill-dir>/scripts/extract-claims.cjs --export "$SCRATCH/claims2" "$SCRATCH/verify2" --open-only   # round 2
   ```
   Headings and `<summary>` labels are pre-marked n/a (about 7% of a typical ledger).
   Fill in `pass 1` for every claim from your notes. Then have **someone who didn't write the page** fill in
   `pass 2`: a fresh verifier subagent that gets only the ledger files and the source list, never your notes or
   your reasoning. It re-finds evidence itself and marks each claim ✅ / ⚠️ / ❌ / ❓ / n/a. Fix every ⚠️, ❌ and ❓ in
   the page, then **re-verify only the changed claims** with a new verifier. Loop until:
   ```bash
   node <skill-dir>/scripts/extract-claims.cjs --status "$SCRATCH/claims"   # must print ALL CLAIMS VALIDATED TWICE
   ```
   If subagents aren't available, do pass 2 yourself as a separate, source-first sweep: for each claim, find the
   evidence again without looking at your pass-1 note.

9. **Verify headless** (reference §14 for browser setup):
   ```bash
   node <skill-dir>/scripts/check-page.cjs   page.html "$SCRATCH/shots"   # light/dark/mobile/reduced (+depth1)
   node <skill-dir>/scripts/click-through.cjs page.html "$SCRATCH/shots"   # every control, no JS errors
   ```
   Then **look at the screenshots**. Check for overlapping labels, clipped text, mid-word breaks, dark-mode contrast,
   narration that contradicts the visible state, and on multi-level pages, depth 1 reading well. Plan at least two
   screenshot → fix rounds. Re-run step 8 `--status` if any text changed.

10. **Ship.** Follow the conventions from step 0: keep the source where generators won't wipe it, register it, link it
    from the index and README, and commit (and push if that's the workflow) as a command of its own. Report to the user:
    - the mode and depth,
    - the chapter and figure counts,
    - validation results (claims, how many passes, what was fixed),
    - the check results.

## Parallel work: subagents only when they pay off

Spawning costs context and coordination, and the budget (above) caps how many run at once. Use subagents for these:

- **Pass-2 verifier: always**, even for a small page. Independence is the point, not speed. Split the ledger into
  groups of the size your budget sets and run them within its concurrency cap, on `model: "sonnet"` unless the budget is `max`.
- **Research fan-out (deep mode):** when there are 5 or more independent source areas (for example settings, hooks,
  MCP and CI docs), use one subagent per area. Each returns a fact sheet of claim → source → quote. This keeps raw
  docs out of your context.
- **Parallel chapter authoring (`standard`/`max` only):** only for large deep-mode pages, around 12 or more chapters, and only after the plan
  and shell are fixed. Each author owns separate files: `part-N.html` (sections), `part-N.js` (figure IIFEs) and
  `part-N.css` (classes prefixed `pN-`). Authors get the shell contract and the plan excerpt, and return pass-1
  notes. You assemble the page and run all checks. See reference §15 for prompts.

Don't use subagents for:
- simple-mode authoring,
- small pages,
- visual QA fixes (they share layout),
- anything that needs the user's answers.

## Pitfalls

- **Stale facts about fast-moving tools.** The draft said `acceptEdits` auto-approves only `mkdir/touch/mv/cp`, but the docs say
  `rm`, `rmdir` and `sed` too. Verify behavior, not only numbers, and have the verifier re-find the evidence itself.
- **Docs-only checking misses runtime constraints.** A `--json-schema` example with an array root passed doc review but
  failed when run (`API Error: 400 … input_schema.type: Input should be 'object'`). Verifiers run cheap, safe snippets.
- **Fix rounds introduce new errors.** Each round of fixes produced new imprecisions (3 rounds on the Claude Code guide).
  Loop re-verification on changed claims until a round comes back clean.
- **Self-verification bias.** Re-reading your own notes confirms your own mistakes. Pass 2 must start from the page text and the sources.
- **Claims hide in JS.** Status narrations and scenario texts appear only after clicks. `extract-claims.cjs` reads the
  script's string literals, attributed to the nearest `/* ---------- N. name ---------- */` comment. Keep that
  comment style.
- **Levels leaking.** An L1 chapter that uses an L2-only term breaks depth-1 reading. Define terms at the lowest level
  that uses them. `check-page.cjs` runs depth 1, but only your reading catches gaps in meaning.
- **Figures taller than the phone viewport** never became "stable" for element screenshots, and `check-page.cjs`
  timed out on mobile; the script now grows the viewport for such shots. Still keep mobile figures compact.
- **Resuming agents after a limit pause** re-read 300–400k tokens each; see Token budget rule 1.
- **Blank screenshots / Chromium crashes** in a font-less sandbox. A "no JS errors" run with blank screenshots is not a pass (reference §14).
- **`pkill -f` matched its own shell** and killed a chained commit. Kill by PID, and never chain cleanup with commit or push.
- **Layout collisions only show up in screenshots.** Put labels on a larger radius than nodes, reserve space for badges,
  and stack exploded labels in a column.
- **Generic class names collide** (`.sub` matched the cover subtitle). Prefix per-figure classes.
- **Mobile overflow.** Grid items need `min-width:0`, `<select>` needs `max-width:100%`, and long unbreakable text needs `overflow-wrap`.
- **CamelCase labels break mid-word** in narrow grids. Use `wbr()`.
- **`hidden` is ignored** under `display:flex` rules. The template forces `[hidden]{display:none!important}`.
- **Narration contradicts state** after a second break-it action. Reset first.
- **Rare random cases look like bugs.** Force them with a button or seed the demo.
- **Plural rules** for interpolated counts (Polish: 1 replika / 2–4 repliki / 5+ replik).
- **Dynamically created `.reveal` elements** need `revealIO.observe()`.
