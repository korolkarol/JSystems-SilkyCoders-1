---
name: E2E Playwright setup
description: How Playwright Java is configured and run in this project; known bugs exposed by tests
type: project
---

## JUnit 5 + Playwright Java E2E tests

Tests live in `src/test/kotlin/com/lppsa/e2e/`. They use `com.microsoft.playwright:playwright:1.51.0` (declared in `build.gradle.kts`).

Run: `./gradlew test --tests "com.lppsa.e2e.*"` (requires `dangerouslyDisableSandbox: true` because Playwright downloads browsers to `~/.cache/ms-playwright`).

`@SpringBootTest(webEnvironment = DEFINED_PORT)` + `@LocalServerPort` — Playwright connects to the running app on the injected port.

Browser setup: `Playwright.create()` → `playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))`. `@BeforeAll`/`@AfterAll` on `@TestInstance(PER_CLASS)`.

**Known bug exposed by test:** `IntakePage.kt` file input has `name="photo"` but `IntakeController` `@RequestPart` expects `"image"`. This causes POST `/submit` to return 400 ("Zdjęcie produktu jest wymagane.") even with a file attached. Test `filling form and submitting sends POST to submit and updates decision container` will FAIL until fixed.

**Playwright browser download:** First run downloads ~270 MB of browsers to `~/.cache/ms-playwright`. Sandbox must be disabled for this.

**Why:** JUnit 5 + Playwright Java was chosen (not TypeScript) to stay in the Kotlin/JVM ecosystem. The bug in the file input name was intentionally tested to expose it.

**How to apply:** When writing new E2E tests, always check that controller `@RequestPart` names match the HTML `name` attributes. Use `dangerouslyDisableSandbox: true` when running Gradle tests with Playwright.
