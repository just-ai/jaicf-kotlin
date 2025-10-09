package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.mcp.McpService
import com.justai.jaicf.activator.llm.mcp.getTool
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel

/**
 * This example demonstrates connecting to MCP services through Docker MCP Gateway.
 *
 * Docker MCP Gateway allows you to run MCP servers in containers and access them via HTTP.
 *
 * ## Supported Transports in Docker MCP Gateway:
 * - **SSE** (`--transport=sse`) - Server-Sent Events, use with `McpService.sse()`
 * - **Streaming** (`--transport=streaming`) - HTTP with streaming support, use with `McpService.streamableHttp()`
 *
 * ## Setup:
 * 1. See docker-compose.yml at: activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/mcp/gateway/docker-compose.yml
 * 2. Start the gateway: `docker-compose up`
 * 3. Connect to the gateway using one of the transports below
 *
 * ## Using MCP Services with API Keys:
 * Some services require API keys. Pass them via environment variables in docker-compose.yml:
 * ```yaml
 * environment:
 *   EXA_API_KEY: your-api-key-here
 * ```
 *
 * For STDIO transport examples, see ScenarioWithMcpServiceStdio.kt
 */

val mcpServiceSse = McpService.sse(
    urlString = "http://localhost:8081/sse"
)

/**
 * Example using DuckDuckGo via Docker MCP Gateway
 * Available tools: "search" and "fetch_content"
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    llmState("main", {
        model = "gpt-4.1-nano"

        // The most simple way to add a tool. Provide a tool name via `toolName` parameter.
        tool(mcpServiceSse.getTool("search"))

        /**
         * You can also use other options for your scenario:
         *
         * Option 1: expose a subset of tools from the MCP service via the `tools` parameter.
         * ```
         * mcp(service = mcpServiceSse, tools = listOf("search", "fetch_content"))
         * ```
         *
         * Option 2: Omit the `tools` parameter to expose all tools from the MCP service.
         * ```
         * mcp(service = mcpServiceSse)
         * ```
         *
         * Option 3: expose a single specific tool from the MCP service.
         * ```
         * tool<JsonValue>(mcpServiceSse, "search")
         * ```
         *
         * Option 4: expose a single tool and override its default description to fit your scenario.
         * ```
         * tool<JsonValue>(
         *     mcp = mcpServiceSse,
         *     toolName = "search",
         *     description = "Search the web when user asks to find information online"
         * )
         * ```
         *
         * You can also handle tool call result before sending it back to LLM
         * ```
         * mcp(service = mcpServiceSse, tools = listOf("search")) {
         *     // Handle the tool results via the `it` parameter.
         *     // For example, you can format or filter the response here
         *     it
         * }
         * ```
         */
    })
}

fun main() {
    ConsoleChannel(BotEngine(scenario))
        .run("Hi")
}