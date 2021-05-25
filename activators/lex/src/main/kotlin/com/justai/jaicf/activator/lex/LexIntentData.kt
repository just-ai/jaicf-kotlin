package com.justai.jaicf.activator.lex

import software.amazon.awssdk.services.lexruntimev2.model.ConfirmationState
import software.amazon.awssdk.services.lexruntimev2.model.DialogActionType
import software.amazon.awssdk.services.lexruntimev2.model.Intent
import software.amazon.awssdk.services.lexruntimev2.model.IntentState
import software.amazon.awssdk.services.lexruntimev2.model.Message
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextResponse

sealed class LexIntentData {

    sealed class Recognized(
        val intent: String,
        val confidence: Float,
        val messages: List<Message>,
        val slots: Map<String, String?>
    ) : LexIntentData() {

        class IntentReady(
            intent: String,
            confidence: Float,
            messages: List<Message>,
            slots: Map<String, String?>
        ) : Recognized(intent, confidence, messages, slots)

        class ElicitSlot(
            intent: String,
            confidence: Float,
            messages: List<Message>,
            slots: Map<String, String?>,
            val slotToElicit: String
        ) : Recognized(intent, confidence, messages, slots)

        class ConfirmIntent(
            intent: String,
            confidence: Float,
            messages: List<Message>,
            slots: Map<String, String?>
        ) : Recognized(intent, confidence, messages, slots)

        class ConfirmationDenied(
            intent: String,
            messages: List<Message>,
            slots: Map<String, String?>
        ) : Recognized(intent, 1f, messages, slots)
    }

    object NotRecognized : LexIntentData()

    object Failed : LexIntentData()
}

fun RecognizeTextResponse.toIntentData() =
    when (sessionState().dialogAction().type()) {
        DialogActionType.CLOSE -> closedResponseToIntentData()

        DialogActionType.ELICIT_SLOT -> LexIntentData.Recognized.ElicitSlot(
            sessionState().intent().name(),
            getConfidence(sessionState().intent()),
            messages() ?: emptyList(),
            sessionState().intent().getInterpretedSlots(),
            sessionState().dialogAction().slotToElicit()
        )

        DialogActionType.CONFIRM_INTENT -> LexIntentData.Recognized.ConfirmIntent(
            sessionState().intent().name(),
            getConfidence(sessionState().intent()),
            messages() ?: emptyList(),
            sessionState().intent().getInterpretedSlots(),
        )

        else -> LexIntentData.NotRecognized
    }

private fun RecognizeTextResponse.closedResponseToIntentData() =
    when (sessionState().intent().state()) {
        IntentState.FULFILLED,
        IntentState.READY_FOR_FULFILLMENT ->
            LexIntentData.Recognized.IntentReady(
                sessionState().intent().name(),
                getConfidence(sessionState().intent()),
                messages() ?: emptyList(),
                sessionState().intent().getInterpretedSlots(),
            )

        IntentState.FAILED -> {
            if (sessionState().intent().confirmationState() == ConfirmationState.DENIED) {
                LexIntentData.Recognized.ConfirmationDenied(
                    sessionState().intent().name(),
                    messages() ?: emptyList(),
                    sessionState().intent().getInterpretedSlots()
                )
            } else {
                LexIntentData.Failed
            }
        }

        else -> LexIntentData.Failed
    }

private fun Intent.getInterpretedSlots() = slots()?.mapValues { it.value?.value()?.interpretedValue() } ?: emptyMap()

private fun RecognizeTextResponse.getConfidence(intent: Intent) = interpretations()
    .firstOrNull { it.intent().name() == intent.name() }
    ?.nluConfidence()?.score()?.toFloat() ?: 1f
