package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.scenario.LLMChatScenario
import com.justai.jaicf.channel.ConsoleChannel

/**
 * Simple LLM chatbot with default reactions.
 * Responds with a single message on each uer request.
 * Static properties are used in scenario constructor.
 */
private val scenario = LLMChatScenario(
    name = "chat",
    model = "gpt-4.1-nano",
    instructions = "You're a helpful assistant"
)

fun main() {
    ConsoleChannel(scenario.asBot).run()
}