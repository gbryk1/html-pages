// Headless smoke test for an animated knowledge page.
// Usage: node check-page.cjs <page.html> [outDir=./shots]
// Runs: light 1400px, dark, mobile 390px, reduced motion, and (multi-level pages) depth 1 on mobile.
// Browser discovery: see _browser.cjs (CHROME_PATH, Playwright's Chromium, or an installed Chrome).
// In a lib-less Linux sandbox export LD_LIBRARY_PATH and FONTCONFIG_FILE first (reference.md §14).
const path = require('path');
const fs = require('fs');
const { launch } = require('./_browser.cjs');

const file = path.resolve(process.argv[2] || 'index.html');
const out = path.resolve(process.argv[3] || 'shots');
fs.mkdirSync(out, { recursive: true });
const url = 'file://' + file;
const WAIT = +process.env.FIG_WAIT_MS || 4000; // longer than the slowest first() delay
let failed = false;

async function run(browser, name, opts, shots, depth1 = false) {
  const p = await browser.newPage(opts);
  const errs = [];
  p.on('pageerror', e => errs.push('pageerror: ' + e.message));
  p.on('console', m => { if (m.type() === 'error') errs.push('console: ' + m.text()); });
  // Count DOM/attribute mutations per figure so we know animations really ran.
  // The observer attaches at DOMContentLoaded, i.e. after the page's own init render,
  // so any count > 0 means something moved after load (scroll trigger, loop, timer).
  await p.addInitScript(() => {
    window.__mut = {};
    addEventListener('DOMContentLoaded', () => {
      document.querySelectorAll('figure[id], .fig[id]').forEach(f => {
        window.__mut[f.id] = 0;
        new MutationObserver(ms => { window.__mut[f.id] += ms.length; })
          .observe(f, { subtree: true, childList: true, attributes: true, characterData: true });
      });
    });
  });
  await p.goto(url);
  // Multi-level pages: optionally switch the reader depth to 1 (essentials) before checking.
  if (depth1) await p.evaluate(() => { const b = document.querySelector('#depth button'); if (b) b.click(); });
  // Only figures that are displayed at the current depth are expected to animate.
  const ids = await p.evaluate(() => Object.keys(window.__mut).filter(id => document.getElementById(id).offsetParent !== null));
  const dead = [];
  for (const id of ids) {
    await p.evaluate(id => document.getElementById(id).scrollIntoView({ block: 'center' }), id);
    await p.waitForTimeout(WAIT);
    const moved = await p.evaluate(id => window.__mut[id] > 0 ||
      document.getAnimations().some(a => a.effect && document.getElementById(id).contains(a.effect.target)), id);
    // Canvas figures draw without DOM mutations: accept a non-blank canvas instead.
    const canvasInk = await p.evaluate(id => [...document.querySelectorAll(`#${id} canvas`)].some(c => {
      const d = c.getContext('2d').getImageData(0, 0, c.width, c.height).data;
      for (let i = 3; i < d.length; i += 4) if (d[i]) return true;
      return false;
    }), id);
    if (!moved && !canvasInk) dead.push(id);
    if (shots) {  // a figure taller than the viewport never becomes "stable" for locator.screenshot: grow the viewport for the shot
      const vp = p.viewportSize(); const h = await p.evaluate(id => document.getElementById(id).getBoundingClientRect().height, id);
      if (h > vp.height - 20) await p.setViewportSize({ width: vp.width, height: Math.ceil(h) + 60 });
      await p.locator('#' + id).screenshot({ path: `${out}/${name}-${id}.png` });
      if (h > vp.height - 20) await p.setViewportSize(vp);
    }
  }
  await p.evaluate(() => scrollTo(0, 0));
  await p.waitForTimeout(500);
  await p.screenshot({ path: `${out}/${name}-top.png` });
  const overflow = await p.evaluate(() => {
    const W = document.documentElement.clientWidth;
    return [...document.querySelectorAll('body *')]
      .filter(e => { const r = e.getBoundingClientRect(); return r.width && r.right > W + 1; })
      .filter(e => !e.closest('.scroll-x, pre'))          // scrollable containers are fine
      .slice(0, 8)
      .map(e => `${e.tagName.toLowerCase()}${e.id ? '#' + e.id : ''}.${[...e.classList].join('.')} right=${Math.round(e.getBoundingClientRect().right)}`);
  });
  const sw = await p.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  console.log(`[${name}] figures=${ids.length} errors=${errs.length} noAnimation=${JSON.stringify(dead)} hOverflow=${sw}px`);
  errs.forEach(e => console.log('   ', e));
  if (sw > 0) overflow.forEach(o => console.log('    overflow:', o));
  if (sw > 0 && !overflow.length) console.log('    overflow comes from unbreakable text (long URL/identifier), not an element box: add overflow-wrap:break-word');
  if (errs.length || dead.length || sw > 0) failed = true;
  await p.close();
}

(async () => {
  const browser = await launch();
  await run(browser, 'light', { viewport: { width: 1400, height: 900 }, colorScheme: 'light' }, true);
  await run(browser, 'dark', { viewport: { width: 1400, height: 900 }, colorScheme: 'dark' }, false);
  await run(browser, 'mobile', { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true }, true);
  await run(browser, 'reduced', { viewport: { width: 1000, height: 800 }, reducedMotion: 'reduce' }, false);
  // Multi-level page: essentials-only reading must also be clean (no errors, no overflow, no dead figures).
  const probe = await browser.newPage(); await probe.goto(url);
  const hasDepth = await probe.evaluate(() => !!document.querySelector('#depth button')); await probe.close();
  if (hasDepth) await run(browser, 'depth1', { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true }, false, true);
  await browser.close();
  console.log(failed ? 'FAIL' : 'OK', '— screenshots in', out);
  process.exit(failed ? 1 : 0);
})().catch(e => { console.error(e); process.exit(2); });
