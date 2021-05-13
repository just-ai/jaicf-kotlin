package com.justai.jaicf.activator.lex.sdk

import com.justai.jaicf.activator.lex.LexIntentData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lexruntime.LexRuntimeClient
import software.amazon.awssdk.services.lexruntime.model.DialogState
import software.amazon.awssdk.services.lexruntime.model.MessageFormatType
import software.amazon.awssdk.services.lexruntime.model.PostTextResponse

internal fun buildLexRuntimeClient(credentials: AwsCredentials, region: Region) =
    LexRuntimeClient.builder()
        .credentialsProvider { credentials }
        .region(region)
        .build()

internal fun PostTextResponse.toResult() = when (dialogState()) {
    DialogState.READY_FOR_FULFILLMENT,
    DialogState.FULFILLED -> LexIntentData.Recognized.IntentReady(intentName(), confidence, parseMessages(), slots())
    DialogState.ELICIT_SLOT -> LexIntentData.Recognized.ElicitSlot(
        intentName(),
        confidence,
        parseMessages(),
        slotToElicit()
    )
    DialogState.CONFIRM_INTENT -> LexIntentData.Recognized.ConfirmIntent(intentName(), confidence, parseMessages())
    DialogState.FAILED -> LexIntentData.Failed(intentName(), parseMessages())
    else -> LexIntentData.NotRecognized
}

internal fun PostTextResponse.parseMessages(): List<String> {
    val message = message() ?: return emptyList()

    val messages = when (messageFormat()) {
        MessageFormatType.COMPOSITE -> {
            Json(JsonConfiguration.Stable).parseJson(message)
                .jsonObject.getArray("messages")
                .map { it.jsonObject.getPrimitive("value").content }
        }
        else -> listOf(message)
    }

    return messages.filter { it.isNotBlank() }
}

private val PostTextResponse.confidence get() = nluIntentConfidence().score().toFloat()