package com.example.server.config;

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

    @Value("${server.port:3001}")
    private int port;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MCP Server API")
                .description("""
                    Spring Boot MCP Server — exposes AI-callable tools over the \
                    Model Context Protocol (streamable-HTTP) and mirrors them as \
                    plain REST endpoints.

                    **MCP Endpoint:** `http://localhost:3001/mcp`

                    **Registered MCP Tools**
                    | Tool | Description |
                    |------|-------------|
                    | `roll-the-dice` | Rolls two six-sided dice |
                    | `calculator` | ADD / SUBTRACT / MULTIPLY / DIVIDE |
                    """)
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
