# PoC Implementation Plan — Sinsay AI Complaint & Return Chat

## Context

The project skeleton exists (Spring Boot 3 + WebFlux, Kotlin, kotlinx.html, Spring AI, SQLite/Flyway). Only a placeholder landing page is implemented. The goal is to build the full PoC: intake form → AI decision (streaming SSE) → post-decision chat — following the PRD, ADR, and Sinsay Design Guidelines.

User decisions:
- Landing page **replaced** by the intake form (GET / serves IntakePage)
- E2E tests written **alongside** each frontend feature
- dotenv-kotlin **skipped** — `OPENROUTER_API_KEY` injected externally

---

## Dependency Matrix

```
Phase 0 (Foundation) ──► Phase 1 (Domain)
                                │
                    ┌───────────┴────────────┐
                    ▼                        ▼
          Phase 2 (Persistence)     Phase 3 (AI Infra)
                    │                        │
                    └───────────┬────────────┘
                                ▼
                       Phase 4 (Use Cases)
                                │
                                ▼
                       Phase 5 (Controllers)
                          │              │
               ┌──────────┘              └──────────┐
               ▼                                    ▼
     Phase 6 (Frontend)                  Phase 7 (E2E Tests)
     [6.1→6.2→6.3 sequential]            [runs alongside 6.x]
     [6.4 needs 5.1+5.2]
```

**Parallelism opportunities:**
- Phase 2 and Phase 3 run in parallel (both depend only on Phase 0+1)
- fe-developer starts Phase 6.1+6.2+6.3 while be-developer builds Phase 4+5
- qa-engineer starts E2E tests after Phase 5.1 is done and Phase 6.2 is done

---

## Phase 0 — Foundation (be-developer)

### Task 0.1 — Flyway migration V1__init.sql

**Agent:** be-developer
**Files:** `src/main/resources/db/migration/V1__init.sql`
**Depends on:** nothing

**TDD:**
1. Write a Spring Boot test (`JdbcSessionRepositoryTest` stub) that expects tables `sessions` and `chat_messages` to exist — fails at compilation since repositories don't exist yet. (Stub the test class with a `@Test fun tablesExist()` that just asserts table count > 0 via JdbcTemplate.)
2. Create the migration file with the exact schema from the ADR:

```sql
CREATE TABLE sessions (
    id TEXT PRIMARY KEY,
    request_type TEXT NOT NULL CHECK (request_type IN ('REKLAMACJA', 'ZWROT')),
    product_name TEXT NOT NULL,
    purchase_date TEXT NOT NULL,
    description TEXT NOT NULL,
    decision_outcome TEXT CHECK (decision_outcome IN ('ACCEPT', 'REJECT')),
    decision_explanation TEXT,
    mismatch_recommendation TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE chat_messages (
    id TEXT PRIMARY KEY,
    session_id TEXT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('USER', 'ASSISTANT')),
    content TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX idx_chat_messages_session ON chat_messages (session_id, created_at);
```

3. Run `./gradlew test` — stub test should pass (tables exist after Flyway migration).
4. **Commit:** `feat: add Flyway migration V1 for sessions and chat_messages tables`

---

### Task 0.2 — Copy policy documents to classpath

**Agent:** be-developer
**Files:** `src/main/resources/policy/regulamin.md`, `policy/reklamacje.md`, `policy/zwrot-30-dni.md`
**Depends on:** nothing (can run in parallel with 0.1)

**Steps:**
1. Create directory `src/main/resources/policy/`
2. Copy `docs/regulamin.md` → `src/main/resources/policy/regulamin.md`
3. Copy `docs/reklamacje.md` → `src/main/resources/policy/reklamacje.md`
4. Copy `docs/zwrot-30-dni.md` → `src/main/resources/policy/zwrot-30-dni.md`
4. Write a test `PolicyDocumentLoadTest` that loads each file as `ClassPathResource` and asserts non-empty content.
5. Run `./gradlew test` — passes.
6. **Commit:** `feat: copy policy documents to classpath resources`

---

## Phase 1 — Domain Layer (be-developer)

All domain files: `src/main/kotlin/com/lppsa/domain/`
Zero external imports — pure Kotlin only.

### Task 1.1 — RequestType enum

**File:** `domain/model/RequestType.kt`
**Depends on:** nothing

```kotlin
enum class RequestType { REKLAMACJA, ZWROT }
```

