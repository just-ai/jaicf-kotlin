package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.examples.llm.channel.ConsoleChannel

/**
 * This example shows how an agent can be exposed as a tool to call it from anywhere instead of hand off an entire conversation to it.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */

// Calculator agent with custom instructions
private val calculatorAgent = LLMAgent(
    name = "calculator",
    model = "gpt-4.1-mini",
    instructions = "Calculate math expressions. Return result in form of step-by-step math calculations without any additional text or comments.",
)
    .withoutMemory()  // Make this agent stateless because LLMAgent has memory by default

private val mainAgent = LLMAgent("main", {
    model = "gpt-4.1-nano"

    // Add calculator agent as a tool to main agent
    // You can also define custom "name", "description" and "parameters" of the created tool using `asTool()`
    tool(calculatorAgent.asTool)
}) {
    activator.withToolCalls {

        // Show tool calls status
        activator.toolCalls()
            .joinToString { "${it.function().name()}(${it.function().arguments()})" }
            .takeIf { it.isNotEmpty() }
            ?.also { reactions.say("CALLING: $it") }

        // Show intermediate or final LLM response
        activator.contentStream()?.also(reactions::say)
    }
}

fun main() {
    ConsoleChannel(mainAgent.asBot)
        .run("Calculate any random math expression")
}