package com.justai.jaicf.activator.intent

import com.justai.jaicf.activator.ActivationRuleMatcher
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.StateMapActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * A base implementation of [IntentActivator].
 * This activator handles intent requests and activates a state if it contains an intent with name that equals to the request's input.
 *
 * @param model dialogue scenario model
 *
 * @see StateMapActivator
 */
open class BaseIntentActivator(
    model: ScenarioModel
) : StateMapActivator(model), IntentActivator {
    override val name = "baseIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasIntent()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher {
        val intents = recogniseIntent(botContext, request)
        return ruleMatcher<IntentActivationRule> { rule -> intents.firstOrNull { it.intent == rule.intent } }
    }

    override fun recogniseIntent(botContext: BotContext, request: BotRequest) =
        listOf(IntentActivatorContext(1f, request.input))

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = BaseIntentActivator(model)
    }
}