package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.ActivationRuleMatcher
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.activator.event.EventActivator
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class LLMActivator(
    model: ScenarioModel,
    private val api: LLMActivatorAPI,
) : BaseActivator(model), EventActivator {
    override val name = "llmActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher {
        return ruleMatcher<LLMActivationRule> { rule ->
            rule.takeIf { it.event == LLMEvent.RESPONSE }?.let {
                api.createActivatorContext(rule.props, botContext, request)
            }
        }
    }

    class Factory(private val api: LLMActivatorAPI) : ActivatorFactory {
        override fun create(model: ScenarioModel) =
            LLMActivator(model, api)
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = LLMActivator(model, LLMActivatorAPI())
    }
}