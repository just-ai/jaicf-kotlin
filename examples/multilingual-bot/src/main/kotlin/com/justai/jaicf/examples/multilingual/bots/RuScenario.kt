package com.justai.jaicf.examples.multilingual.bots

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.caila.CailaNLUSettings
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.examples.multilingual.service.BitcoinExchangeService
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.reactions.buttons
import com.justai.jaicf.test.context.isTestMode

val RuScenario = Scenario {
    handle<AnyErrorHook> {
        logger.error("", exception)
        reactions.say("Извините, технические неполадки! Попробуйте повторить Ваш запрос.")
    }

    state("Welcome") {
        action {
            reactions.say("Приветствую! Я могу помочь получить курс Биткоина к Доллару США.")
            reactions.buttons("Курс биткоина", "Выбор языка")
        }
    }

    state("usdExchangeRate") {
        activators {
            regex("Курс биткоина")
            intent("bitcoinExchangeRate")
        }

        action {
            val rate = BitcoinExchangeService.getBitcoinToUSD(testMode = isTestMode())
            reactions.say("Сейчас за 1 Bitcoin можно получить $rate USD")
            reactions.buttons("Выбор языка")
        }
    }

    state("selectLang") {
        activators {
            regex("Выбор языка")
        }
        action {
            routing.route("main", "/Main")
        }
    }

    fallback {
        reactions.say("Извините, не понятно. Вы сказали: ${request.input}")
        reactions.buttons("Курс биткоина", "Выбор языка")
    }
}

object RuBot {
    const val EngineName = "ru"
    const val accessToken = "b7cd6f32-ed6d-4eed-ac0c-95711dadb4bd"
    val Engine = BotEngine(
        scenario = RuScenario,
        defaultContextManager = InMemoryBotContextManager,
        activators = arrayOf(RegexActivator, CailaIntentActivator.Factory(CailaNLUSettings(accessToken)))
    )
}


