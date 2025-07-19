package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.agent.withGoal
import com.justai.jaicf.activator.llm.tool.interrupt
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import java.util.Optional

/**
 * This example shows how to add some specific goal to any LLM agent.
 * In this case, an agent will try to achieve the goal and call a callback function once it's achieved.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */

/**
 * Goal data description.
 * Agent fills this structure achieving the goal.
 */
private data class Goal(
    val userFirstName: String,
    val userLastName: String,
    val userAge: Optional<Int>,
)

/**
 * Use `withGoal()` extension to add goal to the agent.
 * IT just adds a tool and some special system message that instructs an agent to call this tool once the goal is achieved.
 */
private val agent = LLMAgent(
    name = "agent",
    model = "gpt-4.1-mini",
).withGoal<Goal>("Talk with user to find out their firstname, lastname and age") {
    // This callback is just a tool function that accepts a goal structure described above.
    // You can interrupt tool calling or return some instructions to the agent if the goal was not achieved.

    if (call.arguments.userFirstName.isBlank()) {
        "ERROR: goal is not achieved. Ask for user firstname"  // Instruct agent to ask firstname again if empty
    } else if (call.arguments.userLastName.isBlank()) {
        "ERROR: goal is not achieved. Ask for user lastname"  // Instruct agent to ask lastname again if empty
    } else {
        // Interrupt tool calling loop and execute custom scenario action
        interrupt {
            reactions.say("GOAL IS ACHIEVED: ${call.arguments}")
        }
    }
}

fun main() {
    ConsoleChannel(agent.asBot).run("hello")
}