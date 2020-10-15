package com.justai.jaicf.examples.gameclock

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.channel.alexa.activator.AlexaActivator
import com.justai.jaicf.channel.googleactions.dialogflow.ActionsDialogflowActivator
import com.justai.jaicf.examples.gameclock.scenario.MainScenario

val gameClockBot = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        AlexaActivator,
        ActionsDialogflowActivator
    )
)