package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.activator.regex.RegexActivator

val cailaIntentActivator = CailaIntentActivator.Factory(nluSettings)

val citiesGameBot = BotEngine(
    TelephonyBotModule().model,
    activators = arrayOf(
        BaseEventActivator,
        RegexActivator,
        cailaIntentActivator,
        CatchAllActivator
    )
)
