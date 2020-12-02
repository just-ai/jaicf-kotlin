package com.justai.jaicf.examples.caila

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.caila.CailaNLUSettings

val cailaBot = BotEngine(
    model = CailaScenario.model,
    activators = arrayOf(
        CailaIntentActivator.Factory(CailaNLUSettings("e7e8a23a-1354-47f7-93d4-491c4afb4e55"))
    )
)