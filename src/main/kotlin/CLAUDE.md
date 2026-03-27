# Kotlin Code Style Guide

Applies to all Kotlin source files in this project. Based on the
[official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Indentation

- **4 spaces** — never tabs.
- Continuation lines (wrapped expressions, argument lists) also use 4-space indent from the opening construct.

## Named Parameters

- Use named arguments whenever a function call has **more than one parameter**.
- Always use named arguments for `Boolean` parameters and multiple parameters of the same primitive type,
  regardless of count — to prevent accidental argument-order mistakes.

```kotlin
// Good
drawSquare(
    x = 10,
    y = 10,
    width = 100,
    height = 100,
    fill = true,
)

// Bad — positional arguments hide intent
drawSquare(10, 10, 100, 100, true)
```

## Multi-line Parameter Lists

Split parameters onto separate lines when there are **more than one**, or when the signature exceeds ~100 characters:

- Opening parenthesis stays on the same line as the function/class name.
- Each parameter on its own line, indented 4 spaces.
- Closing parenthesis on its own line, aligned with the opening construct.
- Trailing comma on the last parameter (enables clean diffs).

```kotlin
// Function declaration
fun processRequest(
    sessionId: Long,
    requestType: RequestType,
    imageBytes: ByteArray?,
) : Decision

// Class header
class Person(
    val id: Int,
    val name: String,
    val surname: String,
) : Human(id, name)

// Function call
sendMessage(
    sessionId = session.id,
    role = ChatRole.USER,
    content = userInput,
)
```

Single-parameter calls may stay on one line:

```kotlin
println(message)
repository.findById(id)
```

## Braces

- Opening brace at the end of the line (K&R style) — **never** on its own line.
- Closing brace on its own line, aligned with the opening construct.
- Always use braces for `if`/`else`/`for`/`while` bodies, even single-expression ones.

```kotlin
if (condition) {
    doSomething()
} else {
    doSomethingElse()
}
```

## Expressions vs. Statements

Prefer expression bodies for simple single-expression functions:

```kotlin
fun double(x: Int): Int = x * 2
```

Use block body when the function has multiple statements or complex logic.

## Classes

- Prefer `data class` for plain data holders — the compiler generates `equals`, `hashCode`, `toString`, and `copy`.
- Declare all meaningful properties in the **primary constructor** so they participate in generated functions.
- Properties that should *not* affect equality/hashing go in the class body.

```kotlin
data class ChatMessage(
    val sessionId: Long,
    val role: String,
    val content: String,
)
```

## Null Safety

- Prefer non-nullable types; accept `null` only at system boundaries.
- Use `?.let { }` or `?: return` early-exit over nested null checks.
- Never use `!!` — fix the root cause or redesign the API.

## Immutability

- Prefer `val` over `var`; prefer immutable collections (`listOf`, `mapOf`).
- Use `var` only when mutation is genuinely required.

## String Templates

Use string templates instead of concatenation:

```kotlin
// Good
"Hello, $name! You have ${messages.size} messages."

// Bad
"Hello, " + name + "! You have " + messages.size + " messages."
```

## Coroutines & Flow

- Suspend functions over blocking calls.
- `Flow<T>` for streams; `StateFlow`/`SharedFlow` for state/events.
- Never call blocking I/O from a coroutine without `withContext(Dispatchers.IO)`.

## Imports

- No wildcard imports (`import com.example.*`).
- IDE-managed; keep them sorted alphabetically.

## Naming

| Element           | Convention                        |
|-------------------|-----------------------------------|
| Classes/objects   | `PascalCase`                      |
| Functions/vars    | `camelCase`                       |
| Constants         | `SCREAMING_SNAKE_CASE`            |
| Packages          | `lowercase.dot.separated`         |
| Test functions    | `` `backtick sentence case` ``    |