Test: `RequestTypeTest` — asserts both values exist, `valueOf` round-trips correctly.
**Commit:** `feat: add RequestType enum`

---

### Task 1.2 — Decision sealed class

**File:** `domain/model/Decision.kt`
**Depends on:** nothing

```kotlin
sealed class Decision {
    data class Accept(val explanation: String, val nextSteps: String) : Decision()
    data class Reject(
        val explanation: String,
        val mismatchRecommendation: String? = null
    ) : Decision()
}
```

Test: `DecisionTest` — verify `Accept` and `Reject` construction, `copy()`, and null mismatch default.
**Commit:** `feat: add Decision sealed class`

---

### Task 1.3 — Session aggregate root

**File:** `domain/model/Session.kt`
**Depends on:** 1.1, 1.2

```kotlin
data class Session(
    val id: String,
    val requestType: RequestType,
    val productName: String,
    val purchaseDate: String,          // ISO-8601 YYYY-MM-DD
    val description: String,
    val decision: Decision? = null,    // null while streaming
    val createdAt: String,
)
```

Test: `SessionTest` — construction, `copy()` for decision update, null decision default.
**Commit:** `feat: add Session domain model`

---

### Task 1.4 — ChatMessage model

**File:** `domain/model/ChatMessage.kt`
**Depends on:** 1.1

```kotlin
enum class MessageRole { USER, ASSISTANT }

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val createdAt: String,
)
```

Test: `ChatMessageTest` — construction and role enum values.
**Commit:** `feat: add ChatMessage domain model`

---

### Task 1.5 — Port interfaces

**Files:** `domain/port/SessionRepository.kt`, `domain/port/ChatRepository.kt`, `domain/port/EvaluationPort.kt`
**Depends on:** 1.2, 1.3, 1.4

```kotlin
// SessionRepository.kt
interface SessionRepository {
    suspend fun save(session: Session)
    suspend fun findById(id: String): Session?
    suspend fun updateDecision(id: String, decision: Decision)
}

// ChatRepository.kt
interface ChatRepository {
    suspend fun save(message: ChatMessage)
    suspend fun findBySessionId(sessionId: String): List<ChatMessage>
}

// EvaluationPort.kt
interface EvaluationPort {
    fun evaluate(
        requestType: RequestType,
        productName: String,
        purchaseDate: String,
        description: String,
        imageBytes: ByteArray,
        imageMimeType: String,
    ): Flow<String>

    fun chat(
        session: Session,
        history: List<ChatMessage>,
        userMessage: String,
    ): Flow<String>
}
```

Test: Compile-only — write one stub test class per port that just instantiates an anonymous object implementing the interface. Confirms the interface signatures are valid Kotlin.
**Commit:** `feat: add domain port interfaces`

---

## Phase 2 — Infrastructure: Persistence (be-developer)

**Depends on:** Phase 0.1, Phase 1.3–1.6
**Can run in parallel with Phase 3.**

### Task 2.1 — JdbcSessionRepository

**File:** `infrastructure/persistence/JdbcSessionRepository.kt`
**Test:** `infrastructure/JdbcSessionRepositoryTest.kt`

**TDD steps:**
1. Write `JdbcSessionRepositoryTest` with `@SpringBootTest` using in-memory SQLite (`jdbc:sqlite::memory:`). Tests:
    - `save and findById returns session`
    - `findById returns null for unknown id`
    - `updateDecision sets Accept outcome`
    - `updateDecision sets Reject with mismatch`
    - `updateDecision sets Reject without mismatch`
