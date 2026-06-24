package com.example.prreview.controller;

import com.example.prreview.agent.PrReviewAgent;
import com.example.prreview.model.ReviewRequest;
import com.example.prreview.model.ReviewResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PR Review", description = "AI-powered pull request review using Azure AI Foundry + GitHub MCP server")
@RestController
@RequestMapping("/api/review")
public class PrReviewController {

    private final PrReviewAgent agent;

    public PrReviewController(PrReviewAgent agent) {
        this.agent = agent;
    }

    @Operation(
        summary = "Review a GitHub pull request",
        description = """
            Submits a PR to the AI review agent. The agent:
            1. Starts the GitHub MCP server (`@modelcontextprotocol/server-github`) as a subprocess
            2. Sends the PR URL to the Azure AI Foundry model with GitHub tools available
            3. Runs an agentic loop — the model fetches PR details, diffs, and file contents via MCP tool calls
            4. Returns a structured review with summary, issues, suggestions, and a verdict

            **Prerequisites:**
            - `AZURE_AI_ENDPOINT`, `AZURE_AI_KEY`, `AZURE_AI_MODEL` configured
            - `GITHUB_TOKEN` with `repo` read scope
            - `npx` available on the server's PATH
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(
                        name = "Basic review",
                        value = "{\"prUrl\":\"https://github.com/owner/repo/pull/42\",\"instructions\":null}"
                    ),
                    @ExampleObject(
                        name = "Security-focused review",
                        value = "{\"prUrl\":\"https://github.com/owner/repo/pull/42\",\"instructions\":\"Focus on authentication and SQL injection risks.\"}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Review completed successfully",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                        {
                          "prUrl": "https://github.com/owner/repo/pull/42",
                          "prTitle": "Add user authentication middleware",
                          "review": "## Summary\\nThis PR adds JWT-based auth...\\n## Verdict\\n⚠️ Approve with minor changes",
                          "toolCallCount": 5,
                          "model": "gpt-4o"
                        }
                        """)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid PR URL format"),
            @ApiResponse(responseCode = "500", description = "Review failed — check Azure AI / GitHub token config")
        }
    )
    @PostMapping
    public ReviewResult review(@RequestBody ReviewRequest request) {
        return agent.review(request);
    }
}
