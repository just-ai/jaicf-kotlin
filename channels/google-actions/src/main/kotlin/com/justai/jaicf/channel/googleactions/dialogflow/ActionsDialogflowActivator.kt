package com.justai.jaicf.channel.googleactions.dialogflow

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.googleactions.ActionsIntentRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class ActionsDialogflowActivator(model: ScenarioModel): BaseIntentActivator(model) {

    override val name = "actionsDialogflowActivator"

    override fun canHandle(request: BotRequest) = request is ActionsIntentRequest

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        val actionRequest = request as? ActionsIntentRequest
            ?: return null
        return ActionsDialogflowActivatorContext(
            actionRequest.request
        )
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) =
            ActionsDialogflowActivator(model)
    }
}