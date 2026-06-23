package com.example.client;

import com.example.client.service.McpOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Context-load smoke test.
 *
 * spring.ai.mcp.client.enabled=false disables McpClientAutoConfiguration so it
 * never calls McpSyncClient.initialize() (which eagerly connects to the MCP server
 * and fails in CI where no server is running).
 *
 * @MockitoBean replaces McpOrchestrationService with a mock so its constructor —
 * which expects a List<McpSyncClient> — is never invoked.
 */
@SpringBootTest(properties = "spring.ai.mcp.client.enabled=false")
class McpClientApplicationTests {

    @MockitoBean
    McpOrchestrationService mcpOrchestrationService;

    @Test
    void contextLoads() {
    }
}
