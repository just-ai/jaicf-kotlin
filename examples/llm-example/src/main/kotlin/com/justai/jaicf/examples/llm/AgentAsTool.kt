package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.tool.llmTool
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.Calculator
import kotlinx.coroutines.delay

/**
 * This example shows how an agent can be exposed as a tool to call it from anywhere instead of hand off an entire conversation to it.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */

// Calculator agent with custom instructions
private val calculatorAgent = LLMAgent(
    name = "calculator",
    model = "gpt-4.1-mini",
    instructions = "Calculate math expressions with calculator tool. Answer strictly with tool output without any change.",
    tools = listOf(
        llmTool<Calculator> {
            println(">> CALCULATING ${call.arguments.expression}")
            delay(2000)
            Math.random()
        }
    )
)

private val mainAgent = LLMAgent("main", {
    model = "gpt-4.1-nano"

    // Add calculator agent as a tool to main agent
    // You can also define custom "name", "description" and "parameters" of the created tool using `asTool()`
    // You also can make this tool stateful using `withMemory` argument
    tool(calculatorAgent.asTool)
}) {
    llm.withToolCalls {

        // Show tool calls status
        llm.toolCalls()
            .joinToString { "${it.function().name()}(${it.function().arguments()})" }
            .takeIf { it.isNotEmpty() }
            ?.also { reactions.say(">> CALLING: $it") }

        // Show intermediate or final LLM response
        reactions.streamOrSay()
    }
}

fun main() {
    ConsoleChannel(mainAgent.asBot)
        .run("Calculate some single random math expression")
}