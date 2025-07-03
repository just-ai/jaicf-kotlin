package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.LLMActivator
import com.justai.jaicf.activator.llm.awaitFinalContent
import com.justai.jaicf.activator.llm.llm
import com.justai.jaicf.activator.llm.llmActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.props.llmProps

/**
 * Simple LLM scenario with memory that re-creates system prompt with actual date-time.
 * Creates a single state "chat" that handles all user text requests and responds with a final message from LLM.
 * "start" message is handled by regex instead of LLM for the very first time. Then it switches to the modal "chat" state that handles all user requests with LLM.
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
            reactions.changeState("chat")
        }

        state("chat", modal = true) {
            activators {
                llmActivator(llmProps)
            }

            action(llm) {
                activator.awaitFinalContent()?.also(reactions::say)
            }
        }
    }
}

fun main() {
    val bot = BotEngine(
        scenario = scenario,
        activators = arrayOf(RegexActivator, LLMActivator)
    )
    ConsoleChannel(bot).run("start")
}