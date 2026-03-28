---
name: Use fakes over Mockito for suspend function tests
description: Plain Mockito mocks of interfaces with suspend functions cause cross-test contamination (InvalidUseOfMatchersException) — use fake implementations instead
type: feedback
---

Use plain fake implementations (not Mockito mocks) for domain ports (`SessionRepository`, `EvaluationPort`, `ChatRepository`) in unit tests. These interfaces have `suspend` functions, and Mockito's state machine gets confused when stubbing suspend functions with `when(...).thenReturn(...)` across multiple tests in the same class.

**Why:** Cross-test contamination: Mockito's matcher state leaks between tests when suspend functions are involved, causing `InvalidUseOfMatchersException` and `UnfinishedVerificationException` on unrelated tests.

**How to apply:** Write simple `FakeFoo : FooInterface` classes in the test file that store calls and expose them for assertions (`savedSessions`, `updatedDecisions`, etc.). Use `@BeforeEach` to create fresh instances. This is the pattern used in `SubmitRequestUseCaseTest` and `SendChatMessageUseCaseTest`.

Exception: `@WebFluxTest` with `@MockitoBean` works fine for Spring-managed beans (use cases) since `execute()` is a regular (non-suspend) function.
