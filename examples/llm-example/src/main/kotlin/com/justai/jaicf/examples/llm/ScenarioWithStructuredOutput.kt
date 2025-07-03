package com.justai.jaicf.examples.llm

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.LLMActivator
import com.justai.jaicf.activator.llm.awaitStructuredContent
import com.justai.jaicf.activator.llm.scenario.llmChat
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.CalcTool
import java.util.Optional

@JsonClassDescription("Output with response and optional tool names called during request")
private data class Output(
    val response: String,
    val toolNames: Optional<List<String>>?,
)

/**
 * Simple scenario that handles every user request and responds with structured output instead of a text message.
 */
private val scenario = Scenario {
    llmChat("chat", {
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
    val bot = BotEngine(
        scenario = scenario,
        activators = arrayOf(LLMActivator)
    )
    ConsoleChannel(bot).run()
}