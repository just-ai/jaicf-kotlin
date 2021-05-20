package com.justai.jaicf.activator.lex

import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2Client
import software.amazon.awssdk.services.lexruntimev2.model.ConfirmationState
import software.amazon.awssdk.services.lexruntimev2.model.DialogActionType
import software.amazon.awssdk.services.lexruntimev2.model.IntentState
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextRequest
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextResponse
import software.amazon.awssdk.services.lexruntimev2.model.Slot
import java.util.*

class LexConnector(
    private val botConfig: LexBotConfig,
    private val runtimeClient: LexRuntimeV2Client,
) {

    constructor(
        lexBotConfig: LexBotConfig,
        credentials: AwsCredentials
    ) : this(lexBotConfig, buildLexRuntimeClient(credentials, lexBotConfig.region))

    internal fun recognizeIntent(sessionId: String, inputText: String): LexIntentData {
        val request = RecognizeTextRequest.builder().apply {
            botId(botConfig.botId)
            botAliasId(botConfig.aliasId)
            sessionId(sessionId)
            localeId(botConfig.localeId)
            text(inputText)
        }.build()

        return runtimeClient.recognizeText(request).toIntentData()
    }
}

data class LexBotConfig(
    val botId: String,
    val aliasId: String,
    val region: Region,
    val locale: Locale
) {
    internal val localeId: String by lazy { "${locale.language}_${locale.country}" }
}

internal fun buildLexRuntimeClient(credentials: AwsCredentials, region: Region) =
    LexRuntimeV2Client.builder()
        .credentialsProvider { credentials }
        .region(region)
        .build()

@Suppress("MoveVariableDeclarationIntoWhen")
internal fun RecognizeTextResponse.toIntentData(): LexIntentData {
    val dialogActionType = sessionState().dialogAction().type()

    return when (dialogActionType) {
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
                LexIntentData.Recognized.Denied(
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
