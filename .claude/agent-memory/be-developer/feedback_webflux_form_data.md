---
name: WebFlux form data reading via ServerWebExchange
description: @RequestParam does not reliably read form body in Spring WebFlux — use ServerWebExchange.formData instead
type: feedback
---

In Spring WebFlux controllers, use `exchange.formData.awaitSingle()` (via `ServerWebExchange`) to read `application/x-www-form-urlencoded` POST body, not `@RequestParam`.

**Why:** `@RequestParam` in WebFlux does not always bind form body parameters — it returns the default value instead of the actual body content, causing unexpected 400 errors in tests. Discovered while implementing `ChatController`.

**How to apply:**
```kotlin
suspend fun chat(
    @PathVariable sessionId: String,
    exchange: ServerWebExchange,
): Flow<String> {
    val formData = exchange.formData.awaitSingle()
    val message = formData.getFirst("message").orEmpty()
    ...
}
```
`@RequestBody MultiValueMap<String, String>` with `consumes = [APPLICATION_FORM_URLENCODED_VALUE]` results in 415 UNSUPPORTED_MEDIA_TYPE in `@WebFluxTest` context.
