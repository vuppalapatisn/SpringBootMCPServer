package com.example.client;

import com.example.client.service.McpOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Context-load smoke test.
 * McpOrchestrationService is mocked so no real MCP server connection is needed in CI.
 */
@SpringBootTest
@MockitoBean(types = McpOrchestrationService.class)
class McpClientApplicationTests {

    @Test
    void contextLoads() {
    }
}
