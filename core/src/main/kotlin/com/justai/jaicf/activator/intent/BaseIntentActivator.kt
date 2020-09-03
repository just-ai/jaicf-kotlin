package com.justai.jaicf.activator.intent

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.StateMapActivator
import com.justai.jaicf.activator.event.EventActivationRule
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * A base implementation of [IntentActivator].
 * This activator handles intent requests and activates a state if it contains an intent with name that equals to the request's input.
 *
 * @param model dialogue scenario model
 *
 * @see StateMapActivator
 */
open class BaseIntentActivator(model: ScenarioModel): StateMapActivator(model), IntentActivator {
    override val name = "baseIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasIntent()

    override fun canHandleRule(rule: ActivationRule) = rule is IntentActivationRule

    override fun activate(botContext: BotContext, request: BotRequest): Activation? {
        val context = recogniseIntent(botContext, request)
        return context?.let {
            val state = findState(botContext) { rule -> (rule as? IntentActivationRule)?.intent == it.intent  }
            Activation(state, it)
        }
    }

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        return IntentActivatorContext(1f, request.input)
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return BaseIntentActivator(model)
        }
    }
}