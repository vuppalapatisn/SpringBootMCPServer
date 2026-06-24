package com.example.prreview.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pull request review request")
public record ReviewRequest(

    @Schema(
        description = "Full GitHub PR URL",
        example = "https://github.com/owner/repo/pull/42"
    )
    String prUrl,

    @Schema(
        description = "Optional custom review instructions added to the system prompt",
        example = "Focus on security vulnerabilities and SQL injection risks.",
        nullable = true
    )
    String instructions
) {}
