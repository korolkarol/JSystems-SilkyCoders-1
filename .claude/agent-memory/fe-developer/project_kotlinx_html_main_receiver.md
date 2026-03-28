---
name: kotlinx.html MAIN receiver type in renderLayout
description: The renderLayout function uses MAIN.() -> Unit as the content lambda receiver (not BODY.() -> Unit), because the content is placed inside main { } which has MAIN receiver.
type: project
---

`renderLayout(title, content: MAIN.() -> Unit)` — the lambda receiver is `MAIN` not `BODY` or `FlowContent`. `MAIN` implements `FlowContent` transitively via `HtmlBlockTag`, so all standard flow content functions (`div`, `p`, `h1`, etc.) are available.

**Why:** Placing the content lambda inside `main { }` gives it `MAIN` as the implicit receiver. The linter auto-corrected this from `BODY.() -> Unit` to `MAIN.() -> Unit`.

**How to apply:** Tests that use `renderLayout { p { ... } }` need `import kotlinx.html.p` (or `import kotlinx.html.*`) since `p` is an extension function on `FlowContent`, not a member method.
