# Reference: code patterns for animated knowledge pages

Copy-and-adapt material for the steps in `SKILL.md`. Everything in §1–7, §9 and §12 already exists, working, in
[templates/shell.html](templates/shell.html). Copy that file first instead of retyping these snippets.
§10–11 cover facts and double validation, §13 the deep-mode depth question, §14 browser setup, §15 parallel authoring, and §16 token spend.

Worked examples (public repo `gbryk1/html-tools-local-llm`, `src/`):
- `cassandra-course.html`: distributed-systems internals. Token ring, gossip, Canvas figures, and randomness forced by a button.
- `copilot-pm-course.html`: a product guide for non-engineers, with the 🧰 use-case box and example prompts.
- `claude-code-course.html`: a tool guide "from zero to expert". It has 18 chapters and 16 figures that cover most of the archetypes in §8.

## 1. Page skeleton

```html
<!DOCTYPE html><html lang="en"><head>…
<script>document.documentElement.classList.add('js')</script>  <!-- .reveal hides content only when JS runs -->
<style>/* tokens → base → cover → layout → boxes → figures → generic extras → per-figure → quiz → reduced-motion */</style>
</head><body>
<div id="progress"></div>                                   <!-- scroll progress bar -->
<nav id="toc">…<a href="#ring">2 · Ring</a>…</nav>          <!-- fixed, ≥1440px only, after the cover -->
<header class="cover">…hero text + decorative looping SVG + scroll hint…</header>
<main>
  <section class="chapter" id="ring">…</section>            <!-- one per mechanism -->
  <section class="chapter" id="final"><div id="quiz"></div><div class="score" id="score"></div>…cheat sheet…</section>
</main>
<footer>…sources + date checked, trademark disclaimer…</footer>
<script>'use strict'; /* helpers → scroll machinery → cover → one IIFE per figure → quiz */</script>
</body></html>
```

## 2. Design tokens with light/dark

Every color is a CSS variable, and dark mode only overrides the variables. Use system font stacks, never web fonts.

```css
:root { color-scheme: light dark;
  --paper:#fbf8f1; --paper2:#ffffff; --ink:#1c1c22; --mut:#5d5f6b; --line:#d9d3c4; --shadow:#1c1c22;
  --accent:#1b6fa8; --accent2:#e39b2d; --ok:#2f9e63; --bad:#d64545; --warn:#c98a00;
  --pen:#b8431f; --note:#fff4c2; --card:#f1ece0; --code:#f3efe6; --dim:#9aa0ab;
  --hand:"Segoe Print","Bradley Hand","Comic Neue","Comic Sans MS","Chalkboard SE","Marker Felt",cursive;
  --sans:system-ui,-apple-system,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif;
  --serif:Charter,"Bitstream Charter","Iowan Old Style",Georgia,"Palatino Linotype",serif;
  --mono:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace; }
@media (prefers-color-scheme: dark) { :root { --paper:#15171c; --paper2:#1d2027; --ink:#e7e5df; … } }
[hidden] { display:none !important; }   /* otherwise .ctrl{display:flex} un-hides toggled controls */
.fig svg text { fill:var(--ink); }      /* SVG text follows the theme */
.two > * { min-width:0; }               /* grid items otherwise grow to fit nowrap content */
main, footer { overflow-wrap:break-word; }
@media (prefers-reduced-motion: reduce) { *,*::before,*::after { animation-duration:.001s !important; animation-iteration-count:1 !important; transition-duration:.001s !important; } }
```

JS reads the same variables into `C` and re-reads them when the color scheme changes. Fixed "semantic" series
colors (for example the token categories in a stacked bar) can be hex constants, as long as white text stays
readable on them in both themes.

## 3. Head First box catalogue

Every box shares one `.box` base: a 2px ink border, a hard offset shadow, and a rotated label tab. Use the page's
language for the labels.

