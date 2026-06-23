package com.example.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Disable MCP client auto-connect during tests (no server running in CI)
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.mcp.client.sse.connections.mcp-server.url=http://localhost:9999"
})
class McpClientApplicationTests {

    @Test
    void contextLoads() {
    }
}
