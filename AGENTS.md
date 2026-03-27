# Repository Guidelines

**Primary references**: read `docs/PRD.md` and `docs/ADR.md` before making changes.

## Project Overview

Advisory AI chat for Sinsay customers to submit a complaint (Reklamacja) or return (Zwrot) request, receive a streamed
LLM decision with image evaluation, and ask follow-up questions — Polish language only, no backend integration.

## Tech Stack

| Layer       | Choice                                                                      |
|-------------|-----------------------------------------------------------------------------|
| Language    | Kotlin (JVM 21)                                                             |
| Framework   | Spring Boot 3 + WebFlux (Reactor Netty, non-blocking)                       |
| AI          | Spring AI → OpenRouter (`openai/gpt-5.4-mini`) via OpenAI-compatible client |
| UI          | kotlinx.html DSL + HTMX 2 + vanilla JS (no frontend build step)             |
| Persistence | SQLite via Spring JDBC + Flyway migrations                                  |
| Build       | Gradle 8 (Kotlin DSL) via Gradle wrapper (`./gradlew`)                      |

## Architecture

Clean Architecture enforced by package convention (`com.lppsa`):

```
domain/          ← pure Kotlin, no frameworks
  model/         Session, ChatMessage, Decision (sealed), RequestType (enum)
  port/          SessionRepository, ChatRepository, EvaluationPort (interfaces)
application/     ← orchestration, depends on domain only
  usecase/       SubmitRequestUseCase, SendChatMessageUseCase
infrastructure/  ← implements domain ports
  ai/            SpringAiEvaluationAdapter
  persistence/   JdbcSessionRepository, JdbcChatRepository
  config/        AiConfig (ChatClient bean, policy loader)
presentation/    ← HTTP layer
  web/           IntakeController (POST /submit), ChatController (POST /chat/{id})
  html/          kotlinx.html page renderers
```

Dependency rule: `presentation → application → domain ← infrastructure`

## Key Implementation Rules

**Streaming**: Spring AI returns `Flux<String>`; convert with `.asFlow()` (from `kotlinx-coroutines-reactor`).
Controllers return `Flow<String>` — WebFlux handles it natively. Never use Reactor operators in application code.

**Policy routing** (deterministic, never mix):

- Reklamacja session → `policy/regulamin.md` + `policy/reklamacje.md`
- Zwrot session → `policy/regulamin.md` + `policy/zwrot-30-dni.md`

**Images**: received as multipart, wrapped in Spring AI `Media`, passed inline to the LLM — discarded after the call,
never stored.

**JDBC in reactive stack**: Spring JDBC is blocking — offload to `Schedulers.boundedElastic()`.

**Language**: all UI labels, error messages, and AI responses must be in Polish.

## Workflow

1. Read `docs/PRD.md` and `docs/ADR.md` before making changes.
2. Define expected behavior from the specification before touching code.
3. Use TDD for every feature and bug fix (see TDD Rules below).
4. Verify with `./gradlew test` before committing.
5. Commit only when the changed scope is in a working state — keep commits focused and granular.
6. Do not push to remote unless the user explicitly asks.

## TDD Rules

For every feature or bug fix:

1. Start from the specification, not from the existing implementation.
2. Write or extend tests before production code.
3. Run the new tests and confirm they fail for the expected reason.
4. Implement the minimum code needed to make them pass.
5. Run `./gradlew test` to verify the full suite.
6. Refactor only with tests still green.

If the project does not yet have suitable test infrastructure for the changed area, add it as part of the task instead
of silently skipping tests.

## Forbidden shortcuts

changing assertions to match broken behavior
removing assertions that expose a real defect
adding fallback assertions that allow multiple incompatible outputs
mocking away the unit or flow that is supposed to be tested
skipping a failing test without a justified reason in the code and task report
If a test fails, fix the production code or rewrite the test because the requirement was wrong or changed. Do not edit
the test to accept incorrect behavior.

## Test Honesty Rules

Every new or modified test must be specific enough that it would fail if the related production behavior breaks. After
tests pass, verify by intentionally breaking or removing the relevant implementation, confirming the test fails, then
restoring the code.

## Build & Run

```bash
./gradlew bootRun        # dev server at http://localhost:8080
./gradlew test           # run all tests
./gradlew build          # compile, test, assemble fat JAR
```

Requires `.env` in project root:

```
OPENROUTER_API_KEY=sk-or-...
```

## Database

Flyway manages schema. Migration: `src/main/resources/db/migration/V1__init.sql`.
Tables: `sessions` (one row per form submission) and `chat_messages` (follow-up Q&A).
`sessions.decision_outcome` is NULL during streaming; updated atomically on stream completion.
