package com.justai.jaicf.examples.llm

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.CalcTool
import java.util.*

@JsonClassDescription("Output with response and optional tool names called during request")
private data class Output(
    val response: String,
    val toolNames: Optional<List<String>>?,
)

/**
 * Simple scenario that handles every user request and responds with structured output instead of a text message.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    llmState("chat", {
        model = "gpt-4.1-mini"
        responseFormat = Output::class.java
        tool(CalcTool)
    }) {
        val output = activator.awaitStructuredContent<Output>()
        output?.also {
            reactions.say(output.response)
            output.toolNames?.ifPresent { reactions.say(it.joinToString(prefix = "\tTOOLS: ")) }
        }
    }
}

fun main() {
    ConsoleChannel(BotEngine(scenario)).run()
}