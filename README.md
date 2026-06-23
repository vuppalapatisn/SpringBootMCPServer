# MCP Apps — Two Standalone Spring Boot Projects

Two **completely independent** Spring Boot / Maven projects demonstrating the MCP Server and MCP Client/Orchestrator patterns.

```
repo root/
├── mcp-server/   ← standalone Maven project (port 3001)
└── mcp-client/   ← standalone Maven project (port 3002)
```

Each project has its own `pom.xml` with `spring-boot-starter-parent` and can be built, run, and deployed entirely on its own — no shared parent POM.

---

## Architecture

```
 Claude Desktop / AI agent
        │  MCP protocol (streamable-HTTP)
        ▼
┌─────────────────────────────┐
│   mcp-server  :3001         │◄── MCP protocol (SSE) ── mcp-client :3002
│                             │                          │
│  @Tool  roll-the-dice       │                          │  REST Orchestration API
│  @Tool  calculator          │                          │  GET  /api/orchestrate/tools
│                             │                          │  POST /api/orchestrate/invoke
│  REST API                   │                          │  POST /api/orchestrate/roll-dice
│  GET  /api/dice/roll        │                          │  POST /api/orchestrate/calculate
│  GET  /api/dice/app         │                          │  POST /api/orchestrate/roll-then-calculate
│  POST /api/calculator/calculate                        │  GET  /api/client/info
│  GET  /api/mcp/info         │                          │  GET  /swagger-ui.html
│  GET  /swagger-ui.html      │                          │
└─────────────────────────────┘
```

---

## Build & Run

Each project is built and run independently:

```bash
# ── mcp-server (Terminal 1) ─────────────────────────────
cd mcp-server
mvn clean package -DskipTests
mvn spring-boot:run
# → http://localhost:3001

# ── mcp-client (Terminal 2 — start after server is up) ──
cd mcp-client
mvn clean package -DskipTests
mvn spring-boot:run
# → http://localhost:3002
```

---

## REST API Reference

### mcp-server (port 3001)

| Method | Path | Description |
|--------|------|-------------|
| `GET`  | `/api/dice/roll` | Roll two dice — die values, total, emoji display |
| `GET`  | `/api/dice/app`  | Interactive HTML dice-roller UI |
| `POST` | `/api/calculator/calculate` | `{ "a": 10, "b": 3, "operation": "MULTIPLY" }` |
| `GET`  | `/api/mcp/info`  | Server metadata and registered MCP tools |
| `GET`  | `/swagger-ui.html` | Swagger UI |

**Calculator operations:** `ADD` `SUBTRACT` `MULTIPLY` `DIVIDE`

### mcp-client (port 3002)

| Method | Path | Description |
|--------|------|-------------|
| `GET`  | `/api/orchestrate/tools` | List all tools from the upstream MCP server |
| `POST` | `/api/orchestrate/invoke` | `{ "toolName": "roll-the-dice", "arguments": {} }` |
| `POST` | `/api/orchestrate/roll-dice` | Roll dice via MCP protocol |
| `POST` | `/api/orchestrate/calculate` | `{ "a": 10, "b": 3, "operation": "ADD" }` |
| `POST` | `/api/orchestrate/roll-then-calculate` | Multi-step: `{ "multiplier": 1.5 }` |
| `GET`  | `/api/client/info` | Client metadata and upstream connection info |
| `GET`  | `/swagger-ui.html` | Swagger UI |

---

## MCP Protocol (Claude Desktop)

Add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "mcp-apps-server": {
      "url": "http://localhost:3001/mcp"
    }
  }
}
```

---

## Project Structure

```
mcp-server/                              ← standalone Maven project
├── pom.xml                              (spring-boot-starter-parent 3.4.5)
└── src/main/java/com/example/server/
    ├── McpServerApplication.java
    ├── service/
    │   ├── DiceService.java             (@Tool: roll-the-dice)
    │   └── CalculatorService.java       (@Tool: calculator)
    └── controller/
        ├── DiceController.java
        ├── CalculatorController.java
        └── McpInfoController.java

mcp-client/                              ← standalone Maven project
├── pom.xml                              (spring-boot-starter-parent 3.4.5)
└── src/main/java/com/example/client/
    ├── McpClientApplication.java
    ├── service/
    │   └── McpOrchestrationService.java
    └── controller/
        ├── OrchestrationController.java
        └── ClientInfoController.java
```
