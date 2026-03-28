# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Cache Gradle wrapper and build scripts before copying source
COPY gradle/ gradle/
COPY gradlew ./
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies (cached layer if build files unchanged)
RUN ./gradlew dependencies --no-daemon -q || true

# Copy source and build fat JAR
COPY src/ src/
RUN ./gradlew build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy fat JAR from builder
COPY --from=builder /build/build/libs/sinsay-ai-refund-assistant-0.0.1-SNAPSHOT.jar app.jar

# SQLite data directory (mount as volume for persistence)
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/ || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
