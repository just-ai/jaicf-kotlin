package com.justai.jaicf.activator.dialogflow

import com.google.cloud.dialogflow.v2.Intent
import com.google.cloud.dialogflow.v2.QueryResult
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

data class DialogflowActivatorContext(
    override val intent: String,
    val queryResult: QueryResult
): IntentActivatorContext(
    confidence = queryResult.intentDetectionConfidence,
    intent = intent
) {
    val slots = queryResult.parameters
    val messages = queryResult.fulfillmentMessagesList

    val textResponses: List<String> = messages
        .map { it.text }.map { it.textList }.flatten()

    val simpleResponses: List<Intent.Message.SimpleResponse> = messages
        .map { it.simpleResponses.simpleResponsesList }
        .flatten()
}

val ActivatorContext.dialogflow
    get() = this as? DialogflowActivatorContext