package com.justai.jaicf.activator.lex

import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lexruntime.LexRuntimeClient


class LexConnector(
    private val credentials: AwsCredentials,
    private val botName: String,
    private val botAlias: String,
    private val region: Region = Region.EU_CENTRAL_1
) {
    private val client: LexRuntimeClient by lazy {
        LexRuntimeClient.builder()
            .credentialsProvider { credentials }
            .region(region)
            .build()
    }

    fun postText(userId: String, inputText: String) = client.postText { builder ->
        builder.run {
            botName(botName)
            botAlias(botAlias)
            userId(userId)
            inputText(inputText)
        }
    }

}