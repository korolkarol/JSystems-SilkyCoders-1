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

## Sinsay Brand Styling

All UI must follow the Sinsay brand identity documented in `docs/BRANDING.md`. Assets are in `docs/assets/`.

### Colours

Use CSS custom properties matching Sinsay's `design-tokens.css`:

| Purpose | Token | Hex |
|---------|-------|-----|
| Primary CTA / active | `--color-primary-50` | `#E09243` |
| Primary hover | `--color-primary-40` | `#F2B06D` |
| Body text | `--color-dark-80` | `#303133` |
| Secondary text | `--color-dark-70` | `#494A4D` |
| Placeholder | `--color-dark-50` | `#7B7D80` |
| Border | `--color-dark-30` | `#AFB0B2` |
| Divider | `--color-dark-20` | `#C8C9CC` |
| Surface background | `--color-dark-5` | `#F1F2F4` |
| Error | `--color-red-50` | `#FF0023` |
| Success | `--color-green-50` | `#0DB209` |
| White | `--color-white-100` | `#FFFFFF` |
| Near-black / logo | `--color-dark-90` | `#18191A` |

### Typography

- **Primary font**: `Euclid, Arial, Helvetica, "Helvetica Neue", sans-serif`
- **Button font**: `"Euclid Circular B", Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif`

| Role | Size | Weight | Notes |
|------|------|--------|-------|
| Body | 16px | 400 | Default |
| Label / nav | 14px | 400 | — |
| Section heading | 16px | 600 | Letter-spacing 0.15px |
| Modal heading | 24px | 600 | Line-height 24px, letter-spacing -0.2px |
| Button | 16px | 600 | Uppercase |
| Price / meta | 14px | 500 | — |

### Buttons

```css
/* Primary */
background-color: #E09243;
color: #FFFFFF;
border: 2px solid #E09243;
border-radius: 0;          /* sharp corners — never rounded */
padding: 12px 32px;
font-size: 16px;
font-weight: 600;
text-transform: uppercase;
font-family: "Euclid Circular B", sans-serif;

/* Secondary / outline */
background-color: transparent;
color: #FFFFFF;
border: 2px solid rgba(255,255,255,0.8);
border-radius: 0;
padding: 12px 32px;
```

### Form Inputs

- Inner `<input>` is borderless; border goes on the wrapper element.
- Wrapper border colour: `#AFB0B2` (`--color-dark-30`).
- Focus ring colour: `#E09243` (`--color-primary-50`).
- `border-radius: 0` on all inputs.
- Error state border/text: `#FF0023` (`--color-red-50`).

### Spacing

Use multiples of the base 16px unit:

| Token | Value | Usage |
|-------|-------|-------|
| `--size-s` | `4px` | Icon gaps |
| `--size-sm` | `8px` | Label → input gap |
| `--size-m` | `16px` | Default field gap |
| `--size-l` | `32px` | Section padding |
| `--size-xl` | `64px` | Large sections |

### Elevation

| Level | Shadow |
|-------|--------|
| Card / panel | `0px 2px 12px rgba(24,25,26,0.08), 0px 1px 2px rgba(26,13,0,0.08)` |
| Raised panel | `0px 4px 16px rgba(24,25,26,0.10), 0px 1px 4px rgba(26,13,0,0.10)` |

### Polish copy for form elements

| UI element | Label |
|------------|-------|
| Submit button | Wyślij |
| Required field hint | Pole wymagane |
| Image upload | Dodaj zdjęcie |
| Request type selector | Rodzaj zgłoszenia |
| Complaint option | Reklamacja |
| Return option | Zwrot |
| Order number | Numer zamówienia |
| Problem description | Opis problemu |

---

## Testing

- Controller tests use `@WebFluxTest` with mocked use cases.
- Verify that an invalid image (wrong type, oversized) returns HTTP 400 with a Polish error body.
- Verify that a missing image returns HTTP 400 before the use case is called.
- Verify that `/submit` response content type is `text/event-stream`.
- Do not test HTML structure in controller tests — that belongs in dedicated rendering unit tests for the `html/` classes.
- `html/` tests: render each page/panel variant to a string and assert key Polish-language strings are present for each `Decision` branch.