2. Run tests — fail (class doesn't exist).
3. Implement `JdbcSessionRepository` using `JdbcTemplate`. All JDBC calls in `withContext(Dispatchers.IO)`. Map `decision_outcome` / `decision_explanation` / `mismatch_recommendation` columns to/from `Decision`.
4. Run tests — pass.
5. **Commit:** `feat: implement JdbcSessionRepository with tests`

---

### Task 2.2 — JdbcChatRepository

**File:** `infrastructure/persistence/JdbcChatRepository.kt`
**Test:** `infrastructure/JdbcChatRepositoryTest.kt`

**TDD steps:**
1. Write tests:
    - `save and findBySessionId returns messages in order`
    - `findBySessionId returns empty list when no messages`
    - `multiple messages ordered by created_at`
2. Implement `JdbcChatRepository`.
3. Run tests — pass.
4. **Commit:** `feat: implement JdbcChatRepository with tests`

---

## Phase 3 — Infrastructure: AI (be-developer)

**Depends on:** Phase 0.2, Phase 1 (ports)
**Can run in parallel with Phase 2.**

### Task 3.1 — AiConfig

**File:** `infrastructure/config/AiConfig.kt`

**TDD steps:**
1. Write `AiConfigTest` that loads the Spring context and asserts the `ChatClient` bean is non-null, and that `loadPolicy(RequestType.REKLAMACJA)` returns a non-empty string containing content from both `regulamin.md` and `reklamacje.md` (not `zwrot-30-dni.md`), and vice versa for `ZWROT`.
2. Implement `AiConfig`:
    - `@Configuration` class with `@Bean fun chatClient(builder: ChatClient.Builder): ChatClient`
    - `fun loadPolicy(requestType: RequestType): String` — reads `policy/regulamin.md` + either `reklamacje.md` or `zwrot-30-dni.md` from classpath, concatenates
3. Run tests — pass.
4. **Commit:** `feat: add AiConfig with ChatClient bean and policy document loader`

---

### Task 3.2 — SpringAiEvaluationAdapter

**File:** `infrastructure/ai/SpringAiEvaluationAdapter.kt`
**Test:** `infrastructure/SpringAiEvaluationAdapterTest.kt`

**TDD steps:**
1. Write `SpringAiEvaluationAdapterTest` with a mocked `ChatClient` (use `@MockitoBean` or manual mock). Tests:
    - `evaluate() returns Flow<String> from chatClient stream`
    - `evaluate() builds system prompt with REKLAMACJA policy` (verify policy content injected)
    - `evaluate() builds system prompt with ZWROT policy`
    - `evaluate() includes image as Media in user message`
    - `chat() builds system prompt with decision context and history`
    - `chat() returns Flow<String>`
2. Implement `SpringAiEvaluationAdapter`:

```kotlin
@Component
class SpringAiEvaluationAdapter(
    private val chatClient: ChatClient,
    private val aiConfig: AiConfig,
) : EvaluationPort {

    override fun evaluate(...): Flow<String> {
        val systemPrompt = buildEvaluationSystemPrompt(requestType, aiConfig.loadPolicy(requestType))
        val media = Media(MimeType.valueOf(imageMimeType), imageBytes.toResource())
        val userPrompt = buildEvaluationUserPrompt(productName, purchaseDate, description)
        return chatClient.prompt()
            .system(systemPrompt)
            .user { it.text(userPrompt).media(media) }
            .stream().content().asFlow()
    }

    override fun chat(...): Flow<String> {
        // Build system prompt with policy + decision context
        // Build message history from ChatMessage list
        // Return streaming flow
    }
}
```

System prompt must instruct the LLM to:
- Respond in Polish only
- Output ACCEPT (Wniosek wstępnie pozytywny) or REJECT (Wniosek wstępnie negatywny)
- Keep explanation ≤ 200 words
- Include next steps on ACCEPT, reason on REJECT
- Flag mismatch if detected
- Not initiate backend actions

3. Run tests — pass.
4. **Commit:** `feat: implement SpringAiEvaluationAdapter with streaming and tests`

---

## Phase 4 — Application Layer (be-developer)

**Depends on:** Phase 2 (both repos) + Phase 3 (EvaluationPort implementation for wiring)

### Task 4.1 — SubmitRequestUseCase

**File:** `application/usecase/SubmitRequestUseCase.kt`
**Test:** `application/SubmitRequestUseCaseTest.kt`

**TDD steps:**
1. Write tests using mocked `SessionRepository` and `EvaluationPort`:
    - `execute() saves new session with null decision before streaming`
    - `execute() returns Flow<String> from EvaluationPort`
    - `execute() updates session decision when stream completes` (test with `runTest` + `collect`)
    - `execute() sets requestType correctly when routing to EvaluationPort`
2. Implement `SubmitRequestUseCase`:

```kotlin
class SubmitRequestUseCase(
    private val sessionRepository: SessionRepository,
    private val evaluationPort: EvaluationPort,
) {
    fun execute(command: SubmitCommand): Flow<String> = flow {
        val session = Session(id = UUID.randomUUID().toString(), ...)
        sessionRepository.save(session)
        val sb = StringBuilder()
        evaluationPort.evaluate(...).collect { token ->
            sb.append(token)
            emit(token)
        }
        val decision = parseDecision(sb.toString())
        sessionRepository.updateDecision(session.id, decision)
    }
}
```

Also define `SubmitCommand` data class here (requestType, productName, purchaseDate, description, imageBytes, imageMimeType, sessionId output).

3. Run tests — pass.
4. **Commit:** `feat: implement SubmitRequestUseCase with TDD`

---

### Task 4.2 — SendChatMessageUseCase

**File:** `application/usecase/SendChatMessageUseCase.kt`
**Test:** `application/SendChatMessageUseCaseTest.kt`

**TDD steps:**
1. Write tests with mocked repos and EvaluationPort:
    - `execute() loads session by id`
    - `execute() persists user ChatMessage before streaming`
    - `execute() calls EvaluationPort.chat() with session, history, and user message`
    - `execute() persists assistant ChatMessage after stream completes`
    - `execute() throws if session not found`
2. Implement `SendChatMessageUseCase`.
3. Run tests — pass.
4. **Commit:** `feat: implement SendChatMessageUseCase with TDD`

---

## Phase 5 — Presentation: Controllers (be-developer)

**Depends on:** Phase 4

### Task 5.1 — IntakeController

**Files:**
- `presentation/web/IntakeController.kt` (replaces `LandingController.kt`)
- Delete `LandingController.kt` and `LandingPage.kt` (placeholder files)
  **Test:** `presentation/IntakeControllerTest.kt`

**TDD steps:**
1. Write `@WebFluxTest(IntakeController::class)` tests:
    - `GET / returns 200 with Content-Type text/html`
    - `POST /submit with valid multipart returns SSE stream (200, text/event-stream)`
    - `POST /submit without image returns 400`
    - `POST /submit with image > 10MB returns 400 with Polish error`
    - `POST /submit with invalid MIME type returns 400 with Polish error`
    - `POST /submit with missing required field returns 400`
2. Implement `IntakeController`:
    - `GET /` → renders `IntakePage` (initially a stub `renderIntakePage()` placeholder)
    - `POST /submit` → validates multipart (MIME: jpeg/png/webp, size ≤ 10MB), delegates to `SubmitRequestUseCase`
    - All error messages in Polish
3. Run tests — pass.
4. **Commit:** `feat: implement IntakeController with validation and SSE streaming`

---

### Task 5.2 — ChatController

**File:** `presentation/web/ChatController.kt`
**Test:** `presentation/ChatControllerTest.kt`

**TDD steps:**
1. Write tests:
    - `POST /chat/{id} returns SSE stream`
    - `POST /chat/{id} with unknown sessionId returns 404 with Polish message`
    - `POST /chat/{id} with blank message returns 400`
2. Implement `ChatController` delegating to `SendChatMessageUseCase`.
3. Run tests — pass.
4. **Commit:** `feat: implement ChatController with SSE streaming`

---

## Phase 6 — Presentation: Frontend (fe-developer)

### Task 6.1 — Layout.kt (base shell)

**File:** `presentation/html/Layout.kt`
**Depends on:** nothing (static)

**Spec:**
- Sinsay header: white bg, logo SVG (`/logo.svg`, 84×31px, fill `#16181D`), elevation-01 shadow
- `<link rel="stylesheet" href="/css/sinsay.css">`
- HTMX 2.x CDN script + SSE extension CDN script
- Meta charset UTF-8, viewport, Polish lang attribute
- Footer: "© 2026 Sinsay / LPP S.A."
- Accepts a `content: FlowContent.() -> Unit` lambda for the page body

**TDD:** Test that `renderLayout { }` produces HTML containing `<html lang="pl">`, the logo `src="/logo.svg"`, HTMX script tag, and footer copyright text.
**Commit:** `feat: add Layout.kt base shell with Sinsay branding`

---

### Task 6.2 — IntakePage.kt

**File:** `presentation/html/IntakePage.kt`
**Depends on:** 6.1, 5.1 (controller handles POST /submit)

**Spec (from PRD + Design Guidelines):**
- Heading "Zgłoszenie reklamacji lub zwrotu" (H1, 24px/600/#18191A)
- Radio group "Rodzaj zgłoszenia": Reklamacja / Zwrot (AC-05)
- Text input "Nazwa produktu" (required)
- Date input "Data zakupu" (required, AC-04)
- Textarea "Opis problemu" (required)
- File upload "Dodaj zdjęcie" (required, AC-02): drag-and-drop area, dashed border `#AFB0B2`, accept `.jpg,.jpeg,.png,.webp`
- Submit button "WYŚLIJ" (disabled until all fields + image filled, AC-01): amber `#E09243`, `border-radius: 0`, uppercase
- HTMX: `hx-post="/submit"` `hx-encoding="multipart/form-data"` `hx-target="#decision-container"` `hx-swap="innerHTML"`
- `id="decision-container"` div below form (empty initially, receives SSE decision)
- All labels and error text in Polish

**TDD:** Test that rendered HTML contains all mandatory form fields, the submit button, `hx-post="/submit"`, and `id="decision-container"`.
**Commit:** `feat: implement IntakePage with intake form and HTMX wiring`

---

### Task 6.3 — upload-preview.js

**File:** `src/main/resources/static/js/upload-preview.js`
**Depends on:** 6.2

**Spec:**
- On file input change: validate MIME type (jpeg/png/webp) and size (≤ 10MB)
- If invalid: show inline Polish error below upload field, clear input
- If valid: show image thumbnail preview, enable submit button
- Submit button stays disabled if no valid image attached (AC-01, AC-02, AC-03)
- Referenced from `IntakePage.kt` via `<script src="/js/upload-preview.js">`

**TDD:** This is pure JS — write Playwright test (qa-engineer, Task E2E-1) to cover this behavior instead of a unit test.
**Commit:** `feat: add upload-preview.js with client-side file validation and preview`

---

### Task 6.4 — DecisionPanel.kt

**File:** `presentation/html/DecisionPanel.kt`
**Depends on:** 6.1, 5.1 (SSE from /submit), 5.2 (POST /chat/{id})

**Spec:**
- SSE container: `hx-ext="sse"` `sse-connect="/submit"` streams tokens into decision text area
- ACCEPT panel: green label "Wniosek wstępnie pozytywny" (`#0DB209`), explanation text, next-steps text
- REJECT panel: red label "Wniosek wstępnie negatywny" (`#FF0023`), reason text, optional mismatch recommendation
- Card style: white bg, `elevation-02` shadow, `border-radius: 0` (brand signature)
- Post-decision chat section (opens automatically after decision renders, AC-17):
    - Chat message list area (`id="chat-messages"`)
    - Input field "Twoje pytanie..." + send button "WYŚLIJ"
    - HTMX: `hx-post="/chat/{sessionId}"` `hx-target="#chat-messages"` `hx-swap="beforeend"`
    - SSE for streaming assistant response tokens
- Chat bubbles: USER right-aligned dark, ASSISTANT left-aligned amber tint
- All copy in Polish

**TDD:** Test that `renderDecisionPanel(sessionId, Decision.Accept(...))` HTML contains the ACCEPT label and next-steps text; `renderDecisionPanel(sessionId, Decision.Reject(...))` contains the REJECT label; both contain `#chat-messages` and `hx-post` attributes.
**Commit:** `feat: implement DecisionPanel with ACCEPT/REJECT display and post-decision chat`

---

## Phase 7 — E2E Tests (qa-engineer)

Written **alongside** frontend features. Application must be running (`./gradlew bootRun`) for each test run.

### Task E2E-1 — Form Validation (alongside 6.2 + 6.3)

**Depends on:** 5.1, 6.2, 6.3 complete
**File:** `e2e/form-validation.spec.ts` (or Playwright default location)

**Test cases:**
- Submit button is disabled on page load
- Submit button stays disabled with all text fields filled but no image
- Uploading a PNG enables the submit button
- Uploading a file > 10MB shows Polish error, submit stays disabled
- Uploading a non-image file shows Polish error
- Submitting with missing "Nazwa produktu" shows validation error
- Only one request type radio can be selected at a time (AC-05)

**Commit:** `test(e2e): add form validation tests`

---

### Task E2E-2 — Happy Path: Reklamacja (alongside 6.4)

**Depends on:** full stack + 6.4

**Test cases:**
- Fill form with Reklamacja, valid product data, upload test JPG
- Click submit → SSE starts streaming → decision panel appears
- Decision panel shows either ACCEPT or REJECT label (Polish text)
- Explanation text is present and non-empty
- Chat input appears automatically after decision (AC-17)

**Commit:** `test(e2e): add Reklamacja happy path E2E test`

---

### Task E2E-3 — Happy Path: Zwrot (alongside 6.4)

**Test cases:**
- Fill form with Zwrot, valid product data, upload test PNG
- Decision renders with correct label
- ACCEPT includes return method next steps (courier, InPost, store)

**Commit:** `test(e2e): add Zwrot happy path E2E test`

---

### Task E2E-4 — Post-Decision Chat (alongside 6.4)

**Depends on:** E2E-2

**Test cases:**
- After decision renders, type a Polish question in chat input
- Click send → SSE response streams into chat area
- User bubble right-aligned, assistant bubble left-aligned
- Off-topic question receives Polish redirect to support contact

**Commit:** `test(e2e): add post-decision chat E2E tests`

---

### Task E2E-5 — Edge Cases (after E2E-2/3)

**Test cases:**
- Unreadable image (solid black PNG): decision renders REJECT with image quality explanation
- Request type mismatch message: REJECT on Reklamacja for unwanted item includes Zwrot recommendation

**Commit:** `test(e2e): add edge case E2E tests (unreadable image, mismatch)`

---

## Parallel Execution Summary

| Stage | be-developer | fe-developer | qa-engineer |
|-------|-------------|--------------|-------------|
| **Stage A** | 0.1 + 0.2 (in parallel) | — | — |
| **Stage B** | 1.1 → 1.2 → 1.3+1.4 → 1.5+1.6+1.7 | — | — |
| **Stage C** | 2.1 + 2.2 ‖ 3.1 → 3.2 | 6.1 → 6.2 → 6.3 | — |
| **Stage D** | 4.1 → 4.2 | waiting | E2E-1 (after 5.1+6.3) |
| **Stage E** | 5.1 → 5.2 | 6.4 (after 5.1+5.2) | E2E-2+3+4 (after 6.4) |
| **Stage F** | — | — | E2E-5 |

`‖` = parallel tasks within the stage

---

## Verification

End-to-end smoke test after all phases complete:

1. `./gradlew test` — all unit + integration tests pass (no skips)
2. `./gradlew bootRun` — application starts on port 8080
3. Open `http://localhost:8080` — intake form renders with Sinsay branding
4. Fill Reklamacja form, attach a product image, submit → streaming decision appears within 30s
5. Post a follow-up question in chat → streamed response in Polish
6. Fill Zwrot form → decision with correct return policy reasoning
7. Run Playwright E2E suite — all tests pass

---

## Critical Files

| File | Phase | Layer |
|------|-------|-------|
| `src/main/resources/db/migration/V1__init.sql` | 0.1 | — |
| `src/main/resources/policy/*.md` | 0.2 | — |
| `domain/model/RequestType.kt` | 1.1 | domain |
| `domain/model/Decision.kt` | 1.2 | domain |
| `domain/model/Session.kt` | 1.3 | domain |
| `domain/model/ChatMessage.kt` | 1.4 | domain |
| `domain/port/SessionRepository.kt` | 1.5 | domain |
| `domain/port/ChatRepository.kt` | 1.6 | domain |
| `domain/port/EvaluationPort.kt` | 1.7 | domain |
| `infrastructure/persistence/JdbcSessionRepository.kt` | 2.1 | infra |
| `infrastructure/persistence/JdbcChatRepository.kt` | 2.2 | infra |
| `infrastructure/config/AiConfig.kt` | 3.1 | infra |
| `infrastructure/ai/SpringAiEvaluationAdapter.kt` | 3.2 | infra |
| `application/usecase/SubmitRequestUseCase.kt` | 4.1 | app |
| `application/usecase/SendChatMessageUseCase.kt` | 4.2 | app |
| `presentation/web/IntakeController.kt` | 5.1 | presentation |
| `presentation/web/ChatController.kt` | 5.2 | presentation |
| `presentation/html/Layout.kt` | 6.1 | presentation |
| `presentation/html/IntakePage.kt` | 6.2 | presentation |
| `static/js/upload-preview.js` | 6.3 | static |
| `presentation/html/DecisionPanel.kt` | 6.4 | presentation |
| `e2e/*.spec.ts` | 7.x | test |

**Files to delete:** `presentation/web/LandingController.kt`, `presentation/html/LandingPage.kt`
