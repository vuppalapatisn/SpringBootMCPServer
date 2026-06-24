# Spring Boot MCP Apps

Two **fully independent** Spring Boot 3 / Maven projects implementing the **Model Context Protocol (MCP)** — an open standard that lets AI agents discover and call server-side tools over HTTP.

```
repo root/
├── mcp-server/   ← standalone Maven project · port 3001
└── mcp-client/   ← standalone Maven project · port 3002
```

Each module has its own `pom.xml` (no shared parent), its own `Dockerfile`, and its own GitHub Actions CI/CD pipeline.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                         External Consumers                           │
│                                                                      │
│   Claude Desktop / AI Agent          REST Clients / Swagger UI       │
│          │                                    │                      │
│          │  MCP protocol                       │  HTTP REST           │
└──────────┼─────────────────────────────────────┼────────────────────-┘
           │                                     │
           ▼                                     ▼
┌──────────────────────┐          ┌──────────────────────────────────┐
│   mcp-server :3001   │◄─────────│        mcp-client :3002          │
│                      │  MCP/SSE │                                  │
│  MCP Tools           │          │  Orchestration REST API           │
│  ┌──────────────┐    │          │  GET  /api/orchestrate/tools      │
│  │ roll-the-dice│    │          │  POST /api/orchestrate/invoke     │
│  │ calculator   │    │          │  POST /api/orchestrate/roll-dice  │
│  └──────────────┘    │          │  POST /api/orchestrate/calculate  │
│                      │          │  POST /api/orchestrate/           │
│  REST API            │          │       roll-then-calculate         │
│  GET  /api/dice/roll │          │  GET  /swagger-ui.html            │
│  GET  /api/dice/app  │          │                                   │
│  POST /api/calculator│          │  McpOrchestrationService          │
│       /calculate     │          │  wraps MCP calls and exposes      │
│  GET  /api/mcp/info  │          │  them as plain REST endpoints     │
│  GET  /swagger-ui    │          │                                   │
└──────────────────────┘          └──────────────────────────────────┘
         ▲                                       ▲
         │  Docker Hub                           │  Docker Hub
         │  <user>/mcp-server:latest             │  <user>/mcp-client:latest
         └───────────────────────────────────────┘
```

### Key design decisions

| Concern | Choice |
|---------|--------|
| MCP transport (server) | Streamable-HTTP (`spring.ai.mcp.server.protocol=streamable`) |
| MCP transport (client) | SSE (`spring.ai.mcp.client.sse.connections.*`) |
| Spring AI version | 1.0.1 |
| Java | 21 (Eclipse Temurin) |
| Build | Maven only (no Gradle) |
| Container base | `eclipse-temurin:21-jre` (multi-stage build) |
| CI/CD | GitHub Actions → Docker Hub |

---

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (optional, for containerised runs)

---

## Build & Run

Start **mcp-server first** — mcp-client connects to it on startup.

```bash
# Terminal 1 — mcp-server
cd mcp-server
mvn spring-boot:run
# Listening on http://localhost:3001

# Terminal 2 — mcp-client (after server is ready)
cd mcp-client
mvn spring-boot:run
# Listening on http://localhost:3002
```

### Package JARs

```bash
cd mcp-server && mvn clean package -DskipTests
cd mcp-client && mvn clean package -DskipTests
```

### Docker

```bash
# Build images locally
docker build -t mcp-server:local mcp-server/
docker build -t mcp-client:local mcp-client/

# Run containers (bridge network so client can reach server)
docker network create mcp-net

docker run -d --name mcp-server --network mcp-net -p 3001:3001 mcp-server:local

docker run -d --name mcp-client --network mcp-net -p 3002:3002 \
  -e SPRING_AI_MCP_CLIENT_SSE_CONNECTIONS_MCP-SERVER_URL=http://mcp-server:3001 \
  mcp-client:local
