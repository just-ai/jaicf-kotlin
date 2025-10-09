package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.CalcTool


/**
 * Simple LLM chatbot with calculator tool and custom action block.
 * Responds with intermediate messages in between tool calls.
 * Static properties are used in scenario constructor.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
val AgentWithTools = LLMAgent(
    name = "chat",
    model = "gpt-4.1-nano",
    instructions = "You're a helpful assistant",
    tools = listOf(CalcTool),
) {
    llm.withToolCalls { results ->
        // show tool calls results if any
        results.takeIf { it.isNotEmpty() }?.run {
            reactions.say(joinToString(prefix = ">> RESULTS: ", postfix = "\n") { "${it.result}" })
        }

        // show assistant response if any
        reactions.streamOrSay()

        // show tool calls if any
        toolCalls().takeIf { it.isNotEmpty() }?.run {
            reactions.say(joinToString(prefix = ">> CALLING: ") { "${it.function().name()}(${it.function().arguments()})" })
        }
    }
}

fun main() {
    ConsoleChannel(AgentWithTools.asBot)
        .run("Calculate any random math expression many times")
}