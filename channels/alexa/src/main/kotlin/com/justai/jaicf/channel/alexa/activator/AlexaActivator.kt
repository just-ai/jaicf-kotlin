package com.justai.jaicf.channel.alexa.activator

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.event.EventActivator
import com.justai.jaicf.activator.intent.IntentActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.ScenarioModel

class AlexaActivator private constructor(
    private val intentActivator: AlexaIntentActivator,
    private val eventActivator: AlexaEventActivator
): IntentActivator by intentActivator, EventActivator by eventActivator {

    override fun canHandle(request: BotRequest)
            = intentActivator.canHandle(request) || eventActivator.canHandle(request)

    override fun activate(botContext: BotContext, request: BotRequest): Activation? {
        return intentActivator.activate(botContext, request) ?: eventActivator.activate(botContext, request)
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return AlexaActivator(
                AlexaIntentActivator(model),
                AlexaEventActivator(model)
            )
        }
    }
}