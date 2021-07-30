package com.justai.jaicf.examples.multilingual

import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.examples.multilingual.bots.EnEngine
import com.justai.jaicf.examples.multilingual.bots.RuEngine

val MultilingualBotEngine = BotRoutingEngine(
    main = MainBotEngine,
    routables = mapOf("ru" to RuEngine, "en" to EnEngine)
)
