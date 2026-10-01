// Click every control of every figure (dark mode, desktop) and fail on any JS error.
// Covers what the auto-demo in first() does not: each [data-act] button (reset last) and plain buttons inside
// .calls / .scen / .pat / .opts-like groups. Saves one screenshot per figure after all clicks.
// Usage: node click-through.cjs <page.html> [outDir=./shots] [--light]
const path = require('path');
const fs = require('fs');
const { launch } = require('./_browser.cjs');

const file = path.resolve(process.argv[2] || 'index.html');
const out = path.resolve(process.argv[3] && !process.argv[3].startsWith('--') ? process.argv[3] : 'shots');
const scheme = process.argv.includes('--light') ? 'light' : 'dark';
const WAIT = +process.env.CLICK_WAIT_MS || 3000;   // longest single animation after a click
fs.mkdirSync(out, { recursive: true });

(async () => {
  const b = await launch();
  const p = await b.newPage({ viewport: { width: 1400, height: 900 }, colorScheme: scheme });
  const errs = [];
  p.on('pageerror', e => errs.push('pageerror: ' + e.message));
  p.on('console', m => { if (m.type() === 'error') errs.push('console: ' + m.text()); });
  await p.goto('file://' + file);
  const figs = await p.$$eval('figure[id], .fig[id]', fs => fs.map(f => f.id));
  for (const id of figs) {
    await p.evaluate(id => document.getElementById(id).scrollIntoView({ block: 'center' }), id);
    await p.waitForTimeout(2500);                                  // let first() start
    const acts = await p.$$eval(`#${id} [data-act]`, bs => bs.map(b => b.dataset.act));
    for (const a of acts.filter(a => a !== 'reset')) {
      await p.click(`#${id} [data-act="${a}"]`); await p.waitForTimeout(WAIT);
    }
    const extra = `#${id} button:not([data-act]):not(:disabled)`;
    const n = await p.$$eval(extra, bs => bs.length);
    for (let i = 0; i < n; i++) { await p.locator(extra).nth(i).click().catch(() => {}); await p.waitForTimeout(WAIT / 2); }
    // selects and checkboxes: toggle once
    for (const sel of await p.$$(`#${id} select`)) {
      const vals = await sel.$$eval('option', os => os.map(o => o.value));
      for (const v of vals) { await sel.selectOption(v); await p.waitForTimeout(WAIT / 2); }
    }
    for (const cb of await p.$$(`#${id} input[type=checkbox]`)) { await cb.click().catch(() => {}); await p.waitForTimeout(300); }
    await p.locator('#' + id).screenshot({ path: `${out}/${scheme}-click-${id}.png` });
    if (acts.includes('reset')) { await p.click(`#${id} [data-act=reset]`); await p.waitForTimeout(400); }
  }
  console.log(`figures=${figs.length} errors=${errs.length}`);
  errs.slice(0, 10).forEach(e => console.log('   ', e));
  await b.close();
  process.exit(errs.length ? 1 : 0);
})().catch(e => { console.error(e); process.exit(2); });
