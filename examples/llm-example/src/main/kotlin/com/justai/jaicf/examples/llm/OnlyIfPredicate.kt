package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel

/**
 * With `onlyIf` prop agent can skip request if this predicate returns false.
 * In this example, an agent responds to each odd request only.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    append(LLMAgent(
        name = "agent",
        model = "gpt-4.1-nano",
        onlyIf = {
            var count = context.session["count"] as? Int ?: 0
            context.session["count"] = ++count
            count % 2 != 0
        }
    ))

    state("main", noContext = true) {
        activators {
            catchAll()
        }

        action {
            reactions.say("I respond to each odd request only")
        }
    }
}

fun main() {
    ConsoleChannel(BotEngine(scenario))
        .run("Hello world")
}