```

---

## REST API Reference

### mcp-server — port 3001

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/dice/roll` | Roll two dice — returns die values, total, emoji |
| `GET` | `/api/dice/app` | Interactive HTML dice-roller page |
| `POST` | `/api/calculator/calculate` | Arithmetic — body: `{"a":10,"b":3,"operation":"MULTIPLY"}` |
| `GET` | `/api/mcp/info` | Server metadata and registered MCP tool list |
| `GET` | `/swagger-ui.html` | Swagger UI |

**Calculator operations:** `ADD` · `SUBTRACT` · `MULTIPLY` · `DIVIDE`

### mcp-client — port 3002

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/orchestrate/tools` | List every tool available on the upstream MCP server |
| `POST` | `/api/orchestrate/invoke` | Generic tool invoke — body: `{"toolName":"roll-the-dice","arguments":{}}` |
| `POST` | `/api/orchestrate/roll-dice` | Roll dice via MCP |
| `POST` | `/api/orchestrate/calculate` | Arithmetic via MCP — body: `{"a":10,"b":3,"operation":"ADD"}` |
| `POST` | `/api/orchestrate/roll-then-calculate` | Multi-step: roll dice then multiply total — body: `{"multiplier":1.5}` |
| `GET` | `/swagger-ui.html` | Swagger UI |

---

## MCP Tools

### `roll-the-dice`

Rolls two six-sided dice and returns a structured result.

```json
{
  "die1": 4,
  "die2": 3,
  "total": 7,
  "emoji": "🎲🎲"
}
```

### `calculator`

Performs arithmetic on two doubles.

```json
{ "a": 10, "b": 3, "operation": "MULTIPLY" }
→ { "result": 30.0 }
```

---

## Claude Desktop Integration

Add to `claude_desktop_config.json` to expose MCP tools directly to Claude:

```json
{
  "mcpServers": {
    "mcp-apps-server": {
      "url": "http://localhost:3001/mcp"
    }
  }
}
```

Claude will then be able to call `roll-the-dice` and `calculator` autonomously during conversations.

---

## CI/CD — GitHub Actions

Two independent workflows, one per module, triggered by path filters:

| Workflow | File | Trigger path |
|----------|------|-------------|
| `mcp-server — Build & Push` | `.github/workflows/build.yml` | `mcp-server/**` |
| `mcp-client — Build & Push` | `.github/workflows/mcp-client-build.yml` | `mcp-client/**` |

Each workflow:
1. **Build & Test** — `mvn verify` on JDK 21
2. **Docker Build & Push** — multi-stage build, pushes `latest` and `sha-<short>` tags to Docker Hub

**Required repository secrets:**

| Secret | Value |
|--------|-------|
| `DOCKERHUB_USERNAME` | Your Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token (read/write) |

---

## Project Structure

```
mcp-server/
├── Dockerfile
├── pom.xml                                   (spring-boot-starter-parent 3.4.5)
└── src/
    ├── main/
    │   ├── java/com/example/server/
    │   │   ├── McpServerApplication.java
    │   │   ├── controller/
    │   │   │   ├── CalculatorController.java
    │   │   │   ├── DiceController.java
    │   │   │   └── McpInfoController.java
    │   │   └── service/
    │   │       ├── CalculatorService.java    (@Tool: calculator)
    │   │       └── DiceService.java          (@Tool: roll-the-dice)
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/server/
            └── McpServerApplicationTests.java

mcp-client/
├── Dockerfile
├── pom.xml                                   (spring-boot-starter-parent 3.4.5)
└── src/
    ├── main/
    │   ├── java/com/example/client/
    │   │   ├── McpClientApplication.java
    │   │   ├── controller/
    │   │   │   └── OrchestrationController.java
    │   │   └── service/
    │   │       └── McpOrchestrationService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/client/
            └── McpClientApplicationTests.java
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.4.5 |
| AI / MCP | Spring AI 1.0.1 |
| Language | Java 21 |
| Build | Apache Maven |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Container | Eclipse Temurin 21 JRE |
| CI/CD | GitHub Actions |
| Registry | Docker Hub |
