package com.justai.jaicf.examples.multilingual.bots

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.caila.CailaNLUSettings
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.activator.regex.regex
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.manager.InMemoryBotContextManager

val RuScenario = Scenario {
    state(RuBot.TargetState) {
        action {
            reactions.say("Приветствую!")
        }
    }

    state("return") {
        activators {
            regex("return")
        }
        action {
            reactions.say("Возвращаюсь!")
            routing.routeBack()
        }
    }

    fallback {
        reactions.say("Извините, не понятно. Вы сказали: ${request.input}")
    }
}

object RuBot {
    const val EngineName = "ru"
    const val TargetState = "Welcome"
    const val accessToken = "b7cd6f32-ed6d-4eed-ac0c-95711dadb4bd"
    val Engine = BotEngine(
        scenario = RuScenario,
        defaultContextManager = InMemoryBotContextManager,
        activators = arrayOf(CailaIntentActivator.Factory(CailaNLUSettings(accessToken)), RegexActivator)
    )
}


