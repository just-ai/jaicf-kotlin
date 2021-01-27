package com.justai.jaicf.test

import com.justai.jaicf.activator.ActivationRuleMatcher
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivationRule
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class TestIntentActivator(model: ScenarioModel) : BaseIntentActivator(model) {
    override val name = "testIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasIntent()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher {
        val intents = recogniseIntent(botContext, request)
        return ruleMatcher<IntentActivationRule> { rule ->
            intents.sortedByDescending { it.confidence }.firstOrNull { rule.intentMatches(it.intent) }
        }
    }

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext> {
        val activatorContext = botContext.client[ACTIVATOR_VALUE_KEY] as? IntentActivatorContext ?: return emptyList()
        return listOf(activatorContext)
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = TestIntentActivator(model)

        const val ACTIVATOR_VALUE_KEY = "com/justai/jaicf/test/testActivatorContext"
    }
}