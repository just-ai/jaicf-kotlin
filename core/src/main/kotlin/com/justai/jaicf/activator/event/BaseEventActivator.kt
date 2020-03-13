package com.justai.jaicf.activator.event

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.StateMapActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasEvent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRuleType
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * A base implementation of [EventActivator].
 * Handles event requests and activates a state if it contains an event with name that equals to the request's input.
 *
 * @param model dialogue scenario model
 * @see StateMapActivator
 */
open class BaseEventActivator(model: ScenarioModel) : StateMapActivator(
    ActivationRuleType.event, model
), EventActivator {

    override fun canHandle(request: BotRequest) = request.hasEvent()

    override fun activate(
        botContext: BotContext,
        request: BotRequest
    ): Activation? {
        val state = findState(request.input, botContext)
        return Activation(state, EventActivatorContext(request.input))
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return BaseEventActivator(model)
        }
    }

}