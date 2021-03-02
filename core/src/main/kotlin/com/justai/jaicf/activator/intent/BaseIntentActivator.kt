package com.justai.jaicf.activator.intent

import com.justai.jaicf.activator.ActivationRuleMatcher
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * A base implementation of [IntentActivator].
 * This activator handles intent requests and activates a state if its activation rule matches intent name from the request's input.
 *
 * @param model dialogue scenario model
 *
 * @see BaseActivator
 */
open class BaseIntentActivator(model: ScenarioModel) : BaseActivator(model), IntentActivator {
    override val name = "baseIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasIntent()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher {
        val intents = recogniseIntent(botContext, request)
        return ruleMatcher<IntentActivationRule> { rule ->
            intents.sortedByDescending { it.confidence }.firstOrNull { rule.matches(it) }
        }
    }

    override fun recogniseIntent(botContext: BotContext, request: BotRequest) =
        listOf(IntentActivatorContext(1f, request.input))

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = BaseIntentActivator(model)
    }
}