package com.example.prreview.agent;

import com.example.prreview.model.ReviewRequest;
import com.example.prreview.model.ReviewResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Agentic PR reviewer.
 *
 * Flow:
 *  1. Parse PR URL → extract owner, repo, PR number
 *  2. Spin up GitHub MCP server as a subprocess via npx (stdio transport)
 *  3. List available GitHub tools and convert them to Azure AI tool definitions
 *  4. Run the agentic loop:
 *       a. Send messages + tools to Azure AI Foundry model
 *       b. If the model requests tool calls → execute each via MCP, append results
 *       c. Repeat until the model returns a final answer (finish_reason = stop)
 *  5. Return structured ReviewResult
 */
@Service
public class PrReviewAgent {

    private static final Logger log = LoggerFactory.getLogger(PrReviewAgent.class);
    private static final Pattern PR_URL_PATTERN =
        Pattern.compile("github\\.com/([^/]+)/([^/]+)/pull/(\\d+)");
    private static final int MAX_ITERATIONS = 20;

    @Value("${azure.ai.endpoint}")
    private String endpoint;

    @Value("${azure.ai.key}")
    private String apiKey;

    @Value("${azure.ai.model}")
    private String model;

    @Value("${github.token}")
    private String githubToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewResult review(ReviewRequest request) {
        PrCoords coords = parsePrUrl(request.prUrl());
        log.info("Reviewing PR #{} in {}/{}", coords.number(), coords.owner(), coords.repo());

        // Start GitHub MCP server subprocess
        var transport = new StdioClientTransport(
            ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-github")
                .env(Map.of("GITHUB_PERSONAL_ACCESS_TOKEN", githubToken))
                .build()
        );

        try (var mcpClient = McpClient.sync(transport).build()) {
            mcpClient.initialize();

            // Discover GitHub tools from MCP server
            List<McpSchema.Tool> mcpTools = mcpClient.listTools(null).tools();
            log.info("GitHub MCP server exposes {} tools", mcpTools.size());

            // Convert MCP tool schemas → Azure AI tool definitions
            List<ChatCompletionsToolDefinition> toolDefs = mcpTools.stream()
                .map(this::toAzureToolDefinition)
                .collect(Collectors.toList());

            // Build Azure AI client
            ChatCompletionsClient aiClient = new ChatCompletionsClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();

            // Seed conversation
            List<ChatRequestMessage> messages = new ArrayList<>();
            messages.add(new ChatRequestSystemMessage(buildSystemPrompt(request.instructions())));
            messages.add(new ChatRequestUserMessage(
                "Please review pull request #%d in the repository %s/%s. PR URL: %s"
                    .formatted(coords.number(), coords.owner(), coords.repo(), request.prUrl())
            ));

            String finalReview = null;
            int toolCallCount = 0;
            String prTitle = "";

            // Agentic loop
            for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
                log.debug("Agent iteration {}", iteration + 1);

                ChatCompletionsOptions options = new ChatCompletionsOptions(messages)
                    .setModel(model)
                    .setTools(toolDefs)
                    .setToolChoice(ChatCompletionsToolChoicePreset.AUTO);

                ChatCompletions response = aiClient.complete(options);
                ChatChoice choice = response.getChoices().get(0);
                ChatResponseMessage assistantMsg = choice.getMessage();

                if (CompletionsFinishReason.TOOL_CALLS.equals(choice.getFinishReason())) {
                    // Append assistant turn (with pending tool calls)
                    ChatRequestAssistantMessage assistantRequest =
                        new ChatRequestAssistantMessage(
                            assistantMsg.getContent() != null ? assistantMsg.getContent() : ""
                        );
                    assistantRequest.setToolCalls(assistantMsg.getToolCalls());
                    messages.add(assistantRequest);

                    // Execute each tool call via MCP
                    for (ChatCompletionsToolCall tc : assistantMsg.getToolCalls()) {
                        ChatCompletionsFunctionToolCall ftc = (ChatCompletionsFunctionToolCall) tc;
                        String toolName = ftc.getFunction().getName();
                        String argsJson = ftc.getFunction().getArguments();
                        toolCallCount++;

                        log.info("Tool call #{}: {} args={}", toolCallCount, toolName, argsJson);

                        String toolResult = executeMcpTool(mcpClient, toolName, argsJson);

                        // Extract PR title from get_pull_request call
                        if (toolName.contains("pull_request") && prTitle.isEmpty()) {
                            prTitle = extractPrTitle(toolResult);
                        }

                        messages.add(new ChatRequestToolMessage(toolResult, tc.getId()));
                    }

                } else {
                    // Model is done — final answer
                    finalReview = assistantMsg.getContent();
                    log.info("Agent completed after {} tool calls", toolCallCount);
                    break;
                }
            }

            if (finalReview == null) {
                finalReview = "Review could not be completed within the maximum number of iterations.";
            }

            return new ReviewResult(request.prUrl(), prTitle, finalReview, toolCallCount, model);

        } catch (Exception e) {
            log.error("PR review failed", e);
            throw new RuntimeException("PR review failed: " + e.getMessage(), e);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String buildSystemPrompt(String extraInstructions) {
        String base = """
            You are a senior software engineer performing a thorough code review of a GitHub pull request.

            Use the available GitHub tools to:
            1. Fetch the pull request details (title, description, author, base branch)
            2. List the changed files
            3. Read the diff or file contents for each changed file
            4. Optionally check related files for context

            Then produce a structured review with these sections:
            ## Summary
            Brief description of what the PR does.

            ## Strengths
            Things done well.

            ## Issues
            Bugs, logic errors, security concerns, or performance problems — with file/line references.

            ## Suggestions
            Non-blocking improvement suggestions.

            ## Verdict
            One of: ✅ Approve | ⚠️ Approve with minor changes | 🔴 Request changes

            Be specific, reference file names and line numbers where relevant.
            """;
        if (extraInstructions != null && !extraInstructions.isBlank()) {
            base += "\n\nAdditional instructions from the requester:\n" + extraInstructions;
        }
        return base;
    }

    private ChatCompletionsFunctionToolDefinition toAzureToolDefinition(McpSchema.Tool tool) {
        // Pass the MCP tool's JSON schema directly as the function parameters
        Object schema = tool.inputSchema() != null ? tool.inputSchema() : Map.of("type", "object");
        return new ChatCompletionsFunctionToolDefinition(
            new FunctionDefinition(tool.name())
                .setDescription(tool.description() != null ? tool.description() : "")
                .setParameters(BinaryData.fromObject(schema))
        );
    }

    private String executeMcpTool(io.modelcontextprotocol.client.McpSyncClient mcpClient,
                                   String toolName, String argsJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);
            McpSchema.CallToolResult result = mcpClient.callTool(
                new McpSchema.CallToolRequest(toolName, args)
            );

            if (result.content() == null || result.content().isEmpty()) {
                return "(empty response)";
            }

            return result.content().stream()
                .map(c -> c instanceof McpSchema.TextContent tc ? tc.text() : c.toString())
                .collect(Collectors.joining("\n"));

        } catch (Exception e) {
            log.warn("Tool '{}' execution failed: {}", toolName, e.getMessage());
            return "Error executing tool: " + e.getMessage();
        }
    }

    private String extractPrTitle(String json) {
        try {
            // The GitHub MCP server returns JSON; try to extract "title" field
            if (json.contains("\"title\"")) {
                int idx   = json.indexOf("\"title\"");
                int colon = json.indexOf(':', idx);
                int start = json.indexOf('"', colon + 1) + 1;
                int end   = json.indexOf('"', start);
                if (start > 0 && end > start) return json.substring(start, end);
            }
        } catch (Exception ignored) {}
        return "";
    }

    private PrCoords parsePrUrl(String url) {
        Matcher m = PR_URL_PATTERN.matcher(url);
        if (!m.find()) {
            throw new IllegalArgumentException(
                "Invalid GitHub PR URL: " + url + ". Expected: https://github.com/owner/repo/pull/N");
        }
        return new PrCoords(m.group(1), m.group(2), Integer.parseInt(m.group(3)));
    }

    private record PrCoords(String owner, String repo, int number) {}
}
