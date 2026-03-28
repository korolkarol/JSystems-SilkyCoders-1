---
name: E2E Playwright setup
description: How Playwright is configured and run in this project without a local npm install
type: project
---

Playwright test runner is available globally at `/home/karol/.nvm/versions/node/v24.14.1/lib/node_modules/@playwright/cli/node_modules/playwright/cli.js`. No `npm install` is possible (read-only npm cache).

Tests resolve `playwright/test` via symlinks in `node_modules/playwright/` pointing to the global installation.

Run command: `NODE_PATH=/home/karol/.nvm/versions/node/v24.14.1/lib/node_modules node <global-playwright-cli> test`

Or via npm: `npm run test:e2e` / `npm run test:e2e:list`

**Why:** npm cache is read-only in the sandbox; global `@playwright/cli` package is pre-installed. The sandbox also blocks writes to `/tmp/claude` (OS tmpdir), so `dangerouslyDisableSandbox: true` is needed when running the playwright test runner.

**How to apply:** Always use the global playwright cli path and set `NODE_PATH` when running or listing tests. New test files should import from `playwright/test` — resolved via the local symlink.
