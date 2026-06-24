package com.example.client.controller;

import com.example.client.service.McpOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Orchestration", description = "MCP Client orchestration API — proxies and composes MCP server tools over REST")
@RestController
@RequestMapping("/api/orchestrate")
public class OrchestrationController {

    private final McpOrchestrationService service;

    public OrchestrationController(McpOrchestrationService service) {
        this.service = service;
    }

    @Operation(
        summary = "List MCP tools",
        description = "Queries the upstream MCP server and returns all registered tools with their descriptions.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tool list",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                        [
                          { "name": "roll-the-dice", "description": "Rolls two dice and returns the result" },
                          { "name": "calculator",    "description": "Performs ADD, SUBTRACT, MULTIPLY, DIVIDE arithmetic" }
                        ]
                        """)
                )
            )
        }
    )
    @GetMapping("/tools")
    public List<Map<String, String>> listTools() {
        return service.listTools();
    }

    @Operation(
        summary = "Invoke any MCP tool",
        description = "Generic endpoint — send any tool name and argument map; the call is forwarded to the MCP server.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(name = "Roll dice",  value = "{\"toolName\":\"roll-the-dice\",\"arguments\":{}}"),
                    @ExampleObject(name = "Calculator", value = "{\"toolName\":\"calculator\",\"arguments\":{\"a\":10,\"b\":3,\"operation\":\"ADD\"}}")
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Tool result from MCP server")
        }
    )
    @PostMapping("/invoke")
    public Object invoke(@RequestBody InvokeRequest req) {
        return service.invokeTool(req.toolName(), req.arguments() != null ? req.arguments() : Map.of());
    }

    @Operation(
        summary = "Roll dice via MCP",
        description = "Convenience wrapper — calls the `roll-the-dice` MCP tool and returns the result.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Dice result from MCP server",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"die1\":4,\"die2\":3,\"total\":7,\"emoji\":\"🎲🎲\"}")
                )
            )
        }
    )
    @PostMapping("/roll-dice")
    public Object rollDice() {
        return service.rollDice();
    }

    @Operation(
        summary = "Calculate via MCP",
        description = "Convenience wrapper — calls the `calculator` MCP tool. "
                    + "Supported operations: `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(name = "Multiply", value = "{\"a\":10,\"b\":3,\"operation\":\"MULTIPLY\"}"),
                    @ExampleObject(name = "Divide",   value = "{\"a\":10,\"b\":4,\"operation\":\"DIVIDE\"}")
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Arithmetic result from MCP server")
        }
    )
    @PostMapping("/calculate")
    public Object calculate(@RequestBody CalculateRequest req) {
        return service.calculate(req.a(), req.b(), req.operation());
    }

    @Operation(
        summary = "Roll dice then calculate (multi-step orchestration)",
        description = "Demonstrates chaining two MCP tool calls: rolls two dice, then multiplies the total by `multiplier`.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"multiplier\":1.5}")
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Orchestration result with both tool outputs",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                        {
                          "step1_dice": "{ \\"die1\\": 4, \\"die2\\": 3, \\"total\\": 7 }",
                          "step2_calc": "10.5",
                          "summary": "Dice total 7 × 1.5 = 10.5"
                        }
                        """)
                )
            )
        }
    )
    @PostMapping("/roll-then-calculate")
    public Map<String, Object> rollThenCalculate(@RequestBody RollThenCalcRequest req) {
        return service.rollThenCalculate(req.multiplier());
    }

    public record InvokeRequest(
        @Schema(description = "MCP tool name", example = "roll-the-dice") String toolName,
        @Schema(description = "Tool arguments as key-value pairs") Map<String, Object> arguments
    ) {}

    public record CalculateRequest(
        @Schema(description = "First operand", example = "10") double a,
        @Schema(description = "Second operand", example = "3") double b,
        @Schema(description = "Operation: ADD, SUBTRACT, MULTIPLY, DIVIDE", example = "MULTIPLY") String operation
    ) {}

    public record RollThenCalcRequest(
        @Schema(description = "Factor to multiply the dice total by", example = "1.5") double multiplier
    ) {}
}
