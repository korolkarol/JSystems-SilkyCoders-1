# Presentation Layer — Agent Rules

HTTP entry points and HTML rendering. Depends on `application` (use cases) and `domain` (models). No business logic here.

## What belongs here

| Package | Contents |
|---|---|
| `web/` | `IntakeController` — `POST /submit`; `ChatController` — `POST /chat/{id}` |
| `html/` | `Layout`, `IntakePage`, `DecisionPanel` — kotlinx.html DSL renderers |

---

## Controller rules (`web/`)

### SSE streaming
- Streaming endpoints must declare `produces = [MediaType.TEXT_EVENT_STREAM_VALUE]` and return `Flow<String>`.
- Never collect the flow inside the controller — pass it directly from the use case to the response.
- Spring WebFlux handles `Flow<String>` natively; no explicit conversion needed.

```kotlin
@PostMapping("/submit", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun submit(@ModelAttribute cmd: SubmitCommand): Flow<String> =
    submitRequestUseCase.execute(cmd)
```

### Form binding
- Use `@ModelAttribute` for multipart form data.
- Image file is received as `FilePart` (WebFlux) or `MultipartFile` (Servlet) — read bytes, pass `ByteArray` to use case. **Do not store the file.**
- Validate at this boundary only (PRD §AC-01–AC-05):
  - All mandatory fields present (requestType, productName, purchaseDate, description, image).
  - Image MIME type: `image/jpeg`, `image/png`, `image/webp`.
  - Image size ≤ 10 MB.
  - Return HTTP 400 with a Polish error message for invalid input; do not throw unhandled exceptions.

### No business logic
- Controllers must not contain conditional logic based on `RequestType`, `Decision`, or policy rules.
- Do not call `SessionRepository`, `ChatRepository`, or `SpringAiEvaluationAdapter` directly from a controller.

### Language
- All error messages returned to the client must be in **Polish** (PRD §8, AC-01–AC-05).

---

## HTML rendering rules (`html/`)

### kotlinx.html DSL
- All HTML is generated via the kotlinx.html Kotlin DSL — no template files, no string concatenation.
- Every page must go through `Layout.kt` for consistent shell (head, meta, HTMX script inclusion).
- `DecisionPanel.kt` renders both ACCEPT and REJECT states; it receives a `Decision` domain object, not raw strings.

### HTMX patterns
- Use `hx-ext="sse"` + `sse-connect` for streaming decision and chat responses.
- SSE swap target (`sse-swap`) must be a named event matching what the server emits.
- Submit button must be disabled via `disabled` attribute when mandatory fields are missing — enforced with vanilla JS in `upload-preview.js`, not server-side re-rendering.
- Image upload: validate MIME type and size client-side in `upload-preview.js` before enabling the submit button (AC-02, AC-03). Server must still validate (defence in depth).

### Chat UI
- Chat input is revealed **automatically** after the decision panel renders — no user action required (AC-17).
- Chat messages sent via `POST /chat/{sessionId}` using HTMX `hx-post`.
- Off-topic redirect messages from the agent are rendered identically to normal assistant messages — no special UI treatment needed.

### No inline scripts or styles
- Do not add `<script>` blocks inline in kotlinx.html renderers. Put JS in `src/main/resources/static/js/`.
- Do not use inline `style=""` attributes for layout — use CSS classes only.

---

## Testing

- Controller tests use `@WebFluxTest` with mocked use cases.
- Verify that an invalid image (wrong type, oversized) returns HTTP 400 with a Polish error body.
- Verify that a missing image returns HTTP 400 before the use case is called.
- Verify that `/submit` response content type is `text/event-stream`.
- Do not test HTML structure in controller tests — that belongs in dedicated rendering unit tests for the `html/` classes.
- `html/` tests: render each page/panel variant to a string and assert key Polish-language strings are present for each `Decision` branch.
