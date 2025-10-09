package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.mcp.McpService
import com.justai.jaicf.activator.llm.mcp.getTool
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.openai.core.JsonValue

/**
 * This example shows how to use MCP services within a JAICF scenario.
 *
 * You can expose either a single tool from an MCP service or all tools at once.
 *
 * ## Supported Transport Protocols:
 * - **STDIO** - Direct process communication (use `McpService.stdio()`)
 * - **SSE** - Server-Sent Events (use `McpService.sse()`)
 * - **StreamableHttp** - HTTP with streaming support (use `McpService.streamableHttp()`)
 * - **WebSocket** - WebSocket protocol (use `McpService.websocket()`)
 *
 * ### Docker MCP Gateway Transports:
 * Docker MCP Gateway supports: `stdio`, `sse`, `streaming` (maps to StreamableHttp)
 * - SSE: `--transport=sse` → use with `McpService.sse()`
 * - Streaming: `--transport=streaming` → use with `McpService.streamableHttp()`
 *
 * ## Example 1: STDIO Transport (Direct Process Communication)
 *
 * This example uses an STDIO transport connection to the [Amazon Product Search](https://smithery.ai/server/@SiliconValleyInsight/amazon-product-search) service.
 *
 * IMPORTANT! If you use the STDIO transport type, consider calling the McpService#close() method to free resources.
 */
val mcpServiceStdio = McpService.stdio(
    listOf(
        "npx",
        "-y",
        "@smithery/cli@latest",
        "run",
        "@SiliconValleyInsight/amazon-product-search",
        "--key",
        "<YOUR_API_KEY>"
    )
)

/**
 * ## Example 2: SSE Transport via Docker MCP Gateway
 *
 * This example demonstrates connecting to MCP services through Docker MCP Gateway.
 * Docker MCP Gateway allows you to run MCP servers in containers and access them via HTTP.
 *
 * ### Setup:
 * 1. See docker-compose.yml example at: activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/mcp/gateway/docker-compose.yml
 * 2. Start the gateway: `docker-compose up`
 * 3. Connect to the gateway via SSE transport as shown below
 *
 * ### Using MCP Services with API Keys:
 * Some services require API keys. Pass them via environment variables in docker-compose.yml:
 * ```
 * environment:
 *   EXA_API_KEY: your-api-key-here
 * ```
 *
 * ### StreamableHttp Transport:
 * For streaming HTTP support, use `McpService.streamableHttp()` which corresponds to
 * Docker MCP Gateway's `--transport=streaming` option. This provides HTTP-based streaming
 * for better performance with long-running operations.
 * ```
 * val mcpStreamable = McpService.streamableHttp("http://localhost:8083/mcp")
 * ```
 *
 * This example connects to DuckDuckGo search service running on port 8081 (SSE).
 */
val mcpServiceSse = McpService.sse(
    urlString = "http://localhost:8081/sse"
)

/**
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    llmState("main", {
        model = "gpt-4.1-nano"

        // Example using DuckDuckGo via Docker MCP Gateway
        // Available tools: "search" and "fetch_content"

        // Option 1: The most simple way to add a tool. Provide a tool name via `toolName` parameter.
        tool(mcpServiceSse.getTool("search"))

        // Option 2: expose a subset of tools from the MCP service via the `tools` parameter.
        mcp(service = mcpServiceSse, tools = listOf("search", "fetch_content"))

        // Option 3: Omit the `tools` parameter to expose all tools from the MCP service.
        mcp(service = mcpServiceSse)

        // Option 4: expose a single specific tool from the MCP service.
        tool<JsonValue>(mcpServiceSse, "search")

        // Option 5: expose a single tool and override its default description to fit your scenario.
        tool<JsonValue>(
            mcp = mcpServiceSse,
            toolName = "search",
            description = "Search the web using DuckDuckGo when user asks to find information online"
        )

        // You can also handle tool call result before sending it back to LLM
        mcp(service = mcpServiceSse, tools = listOf("search")) {
            // Handle the tool results via the `it` parameter.
            // For example, you can format or filter the response here
            it
        }
    }) {
        llm.withToolCalls {
            reactions.streamOrSay()
        }
    }
}

fun main() {
    ConsoleChannel(BotEngine(scenario))
        .run("Hi")
}