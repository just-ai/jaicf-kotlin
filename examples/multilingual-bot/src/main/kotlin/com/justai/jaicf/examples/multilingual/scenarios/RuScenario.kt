package com.justai.jaicf.examples.multilingual.scenarios

import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.multilingual.service.BitcoinExchangeService
import com.justai.jaicf.hook.AnyErrorHook
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
            routing.routeToMain(targetState = "/Main")
        }
    }

    fallback {
        reactions.say("Извините, не понятно. Вы сказали: ${request.input}")
        reactions.buttons("Курс биткоина", "Выбор языка")
    }
}




