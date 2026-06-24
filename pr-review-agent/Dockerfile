# ── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline --no-transfer-progress
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

# ── Runtime stage ─────────────────────────────────────────────────────────────
# Node.js is required at runtime because the agent spawns the GitHub MCP server
# via `npx @modelcontextprotocol/server-github`
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends nodejs npm \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 3003

# Pass secrets via environment variables at runtime:
#   docker run -e AZURE_AI_ENDPOINT=... -e AZURE_AI_KEY=... -e GITHUB_TOKEN=... ...
ENTRYPOINT ["java", "-jar", "app.jar"]
