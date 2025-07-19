package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.activator.llm.streamOrSay
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.props.llmProps

/**
 * Simple LLM scenario with memory that re-creates system prompt with actual date-time.
 * Creates a state "chat" that handles all user text requests and responds with each intermediate assistant message in between tool calls.
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
            // Custom action block
            activator.withToolCalls {
                reactions.streamOrSay(activator)
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