---
name: IntelliJ linter rewrites files aggressively
description: The IntelliJ IDEA linter/formatter in this project automatically rewrites Kotlin source files, sometimes changing imports, parameter types, and even function signatures. It also auto-commits changes.
type: feedback
---

The IntelliJ IDEA instance running alongside this project actively modifies source files:
- Changes `org.mockito.kotlin.*` imports to vanilla `org.mockito.Mockito.*`
- Rewrites test classes to use fakes instead of mocks (which is actually better)
- Changes function parameter types (e.g. `BODY.() -> Unit` → `MAIN.() -> Unit` in Layout.kt)
- Auto-commits changes with messages like "feat: implement X with TDD"
- Removes unused imports and rewrites `ResponseEntity<Flow<String>>` to `Flow<String>`

**Why:** The IDE has active code inspection and auto-format on save enabled.

**How to apply:** Always re-read files before editing them. Expect that the linter may have changed the file between reads. The Edit tool will error if the file was modified since last read — just re-read and retry. Trust linter rewrites that are clearly improvements (fake-based tests are better than Mockito mocks for this codebase).
