package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.LLMInputs
import com.justai.jaicf.activator.llm.LLMMessage
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel

/**
 * This scenario shows how a static messages list can be used instead of persistent memory.
 * `messages` prop can provide a list of messages that will be propagated to LLM context for each request.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    llmState("chat", {
        model = "gpt-4.1-nano"
        input = LLMInputs.WithImages
        messages = listOf(LLMMessage.system("Describe each received image"))
    })
}

fun main() {
    ConsoleChannel(BotEngine(scenario))
        .run("https://wl-adme.cf.tsp.li/resize/728x/jpg/81d/450/40346d5608bf4acbaa47d8bbdb.jpg")
}