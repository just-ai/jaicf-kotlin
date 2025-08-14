package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.mcp.McpServiceImplFactory
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.openai.core.JsonValue

/**
 * This example shows how to use MCP services within a JAICF scenario.
 *
 * You can expose either a single tool from an MCP service or all tools at once.
 * Supported transport protocols: STDIO, SSE, and WebSocket.
 * This example uses an STDIO transport connection to the [Amazon Product Search](https://smithery.ai/server/@SiliconValleyInsight/amazon-product-search) service.
 */
val mcpService = McpServiceImplFactory().stdio(
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
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    llmState("main", {
        model = "gpt-4.1-nano"

        // Option 1: expose a subset of tools from the MCP service via the `tools` parameter.
        // In this example, only the `find_products_to_buy` and `get_product_details` tools are available.
        // Omit the `tools` parameter to expose all tools from the MCP service.
        mcp(service = mcpService, tools = listOf("find_products_to_buy", "get_product_details")) {
            // Handle the tool results via the `it` parameter.
        }

        // Option 2: expose a single specific tool from the MCP service.
        tool<JsonValue>(mcpService, "shop_for_items") {
            // Handle the tool results via the `it` parameter.
        }

        // Option 3: expose a single tool and override its default description to fit your scenario.
        tool<JsonValue>(
            service = mcpService,
            toolName = "get_search_options",
            description = "Use this tool only if user directly asks about search options"
        ) {
            // Handle the tool results via the `it` parameter.
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