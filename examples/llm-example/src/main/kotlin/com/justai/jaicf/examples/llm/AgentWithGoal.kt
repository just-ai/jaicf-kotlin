package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.agent.withGoal
import com.justai.jaicf.activator.llm.tool.interrupt
import com.justai.jaicf.channel.ConsoleChannel
import java.util.Optional

private data class Goal(
    val userFirstName: String,
    val userLastName: String,
    val userAge: Optional<Int>,
)

private val agent = LLMAgent(
    name = "agent",
    model = "gpt-4.1-mini",
).withGoal<Goal>("Talk with user to find out their firstname, lastname and age") {
    interrupt {
        reactions.say(call.arguments.toString())
    }
}

fun main() {
    ConsoleChannel(agent.asBot).run("hello")
}