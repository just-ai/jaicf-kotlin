package com.justai.jaicf.activator.lex

import com.justai.jaicf.activator.lex.sdk.buildLexRuntimeClient
import com.justai.jaicf.activator.lex.sdk.toResult
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lexruntime.LexRuntimeClient
import software.amazon.awssdk.services.lexruntime.model.PostTextRequest


class LexConnector(
    private val botName: String,
    private val botAlias: String,
    private val client: LexRuntimeClient
) {
    constructor(
        botName: String,
        botAlias: String,
        credentials: AwsCredentials,
        region: Region = Region.EU_CENTRAL_1
    ) : this(botName, botAlias, buildLexRuntimeClient(credentials, region))

    internal fun recognizeIntent(userId: String, inputText: String): LexIntentData {
        val request = PostTextRequest.builder().apply {
            botName(botName)
            botAlias(botAlias)
            userId(userId)
            inputText(inputText)
        }.build()

        return client.postText(request).toResult()
    }

}