package com.justai.jaicf.examples.multilingual

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.examples.multilingual.bots.EnBot
import com.justai.jaicf.examples.multilingual.bots.RuBot
import com.justai.jaicf.examples.multilingual.service.LanguageDetectService
import com.justai.jaicf.examples.multilingual.service.SupportedLanguage
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.reactions.buttons

val MainScenario = Scenario {
    val defaultTargetState = "/Welcome"

    handle<AnyErrorHook> {
        logger.error("", exception)
        reactions.say("Sorry, I can't handle these technical difficulties, but you can try repeat your question!")
    }

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
            routing.route(RuBot.EngineName, targetState = defaultTargetState)
        }
    }

    state("En") {
        action {
            routing.route(EnBot.EngineName, targetState = defaultTargetState)
        }
    }

    fallback {
        when (val lang = LanguageDetectService.detectLanguage(request.input)) {
            null -> {
                reactions.say("Sorry, I can't get it! Please, select your language")
                reactions.buttons("Русский" to "/Ru", "English" to "/En")
            }
            else -> routing.route(lang.name, defaultTargetState)
        }
    }
}

object MainBot {
    const val accessToken = "3072bcc3-c053-4986-b69e-8aa47884c8a3"
    val engine = BotEngine(MainScenario, InMemoryBotContextManager, activators = arrayOf(RegexActivator))
}