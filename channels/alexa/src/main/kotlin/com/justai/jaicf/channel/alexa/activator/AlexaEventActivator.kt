package com.justai.jaicf.channel.alexa.activator

import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.alexa.AlexaEventRequest
import com.justai.jaicf.model.scenario.ScenarioModel

internal class AlexaEventActivator(model: ScenarioModel): BaseEventActivator(model) {

    override fun canHandle(request: BotRequest) = request is AlexaEventRequest
}