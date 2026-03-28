# ADR-003 — Observability: Logging, Metrics, and Distributed Tracing

**Status**: Accepted
**Date**: 2026-03-28

---

## Context

The application is a single-node Spring Boot 3 + WebFlux service running in Docker Compose (see ADR-002). It has no observability infrastructure today — no structured logs, no metrics endpoint, no tracing. Gaps identified:

- No way to measure AI decision latency or ACCEPT/REJECT ratios
- JVM and HTTP error rates are invisible
- Errors in the SSE stream are swallowed silently (ISSUE-0001 exposed this)
- No correlated request traces across controller → use case → OpenRouter

### Constraints

- Deployment is **self-hosted Docker Compose** — no managed cloud observability service
- Actuator endpoints must be secured; the same credentials must work for automated scrapers (Prometheus, Grafana)
- Scope: dashboards only — no alerting rules in this ADR
- Must not add complexity to the application's Clean Architecture (observability is infrastructure concern)

---

## Decision

### Observability Stack

| Concern | Tool | Rationale |
|---|---|---|
| Metrics exposition | Spring Boot Actuator + Micrometer | Standard Spring Boot integration; zero-code JVM, HTTP, and custom metrics |
| Metrics storage | Prometheus | De-facto standard for pull-based scraping; integrates natively with Grafana |
| Log shipping | Promtail → Loki | Reads Docker container stdout; no log file management; Loki is Grafana-native |
| Distributed tracing | Micrometer Tracing → Zipkin | Spring Boot 3 first-class support; simple single-binary deployment; trace IDs automatically injected into logs |
| Dashboards | Grafana | Single pane connecting Prometheus (metrics), Loki (logs), and Zipkin (traces) |

### Why Zipkin over OpenTelemetry Collector + Tempo

Tempo + OTel Collector is the production-grade choice but adds two extra services and significant configuration. For a self-hosted single-node MVP, Zipkin provides the same trace-correlation value with one Docker image and native Spring Boot auto-configuration via `micrometer-tracing-bridge-brave`.

The decision can be revisited (ADR amendment) if the team needs tail-based sampling or multi-service traces.

---

### Spring Boot Changes

#### Dependencies to add (`build.gradle.kts`)

```kotlin
// Actuator — metrics, health, info endpoints
implementation("org.springframework.boot:spring-boot-starter-actuator")

// Prometheus registry — exposes /actuator/prometheus for scraping
runtimeOnly("io.micrometer:micrometer-registry-prometheus")

// Micrometer Tracing — distributed trace propagation
implementation("io.micrometer:micrometer-tracing-bridge-brave")
runtimeOnly("io.zipkin.reporter2:zipkin-reporter-brave")

// Spring Security — secures /actuator/** endpoints
implementation("org.springframework.boot:spring-boot-starter-security")
```

#### `application.properties` additions

```properties
# Actuator — expose only the endpoints we use; keep UI endpoints off actuator
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=when-authorized

# Structured JSON logging (Loki/Grafana-friendly)
logging.structured.format.console=ecs

# Tracing — send 100% of traces in dev; reduce in prod
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
```

#### Actuator Security (`ActuatorSecurityConfig.kt`)

Spring Security restricts `/actuator/**` to a dedicated `ACTUATOR` role. Both Prometheus (scraping) and Grafana (health probe) use HTTP Basic credentials from `.env`.

```kotlin
@Configuration
@EnableWebFluxSecurity
class ActuatorSecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/actuator/health").permitAll()   // Docker HEALTHCHECK — no creds
                    .pathMatchers("/actuator/**").hasRole("ACTUATOR")
                    .anyExchange().permitAll()
            }
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .build()

    @Bean
    fun userDetailsService(
        @Value("\${ACTUATOR_USER}") user: String,
        @Value("\${ACTUATOR_PASSWORD}") password: String,
        encoder: PasswordEncoder,
    ): ReactiveUserDetailsService =
        MapReactiveUserDetailsService(
            User.withUsername(user)
                .password(encoder.encode(password))
                .roles("ACTUATOR")
                .build()
        )

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

**`.env` additions** (never committed):
```
ACTUATOR_USER=metrics
ACTUATOR_PASSWORD=<strong-random-password>
```

---

### Custom Business Metrics

Register a `MeterRegistry`-based component in the infrastructure layer to record:

| Metric | Type | Tags |
|---|---|---|
| `sinsay.session.submitted` | Counter | `request_type` (REKLAMACJA/ZWROT) |
| `sinsay.session.decision` | Counter | `request_type`, `outcome` (ACCEPT/REJECT) |
| `sinsay.ai.evaluation.duration` | Timer | `request_type` |
| `sinsay.chat.message` | Counter | `role` (USER/ASSISTANT) |

These are recorded in `SubmitRequestUseCase` and `SendChatMessageUseCase` via constructor-injected `MeterRegistry` — no framework annotations needed, preserving Clean Architecture.

---

### Docker Compose Additions (`docker-compose.yml`)

```yaml
services:
  # ... existing app service ...

  prometheus:
    image: prom/prometheus:v3.2.1
    volumes:
      - ./observability/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    restart: unless-stopped

  loki:
    image: grafana/loki:3.4.2
    volumes:
      - ./observability/loki-config.yml:/etc/loki/local-config.yaml:ro
      - loki_data:/loki
    ports:
      - "3100:3100"
    restart: unless-stopped

  promtail:
    image: grafana/promtail:3.4.2
    volumes:
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./observability/promtail-config.yml:/etc/promtail/config.yml:ro
    restart: unless-stopped
    depends_on: [loki]

  zipkin:
    image: openzipkin/zipkin:3
    ports:
      - "9411:9411"
    restart: unless-stopped

  grafana:
    image: grafana/grafana:11.5.2
    environment:
      - GF_SECURITY_ADMIN_USER=${GRAFANA_USER}
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - grafana_data:/var/lib/grafana
      - ./observability/grafana/provisioning:/etc/grafana/provisioning:ro
    ports:
      - "3000:3000"
    restart: unless-stopped
    depends_on: [prometheus, loki, zipkin]

