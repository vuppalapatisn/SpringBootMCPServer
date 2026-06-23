package com.example.client.controller;

import com.example.client.service.McpOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Orchestration", description = "MCP Client orchestration API — proxies and composes MCP server tools")
@RestController
@RequestMapping("/api/orchestrate")
public class OrchestrationController {

    private final McpOrchestrationService service;

    public OrchestrationController(McpOrchestrationService service) {
        this.service = service;
    }

    // ── tool discovery ────────────────────────────────────────────────────────

    @Operation(summary = "List MCP tools", description = "Returns all tools registered on the upstream MCP server.")
    @GetMapping("/tools")
    public List<Map<String, String>> listTools() {
        return service.listTools();
    }

    // ── generic invocation ────────────────────────────────────────────────────

    @Operation(
        summary = "Invoke any MCP tool",
        description = "Generic endpoint. Body: { \"toolName\": \"roll-the-dice\", \"arguments\": {} }"
    )
    @PostMapping("/invoke")
    public Object invoke(@RequestBody InvokeRequest req) {
        return service.invokeTool(req.toolName(), req.arguments() != null ? req.arguments() : Map.of());
    }

    // ── dice ──────────────────────────────────────────────────────────────────

    @Operation(summary = "Roll dice via MCP", description = "Calls the roll-the-dice MCP tool on the server.")
    @PostMapping("/roll-dice")
    public Object rollDice() {
        return service.rollDice();
    }

    // ── calculator ────────────────────────────────────────────────────────────

    @Operation(
        summary = "Calculate via MCP",
        description = "Calls the calculator MCP tool. Body: { \"a\": 10, \"b\": 3, \"operation\": \"MULTIPLY\" }"
    )
    @PostMapping("/calculate")
    public Object calculate(@RequestBody CalculateRequest req) {
        return service.calculate(req.a(), req.b(), req.operation());
    }

    // ── multi-step orchestration ──────────────────────────────────────────────

    @Operation(
        summary = "Roll then calculate (multi-step)",
        description = "Orchestrates two MCP tools: rolls dice, then multiplies the total by the given factor."
    )
    @PostMapping("/roll-then-calculate")
    public Map<String, Object> rollThenCalculate(@RequestBody RollThenCalcRequest req) {
        return service.rollThenCalculate(req.multiplier());
    }

    // ── request records ───────────────────────────────────────────────────────

    public record InvokeRequest(String toolName, Map<String, Object> arguments) {}
    public record CalculateRequest(double a, double b, String operation) {}
    public record RollThenCalcRequest(double multiplier) {}
}
