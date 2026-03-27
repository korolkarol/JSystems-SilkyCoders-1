# Domain Layer — Agent Rules

Pure Kotlin. Zero external dependencies. No Spring, no JDBC, no AI, no Reactor, no coroutines imports.

## What belongs here

| Package | Contents |
|---|---|
| `model/` | `Session`, `ChatMessage`, `Decision` (sealed), `RequestType` (enum) |
| `port/` | `SessionRepository`, `ChatRepository`, `EvaluationPort` (interfaces only) |

## Model rules

- `Decision` is a **sealed class** with exactly two subclasses: `Accept` and `Reject`. Never use a string or enum for outcome.
- `RequestType` is an **enum**: `REKLAMACJA`, `ZWROT`. No other values.
- `Session` is the aggregate root. It holds `requestType`, `productName`, `purchaseDate`, `description`, and the resolved `Decision` (nullable until evaluated).
- `ChatMessage` holds `sessionId`, `role` (`USER` | `ASSISTANT`), and `content`.
- All model classes are immutable (`val` properties only). Use `copy()` for updates.
- No validation logic that depends on business rules beyond basic non-null/non-blank constraints. Policy window validation (30-day / 2-year) is intentionally out of scope (PRD §7).

## Port rules

- Ports are **interfaces only** — no implementations, no `@Component`, no annotations of any kind.
- `EvaluationPort` must declare a `Flow<String>` return type for streaming (not `Flux`, not `suspend String`).
- `SessionRepository` and `ChatRepository` must declare `suspend` functions — the infrastructure layer decides the scheduler.
- Never import `kotlinx.coroutines.reactor` or any Reactor type in this package.

## Dependency rule

```
domain  ←  (no imports from any other layer)
```

If adding an import from `application`, `infrastructure`, or `presentation`, stop — it is a violation.

## Testing

- Domain tests are plain unit tests: no Spring context, no mocks of external systems.
- Test every `Decision` branch (Accept, Reject) and every `RequestType` value.
- `Decision` equality must be tested structurally, not by reference.
