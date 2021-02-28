package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.model.scenario.Scenario

object ScenarioFactory {
    fun echo() = Scenario {
        fallback { reactions.say("You said: ${request.input}") }
    }

    fun echoWithAction(block: ActionContext<*, *, *>.() -> Unit) = Scenario {
        fallback {
            block()
        }
    }
}