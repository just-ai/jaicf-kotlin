package com.justai.jaicf.examples.multilingual.bots

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.caila.CailaNLUSettings
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.helpers.logging.WithLogger

val EnScenario = Scenario {

    state(EnBot.TargetState) {
        action {
            reactions.say("Hello there!")
        }
    }
    state("return") {
        activators {
            regex("return")
        }
        action {
            reactions.say("Returning the route!")
            routing.routeBack()
        }
    }

    state("setEn") {
        activators {
            regex(".setEn")
        }

        action {
            routing.route(EnBot.EngineName, EnBot.TargetState)
        }
    }

    fallback {
        reactions.say("I can't get it. You said: ${request.input}")
    }
}

object EnBot {
    const val EngineName = "en"
    const val TargetState = "Welcome"
    const val accessToken = "8a4dc3a1-a19c-47db-997e-01a9c14d1811"
    val Engine = BotEngine(
        scenario = EnScenario,
        defaultContextManager = InMemoryBotContextManager,
        activators = arrayOf(CailaIntentActivator.Factory(CailaNLUSettings(accessToken)))
    )
}