| class | label | purpose |
|---|---|---|
| `brain` | 🧠 Brain power | open "think about it" question before a concept; chapter 0's question gets answered in the finale |
| `nodumb` | ❓ There are no dumb questions | Q:/A: grid of naive-but-real questions |
| `pencil` | ✏️ Sharpen your pencil | exercise; answer in `<details><summary>Answer</summary>` |
| `fireside` | 🔥 Fireside chat | dialogue between two personified concepts (`/compact` vs `/clear`, Seed vs Node) |
| `watch` | ⚠️ Watch it! | anti-pattern / production pitfall (red) |
| `geek` | 🤓 Deep dive | optional depth, smaller font |
| `usecase` | 🧰 Workflow / use case | numbered real-world recipe (`<ol>`), prompts in `.prompt-ex` |
| `bullets` | 📌 Bullet points | chapter summary on a rotated sticky note |
| `relax` | 😌 Relax / 🎓 Congratulations | "no need to memorize" / finale |

Other devices: a chapter header with a rotated number disc and a handwritten `.tag`; `.anno` for a handwritten arrow
note; `.pull` for a pull quote. For option-heavy topics (tools, CLIs, configs), use a `table.mini` inside `.scroll-x`
with the columns **Option | What it does | Use it when**. That table is the "describe every option, when to use it" part.

## 4. Figure markup

```html
<figure class="fig wide" id="fig-ring">
  <div class="ftitle">▶ Token ring <span>type any key</span></div>
  <div class="ctrl">
    <input type="text" aria-label="partition key">
    <button class="btn primary" data-act="add">Insert key</button>
    <button class="btn bad" data-act="kill">💥 Kill node C</button>        <!-- the "break it" action -->
    <button class="btn" data-act="reset">↺ Reset</button>
  </div>
  <div class="two"><svg viewBox="0 0 420 420"></svg><div>…live table / log…</div></div>
  <div class="status" id="ring-status">…teacher's voice, one sentence per step…</div>
  <figcaption>What is simplified vs reality; "numbers are illustrative" where they are invented.</figcaption>
</figure>
```

- SVG: `viewBox` + `width:100%`. A figure that becomes illegible when narrow goes in `.scroll-x` with `min-width`.
- HTML-grid figures (bars, cards, timelines, logs) reflow better on mobile than SVG. Prefer them when geometry doesn't matter.
- The `.status` classes `ok` / `warn` / `bad` color the verdict.

## 5. JS helpers (all in the template)

`$`, `$$`, `REDUCE`, `sleep()` (0 under reduced motion), `S()` for SVG elements, `T()` for centered SVG text, and
`H()` for HTML elements. Also `tween(ms, fn)` (a promise that is instant under reduced motion), `flyDot(parent, x0,y0,x1,y1)`,
`ringXY(cx,cy,r,f)`, `C` (colors from CSS variables), and `makeQueue()`. Generic extras: `paintBar(el, segs, cap)` for a
stacked bar, `logLine(box, text, cls)` for a fading log line, `say(statusEl, html, cls)`, and `wbr(s)` to insert
`<wbr>` into CamelCase labels.

Because every step returns a promise, a mechanism reads like a script:
`await flyDot(client→coord); say(st,'Step 1: commit log'); await tween(…); if (my !== run) return;`

## 6. Scroll machinery (in the template)

- `revealIO`: adds `.shown` once at 12% visibility. Elements created dynamically must be passed to `revealIO.observe()` explicitly.
- `tocIO`: marks the active chapter (`rootMargin:'-35% 0px -60% 0px'`).
- `figIO` + `registerFig(el, {first, enter, leave})`: `first()` runs once at 25% visibility, and `enter/leave` start and stop loops.
- One rAF-throttled passive scroll listener drives the progress bar and `body.past-cover`.

## 7. Figure recipes

```js
(() => {
  const root = $('#fig-x'), st = $('#x-status');
  let state, run = 0;                    // run = runId
  const enqueue = makeQueue();           // serialize clicks, drop beyond 3 pending
  function reset() { run++; state = initial(); paint(); say(st, 'initial hint'); }
  async function action() {
    const my = ++run;                    // or `const my = run` if actions should queue, not cancel
    await tween(600, k => { …; paint(); }); if (my !== run) return;
    say(st, '<b>What happened.</b> Why it matters.', 'ok');
  }
  $('[data-act=action]', root).onclick = () => enqueue(action);
  $('[data-act=reset]', root).onclick = reset;
  reset();
  registerFig(root, { first() { enqueue(action); } });
})();
```

