# Presentation — HTML Layer (`html/`)

All HTML rendering via kotlinx.html DSL. No HTTP logic, no business rules.

## Package structure

| Package | Contents |
|---|---|
| `html/` | Page renderers: `Layout`, `IntakePage`, `DecisionPanel` |
| `html/components/` | Reusable DSL components: `Buttons`, `FormFields`, `ChatBubble`, `StatusBadge`, `Htmx` |

---

## kotlinx.html DSL

- All HTML is generated via the kotlinx.html Kotlin DSL — no template files, no string concatenation.
- Every page must go through `Layout.kt` for consistent shell (head, meta, HTMX script inclusion).
- `DecisionPanel.kt` renders both ACCEPT and REJECT states; it receives a `Decision` domain object, not raw strings.

### Custom attributes

Use `attributes["name"] = "value"` for any attribute not covered by a typed DSL property:

```kotlin
div {
    attributes["id"] = "chat-box"
    attributes["aria-live"] = "polite"
}
```

Never build HTML via string concatenation — always stay inside the DSL.

### Components — extract to `html/components/`

Extract any reusable fragment (≥ 2 use sites, or non-trivial structure) to its own file under
`html/components/`. Components are Kotlin extension functions on the appropriate receiver type:

```kotlin
// html/components/Buttons.kt
fun FlowContent.primaryButton(label: String, classes: String = "") {
    button(classes = "btn btn--primary $classes".trim()) {
        type = ButtonType.submit
        +label
    }
}

// html/components/FormFields.kt
fun FlowContent.labeledInput(
    id: String,
    labelText: String,
    inputName: String,
    placeholder: String = "",
) {
    div(classes = "form-field") {
        label {
            htmlFor = id
            +labelText
        }
        input(classes = "form-field__input") {
            this.id = id
            name = inputName
            this.placeholder = placeholder
        }
    }
}
```

Naming rules:
- File name = component group: `Buttons.kt`, `FormFields.kt`, `ChatBubble.kt`, `StatusBadge.kt`.
- Function name = what it renders, camelCase.
- Receiver type = narrowest applicable: `FlowContent` for generic content, `UL` for list items, etc.

### Page-level structure

Page renderers (`IntakePage.kt`, `DecisionPanel.kt`, …) call component functions — they must not
contain raw `div`/`span` nesting that belongs in a component. Keep page files thin orchestrators.

---

## HTMX helper extensions

All HTMX attributes are set via the `Htmx` helper object defined in `html/components/Htmx.kt`.
**Never scatter raw `attributes["hx-*"]` calls across page renderers** — use the helpers instead.

```kotlin
// html/components/Htmx.kt
import kotlinx.html.CommonAttributeGroupFacade

/** Fluent HTMX attribute helpers for kotlinx.html tags. */
object Htmx {
    fun CommonAttributeGroupFacade.hxPost(url: String) { attributes["hx-post"] = url }
    fun CommonAttributeGroupFacade.hxGet(url: String) { attributes["hx-get"] = url }
    fun CommonAttributeGroupFacade.hxTarget(selector: String) { attributes["hx-target"] = selector }
    fun CommonAttributeGroupFacade.hxSwap(strategy: String) { attributes["hx-swap"] = strategy }
    fun CommonAttributeGroupFacade.hxTrigger(trigger: String) { attributes["hx-trigger"] = trigger }
    fun CommonAttributeGroupFacade.hxIndicator(selector: String) { attributes["hx-indicator"] = selector }
    fun CommonAttributeGroupFacade.hxDisabledElt(selector: String) { attributes["hx-disabled-elt"] = selector }
    fun CommonAttributeGroupFacade.hxEncoding(encoding: String) { attributes["hx-encoding"] = encoding }
    fun CommonAttributeGroupFacade.hxExt(vararg extensions: String) { attributes["hx-ext"] = extensions.joinToString(",") }
    fun CommonAttributeGroupFacade.sseConnect(url: String) { attributes["sse-connect"] = url }
    fun CommonAttributeGroupFacade.sseSwap(eventName: String) { attributes["sse-swap"] = eventName }
}
```

Import helpers individually and call directly on any tag:

