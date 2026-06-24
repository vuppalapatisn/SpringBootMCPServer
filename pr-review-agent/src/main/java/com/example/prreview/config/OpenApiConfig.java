package com.example.prreview.config;

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

    @Value("${server.port:3003}")
    private int port;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PR Review Agent API")
                .description("""
                    AI-powered GitHub Pull Request reviewer.

                    **How it works**
                    1. You submit a GitHub PR URL
                    2. The agent starts `@modelcontextprotocol/server-github` as a subprocess
                    3. The Azure AI Foundry model uses GitHub MCP tools to read the PR, diffs, and files
                    4. The agent runs in a loop until the model produces a complete review
                    5. A structured review is returned with summary, issues, suggestions, and a verdict

                    **Required environment variables**

                    | Variable | Purpose |
                    |----------|---------|
                    | `AZURE_AI_ENDPOINT` | Azure AI Foundry project endpoint |
                    | `AZURE_AI_KEY` | Azure AI API key |
                    | `AZURE_AI_MODEL` | Model deployment name (e.g. `gpt-4o`) |
                    | `GITHUB_TOKEN` | GitHub PAT with `repo` read scope |

                    **Runtime dependency:** `npx` must be available on the server's PATH \
                    (Node.js ≥ 18 recommended).
                    """)
                .version("1.0.0")
                .contact(new Contact().name("MCP Apps").email("admin@example.com"))
                .license(new License().name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server().url("http://localhost:" + port).description("Local development")));
    }
}
