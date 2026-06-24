package com.example.server.controller;

import com.example.server.service.CalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Calculator", description = "Arithmetic REST API (mirrors the `calculator` MCP tool)")
@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Operation(
        summary = "Perform arithmetic",
        description = "Calculates the result of applying `operation` to operands `a` and `b`. "
                    + "Supported operations: `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(name = "Add",      value = "{\"a\":10,\"b\":5,\"operation\":\"ADD\"}"),
                    @ExampleObject(name = "Subtract", value = "{\"a\":10,\"b\":5,\"operation\":\"SUBTRACT\"}"),
                    @ExampleObject(name = "Multiply", value = "{\"a\":10,\"b\":5,\"operation\":\"MULTIPLY\"}"),
                    @ExampleObject(name = "Divide",   value = "{\"a\":10,\"b\":5,\"operation\":\"DIVIDE\"}")
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Calculation result",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"result\": 50.0, \"operation\": \"MULTIPLY\", \"a\": 10.0, \"b\": 5.0}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid operation or division by zero")
        }
    )
    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody CalculateRequest req) {
        return calculatorService.calculate(req.a(), req.b(), req.operation());
    }

    public record CalculateRequest(
        @Schema(description = "First operand", example = "10") double a,
        @Schema(description = "Second operand", example = "5") double b,
        @Schema(description = "Operation: ADD, SUBTRACT, MULTIPLY, DIVIDE", example = "MULTIPLY") String operation
    ) {}
}
