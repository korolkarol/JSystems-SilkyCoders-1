# Infrastructure Layer — Agent Rules

Implements domain ports. Depends on `domain`. May be referenced by `presentation` for bean wiring only.

## What belongs here

| Package | Contents |
|---|---|
| `ai/` | `SpringAiEvaluationAdapter` — implements `EvaluationPort` |
| `persistence/` | `JdbcSessionRepository`, `JdbcChatRepository` — implement domain repos |
| `config/` | `AiConfig` — `ChatClient` bean, policy document loader |

---

## AI adapter rules (`ai/`)

### Streaming
- `ChatClient.prompt().stream().content()` returns `Flux<String>`.
- Convert **immediately** with `.asFlow()` from `kotlinx-coroutines-reactor`. Never expose `Flux` outside this class.
- Do not use Reactor operators downstream of `.asFlow()`.

```kotlin
// correct
fun evaluate(...): Flow<String> =
    chatClient.prompt(prompt).stream().content().asFlow()
```

### Policy routing (ADR §Policy Document Routing)
- This is the **only** place that loads policy documents.
- Routing is deterministic based on `RequestType`:
  - `REKLAMACJA` → `policy/regulamin.md` + `policy/reklamacje.md`
  - `ZWROT` → `policy/regulamin.md` + `policy/zwrot-30-dni.md`
- **Never load both `reklamacje.md` and `zwrot-30-dni.md` in the same session.**
- Load files as `ClassPathResource` from `src/main/resources/policy/`.

### Image handling (ADR §Image Handling)
- Receive image as `ByteArray`.
- Wrap in `Media(mimeType, bytes.toResource())` and attach to `UserMessage` inline.
- **Discard bytes after the LLM call** — never persist, cache, or log image data.
- Accepted MIME types from the form: `image/jpeg`, `image/png`, `image/webp`.

### OpenRouter configuration (ADR §Spring AI + OpenRouter)
```properties
spring.ai.openai.base-url=https://openrouter.ai/api/v1
spring.ai.openai.api-key=${OPENROUTER_API_KEY}
spring.ai.openai.chat.options.model=openai/gpt-4o-mini
```
Base URL must end with `/v1`. The `OPENROUTER_API_KEY` is sourced from `.env`.

---

## Persistence rules (`persistence/`)

### Blocking JDBC in reactive stack (ADR §Rejected Alternatives)
Spring JDBC is blocking. **Every JDBC call must be wrapped in `withContext(Dispatchers.IO)` or offloaded to `Schedulers.boundedElastic()`** to avoid blocking the Reactor event loop.

```kotlin
// correct
suspend fun findById(id: UUID): Session? = withContext(Dispatchers.IO) {
    jdbcTemplate.queryForObject(...)
}
```

### Schema contract (ADR §Database Schema)
- `sessions.id` — UUID v4 as TEXT.
- `sessions.decision_outcome` — NULL until stream completes; then `'ACCEPT'` or `'REJECT'`.
- `sessions.decision_explanation` — Polish plain-language, ≤ 200 words.
- `sessions.mismatch_recommendation` — NULL unless a request type mismatch was detected.
- `chat_messages.role` — `'USER'` or `'ASSISTANT'` only.
- Never store image bytes or policy document content in the database.
- Never hand-write SQL that bypasses Flyway — all schema changes go through a new migration file.

---

## Config rules (`config/`)

- `AiConfig` is the single place where `ChatClient` bean is defined.
- Policy loader reads files from classpath; cache the content as strings at startup (they don't change at runtime).
- Do not define beans that belong to application or presentation layer here.
- Load `.env` via `dotenv-kotlin` or Spring's `DotenvPropertySource` — never read `System.getenv("OPENROUTER_API_KEY")` inline in business code.

---

## Testing

- `JdbcSessionRepository` and `JdbcChatRepository`: use an in-memory SQLite database (same driver, `jdbc:sqlite::memory:`), apply Flyway migrations before each test.
- `SpringAiEvaluationAdapter`: mock `ChatClient`; verify that:
  - `REKLAMACJA` sessions never receive `zwrot-30-dni.md` content.
  - `ZWROT` sessions never receive `reklamacje.md` content.
  - `Flux<String>` is correctly converted to `Flow<String>`.
- Do not test Spring context startup in this layer — that is covered by the smoke test in `LppsaApplicationTests`.
