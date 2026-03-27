# Presentation Layer

HTTP entry points and HTML rendering. Depends on `application` (use cases) and `domain` (models). No business logic here.

## Packages

| Package | Contents | Guidelines |
|---|---|---|
| `web/` | `IntakeController` — `POST /submit`; `ChatController` — `POST /chat/{id}` | `web/AGENTS.md` |
| `html/` | `Layout`, `IntakePage`, `DecisionPanel` — kotlinx.html DSL renderers | `html/AGENTS.md` |
| `html/components/` | Reusable DSL components and `Htmx` helper | `html/AGENTS.md` |
