package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.LLMEvent
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.CalcTool

/**
 * This example shows how `eventStream()` can be used to get stream of LLM events.
 * Each event represents a step in a tool calling loop providing a way to compose intermediate responses.
 * Once it is a standard [java.util.stream.Stream], filters, mappings and other utilities can be used.
 *
 * This is event-styled shorthand of `activator.withToolCalls {}` variant.
 *
 * @see [LLMEvent]
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val agent = LLMAgent(
    name = "agent",
    model = "gpt-4.1-nano",
    tools = listOf(CalcTool),
) {
    llm.eventStream().forEach { event ->
        when (event) {
            is LLMEvent.Start -> println(">> STEP STARTED")
            is LLMEvent.ContentDelta -> print(event.delta)
            is LLMEvent.Content -> println()
            is LLMEvent.ToolCalls -> println(">> CALLING: ${event.calls.joinToString { "${it.function().name()}(${it.function().arguments()})" }}")
            is LLMEvent.ToolCallResults -> println(">> RESULTS: ${event.results.joinToString("\n\t", "\n\t") { "${it.name} = ${it.result}" }}")
            is LLMEvent.Finish -> println(">> STEP FINISHED: ${event.reason}${event.usage?.let { ", ${it.totalTokens()} tokens used" } ?: ""}")
            else -> Unit
        }
    }
    reactions.say(">> COMPLETE")
}

fun main() {
    ConsoleChannel(agent.asBot).run()
}