```kotlin
import com.lppsa.presentation.html.components.Htmx.hxPost
import com.lppsa.presentation.html.components.Htmx.hxTarget
import com.lppsa.presentation.html.components.Htmx.hxSwap

form(classes = "intake-form") {
    hxPost("/submit")
    hxTarget("#decision-panel")
    hxSwap("innerHTML")
    hxEncoding("multipart/form-data")
    hxIndicator("#loading-spinner")
}
```

---

## HTMX patterns

### Multipart form submission (intake form)

```kotlin
form(classes = "intake-form") {
    hxPost("/submit")
    hxTarget("#decision-panel")
    hxSwap("innerHTML")
    hxEncoding("multipart/form-data")   // required for file upload
    hxIndicator("#spinner")
    hxDisabledElt("find button[type=submit]")
    // form fields …
    primaryButton("Wyślij")
}
div { id = "decision-panel" }
div(classes = "htmx-indicator") { id = "spinner" }
```

### SSE streaming (decision + chat)

```kotlin
// Decision panel — connect to SSE stream, swap content on "decision" event
div(classes = "decision-stream") {
    hxExt("sse")
    sseConnect("/submit-stream/$sessionId")
    div(classes = "decision-content") {
        sseSwap("decision")     // server emits: event: decision\ndata: <html fragment>
    }
}

// Chat response area — swap on "chat" event
div(classes = "chat-stream") {
    hxExt("sse")
    sseConnect("/chat/$sessionId")
    div(classes = "chat-messages") {
        id = "chat-messages"
        sseSwap("chat")         // server emits: event: chat\ndata: <html fragment>
    }
}
```

### Chat message submission

```kotlin
form(classes = "chat-form") {
    hxPost("/chat/$sessionId")
    hxTarget("#chat-messages")
    hxSwap("beforeend")         // append new messages at the bottom
    hxTrigger("submit")
    hxDisabledElt("find button[type=submit]")
    input(classes = "chat-form__input") {
        name = "message"
        placeholder = "Zadaj pytanie…"
        required = true
    }
    primaryButton("Wyślij")
}
```

### Submit button — client-side gate

- The submit button starts `disabled`; `upload-preview.js` enables it only after all required fields
  (including a valid image) are filled. Client-side UX only — server validates independently.
- Use `hx-disabled-elt="find button[type=submit]"` to keep it disabled during the HTMX request.

### Chat UI reveal

- Chat input area is hidden (`display: none`) until the decision SSE stream completes.
- An `htmx:sseMessage` listener in JS removes the hidden class — no extra round-trip needed (AC-17).
- Chat messages sent via `POST /chat/{sessionId}`; off-topic redirect messages render identically to normal assistant messages.

### SSE event naming

Server emits named events; the `sseSwap` value must match exactly:

| SSE event name | Rendered fragment |
|---|---|
| `decision` | Full `DecisionPanel` HTML |
| `chat` | Single `ChatBubble` HTML |

---

## No Inline Scripts or Styles

- Do not add `<script>` blocks inline in kotlinx.html renderers. Put JS in `src/main/resources/static/js/`.
- Do not use inline `style=""` attributes for layout — use CSS classes only.

---

## Sinsay Brand Styling

All UI must follow `docs/design-guidelines.md`. Quick reference:

### Colours

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
- Wrapper border: `#AFB0B2` (`--color-dark-30`).
- Focus ring: `#E09243` (`--color-primary-50`).
- `border-radius: 0` on all inputs.
- Error state border/text: `#FF0023` (`--color-red-50`).

### Spacing

| Token | Value | Usage |
|-------|-------|-------|
| `--size-s` | `4px` | Icon gaps |
| `--size-sm` | `8px` | Label → input gap |
| `--size-m` | `16px` | Default field gap |
| `--size-l` | `32px` | Section padding |
| `--size-xl` | `64px` | Large sections |

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

- Render each page/panel variant to a string and assert key Polish-language strings are present for each `Decision` branch.
- Do not test HTML structure in controller tests — rendering tests live here in `html/`.
- Test both ACCEPT and REJECT states of `DecisionPanel`.
- Verify that `Htmx` helper attributes are present in the rendered output for form and SSE elements.
