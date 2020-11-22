package com.justai.jaicf.activator.strict

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.transition.Transition

class ButtonActivator(model: ScenarioModel) : BaseActivator(model) {

    override val name = "buttonActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest) =
        ruleMatcher<StrictActivationRule> {
            val req = request.input.toLowerCase()
            val strictTransitions = botContext.dialogContext.transitions
            strictTransitions[req]
                ?.let { StrictActivatorContext() }
                .also { strictTransitions.clear() }
        }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = ButtonActivator(model)
    }

    override fun generateTransitions(botContext: BotContext) = botContext.dialogContext.transitions.map {
        Transition(botContext.dialogContext.currentContext, it.value, StrictActivationRule())
    }
}