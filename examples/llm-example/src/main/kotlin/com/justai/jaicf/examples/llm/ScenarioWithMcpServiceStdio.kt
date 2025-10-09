package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.mcp.McpService
import com.justai.jaicf.activator.llm.mcp.getTool
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.openai.core.JsonValue

/**
 * This example demonstrates using MCP services via STDIO transport.
 *
 * STDIO transport allows direct process communication with MCP servers running as external processes.
 * This is useful for npx-based MCP servers from [Smithery.ai](https://smithery.ai)
 *
 * ## STDIO Transport:
 * - Direct process communication
 * - Use with npx commands to run MCP servers
 * - IMPORTANT! Call `McpService#close()` to free resources when done
 *
 * ## Example: Amazon Product Search via STDIO
 *
 * This example uses STDIO transport to connect to the [Amazon Product Search](https://smithery.ai/server/@SiliconValleyInsight/amazon-product-search) service.
 * The service is launched via npx and communicates through standard input/output.
 *
 * For SSE, StreamableHttp transport examples, see ScenarioWithMcpServiceSSE.kt
 */
val mcpService = McpService.stdio(
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
 * Example using Amazon Product Search via STDIO transport
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    llmState("main", {
        model = "gpt-4.1-nano"

        // The most simple way to add a tool. Provide a tool name via `toolName` parameter.
        tool(mcpService.getTool("find_products_to_buy"))

        /**
         * You can also use other options for your scenario:
         *
         * Option 1: expose a subset of tools from the MCP service via the `tools` parameter.
         * In this example, only the `find_products_to_buy` and `get_product_details` tools are available.
         * ```
         * mcp(service = mcpService, tools = listOf("find_products_to_buy", "get_product_details"))
         * ```
         *
         * Option 2: Omit the `tools` parameter to expose all tools from the MCP service.
         * ```
         * mcp(service = mcpService)
         * ```
         *
         * Option 3: expose a single specific tool from the MCP service.
         * ```
         * tool<JsonValue>(mcpService, "shop_for_items")
         * ```
         *
         * Option 4: expose a single tool and override its default description to fit your scenario.
         * ```
         * tool<JsonValue>(
         *     mcp = mcpService,
         *     toolName = "get_search_options",
         *     description = "Use this tool only if user directly asks about search options"
         * )
         * ```
         *
         * You can also handle tool call result before sending it back to LLM
         * ```
         * mcp(service = mcpService, tools = listOf("get_cache_stats")) {
         *     // Handle the tool results via the `it` parameter.
         *     it
         * }
         * ```
         */
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