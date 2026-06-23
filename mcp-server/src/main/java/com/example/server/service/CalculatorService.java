package com.example.server.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CalculatorService {

    @Tool(name = "calculator",
          description = "Performs basic arithmetic: add, subtract, multiply, divide. "
                      + "Supported operations: ADD, SUBTRACT, MULTIPLY, DIVIDE.")
    public Map<String, Object> calculate(
            @ToolParam(description = "First operand (number)") double a,
            @ToolParam(description = "Second operand (number)") double b,
            @ToolParam(description = "Operation: ADD, SUBTRACT, MULTIPLY, or DIVIDE") String operation) {

        double result = switch (operation.toUpperCase()) {
            case "ADD"      -> a + b;
            case "SUBTRACT" -> a - b;
            case "MULTIPLY" -> a * b;
            case "DIVIDE"   -> {
                if (b == 0) throw new ArithmeticException("Division by zero");
                yield a / b;
            }
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };

        return Map.of(
                "a", a,
                "b", b,
                "operation", operation.toUpperCase(),
                "result", result,
                "expression", a + " " + symbol(operation) + " " + b + " = " + result
        );
    }

    private String symbol(String op) {
        return switch (op.toUpperCase()) {
            case "ADD"      -> "+";
            case "SUBTRACT" -> "-";
            case "MULTIPLY" -> "×";
            case "DIVIDE"   -> "÷";
            default         -> op;
        };
    }
}
