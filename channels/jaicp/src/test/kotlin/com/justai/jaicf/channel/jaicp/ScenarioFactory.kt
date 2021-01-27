package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.builder.startScenario
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.model.scenario.Scenario

object ScenarioFactory {
    fun echo() = startScenario {
        fallback { reactions.say("You said: ${request.input}") }
    }

    fun echoWithAction(block: ActionContext<*, *, *>.() -> Unit) = startScenario {
        fallback {
            block()
        }
    }
}