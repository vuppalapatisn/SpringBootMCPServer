package com.example.client.service;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestration service that wraps MCP client calls to the upstream mcp-server.
 * Each public method corresponds to one MCP tool exposed by the server.
 */
@Service
public class McpOrchestrationService {

    private final McpSyncClient mcpSyncClient;

    public McpOrchestrationService(McpSyncClient mcpSyncClient) {
        this.mcpSyncClient = mcpSyncClient;
    }

    /** Lists every tool registered on the remote MCP server. */
    public List<Map<String, String>> listTools() {
        McpSchema.ListToolsResult result = mcpSyncClient.listTools(null);
        return result.tools().stream()
                .map(t -> Map.of(
                        "name",        t.name(),
                        "description", t.description() != null ? t.description() : ""))
                .collect(Collectors.toList());
    }

    /** Invokes any tool on the MCP server by name, passing arbitrary arguments. */
    public Object invokeTool(String toolName, Map<String, Object> arguments) {
        McpSchema.CallToolRequest req = new McpSchema.CallToolRequest(toolName, arguments);
        McpSchema.CallToolResult result = mcpSyncClient.callTool(req);
        return extractContent(result);
    }

    /** Convenience wrapper — rolls the dice via the MCP tool. */
    public Object rollDice() {
        return invokeTool("roll-the-dice", Map.of());
    }

    /** Convenience wrapper — performs arithmetic via the MCP calculator tool. */
    public Object calculate(double a, double b, String operation) {
        return invokeTool("calculator", Map.of("a", a, "b", b, "operation", operation));
    }

    /**
     * Demonstrates multi-tool orchestration:
     * rolls the dice, then uses the total as input for a calculator operation.
     */
    public Map<String, Object> rollThenCalculate(double multiplier) {
        Object diceResult = rollDice();
        // Extract total from dice result (content is a JSON string from the tool)
        String diceContent = contentToString(diceResult);

        // Parse the total — tools return JSON text; do a simple extraction
        int total = extractTotal(diceContent);

        Object calcResult = calculate(total, multiplier, "MULTIPLY");
        return Map.of(
                "step1_dice",   diceResult,
                "step2_calc",   calcResult,
                "summary",      "Dice total " + total + " × " + multiplier + " = " + (total * multiplier)
        );
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Object extractContent(McpSchema.CallToolResult result) {
        if (result.content() == null || result.content().isEmpty()) {
            return Map.of("error", "Empty response from tool");
        }
        if (result.content().size() == 1) {
            return contentItem(result.content().get(0));
        }
        return result.content().stream()
                .map(this::contentItem)
                .collect(Collectors.toList());
    }

    private Object contentItem(McpSchema.Content c) {
        if (c instanceof McpSchema.TextContent tc) return tc.text();
        return c.toString();
    }

    private String contentToString(Object content) {
        return content != null ? content.toString() : "";
    }

    private int extractTotal(String json) {
        try {
            int idx = json.indexOf("\"total\"");
            if (idx < 0) return 7; // fallback
            int colon = json.indexOf(':', idx);
            int comma  = json.indexOf(',', colon);
            int brace  = json.indexOf('}', colon);
            int end    = (comma > 0 && comma < brace) ? comma : brace;
            return (int) Double.parseDouble(json.substring(colon + 1, end).trim());
        } catch (Exception e) {
            return 7;
        }
    }
}
