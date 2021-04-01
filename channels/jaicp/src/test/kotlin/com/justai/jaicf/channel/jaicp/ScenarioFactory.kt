package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.DefaultActionContext

object ScenarioFactory {
    fun echo() = Scenario {
        fallback { reactions.say("You said: ${request.input}") }
    }

    fun echoWithAction(block: DefaultActionContext.() -> Unit) = Scenario {
        fallback {
            block()
        }
    }
}