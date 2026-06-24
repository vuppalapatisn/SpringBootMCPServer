package com.example.prreview;

import com.example.prreview.agent.PrReviewAgent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PrReviewAgentApplicationTests {

    // Mock the agent so no real Azure AI / GitHub calls are made during context load
    @MockitoBean
    PrReviewAgent prReviewAgent;

    @Test
    void contextLoads() {
    }
}
