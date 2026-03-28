---
name: Docker configuration facts
description: JAR artifact name, volume paths, health check endpoint, and secrets for this project's Docker setup
type: project
---

Fat JAR artifact: `build/libs/sinsay-ai-refund-assistant-0.0.1-SNAPSHOT.jar` (group=com.lppsa, version=0.0.1-SNAPSHOT, rootProject.name=sinsay-ai-refund-assistant).

**Why:** Gradle uses `${rootProject.name}-${version}.jar` by default; confirmed from `settings.gradle.kts` and `build.gradle.kts`.

**How to apply:** Use this exact filename in COPY instructions when updating the Dockerfile.

SQLite DB is at `/app/data/lppsa.db` inside the container; bind-mounted from `./data` on the host via docker-compose.

Health check hits `GET /` (no Spring Actuator present).

Runtime secret: `OPENROUTER_API_KEY` — injected via `--env-file .env` at runtime, never baked into image.

No Spring Actuator — do not add `/actuator/health` health checks.
