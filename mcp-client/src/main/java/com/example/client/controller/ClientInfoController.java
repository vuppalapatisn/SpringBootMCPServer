package com.example.client.controller;

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

import java.util.Map;

@Tag(name = "Client Info", description = "MCP Client / Orchestrator metadata and connection details")
@RestController
@RequestMapping("/api/client")
public class ClientInfoController {

    @Value("${server.port:3002}")
    private int port;

    @Value("${mcp.server.url:http://localhost:3001}")
    private String mcpServerUrl;

    @Operation(
        summary = "Client info",
        description = "Returns orchestrator metadata and the upstream MCP server URL.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Client metadata",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                        {
                          "name": "mcp-client-orchestrator",
                          "version": "1.0.0",
                          "port": 3002,
                          "mcpServerUrl": "http://localhost:3001"
                        }
                        """)
                )
            )
        }
    )
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
            "name",          "mcp-client-orchestrator",
            "version",       "1.0.0",
            "port",          port,
            "mcpServerUrl",  mcpServerUrl,
            "restApis", Map.of(
                "listTools",         "GET  /api/orchestrate/tools",
                "invokeAnyTool",     "POST /api/orchestrate/invoke",
                "rollDice",          "POST /api/orchestrate/roll-dice",
                "calculate",         "POST /api/orchestrate/calculate",
                "rollThenCalculate", "POST /api/orchestrate/roll-then-calculate",
                "clientInfo",        "GET  /api/client/info"
            )
        );
    }
}
