# Application Layer — Agent Rules

Orchestration only. Depends on `domain` exclusively — never import from `infrastructure` or `presentation`.

## What belongs here

| Package | Contents |
|---|---|
| `usecase/` | `SubmitRequestUseCase`, `SendChatMessageUseCase` |

## Use case rules

- Each use case has a single public `execute` method.
- Accepts a command object (plain data class, no framework annotations).
- Returns `Flow<String>` for streaming use cases — do not return `Flux` or a blocking type.
- Call domain ports (`EvaluationPort`, `SessionRepository`, `ChatRepository`) only — never call infrastructure classes directly.
- No `@Autowired`, no `@Component` — use constructor injection with `@Service` at most if Spring is required to discover the bean. Prefer explicit wiring in config.

## SubmitRequestUseCase responsibilities (ADR §Request Flow)

1. Insert a `Session` row via `SessionRepository` with `decision_outcome = NULL`.
2. Call `EvaluationPort.evaluate(requestType, description, imageBytes)` → `Flow<String>`.
3. Stream tokens back to the caller while collecting the full response.
4. On stream completion: parse `Decision` from accumulated text, update `Session` via `SessionRepository` (outcome + explanation + optional mismatch recommendation).

**Important**: the DB write on completion must happen inside a coroutine scope that outlives the stream. Never fire-and-forget with `launch` without a proper scope.

## SendChatMessageUseCase responsibilities

1. Load `Session` by ID via `SessionRepository`.
2. Load `ChatMessage` history via `ChatRepository`.
3. Persist the incoming user message (`role = USER`).
4. Call `EvaluationPort.chat(sessionContext, history, userMessage)` → `Flow<String>`.
5. On stream completion: persist assistant reply (`role = ASSISTANT`).

## Policy routing — application layer must NOT own this

Policy document loading is infrastructure concern (`AiConfig` / `SpringAiEvaluationAdapter`). The use case passes only `RequestType` — never policy text or file paths.

## Reactor operators

Never use Reactor operators (`flatMap`, `switchMap`, `zip`, etc.) in this layer. Use Kotlin coroutine primitives (`collect`, `map`, `onEach`, `flow { }`) only.

## Testing

- Unit-test use cases with fakes (not mocks) of the domain ports.
- `SubmitRequestUseCase`: verify that `SessionRepository.save()` is called before streaming starts and that the update is called after stream completion.
- `SendChatMessageUseCase`: verify that history is loaded and that both USER and ASSISTANT messages are persisted.
- Test the policy routing invariant: `RequestType.REKLAMACJA` must never reach the use case paired with zwrot policy context (the invariant is enforced in infrastructure, but the use case must pass the correct `RequestType`).
