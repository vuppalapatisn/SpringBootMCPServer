package com.example.server.controller;

import com.example.server.service.DiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Dice", description = "Dice rolling REST API (mirrors the `roll-the-dice` MCP tool)")
@RestController
@RequestMapping("/api/dice")
public class DiceController {

    private final DiceService diceService;

    public DiceController(DiceService diceService) {
        this.diceService = diceService;
    }

    @Operation(
        summary = "Roll two dice",
        description = "Rolls two six-sided dice and returns individual die values, the total, and an emoji display.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Dice roll result",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                        {
                          "die1": 4,
                          "die2": 3,
                          "total": 7,
                          "emoji": "🎲🎲"
                        }
                        """)
                )
            )
        }
    )
    @GetMapping("/roll")
    public Map<String, Object> roll() {
        return diceService.rollTheDice();
    }

    @Operation(
        summary = "Interactive dice-roller UI",
        description = "Returns an HTML page with a clickable dice-roller UI.",
        responses = {
            @ApiResponse(responseCode = "200", description = "HTML page",
                content = @Content(mediaType = MediaType.TEXT_HTML_VALUE))
        }
    )
    @GetMapping(value = "/app", produces = MediaType.TEXT_HTML_VALUE)
    public String app() throws IOException {
        return diceService.getDiceAppHtml();
    }
}
