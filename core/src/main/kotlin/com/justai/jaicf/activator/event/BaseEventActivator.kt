package com.justai.jaicf.activator.event

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasEvent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * A base implementation of [EventActivator].
 * Handles event requests and activates a state if it contains an event with name that equals to the request's input.
 *
 * @param model dialogue scenario model
 * @see BaseActivator
 */
open class BaseEventActivator(model: ScenarioModel) : BaseActivator(model), EventActivator {

    override val name = "baseEventActivator"

    override fun canHandle(request: BotRequest) = request.hasEvent()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest) = ruleMatcher<EventActivationRule> {
        if (it.eventMatches(request.input)) EventActivatorContext(request.input) else null
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = BaseEventActivator(model)
    }
}