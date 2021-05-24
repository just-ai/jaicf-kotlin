package com.justai.jaicf.activator.lex

import software.amazon.awssdk.services.lexruntimev2.model.ConfirmationState
import software.amazon.awssdk.services.lexruntimev2.model.DialogActionType
import software.amazon.awssdk.services.lexruntimev2.model.IntentState
import software.amazon.awssdk.services.lexruntimev2.model.Message
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextResponse
import software.amazon.awssdk.services.lexruntimev2.model.Slot

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
            confidence,
            messages() ?: emptyList(),
            sessionState().intent().slots()?.toInterpretedValueList() ?: emptyMap(),
            sessionState().dialogAction().slotToElicit()
        )

        DialogActionType.CONFIRM_INTENT -> LexIntentData.Recognized.ConfirmIntent(
            sessionState().intent().name(),
            confidence,
            messages() ?: emptyList(),
            sessionState().intent().slots()?.toInterpretedValueList() ?: emptyMap()
        )

        else -> LexIntentData.NotRecognized
    }

private fun RecognizeTextResponse.closedResponseToIntentData() =
    when (sessionState().intent().state()) {
        IntentState.FULFILLED,
        IntentState.READY_FOR_FULFILLMENT ->
            LexIntentData.Recognized.IntentReady(
                sessionState().intent().name(),
                confidence,
                messages() ?: emptyList(),
                sessionState().intent().slots()?.toInterpretedValueList() ?: emptyMap()
            )

        IntentState.FAILED -> {
            if (sessionState().intent().confirmationState() == ConfirmationState.DENIED) {
                LexIntentData.Recognized.ConfirmationDenied(
                    sessionState().intent().name(),
                    messages() ?: emptyList(),
                    sessionState().intent().slots()?.toInterpretedValueList() ?: emptyMap()
                )
            } else {
                LexIntentData.Failed
            }
        }

        else -> LexIntentData.Failed
    }

private fun MutableMap<String, Slot?>.toInterpretedValueList(): Map<String, String?> =
    mapValues { it.value?.value()?.interpretedValue() }

private val RecognizeTextResponse.confidence
    get() = interpretations()
        .firstOrNull { it.intent().stateAsString() != null }
        ?.nluConfidence()?.score()?.toFloat() ?: 1f
