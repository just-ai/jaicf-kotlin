package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.content
import com.justai.jaicf.activator.llm.scenario.LLMChatScenario
import com.justai.jaicf.activator.llm.toolCalls
import com.justai.jaicf.activator.llm.withToolCalls
import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.CalcTool


/**
 * Simple LLM chatbot with calculator tool and custom action block.
 * Responds with intermediate messages in between tool calls.
 * Static properties are used in scenario constructor.
 */
private val scenario = LLMChatScenario(
    name = "chat",
    model = "gpt-4.1-nano",
    instructions = "You're a helpful assistant",
    tools = listOf(CalcTool),
) {
    activator.withToolCalls { results ->
        // show tool calls results if any
        results.takeIf { it.isNotEmpty() }?.run {
            println(joinToString(prefix = "RESULTS: ", postfix = "\n") { "${it.result}" })
        }

        // show assistant response if any
        content?.also {
            println("< $it")
        }

        // show tool calls if any
        toolCalls.takeIf { it.isNotEmpty() }?.run {
            println(joinToString(prefix = "CALL: ") { "${it.function().name()}(${it.function().arguments()})" })
        }
    }
}

fun main() {
    ConsoleChannel(scenario.asBot)
        .run("Calculate any random math expression many times")
}