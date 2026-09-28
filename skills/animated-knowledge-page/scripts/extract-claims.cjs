// Claims ledger for double validation.
//
// Extract: node extract-claims.cjs <page.html> <outDir> [--split]
//   Renders the page at full depth with every "go deeper" block open, and lists everything a reader could take as
//   a fact: paragraphs, list items, table rows, code blocks, captions, Q/A, dialogue lines, the quiz (question +
//   correct answer + explanation) and prose-like string literals in the inline script (status narrations, etc.).
//   Writes <outDir>/claims.md (or one file per chapter with --split, for parallel verifiers) + claims.json.
//
// Carry:  node extract-claims.cjs <page.html> <outDir> [--split] --carry <oldDir>
//   After extracting, copies pass-1/pass-2 lines from an older ledger for every claim whose text is unchanged,
//   so only new or edited claims need checking again.
// Verifier export / merge (keeps pass-1 notes away from the independent verifier):
//   node extract-claims.cjs --export <ledgerDir> <outDir> [--open-only]   pass-1 hidden; --open-only = only claims whose pass 2 is empty/⚠️/❌/❓
//   node extract-claims.cjs --merge  <verifierDir> <ledgerDir>             copies filled pass-2 lines back by claim ID
// Headings (h3) and <summary> lines are pre-marked n/a in both passes: they are labels, not claims.
//
// Status: node extract-claims.cjs --status <outDir>
//   Counts verdicts in the ledger files. Exit 1 while any claim is unchecked, ❌ wrong, ❓ unverifiable or ⚠️ imprecise.
//
// Ledger line format (filled in by the author = pass 1, and by an independent verifier = pass 2):
//   - pass 1: ✅ <source URL or file:line> — <short quote>
//   - pass 2: ✅|⚠️|❌|❓|n/a <evidence found independently> — <fix if not ✅>
// n/a is for pedagogy, opinion, or numbers already labelled illustrative on the page.
const path = require('path');
const fs = require('fs');


const BLOCK = /### (C\d+) ([^\n]*)\n((?:>[^\n]*\n)+)- pass 1:([^\n]*)\n- pass 2:([^\n]*)/g;
const mdFiles = d => fs.readdirSync(d).filter(f => f.endsWith('.md')).map(f => path.join(d, f));
const isOpen2 = v => { v = v.trim(); return !v || ['⚠️', '❌', '❓'].some(t => v.startsWith(t)); };
if (process.argv[2] === '--export') {
  const [src, dst] = [path.resolve(process.argv[3]), path.resolve(process.argv[4])];
  const openOnly = process.argv.includes('--open-only');
  fs.mkdirSync(dst, { recursive: true }); let n = 0;
  for (const f of mdFiles(src)) {
    const blocks = [];
    for (const m of fs.readFileSync(f, 'utf8').matchAll(BLOCK)) {
      if (openOnly && !isOpen2(m[5])) continue;
      blocks.push(`### ${m[1]} ${m[2]}\n${m[3]}- pass 1: (hidden from verifier)\n- pass 2:${openOnly ? '' : m[5]}\n`); n++;
    }
    if (blocks.length) fs.writeFileSync(path.join(dst, path.basename(f)), `# ${path.basename(f)} — for verifier\n\n` + blocks.join('\n'));
  }
  console.log(`${n} claims exported → ${dst}`); process.exit(0);
}
if (process.argv[2] === '--merge') {
  const [src, dst] = [path.resolve(process.argv[3]), path.resolve(process.argv[4])];
  const p2 = {};
  for (const f of mdFiles(src)) for (const m of fs.readFileSync(f, 'utf8').matchAll(BLOCK)) if (m[5].trim()) p2[m[1]] = m[5].trim();
  let n = 0;
  for (const f of mdFiles(dst)) {
    const s = fs.readFileSync(f, 'utf8').replace(BLOCK, (all, id, h, q, a, b) => p2[id] ? (n++, `### ${id} ${h}\n${q}- pass 1:${a}\n- pass 2: ${p2[id]}`) : all);
    fs.writeFileSync(f, s);
  }
  console.log(`merged ${n} pass-2 verdicts into ${dst}`); process.exit(0);
}