- **Known starting state for each action.** If action B only makes sense from a fresh state, but action A already
  changed it (for example "rewind code" followed by "rewind conversation"), B calls `reset()` first. Otherwise the
  narration contradicts what the reader sees.
- Loops (gossip ticks, live meters) run only while visible: `enter(){ timer=setInterval(…) } leave(){ clearInterval(timer) }`.
- Canvas: size it by `devicePixelRatio`, and re-layout on `resize` and in `enter()`, because a canvas measured off-screen can be 0 wide.
- Deterministic data comes from hashing strings. If randomness is part of the lesson, add a button that forces the interesting case.
- Persist per-reader state (checklists) in `localStorage` inside `try/catch`, and say so in the figcaption.

## 8. Figure archetypes (pick the one that *is* the mechanism)

| Archetype | Mechanism it shows | Example |
|---|---|---|
| Ring / graph + flying dots | messages between nodes, routing, replication | Cassandra ring, gossip; MCP hub |
| Loop diagram with phases | an iterative process with feedback | agent loop gather → act → verify, with a "remove the tests" break |
| Stacked capacity bar | a finite resource filling up, and cleanup operations | context window with read / noise / subagent / compact / clear |
| Rule gate / decision pipeline | ordered rule evaluation with a fallback | permissions deny → ask → allow, then the mode |
| Race lanes | two strategies over time, and where each wins | plan-first vs code-first for 3 task sizes |
| Builder with checkboxes | ingredients remove uncertainty | prompt builder where "guesses" get struck through |
| Timeline + state cards | history, checkpoints, undo | rewind: what gets restored and what doesn't (Bash) |
| Event track + toggles | lifecycle hooks, middleware, interceptors | hooks firing per event, with an exit-2 block |
| Fake terminal / UI | commands, shortcuts, prefixes | typed `/`, `@`, `!`, Shift+Tab, with a mode indicator |
| Scenario chooser | "which tool for which job" | parallel-work picker lighting up the approach |
| Pipeline stages | CI / request / build flow | PR, then workflow YAML, then tool calls, then JSON result |
| Dial / qualitative bars | trade-offs without real numbers | model × effort. Label it "heuristic". |
| Self-assessment ladder | the learning path, habits | expert ladder with a "next rung" plus an anti-pattern clinic |
| Simulator / calculator | formula or parameter effects | compaction simulator, RAID priority, quorum calculator |

## 9. Quiz + cheat sheet (in the template)

`Q = [[question, options, correctIndex, why], …]`. Build the buttons with `textContent` rather than `innerHTML`, so
option text such as `<select>` or `$VAR` renders literally. Allow one answer per question, show right and wrong
with the explanation, and keep a sticky score pill. Questions test mechanisms, and the wrong options are the
misconceptions from the chapters. The cheat sheet is a `table.mini.cheat` inside `.scroll-x` with a monospace first column.

## 10. Fact-check workflow for fast-moving topics (tools, APIs, cloud products)

Model knowledge goes stale. Treat every command, flag, default, file path and behavior as unverified until you
have checked it against the current official docs.

