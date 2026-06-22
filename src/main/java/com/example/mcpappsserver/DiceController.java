package com.example.mcpappsserver;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/dice")
public class DiceController {

    private final DiceApp diceApp;

    public DiceController(DiceApp diceApp) {
        this.diceApp = diceApp;
    }

    @GetMapping(value = "/app", produces = MediaType.TEXT_HTML_VALUE)
    public String getApp() throws IOException {
        return diceApp.getDiceAppResource();
    }

    @PostMapping(value = "/roll", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> roll() {
        String message = diceApp.rollTheDice();
        return Map.of("message", message);
    }
}
