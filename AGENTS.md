# Repository Guidelines

**Primary references**: read `docs/PRD.md` and `docs/ADR.md` before making changes.

## Project Overview

Advisory AI chat for Sinsay customers to submit a complaint (Reklamacja) or return (Zwrot) request, receive a streamed LLM decision with image evaluation, and ask follow-up questions — Polish language only, no backend integration.

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin (JVM 21) |
| Framework | Spring Boot 3 + WebFlux (Reactor Netty, non-blocking) |
| AI | Spring AI → OpenRouter (`openai/gpt-5.4-mini`) via OpenAI-compatible client |
| UI | kotlinx.html DSL + HTMX 2 + vanilla JS (no frontend build step) |
| Persistence | SQLite via Spring JDBC + Flyway migrations |
| Build | Gradle 8 (Kotlin DSL) via Gradle wrapper (`./gradlew`) |

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

**Streaming**: Spring AI returns `Flux<String>`; convert with `.asFlow()` (from `kotlinx-coroutines-reactor`). Controllers return `Flow<String>` — WebFlux handles it natively. Never use Reactor operators in application code.

**Policy routing** (deterministic, never mix):
- Reklamacja session → `policy/regulamin.md` + `policy/reklamacje.md`
- Zwrot session → `policy/regulamin.md` + `policy/zwrot-30-dni.md`

**Images**: received as multipart, wrapped in Spring AI `Media`, passed inline to the LLM — discarded after the call, never stored.

**JDBC in reactive stack**: Spring JDBC is blocking — offload to `Schedulers.boundedElastic()`.

**Language**: all UI labels, error messages, and AI responses must be in Polish.

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
