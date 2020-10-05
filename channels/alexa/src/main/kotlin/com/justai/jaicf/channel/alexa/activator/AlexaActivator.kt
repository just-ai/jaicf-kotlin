package com.justai.jaicf.channel.alexa.activator

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.event.EventActivator
import com.justai.jaicf.activator.intent.IntentActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.SlotFillingResult
import com.justai.jaicf.slotfilling.SlotFillingSkipped
import com.justai.jaicf.slotfilling.SlotReactor

class AlexaActivator private constructor(
    private val intentActivator: AlexaIntentActivator,
    private val eventActivator: AlexaEventActivator
) : IntentActivator by intentActivator, EventActivator by eventActivator {

    override val name = "alexaActivator"

    override fun canHandle(request: BotRequest) =
        intentActivator.canHandle(request) || eventActivator.canHandle(request)

    override fun activate(botContext: BotContext, request: BotRequest, selector: ActivationSelector): Activation? {
        return intentActivator.activate(botContext, request, selector)
            ?: eventActivator.activate(botContext, request, selector)
    }

    override fun fillSlots(
        request: BotRequest,
        reactions: Reactions,
        botContext: BotContext,
        activatorContext: ActivatorContext?,
        slotReactor: SlotReactor?
    ): SlotFillingResult = SlotFillingSkipped

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return AlexaActivator(
                AlexaIntentActivator(model),
                AlexaEventActivator(model)
            )
        }
    }
}