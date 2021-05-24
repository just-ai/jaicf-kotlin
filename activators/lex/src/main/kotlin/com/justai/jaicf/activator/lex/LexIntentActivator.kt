package com.justai.jaicf.activator.lex

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.SlotFillingFinished
import com.justai.jaicf.slotfilling.SlotFillingInProgress
import com.justai.jaicf.slotfilling.SlotFillingInterrupted
import com.justai.jaicf.slotfilling.SlotFillingResult
import com.justai.jaicf.slotfilling.SlotReactor

class LexIntentActivator(
    model: ScenarioModel,
    private val connector: LexConnector
) : BaseIntentActivator(model) {

    override val name = "lexIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest) =
        when (val lexIntentData = connector.recognizeIntent(request.clientId, request.input).toIntentData()) {
            is LexIntentData.Recognized.ElicitSlot -> {
                botContext.session[INTENT_NAME] = lexIntentData.intent
                botContext.session[SLOT_TO_ELICIT] = lexIntentData.slotToElicit
                listOf(LexActivatorContext(lexIntentData))
            }

            is LexIntentData.Recognized.ConfirmIntent -> {
                botContext.session[INTENT_NAME] = lexIntentData.intent
                listOf(LexActivatorContext(lexIntentData))
            }

            is LexIntentData.Recognized.IntentReady -> {
                listOf(LexActivatorContext(lexIntentData))
            }

            else -> emptyList()
        }

    override fun fillSlots(
        request: BotRequest,
        reactions: Reactions,
        botContext: BotContext,
        activatorContext: ActivatorContext?,
        slotReactor: SlotReactor?
    ): SlotFillingResult {

        activatorContext?.lex?.apply {
            if (intentData is LexIntentData.Recognized.IntentReady) {
                return SlotFillingFinished(activatorContext)
            }

            intentData.messages
                .filter { it.content() != null }
                .forEach { reactions.say(it.content()) }

            return SlotFillingInProgress
        }

        val slotToElicit = botContext.session[SLOT_TO_ELICIT] as? String
        val recognizedIntentData = connector.recognizeIntent(request.clientId, request.input).toIntentData()

        if ((recognizedIntentData as? LexIntentData.Recognized)?.intent != botContext.session[INTENT_NAME]) {
            return SlotFillingInterrupted()
        }

        if (recognizedIntentData is LexIntentData.Recognized.ElicitSlot) {
            botContext.session[SLOT_TO_ELICIT] = recognizedIntentData.slotToElicit
        }

        return when (recognizedIntentData) {
            is LexIntentData.Recognized.IntentReady -> {
                recognizedIntentData.messages
                    .filter { it.content() != null }
                    .forEach { reactions.say(it.content()) }

                SlotFillingFinished(LexActivatorContext(recognizedIntentData))
            }

            is LexIntentData.Recognized.ConfirmationDenied -> {
                recognizedIntentData.messages
                    .filter { it.content() != null }
                    .forEach { reactions.say(it.content()) }

                SlotFillingInterrupted(shouldReprocess = false)
            }

            is LexIntentData.Recognized.ElicitSlot,
            is LexIntentData.Recognized.ConfirmIntent -> {
                recognizedIntentData as LexIntentData.Recognized
                if (slotToElicit != null && slotReactor?.canReact(slotToElicit) == true) {
                    slotReactor.react(
                        request,
                        botContext,
                        reactions,
                        LexActivatorContext(recognizedIntentData),
                        slotToElicit,
                        recognizedIntentData.messages.map { objectMapper.writeValueAsString(it) }
                    )
                } else {
                    recognizedIntentData.messages
                        .filter { it.content() != null }
                        .forEach { reactions.say(it.content()) }
                }
                SlotFillingInProgress
            }

            else -> SlotFillingInterrupted()
        }.also { result ->
            if (result !is SlotFillingInProgress) {
                cleanSession(botContext, request)
            }
        }
    }

    override fun cleanSession(botContext: BotContext, request: BotRequest) {
        botContext.session.remove(INTENT_NAME)
        botContext.session.remove(SLOT_TO_ELICIT)
    }

    class Factory(private val connector: LexConnector) : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return LexIntentActivator(model, connector)
        }
    }

    companion object {
        private const val INTENT_NAME = "lex/intent"
        private const val SLOT_TO_ELICIT = "lex/slot"

        private val objectMapper = ObjectMapper().apply {
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        }
    }
}
