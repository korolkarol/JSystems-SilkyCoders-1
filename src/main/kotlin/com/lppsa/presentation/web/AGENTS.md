# Presentation — Web Layer (`web/`)

HTTP entry points only. No business logic, no HTML rendering.

## Controllers

| Controller | Endpoint |
|---|---|
| `IntakeController` | `POST /submit` |
| `ChatController` | `POST /chat/{id}` |

---

## SSE Streaming

- Streaming endpoints must declare `produces = [MediaType.TEXT_EVENT_STREAM_VALUE]` and return `Flow<String>`.
- Never collect the flow inside the controller — pass it directly from the use case to the response.
- Spring WebFlux handles `Flow<String>` natively; no explicit conversion needed.

```kotlin
@PostMapping("/submit", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun submit(@ModelAttribute cmd: SubmitCommand): Flow<String> =
    submitRequestUseCase.execute(cmd)
```

---

## Form Binding

- Use `@ModelAttribute` for multipart form data.
- Image file is received as `FilePart` (WebFlux) — read bytes, pass `ByteArray` to use case. **Do not store the file.**
- Validate at this boundary only (PRD §AC-01–AC-05):
  - All mandatory fields present (`requestType`, `productName`, `purchaseDate`, `description`, `image`).
  - Image MIME type: `image/jpeg`, `image/png`, `image/webp`.
  - Image size ≤ 10 MB.
  - Return HTTP 400 with a Polish error message for invalid input; do not throw unhandled exceptions.

---

## No Business Logic

- Controllers must not contain conditional logic based on `RequestType`, `Decision`, or policy rules.
- Do not call `SessionRepository`, `ChatRepository`, or `SpringAiEvaluationAdapter` directly from a controller.

---

## Language

All error messages returned to the client must be in **Polish** (PRD §8, AC-01–AC-05).

---

## Testing

- Use `@WebFluxTest` with mocked use cases.
- Verify that an invalid image (wrong MIME type, oversized) returns HTTP 400 with a Polish error body.
- Verify that a missing image returns HTTP 400 before the use case is called.
- Verify that `/submit` response content type is `text/event-stream`.
- Do not test HTML structure in controller tests — that belongs in `html/` rendering tests.
