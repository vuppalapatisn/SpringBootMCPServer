package com.example.server.controller;

import com.example.server.service.CalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Calculator", description = "Arithmetic REST API (mirrors the MCP calculator tool)")
@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Operation(
        summary = "Calculate",
        description = "Body: { \"a\": 10, \"b\": 5, \"operation\": \"ADD\" }. "
                    + "Operations: ADD, SUBTRACT, MULTIPLY, DIVIDE."
    )
    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody CalculateRequest req) {
        return calculatorService.calculate(req.a(), req.b(), req.operation());
    }

    public record CalculateRequest(double a, double b, String operation) {}
}
