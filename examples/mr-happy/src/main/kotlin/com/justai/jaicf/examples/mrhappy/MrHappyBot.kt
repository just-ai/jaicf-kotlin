package com.justai.jaicf.examples.mrhappy

import com.justai.jaicf.BotEngine
import com.justai.jaicf.examples.mrhappy.activator.MrHappyActivator

val mrHappyBot = BotEngine(
    scenario = MrHappyScenario,
    activators = arrayOf(MrHappyActivator)
)