```bash
D="$SCRATCH/docs"; mkdir -p "$D"
curl -sL https://<docs-host>/llms.txt -o "$D/llms.txt"          # many doc sites publish an index; list .md URLs
grep -o 'https://[^)]*\.md' "$D/llms.txt" | head -200
for p in overview settings permissions …; do curl -sL "https://<docs-host>/docs/en/$p.md" -o "$D/$p.md" & done; wait
strip() { grep -v '^\s*export\|^\s*const' "$1" | sed -e 's/<[^>]*>//g' | grep -v '^\s*$' | cut -c1-300; }
strip "$D/permissions.md" | grep -E '^\| *`'                     # tables of options are gold
```

- Grep for specific claims (`grep -hiE 'exit code 2|acceptEdits'`). Don't read whole pages into context.
- Verify **behavior claims**, not only numbers. In the Claude Code guide, `acceptEdits` turned out to auto-approve `rm`,
  which contradicted the draft.
- Write "Facts checked against <source> in <month year>" in chapter 0 and the footer, and point to the product's changelog.

## 11. Double validation protocol

**The ledger.** `scripts/extract-claims.cjs page.html $SCRATCH/claims --split` renders the page at full depth, opens
every `details`, answers the quiz once so the right options show, and writes one ledger file per chapter:

```
### C0069 · perm · L1 · tr
> acceptEdits | Reads, file edits, and common filesystem commands (mkdir, touch, rm, rmdir, mv, cp, sed) … | …
- pass 1: ✅ code.claude.com/docs/en/permission-modes.md — "auto-approves common filesystem Bash commands: mkdir, touch, rm, rmdir, mv, cp, and sed"
- pass 2: ✅ permission-modes.md, section acceptEdits — same list; "inside the working directories" confirmed
```

Verdicts: ✅ confirmed · ⚠️ imprecise (state the fix) · ❌ wrong (state the fix) · ❓ unverifiable (soften or remove) ·
n/a (pedagogy, opinion, jokes, or numbers already labelled illustrative on the page). A table row is one claim, so every cell must hold.

**Pass 1 (author).** Fill it in from the notes you kept while writing. Every non-n/a claim cites a source and a short quote.

**Pass 2 (independent).** A fresh verifier gets **only** the ledger files, the source list and the downloaded docs
folder. It doesn't get the author's pass-1 text: strip those lines first, or tell the verifier to ignore them. Prompt:

> You are fact-checking an educational page. For each claim block in the files below, find evidence yourself in these
> sources: <list / folder>. Do not trust the page or any existing notes. Fill the `- pass 2:` line with a verdict
> (✅ ⚠️ ❌ ❓ n/a), the evidence location and a short quote. For ⚠️ or ❌, write the corrected wording. Treat every cell of
> a table row, every flag in a code block, and every number as part of the claim. Mark n/a only for pedagogy, opinion
> or numbers the page labels illustrative. Return a summary: counts per verdict and the list of non-✅ IDs with fixes.
> Where a snippet is cheap and safe to run (a CLI flag combination, a glob, a shell loop, a JSON schema), run it in a
> throwaway folder and cite the observed output — docs examples can hide constraints (e.g. a schema root that must be
> an object) that only execution reveals. Never run anything that edits files outside that folder or spends real money
> beyond a tiny prompt.

**Disputes.** Verifiers can be wrong too (one flagged `/context all` as undocumented; a doc page said the opposite).
Overrule a verdict only with a quote from a source, record it on the claim's `pass 2` line (`✅ (author override) <file> — "<quote>"`),
and prefer sending the dispute to a fresh verifier over deciding it yourself.

**Resolve.** Apply the fixes to the page and re-extract. The IDs of unchanged claims may shift, so match on text.
Carry verdicts over for text that didn't change, and send only new or changed claims to a new verifier. The ship gate:

```bash
node scripts/extract-claims.cjs --status "$SCRATCH/claims"   # exit 0 + "ALL CLAIMS VALIDATED TWICE"
```

Keep the ledger in scratch unless the user wants it committed. In the final report, give the claim count, the verdict
counts from pass 2, and the notable corrections.

## 12. Multi-level structure contract (template)

```html
<section class="part" id="part-2" data-level="2">                       <!-- divider, dims in TOC/map at lower depth -->
  <span class="part-no">Part II</span><h2>Extending the tool</h2><p>What this part gives you.</p>
</section>
<section class="chapter" id="hooks" data-level="2" data-title="Hooks">  <!-- data-title = TOC/map label -->
  <div class="chap-head reveal"><div class="chap-num"></div>…</div>     <!-- empty .chap-num → auto-numbered -->
  …L2 prose and figure…
  <div class="box geek reveal" data-level="3">…</div>                    <!-- any block can be deeper than its chapter -->
  <details class="deeper" data-level="3"><summary>🔬 Go deeper: exit-code semantics per event</summary>…</details>
</section>
```

- `LEVELS = ['Essentials', 'Practitioner', 'Expert']` names the levels. Add a fourth if the plan needs one; chip colors exist up to `l4`.
- The depth switch (bottom-left, shown after the cover) hides `[data-level] > depth`. The choice persists per page in
  `localStorage`. The default is the deepest level, so nothing is hidden on a first visit or for the checkers.
- Every chapter with hidden blocks gets a "＋ N deeper blocks hidden — show level K" button. Clicking a dimmed TOC or map
  entry raises the depth automatically.
- `#map` (put it in chapter 0) is the course overview. It shows parts as cards and chapters with level-colored borders,
  marks the current chapter with ▶, adds ✓ for chapters already seen, and is the mobile navigation (the TOC shows only at ≥1440px).
