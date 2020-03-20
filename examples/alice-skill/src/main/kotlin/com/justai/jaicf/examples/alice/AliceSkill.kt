package com.justai.jaicf.examples.alice

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.activator.regex.RegexActivator

val skill = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        RegexActivator,
        BaseEventActivator,
        CatchAllActivator
    )
)