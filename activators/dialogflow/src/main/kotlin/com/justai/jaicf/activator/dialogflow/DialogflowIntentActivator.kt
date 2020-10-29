package com.justai.jaicf.activator.dialogflow

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasEvent
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.*

class DialogflowIntentActivator(
    model: ScenarioModel,
    private val connector: DialogflowConnector,
    private val queryParametersProvider: QueryParametersProvider = QueryParametersProvider.default
): BaseIntentActivator(model) {

    override val name = "dialogflowIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery() || request.hasEvent()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<DialogflowActivatorContext> {
        val params = queryParametersProvider.provideParameters(botContext, request)

        val qr = when {
            request.hasQuery() -> connector.detectIntentByQuery(request, params)
            else -> connector.detectIntentByEvent(request, params)
        }

        val intent = when {
            qr.intent.displayName.isNotEmpty() -> qr.intent.displayName
            qr.action.startsWith(DialogflowIntent.SMALLTALK) -> DialogflowIntent.SMALLTALK
            else -> null
        }

        return intent?.let { listOf(DialogflowActivatorContext(intent, qr)) } ?: emptyList()
    }

    override fun fillSlots(
        request: BotRequest,
        reactions: Reactions,
        botContext: BotContext,
        activatorContext: ActivatorContext?,
        slotReactor: SlotReactor?
    ): SlotFillingResult {
        var context = activatorContext?.dialogflow?.apply {
            if (queryResult.allRequiredParamsPresent) {
                return SlotFillingSkipped
            }
        }
        if (context == null) {
            context = recogniseIntent(botContext, request).firstOrNull()
        }
        return when {
            context == null -> SlotFillingInterrupted
            context.queryResult.allRequiredParamsPresent -> SlotFillingFinished(context)

            else -> SlotFillingInProgress.also {
                val prompt = context.queryResult.fulfillmentText
                val slotName = context.queryResult.outputContextsList
                    .map { it.name }
                    .find { it.contains("_dialog_params_") }
                    ?.substringAfterLast("_dialog_params_")

                if (slotName != null && slotReactor?.canReact(slotName) == true) {
                    slotReactor.react(request, botContext, reactions, context, slotName, listOf(prompt))
                } else {
                    reactions.say(prompt)
                }
            }
        }
    }

    class Factory(
        private val connector: DialogflowConnector,
        private val queryParametersProvider: QueryParametersProvider = QueryParametersProvider.default
    ): ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return DialogflowIntentActivator(model, connector, queryParametersProvider)
        }
    }

}