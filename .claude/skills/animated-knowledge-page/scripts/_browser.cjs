// Shared browser loader for check-page.cjs and click-through.cjs.
// Finds playwright-core (local, NODE_PATH, global, or bundled with OpenClaw) and a Chromium to drive:
//   1. CHROME_PATH if set,
//   2. Playwright's own downloaded Chromium (~/.cache/ms-playwright, ~/Library/Caches/ms-playwright),
//   3. an installed Chrome / Chromium / Edge at a well-known path.
const path = require('path');
const fs = require('fs');
const { execSync } = require('child_process');

function loadPlaywright() {
  const tries = [() => require('playwright-core'), () => require('playwright')];
  try {
    const root = execSync('npm root -g', { stdio: ['ignore', 'pipe', 'ignore'] }).toString().trim();
    tries.push(() => require(path.join(root, 'playwright-core')),
               () => require(path.join(root, 'openclaw/node_modules/playwright-core')));
  } catch {}
  for (const t of tries) { try { return t(); } catch {} }
  console.error('playwright-core not found. Install it somewhere and point NODE_PATH at that node_modules, e.g.\n' +
    '  npm i --prefix "$TMPDIR/pw" playwright-core && export NODE_PATH="$TMPDIR/pw/node_modules"');
  process.exit(2);
}

const KNOWN = [
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/Applications/Chromium.app/Contents/MacOS/Chromium',
  '/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge',
  '/usr/bin/google-chrome', '/usr/bin/chromium', '/usr/bin/chromium-browser',
  'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
];

async function launch(extra = {}) {
  const { chromium } = loadPlaywright();
  const args = ['--disable-gpu'];
  if (process.env.CHROME_PATH) return chromium.launch({ args, executablePath: process.env.CHROME_PATH, ...extra });
  try { return await chromium.launch({ args, ...extra }); } catch (e) {
    const found = KNOWN.find(p => fs.existsSync(p));
    if (!found) throw new Error('No Chromium found. Run `npx playwright install chromium` or set CHROME_PATH.\n' + e.message);
    return chromium.launch({ args, executablePath: found, ...extra });
  }
}

module.exports = { launch };
