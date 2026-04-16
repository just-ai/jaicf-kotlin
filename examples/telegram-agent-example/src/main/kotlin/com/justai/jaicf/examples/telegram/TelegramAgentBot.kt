package com.justai.jaicf.examples.telegram

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.examples.telegram.tools.BotModeTool
import com.justai.jaicf.examples.telegram.tools.CalcTool

/**
 * Telegram bot with LLM agent and tools.
 * Supports streaming responses and demonstrates tool usage patterns.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
val TelegramAgentBot = LLMAgent(
    name = "telegram-agent",
    model = "gpt-4.1-nano",
    instructions = """
        You're a helpful Telegram bot assistant.
        You can chat with users, perform calculations, and help them understand how this bot works.
    """.trimIndent(),
    tools = listOf(BotModeTool, CalcTool)
) {
    llm.withToolCalls { results ->
        // Show tool calls results if any
        results.takeIf { it.isNotEmpty() }?.run {
            reactions.say(joinToString(prefix = ">> RESULTS: ", postfix = "\n") { "${it.result}" })
        }

        // Show tool calls if any
        toolCalls().takeIf { it.isNotEmpty() }?.run {
            reactions.say(joinToString(prefix = ">> CALLING: ") {
                "${it.function().name()}(${it.function().arguments()})"
            })
        }
    }

    // Stream final response after all tool calls are done
    reactions.streamOrSay()
}