if (process.argv[2] === '--status') {
  const dir = path.resolve(process.argv[3] || 'claims');
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.md')).map(f => path.join(dir, f));
  const tally = { unchecked: 0, '✅': 0, '⚠️': 0, '❌': 0, '❓': 0, 'n/a': 0 };
  const open = [];
  for (const f of files) {
    const blocks = fs.readFileSync(f, 'utf8').split(/^### /m).slice(1);
    for (const b of blocks) {
      const id = b.split(' ')[0];
      for (const pass of ['pass 1', 'pass 2']) {
        const line = (b.match(new RegExp(`^- ${pass}:(.*)$`, 'm')) || [, ''])[1].trim();
        const v = ['✅', '⚠️', '❌', '❓', 'n/a'].find(t => line.startsWith(t));
        if (pass === 'pass 2') {
          tally[v || 'unchecked']++;
          if (!v || ['⚠️', '❌', '❓'].includes(v)) open.push(`${id} ${pass}: ${v || 'unchecked'} ${path.basename(f)}`);
        } else if (!v) open.push(`${id} pass 1: unchecked ${path.basename(f)}`);
      }
    }
  }
  console.log(Object.entries(tally).map(([k, v]) => `${k}=${v}`).join('  '));
  open.slice(0, 40).forEach(o => console.log('  open:', o));
  if (open.length > 40) console.log(`  … ${open.length - 40} more`);
  console.log(open.length ? `OPEN ${open.length}` : 'ALL CLAIMS VALIDATED TWICE');
  process.exit(open.length ? 1 : 0);
}

const { launch } = require('./_browser.cjs');
// Prose-like string literals from the inline script (status narrations and labels that appear only after clicks),
// attributed to the nearest preceding section comment such as /* ---------- 3. context window ---------- */.
// A small tokenizer (comments, '…', "…", `…` with ${} nesting) instead of a regex: apostrophes in prose broke regexes.
function scriptStrings(src) {
  const res = []; let fig = 'script', i = 0;
  const clean = t => t.replace(/<[^>]+>/g, '').replace(/\\(['"`])/g, '$1').replace(/\s+/g, ' ').trim();
  const push = raw => {
    if (/quiz/i.test(fig)) return;                           // quiz is captured rendered
    const t = clean(raw.replace(/\$\{[^}]*\}/g, '…'));
    if (t.length < 30 || t.split(' ').length < 5) return;
    if (!/[a-z]{3,} [a-z]{2,} [a-z]{2,}/i.test(t)) return;   // needs some prose
    if (/^[\s\w.#:,>\-[\]()*=]+$/.test(t) && /[#.][\w-]+\s*[,>]/.test(t)) return; // CSS selector lists
    res.push({ where: 'js:' + fig, level: '-', kind: 'script', text: t });
  };
  while (i < src.length) {
    const c = src[i], n = src[i + 1];
    if (c === '/' && n === '*') {
      const j = src.indexOf('*/', i + 2); const body = src.slice(i + 2, j < 0 ? src.length : j);
      const m = body.match(/^\s*-{3,}\s*(.+?)\s*-{3,}\s*$/); if (m) fig = m[1];
      i = j < 0 ? src.length : j + 2; continue;
    }
    if (c === '/' && n === '/') { const j = src.indexOf('\n', i); i = j < 0 ? src.length : j; continue; }
    if (c === "'" || c === '"') {
      let j = i + 1, buf = '';
      while (j < src.length && src[j] !== c && src[j] !== '\n') { if (src[j] === '\\') { buf += src[j] + src[j + 1]; j += 2; continue; } buf += src[j++]; }
      push(buf); i = j + 1; continue;
    }
    if (c === '`') {
      let j = i + 1, buf = '', depth = 0;
      while (j < src.length) {
        if (src[j] === '\\') { buf += src[j] + src[j + 1]; j += 2; continue; }
        if (depth === 0 && src[j] === '`') break;
        if (src[j] === '$' && src[j + 1] === '{') { depth++; buf += '${'; j += 2; continue; }
        if (depth > 0 && src[j] === '}') { depth--; buf += '}'; j++; continue; }
        buf += src[j++];
      }
      push(buf); i = j + 1; continue;
    }
    i++;
  }
  return res;
}

const file = path.resolve(process.argv[2] || 'index.html');
const out = path.resolve(process.argv[3] || 'claims');
const split = process.argv.includes('--split');
const carryDir = process.argv.includes('--carry') ? path.resolve(process.argv[process.argv.indexOf('--carry') + 1]) : null;
fs.mkdirSync(out, { recursive: true });

(async () => {
  const b = await launch();
  const p = await b.newPage({ viewport: { width: 1400, height: 900 } });
  await p.goto('file://' + file);
  const { out: items, src } = await p.evaluate(() => {
    // full depth, everything open, every quiz question answered once to reveal the right option
    const btns = [...document.querySelectorAll('#depth button')]; if (btns.length) btns[btns.length - 1].click();
    document.querySelectorAll('details').forEach(d => d.open = true);
    document.querySelectorAll('.quiz-q').forEach(q => { const b = q.querySelector('.opts button'); if (b) b.click(); });
    const clean = t => t.replace(/\s+/g, ' ').trim();
    const out = [];
    const where = el => { const s = el.closest('section[id]'); return s ? s.id : 'page'; };
    const level = el => { const l = el.closest('[data-level]'); return l ? l.dataset.level : '1'; };
    const SEL = 'main p, main li, main tr, main figcaption, main pre, main .qa > div:not(.q):not(.a), main .fire .line, main summary, main h3';
    const picked = new Set();
    for (const el of document.querySelectorAll(SEL)) {
      if (el.closest('#map, #quiz, #toc, .ctrl, .legend')) continue;
      if ([...picked].some(a => a.contains(el))) continue;       // parent already taken
      const t = clean(el.tagName === 'TR' ? [...el.children].map(c => c.textContent).join(' | ') : el.textContent);
      if (t.length < 12 || el.closest('thead')) continue;
      picked.add(el);
      out.push({ where: where(el), level: level(el), kind: el.tagName.toLowerCase(), text: t });
    }
    document.querySelectorAll('.quiz-q').forEach(q => out.push({
      where: 'quiz', level: '1', kind: 'quiz',
      text: clean(`${q.querySelector('h4').textContent} → ${q.querySelector('.right')?.textContent || '?'} — ${q.querySelector('.why')?.textContent || ''}`)
    }));
    const src = [...document.querySelectorAll('script:not([src])')].map(s => s.textContent).join('\n');
    return { out, src };
  });
  await b.close();
  items.push(...scriptStrings(src));
  items.forEach((it, i) => { it.id = 'C' + String(i + 1).padStart(4, '0'); });
  fs.writeFileSync(path.join(out, 'claims.json'), JSON.stringify(items, null, 1));
  const carried = {}; let nCarried = 0;
  if (carryDir) for (const f of mdFiles(carryDir)) for (const m of fs.readFileSync(f, 'utf8').matchAll(BLOCK))
    if (m[4].trim() || m[5].trim()) carried[m[3]] = [m[4].trim(), m[5].trim()];
  const groups = split ? items.reduce((g, it) => ((g[it.where.replace(/[^\w-]+/g, '_')] ||= []).push(it), g), {}) : { claims: items };
  for (const [name, list] of Object.entries(groups)) {
    const md = [`# Claims ledger — ${path.basename(file)} — ${name}`, '',
      'Verdicts: ✅ confirmed · ⚠️ imprecise (give fix) · ❌ wrong (give fix) · ❓ unverifiable (soften or remove) · n/a pedagogy/opinion/labelled illustrative', ''];
    for (const it of list) {
      const auto = ['h3', 'summary'].includes(it.kind) ? ' n/a (heading, auto)' : '';
      const old = carried[`> ${it.text}\n`];
      md.push(`### ${it.id} · ${it.where} · L${it.level} · ${it.kind}`, `> ${it.text}`,
        '- pass 1:' + (old ? ' ' + old[0] : auto), '- pass 2:' + (old ? ' ' + old[1] : auto), '');
      if (old) nCarried++;
    }
    fs.writeFileSync(path.join(out, `${name}.md`), md.join('\n'));
  }
  const byWhere = items.reduce((a, it) => (a[it.where] = (a[it.where] || 0) + 1, a), {});
  console.log(`${items.length} claims → ${out}` + (carryDir ? ` (${nCarried} verdicts carried from ${carryDir})` : ''));
  Object.entries(byWhere).forEach(([k, v]) => console.log(`  ${k}: ${v}`));
})().catch(e => { console.error(e); process.exit(2); });
