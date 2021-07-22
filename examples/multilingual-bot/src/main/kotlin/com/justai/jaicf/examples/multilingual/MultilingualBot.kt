package com.justai.jaicf.examples.multilingual

import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.examples.multilingual.bots.EnBot
import com.justai.jaicf.examples.multilingual.bots.MainBot
import com.justai.jaicf.examples.multilingual.bots.RuBot


val RoutingEngine = BotRoutingEngine(
    main = MainBot.engine,
    routables = mapOf(RuBot.EngineName to RuBot.Engine, EnBot.EngineName to EnBot.Engine)
) {
    when (request.input) {
        "/setRu" -> "ru"
        "/setEn" -> "en"
        "/reset" -> "main"
        else -> "main"
    }
}