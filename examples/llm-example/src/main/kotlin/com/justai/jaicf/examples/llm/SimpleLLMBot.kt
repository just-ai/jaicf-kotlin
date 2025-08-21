package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.OpenAIClientBuilder
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.openai.client.okhttp.OpenAIOkHttpClient

/**
 * Simple LLM chatbot with default reactions.
 * Responds with a single message on each uer request.
 * Static properties are used in scenario constructor.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
val SimpleLLMAgent = LLMAgent(
    name = "agent",
    model = "gpt-4.1-nano",
    instructions = "You're a helpful assistant",
    client = OpenAIClientBuilder(
        openAIClient = OpenAIOkHttpClient.fromEnv(),
        props = LLMProps(
            withUsages = true,
        ),
        interceptors = mutableListOf()
    ).build()
)

fun main() {
    ConsoleChannel(SimpleLLMAgent.asBot).run()
}