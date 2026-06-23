package com.example.server.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

@Service
public class DiceService {

    private static final String[] FACES = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};
    private final Random random = new Random();

    @Value("classpath:/app/dice-app.html")
    private Resource diceAppHtml;

    public String getDiceAppHtml() throws IOException {
        return diceAppHtml.getContentAsString(StandardCharsets.UTF_8);
    }

    @Tool(name = "roll-the-dice",
          description = "Rolls two dice and returns the result. Returns the face values (1-6) for each die.")
    public Map<String, Object> rollTheDice() {
        int die1 = random.nextInt(6) + 1;
        int die2 = random.nextInt(6) + 1;
        return Map.of(
                "die1", die1,
                "die2", die2,
                "total", die1 + die2,
                "display", FACES[die1 - 1] + " " + FACES[die2 - 1],
                "message", "Rolled " + die1 + " and " + die2 + " — total: " + (die1 + die2)
        );
    }
}
