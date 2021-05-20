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

        val qr = connector.detectIntent(request, params)
                ?.takeIf { it.intentDetectionConfidence > 0 } ?: return emptyList()

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
                return SlotFillingFinished(activatorContext)
            } else {
                botContext.session[INTENT_NAME] = intent
            }
        }

        if (context == null) {
            context = recogniseIntent(botContext, request).firstOrNull()
        }

        return when {
            context == null || canInterruptSlotFilling(context, botContext) -> SlotFillingInterrupted()
            canFinishSlotFilling(context) -> SlotFillingFinished(context)

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
        }.also { result ->
            if (result !is SlotFillingInProgress) {
                botContext.session.remove(INTENT_NAME)
                cleanSession(botContext, request)
            }
        }
    }

    override fun cleanSession(botContext: BotContext, request: BotRequest) {
        connector.deleteAllContexts(request)
    }

    class Factory(
        private val connector: DialogflowConnector,
        private val queryParametersProvider: QueryParametersProvider = QueryParametersProvider.default
    ): ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return DialogflowIntentActivator(model, connector, queryParametersProvider)
        }
    }

    companion object {
        private const val INTENT_NAME = "dialogflow/intent"

        private fun canInterruptSlotFilling(context: DialogflowActivatorContext, botContext: BotContext)
            = context.intent != botContext.session[INTENT_NAME] || context.slots.isEmpty()

        private fun canFinishSlotFilling(context: DialogflowActivatorContext)
            = context.queryResult.allRequiredParamsPresent
    }

}
