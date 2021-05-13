package com.justai.jaicf.activator.lex

internal sealed class LexIntentData {
    sealed class Recognized(val intent: String, val confidence: Float, val messages: List<String>): LexIntentData() {
        class IntentReady(
            intent: String,
            confidence: Float,
            messages: List<String>,
            val slots: Map<String, String?>
        ) : Recognized(intent, confidence, messages)

        class ElicitSlot(
            intent: String,
            confidence: Float,
            messages: List<String>,
            val slotToElicit: String
        ) : Recognized(intent, confidence, messages)

        class ConfirmIntent(
            intent: String,
            confidence: Float,
            messages: List<String>
        ) : Recognized(intent, confidence, messages)
    }

    class Failed(val intent: String, val messages: List<String>) : LexIntentData()

    object NotRecognized : LexIntentData()
}