- The TOC is built from the DOM. Don't hand-write it.
- A flat page: omit the parts and `data-level`, and the switch and chips disappear by themselves.
- Writing rule: each level must read complete without the levels above it. L2 and L3 add depth; they never fill gaps in L1.

## 13. Deep mode: asking how deep

Propose **cumulative** tiers built from the step-1 survey, and let previews show the actual chapters:

```json
{"questions":[
 {"header":"Depth","question":"How deep should the guide go? Each tier includes the previous ones.","multiSelect":false,
  "options":[
   {"label":"Essentials","description":"Mental model + daily use (≈6 chapters)","preview":"Part I · Foundations\n 1 What it is: the loop\n 2 First session\n 3 Context\nPart II · Daily use\n 4 Memory\n 5 Permissions\n 6 Plan → code → verify"},
   {"label":"+ Practitioner (Recommended)","description":"Adds the extension layer (≈11)","preview":"…Essentials, plus\nPart III · Extending\n 7 Skills\n 8 Subagents\n 9 Hooks\n10 MCP & plugins\n11 Sessions & rewind"},
   {"label":"+ Expert","description":"Adds scale, automation, internals (≈16)","preview":"…Practitioner, plus\nPart IV · Expert\n12 Parallel work\n13 Headless & CI\n14 Models & cost\n15 Internals deep dives (L3)\n16 Habits & anti-patterns"},
   {"label":"+ Everything","description":"Adds edge cases, ops, comparisons (≈20+)","preview":"…Expert, plus\nPart V · Mastery\n17 Troubleshooting\n18 Security model\n19 Team rollout\n20 Alternatives compared"}]},
 {"header":"Extras","question":"Which extras should be included?","multiSelect":true,
  "options":[
   {"label":"Hands-on exercises","description":"✏️ tasks with answers in every chapter"},
   {"label":"Go-deeper blocks","description":"🔬 L3 internals inside chapters"},
   {"label":"Troubleshooting","description":"symptom → cause → fix tables"},
   {"label":"Comparisons","description":"vs alternatives, when to choose which"}]}]}
```

Map the choice to levels: the tiers become the parts, and the tier a chapter first appears in is usually its
`data-level`. Then write `<page>.plan.md` and get the user's approval on a compact outline before building.

## 14. Browser setup for the headless scripts

`scripts/_browser.cjs` finds `playwright-core` (local, `NODE_PATH`, global, or OpenClaw-bundled) and a browser.
It tries `CHROME_PATH` first, then Playwright's downloaded Chromium, then an installed Chrome, Chromium or Edge.

```bash
# anywhere (macOS / Linux / Windows)
npm i --prefix "$SCRATCH/pw" playwright-core && export NODE_PATH="$SCRATCH/pw/node_modules"
# optional if no Chrome is installed:  npx playwright install chromium
```

**Lib-less Linux sandbox** (no sudo, no fonts, Chromium missing shared libraries). Extract packages into `/tmp`:

```bash
mkdir -p /tmp/libs && cd /tmp/libs
apt-get download libgbm1 libxkbcommon0 libasound2t64 libatk1.0-0t64 libatk-bridge2.0-0t64 libatspi2.0-0t64 \
  libcups2t64 libdrm2 libwayland-server0 libxcomposite1 libxdamage1 libxfixes3 libxi6 libxrandr2 libxrender1 \
  libxres1 libxss1 libxtst6 fonts-dejavu-core fonts-noto-color-emoji
for f in *.deb; do dpkg -x "$f" root; done
cat > fonts.conf <<'EOF'
<?xml version="1.0"?><!DOCTYPE fontconfig SYSTEM "fonts.dtd">
<fontconfig><dir>/tmp/libs/root/usr/share/fonts</dir><cachedir>/tmp/libs/fc-cache</cachedir></fontconfig>
EOF
export LD_LIBRARY_PATH=/tmp/libs/root/usr/lib/x86_64-linux-gnu FONTCONFIG_FILE=/tmp/libs/fonts.conf
```

