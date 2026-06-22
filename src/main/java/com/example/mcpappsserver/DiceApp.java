package com.example.mcpappsserver;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;

@Service
public class DiceApp {

  @Value("classpath:/app/dice-app.html")
  private Resource diceAppResource;

  public String getDiceAppResource() throws IOException {
    return diceAppResource.getContentAsString(Charset.defaultCharset());
  }

  @Tool(name = "roll-the-dice", description = "Rolls the dice")
  public String rollTheDice() {
    return "Opening dice roller app.";
  }

}
