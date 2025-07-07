package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.Calculator
import com.openai.core.JsonValue

/**
 * This example shows how tools can be defined right in agent props.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val agent = LLMAgent("agent", {
    model = "gpt-4.1-nano"

    // Define tool inline
    tool<Calculator> {
        Math.random()  // Returns random number
    }

    // Define the same tool with custom name and description.
    // This will create an additional tool named "complex_calc".
    tool<Calculator>(
        name = "complex_calc",
        description = "Call this tool for complex calculations"
    ) {
        Math.random()
    }
        .withConfirmation()  // Requires confirmation before calling

    // Define tool with custom parameters schema.
    // You can use JsonValue if you don't want to write a tool class for such a simple tool.
    tool<JsonValue>(
        name = "send_message",
        description = "Sends message without recipient and subject",
        parameters = {
            str("message", required = true)
        }
    ) {
        "Message sent: ${it.arguments.asObject().get()["message"]?.asStringOrThrow()}"
    }
        .withConfirmation("Confirm to send MESSAGE_TEXT")  // Requires confirmation before calling
})

fun main() {
    ConsoleChannel(agent.asBot).run("write and send random message")
}