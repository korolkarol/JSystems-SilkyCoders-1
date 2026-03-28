# ADR-002 — Containerization: Multi-Stage Docker Build

**Status**: Accepted
**Date**: 2026-03-28

---

## Context

The application is a standalone Spring Boot fat JAR targeting JVM 21. It needs a reproducible, portable deployment unit that:

- Does not require a pre-installed JDK on the host
- Keeps secrets (OpenRouter API key) out of the image
- Supports local development parity with production
- Persists the SQLite database file across container restarts

---

## Decision

Containerize using a **two-stage Dockerfile**:

| Stage | Base image | Purpose |
|---|---|---|
| `builder` | `eclipse-temurin:21-jdk-alpine` | Compile and assemble fat JAR via `./gradlew build -x test` |
| `runtime` | `eclipse-temurin:21-jre-alpine` | Run the JAR as non-root user `appuser` |

Key choices:

- **Layer caching**: Gradle wrapper and build scripts are copied before source so dependency downloads are cached when only source changes.
- **No secrets in image**: `OPENROUTER_API_KEY` and any future secrets are injected at runtime via `--env-file .env` or `docker-compose env_file`. They are never set as `ARG`/`ENV` in the Dockerfile.
- **Non-root user**: A dedicated `appuser` is created in the runtime stage; the process runs as that user.
- **SQLite persistence**: The database file path is `/app/data/lppsa.db`. A bind mount (`./data:/app/data`) in `docker-compose.yml` preserves data across container recreations.
- **Health check**: `wget -qO- http://localhost:8080/` — uses the root endpoint since Spring Actuator is not present.
- **`.dockerignore`**: Excludes `.env`, `.git`, `.gradle`, `build/`, `node_modules/`, `docs/`, `e2e/`, and log files to keep the build context minimal and prevent accidental secret leakage.

---

## Consequences

**Positive**:
- Reproducible builds on any Docker host without a local JDK
- Secrets never baked into the image; safe to push image to a registry
- Minimal runtime image (JRE-only Alpine) reduces attack surface and image size
- `docker-compose up --build` provides a one-command local development setup

**Negative / Trade-offs**:
- SQLite in a container requires explicit volume management; data is lost if the container is removed without a bind mount
- First build is slow (Gradle dependency download); subsequent builds use the cached dependency layer
- The `./gradlew dependencies` pre-fetch step only caches dependencies if `build.gradle.kts` is unchanged — adding a dependency invalidates the layer as expected

**Not addressed** (out of scope for MVP):
- Multi-replica deployment (SQLite is single-writer; horizontal scaling would require switching to PostgreSQL)
- CI/CD pipeline integration (image tagging, registry push)
