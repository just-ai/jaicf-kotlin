package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.LLMInputs
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.examples.llm.channel.ConsoleChannel

/**
 * Every LLM agent handles texts requests.
 * This can be customized with "input" prop.
 * The input lambda can return `null` instead of the message list to skip LLM processing for the current request.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val agent = LLMAgent(
    name = "agent",
    props = {
        model = "gpt-4.1-mini"
        // You can convert BotRequest to LLM messages via input prop
        input = LLMInputs.WithImages  // Handle image URLs as an input without mandatory text
    }
)

fun main() {
    ConsoleChannel(agent.asBot)
        .run("Solve the task from this image https://wl-adme.cf.tsp.li/resize/728x/jpg/81d/450/40346d5608bf4acbaa47d8bbdb.jpg")
}