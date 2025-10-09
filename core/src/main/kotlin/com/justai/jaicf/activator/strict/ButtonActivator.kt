package com.justai.jaicf.activator.strict

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.ScenarioModel
import java.util.Locale.getDefault

internal class ButtonActivator : Activator {

    override val name = "buttonActivator"

    override fun canHandle(request: BotRequest): Boolean = request.hasQuery()

    override fun activate(botContext: BotContext, request: BotRequest, selector: ActivationSelector): Activation? {
        val req = request.input.lowercase(getDefault())
        val strictTransitions = botContext.dialogContext.transitions
        val context = strictTransitions[req].also { strictTransitions.clear() }
        return context?.let { Activation(it, StrictActivatorContext()) }
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = ButtonActivator()
    }
}