volumes:
  prometheus_data:
  loki_data:
  grafana_data:
```

**Prometheus scrape config** (`observability/prometheus.yml`):
```yaml
scrape_configs:
  - job_name: sinsay-app
    scrape_interval: 15s
    metrics_path: /actuator/prometheus
    basic_auth:
      username: ${ACTUATOR_USER}
      password: ${ACTUATOR_PASSWORD}
    static_configs:
      - targets: ['app:8080']
```

**`.env` additions**:
```
GRAFANA_USER=admin
GRAFANA_PASSWORD=<strong-random-password>
```

---

### Grafana Dashboards (provisioned)

Three dashboards provisioned via `observability/grafana/provisioning/dashboards/`:

| Dashboard | Data source | Key panels |
|---|---|---|
| **App Overview** | Prometheus | Request rate, error rate, P95 latency, active SSE connections |
| **Business Metrics** | Prometheus | Session submissions/hour, ACCEPT vs REJECT ratio, AI evaluation P50/P95 |
| **Logs** | Loki | Structured log stream, error filter, correlated trace ID links |

Trace correlation: Grafana Loki datasource configured with a derived field on `traceId` → links directly to Zipkin.

---

## Directory Structure Added

```
project-root/
└── observability/
    ├── prometheus.yml
    ├── loki-config.yml
    ├── promtail-config.yml
    └── grafana/
        └── provisioning/
            ├── datasources/
            │   └── datasources.yml   # Prometheus + Loki + Zipkin
            └── dashboards/
                ├── dashboards.yml
                ├── app-overview.json
                ├── business-metrics.json
                └── logs.json
```

---

## Rejected Alternatives

| Alternative | Reason rejected |
|---|---|
| OpenTelemetry Collector + Tempo | Two extra services + OTel config overhead; Zipkin is simpler for single-node MVP |
| ELK (Elasticsearch + Logstash + Kibana) | Heavy memory footprint; Loki + Grafana already present and covers the same use case |
| Datadog / Grafana Cloud | Requires external network access and paid tier for this data volume; self-hosted chosen (ADR-002) |
| Expose `/actuator/**` without auth | Leaks JVM internals, environment info, heap dumps; unacceptable even for internal MVP |
| `permitAll()` on `/actuator/health` only | Required for Docker HEALTHCHECK which runs without credentials inside the container |

---

## Consequences

**Positive**
- JVM, HTTP, and business metrics available out of the box via Micrometer auto-instrumentation
- Trace IDs in structured logs allow correlation between log lines and Zipkin traces with zero manual effort
- Grafana dashboards provisioned as code — reproducible, version-controlled
- Actuator secured with HTTP Basic; same credential works for both Prometheus scraping and Grafana health datasource

**Negative / risks**
- Spring Security adds a dependency and startup configuration; must ensure `/actuator/health` remains unauthenticated for Docker HEALTHCHECK
- `management.tracing.sampling.probability=1.0` is appropriate for dev/low traffic; must be reduced (e.g. `0.1`) before any production-scale deployment to avoid Zipkin storage pressure
- Promtail requires access to Docker socket (`/var/run/docker.sock`) — acceptable for single-node self-hosted but a security consideration in shared environments
- `ACTUATOR_PASSWORD` and `GRAFANA_PASSWORD` must be strong random values; never committed to `.env.example` with real values

---

## Revision Trigger

Re-evaluate this ADR if:
- Deployment moves from single-node Docker Compose to multi-node (→ consider OpenTelemetry Collector + Tempo)
- Log volume exceeds Loki's local storage capacity (→ consider remote Loki or S3 backend)
- Alerting is required (→ add Alertmanager to Prometheus stack)
- A second service is added (→ distributed tracing becomes critical; evaluate OTel)