The package names are for Ubuntu 24.04+ (with `t64` suffixes). If Chromium still fails, run `ldd` on the
headless-shell binary and download whatever reports `not found`.

## 15. Parallel authoring contract (large deep-mode pages only)

Use this after the plan is approved and the shell is filled (tokens, helpers, structure, quiz scaffolding).

- Split the plan by part. Author N owns `$SCRATCH/parts/part-N.html` (only `section.part` and `section.chapter` elements),
  `part-N.js` (only figure IIFEs, each preceded by `/* ---------- <n>. <name> ---------- */`) and `part-N.css`
  (every class prefixed `pN-`). Authors never edit the shell, and never touch another author's files.
- Give each author the SKILL.md steps 5–7, reference §3–8 and §12, the plan excerpt for their part, the facts/sources
  sheet, and the helper API list (§5). Ask for pass-1 notes (claim → source → quote) alongside their files.
- Assemble the page yourself: insert the CSS into the per-figure slot, the HTML into `main` before the final chapter,
  and the JS before the quiz. Run `node --check`, `check-page.cjs` and `click-through.cjs`, then the validation loop (§11).
  Screenshot fixes stay with you, because layout is shared.
- Budget rules for authors (SKILL.md "Token budget"): cap every command's output (`grep -m`, `cut -c1-200`, `head`),
  never print whole doc pages or the shell, write the part with Write in 1–3 calls, run `node --check` plus at most one
  `click-through.cjs` on a throwaway page, and **fill pass 1 before returning**: assemble the part into a test page,
  run `extract-claims.cjs <test> <dir> --split`, fill every `pass 1` line from the notes, and hand back the ledger dir.
  Report in ≤ 15 lines. After returning, the author is finished — fixes go to you or a fresh agent.
- Prompt skeleton:

  > Write Part <N> of an animated knowledge page. Output exactly three files: <paths>. Follow the attached
  > contract: element types, level attributes, prefixed classes, one figure per mechanism chapter with a break-it
  > action and a reset, and a `.status` narration per step. Use only these helpers: <list>. Every factual claim must
  > come from the attached sources. Return a notes file with claim → source → quote. Don't invent flags, defaults or numbers.

## 16. Token spend: what a large deep page costs, and where

Measured on the Kafka internals guide (27 chapters, 28 figures, 1653 claims, `max`-style run with 5 authors and
12 verifiers on one model). "Weighted" is an input-equivalent proxy (input 1×, cache write 1.25×, cache read 0.1×,
output 5×); plan limits are not published per token type, so treat it as relative, not absolute.

| Who | Calls | Avg context per call | Cache writes | Cache reads | Share of weighted |
|---|---|---|---|---|---|
| 5 author subagents | 681 | ~200k (max 400k) | 9.6M | 127M | ~51% |
| 12 verifier subagents | 669 | ~90k | 1.9M | 59M | ~17% |
| main session | ~220 | ~235k | 1.7M | 50M | ~16% |
| an unrelated session in the same account | 347 | ~140k | 1.8M | 46M | ~16% |

Where it went, and the rule that fixes it:
- Author contexts grew to 300–400k because the same agents were brought back four more times (pass 1, two fix
  batches, a missing chapter) and ran their own headless QA. → one job per agent; authors fill pass 1 while warm.
- 29 cold resumes after rate-limit pauses re-wrote ~8M tokens of cache. → never resume a big idle agent.
- Five Opus authors in parallel hit the 5-hour window in ~1.5 h; verifiers then hit it again. → concurrency cap,
  `model: "sonnet"` for checking.
- The main context reached ~235k per call from subagent reports, screenshots (~1.5k tokens each) and debugging
  of the check script. → short reports, screenshots only of changed figures, ledger tools instead of ad-hoc scripts.
- Output tokens (the HTML itself, ~1.3 MB written) were a small share. Writing less text saves little; re-reading context saves a lot.

Rough planning numbers for a `lean` run of the same page: one authoring context (you), 5–6 sequential Sonnet verifiers,
one Sonnet re-verification round — expect well under half of the spend above, at the price of a longer wall-clock time.
