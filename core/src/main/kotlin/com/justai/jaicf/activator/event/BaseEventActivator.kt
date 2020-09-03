package com.justai.jaicf.activator.event

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.StateMapActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasEvent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * A base implementation of [EventActivator].
 * Handles event requests and activates a state if it contains an event with name that equals to the request's input.
 *
 * @param model dialogue scenario model
 * @see StateMapActivator
 */
open class BaseEventActivator(model: ScenarioModel) : StateMapActivator(model), EventActivator {
    override val name = "baseEventActivator"

    override fun canHandle(request: BotRequest) = request.hasEvent()

    override fun canHandleRule(rule: ActivationRule) = rule is EventActivationRule

    override fun activate(
        botContext: BotContext,
        request: BotRequest
    ): Activation? {
        val state = findState(botContext) { (it as? EventActivationRule)?.event == request.input }
        return Activation(state, EventActivatorContext(request.input))
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return BaseEventActivator(model)
        }
    }

}