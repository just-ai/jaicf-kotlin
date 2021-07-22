package com.justai.jaicf.api.routing.activators

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.api.routing.routingContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.ScenarioModel

internal class TargetStateActivator : Activator, WithLogger {

    override val name = "targetStateActivator"

    override fun canHandle(request: BotRequest): Boolean = request.hasQuery()

    override fun activate(botContext: BotContext, request: BotRequest, selector: ActivationSelector): Activation? =
        botContext.routingContext.targetState?.let {
            botContext.routingContext.targetState = null
            Activation(it, StrictActivatorContext())
        }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = TargetStateActivator()
    }
}