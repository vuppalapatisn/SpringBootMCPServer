package com.example.client;

import com.example.client.service.McpOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Context-load smoke test.
 * McpOrchestrationService is mocked as a field so no real MCP server
 * connection is needed in CI. @MockitoBean must be a field annotation —
 * class-level 'types' attribute does not exist in Spring Framework 6.2.
 */
@SpringBootTest
class McpClientApplicationTests {

    @MockitoBean
    McpOrchestrationService mcpOrchestrationService;

    @Test
    void contextLoads() {
    }
}
