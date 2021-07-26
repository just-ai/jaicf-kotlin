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
import com.justai.jaicf.test.context.isTestMode

val EnScenario = Scenario {
    handle<AnyErrorHook> {
        logger.error("", exception)
        reactions.say("Sorry, I can't handle these technical difficulties, but you can try repeat your question!")
    }

    state("Welcome") {
        action {
            reactions.say("Hello there! I can help get current Bitcoin to USD exchange rate.")
            reactions.buttons("Bitcoin to USD", "Select language")
        }
    }

    state("usdExchangeRate") {
        activators {
            regex("Bitcoin to USD")
            intent("bitcoinExchangeRate")
        }

        action {
            val rate = BitcoinExchangeService.getBitcoinToUSD(testMode = isTestMode())
            reactions.say("You can get $rate USD for 1 Bitcoin.")
            reactions.buttons("Select language")
        }
    }

    state("selectLang") {
        activators {
            regex("Select language")
        }
        action {
            routing.route("main", "/Main")
        }
    }

    fallback {
        reactions.say("I can't get it. You said: ${request.input}")
        reactions.buttons("Bitcoin to USD", "Select language")
    }
}

object EnBot {
    const val EngineName = "en"
    const val accessToken = "8a4dc3a1-a19c-47db-997e-01a9c14d1811"
    val Engine = BotEngine(
        scenario = EnScenario,
        defaultContextManager = InMemoryBotContextManager,
        activators = arrayOf(RegexActivator, CailaIntentActivator.Factory(CailaNLUSettings(accessToken)))
    )
}
