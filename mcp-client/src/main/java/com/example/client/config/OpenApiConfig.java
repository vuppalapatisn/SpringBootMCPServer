package com.example.client.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:3002}")
    private int port;

    @Value("${mcp.server.url:http://localhost:3001}")
    private String mcpServerUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MCP Client / Orchestrator API")
                .description("""
                    Spring Boot MCP Client — connects to the upstream MCP Server \
                    and exposes its tools as a plain REST orchestration API.

                    **Upstream MCP Server:** `%s`

                    **Endpoints**
                    | Path | Description |
                    |------|-------------|
                    | `GET /api/orchestrate/tools` | List all tools on the MCP server |
                    | `POST /api/orchestrate/invoke` | Generic tool invocation |
                    | `POST /api/orchestrate/roll-dice` | Roll dice via MCP |
                    | `POST /api/orchestrate/calculate` | Arithmetic via MCP |
                    | `POST /api/orchestrate/roll-then-calculate` | Multi-step orchestration |
                    """.formatted(mcpServerUrl))
                .version("1.0.0")
                .contact(new Contact()
                    .name("MCP Apps")
                    .email("admin@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + port)
                    .description("Local development server")));
    }
}
