---
name: Mockito ArgumentCaptor NPE with Kotlin non-null types
description: Using Mockito's ArgumentCaptor.capture() with Kotlin non-null parameter types causes NPE at runtime. Solution is to use fakes or @Suppress UNCHECKED_CAST.
type: feedback
---

`ArgumentCaptor.forClass(T::class.java).capture()` returns null at Mockito matcher-setup time. Kotlin generates intrinsic null checks at call sites for non-null parameter types, causing NPE before Mockito can intercept.

**Why:** This is a fundamental Kotlin/Mockito interop issue. `mockito-kotlin 5.x` handles it internally but vanilla Mockito does not.

**How to apply:** Prefer fakes (hand-rolled test doubles implementing the interface) over Mockito mocks for domain ports (`SessionRepository`, `EvaluationPort`, etc.). The linter in this project already rewrites Mockito-based tests to use fakes. If Mockito is needed, use a `@Suppress("UNCHECKED_CAST") private fun <T> captureKt(captor: ArgumentCaptor<T>): T = captor.capture() as T` helper.
