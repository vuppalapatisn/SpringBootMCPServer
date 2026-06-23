package com.example.server.controller;

import com.example.server.service.DiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Dice", description = "Dice rolling REST API (mirrors the MCP tool)")
@RestController
@RequestMapping("/api/dice")
public class DiceController {

    private final DiceService diceService;

    public DiceController(DiceService diceService) {
        this.diceService = diceService;
    }

    @Operation(summary = "Roll two dice", description = "Returns die values, total, and emoji display.")
    @GetMapping("/roll")
    public Map<String, Object> roll() {
        return diceService.rollTheDice();
    }

    @Operation(summary = "Dice roller HTML app", description = "Returns the interactive HTML dice-roller UI.")
    @GetMapping(value = "/app", produces = MediaType.TEXT_HTML_VALUE)
    public String app() throws IOException {
        return diceService.getDiceAppHtml();
    }
}
