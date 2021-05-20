package com.justai.jaicf.activator.lex

import software.amazon.awssdk.services.lexruntimev2.model.Message

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

        class Denied(
            intent: String,
            messages: List<Message>,
            slots: Map<String, String?>
        ) : Recognized(intent, 1f, messages, slots)
    }

    object NotRecognized : LexIntentData()

    object Failed : LexIntentData()
}
