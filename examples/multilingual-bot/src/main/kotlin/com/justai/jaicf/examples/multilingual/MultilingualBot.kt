package com.justai.jaicf.examples.multilingual

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.caila.CailaNLUSettings
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.examples.multilingual.scenarios.EnScenario
import com.justai.jaicf.examples.multilingual.scenarios.RuScenario
import java.util.*

val RuEngine = BotEngine(
    scenario = RuScenario,
    activators = arrayOf(RegexActivator,
        CailaIntentActivator.Factory(CailaNLUSettings("b7cd6f32-ed6d-4eed-ac0c-95711dadb4bd")))
)

val EnEngine = BotEngine(
    scenario = EnScenario,
    activators = arrayOf(RegexActivator,
        CailaIntentActivator.Factory(CailaNLUSettings("8a4dc3a1-a19c-47db-997e-01a9c14d1811")))
)

val mainAccessToken: String = System.getenv("JAICP_API_TOKEN") ?: Properties().run {
    load(CailaNLUSettings::class.java.getResourceAsStream("/jaicp.properties"))
    getProperty("apiToken")
}

val MainBotEngine = BotEngine(MainScenario, activators = arrayOf(RegexActivator))

val MultilingualBotEngine = BotRoutingEngine(
    main = MainBotEngine,
    routables = mapOf("en" to EnEngine, "ru" to RuEngine)
)
