package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.channel.ConsoleChannel

/**
 * Simple LLM chatbot with default reactions.
 * Responds with a single message on each uer request.
 * Static properties are used in scenario constructor.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val agent = LLMAgent(
    name = "agent",
    model = "gpt-4.1-nano",
    instructions = "You're a helpful assistant"
)

fun main() {
    ConsoleChannel(agent.asBot).run()
}