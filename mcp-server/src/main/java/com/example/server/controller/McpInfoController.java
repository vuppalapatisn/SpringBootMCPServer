package com.example.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "MCP Info", description = "Metadata about this MCP server and its registered tools")
@RestController
@RequestMapping("/api/mcp")
public class McpInfoController {

    @Value("${server.port:3001}")
    private int port;

    @Operation(
        summary = "Server info",
        description = "Returns MCP server metadata, registered tool names, and available REST API paths.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Server metadata",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                        {
                          "name": "mcp-apps-server",
                          "version": "1.0.0",
                          "protocol": "streamable-http",
                          "mcpEndpoint": "http://localhost:3001/mcp",
                          "tools": [
                            { "name": "roll-the-dice", "description": "Rolls two dice and returns the result" },
                            { "name": "calculator",    "description": "Performs ADD, SUBTRACT, MULTIPLY, DIVIDE arithmetic" }
                          ]
                        }
                        """)
                )
            )
        }
    )
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
            "name", "mcp-apps-server",
            "version", "1.0.0",
            "protocol", "streamable-http",
            "mcpEndpoint", "http://localhost:" + port + "/mcp",
            "tools", List.of(
                Map.of("name", "roll-the-dice",
                       "description", "Rolls two dice and returns the result"),
                Map.of("name", "calculator",
                       "description", "Performs ADD, SUBTRACT, MULTIPLY, DIVIDE arithmetic")
            ),
            "restApis", Map.of(
                "rollDice",    "GET  /api/dice/roll",
                "diceApp",     "GET  /api/dice/app",
                "calculate",   "POST /api/calculator/calculate",
                "mcpInfo",     "GET  /api/mcp/info"
            )
        );
    }
}
