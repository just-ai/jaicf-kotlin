package com.justai.jaicf.activator.lex

import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2Client
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextRequest
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextResponse
import java.util.*

class LexConnector(
    private val botConfig: LexBotConfig,
    private val runtimeClient: LexRuntimeV2Client,
) {

    constructor(
        lexBotConfig: LexBotConfig,
        credentials: AwsCredentials
    ) : this(lexBotConfig, buildLexRuntimeClient(credentials, lexBotConfig.region))

    fun recognizeIntent(sessionId: String, inputText: String): RecognizeTextResponse {
        val request = RecognizeTextRequest.builder().apply {
            botId(botConfig.botId)
            botAliasId(botConfig.aliasId)
            sessionId(sessionId)
            localeId(botConfig.localeId)
            text(inputText)
        }.build()

        return runtimeClient.recognizeText(request)
    }
}

data class LexBotConfig(
    val botId: String,
    val aliasId: String,
    val region: Region,
    val locale: Locale
) {
    val localeId: String by lazy { "${locale.language}_${locale.country}" }
}

internal fun buildLexRuntimeClient(credentials: AwsCredentials, region: Region) =
    LexRuntimeV2Client.builder()
        .credentialsProvider { credentials }
        .region(region)
        .build()
