package com.justai.jaicf.examples.multilingual.bots

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.reactions.buttons

val MainScenario = Scenario {

    state("Main") {
        activators {
            regex(".start")
        }
        action {
            reactions.say("Hello! Please, select your language")
            reactions.buttons("Русский" to "/Ru", "English" to "/En")
        }
    }

    state("Ru") {
        action {
            reactions.say("Okay there!")
            routing.route(RuBot.EngineName, targetState = RuBot.TargetState)
        }
    }

    state("En") {
        action {
            routing.route(EnBot.EngineName, targetState = EnBot.TargetState)
        }
    }

    fallback {
        reactions.say("[MAIN] - You said: ${request.input}")
    }
}

object MainBot {
    const val accessToken = "3072bcc3-c053-4986-b69e-8aa47884c8a3"
    val engine = BotEngine(MainScenario, InMemoryBotContextManager, activators = arrayOf(RegexActivator))
}