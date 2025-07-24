package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.props.llmProps

/**
 * LLM scenario with memory that re-creates system prompt with actual date-time.
 * Creates a single state "chat" that handles all user text requests and streaming the LLM output with tool calls statuses and results.
 * "start" message is handled by regex instead of LLM.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    state("start") {
        activators {
            regex("start")
        }
        action {
            reactions.say("TYPE ME ANYTHING")
        }

        llmState("chat", llmProps) {
            llm.withToolCalls { results ->
                // Printing tool call results
                results.forEach {
                    reactions.say("\t${it.name}: ${it.result}")
                }

                // Streaming content chunks
                reactions.streamOrSay()

                // Printing tool calls statuses
                if (llm.hasToolCalls()) {
                    reactions.say("CALLING: ${llm.toolCalls().joinToString { "${it.function().name()}(${it.function().arguments()})" }}")
                }
            }
        }
    }
}

fun main() {
    val bot = BotEngine(
        scenario = scenario,
        activators = arrayOf(RegexActivator)
    )
    ConsoleChannel(bot).run("start")
}