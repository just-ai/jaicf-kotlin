package com.justai.jaicf.channel.alexa.activator

import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.alexa.AlexaIntentRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

internal class AlexaIntentActivator(model: ScenarioModel): BaseIntentActivator(model) {

    override val name = "alexaIntentActivator"

    override fun canHandle(request: BotRequest) = request is AlexaIntentRequest

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        val alexaRequest = request as? AlexaIntentRequest ?: return null
        return AlexaIntentActivatorContext(alexaRequest.intentRequest)
    }
}