package com.example.prreview.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Structured pull request review result")
public record ReviewResult(

    @Schema(description = "The PR URL that was reviewed")
    String prUrl,

    @Schema(description = "PR title fetched from GitHub")
    String prTitle,

    @Schema(description = "Full review text produced by the AI agent")
    String review,

    @Schema(description = "Number of MCP tool calls the agent made during the review")
    int toolCallCount,

    @Schema(description = "Azure AI model used for the review")
    String model
) {}
