package com.justai.jaicf.activator.dialogflow

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.*
import com.justai.jaicf.api.BotRequest
import java.io.InputStream

data class DialogflowAgentConfig(
    val language: String,
    val credentials: InputStream
) {
    constructor(language: String, credentialsResourcePath: String)
            : this(language, DialogflowAgentConfig::class.java.getResourceAsStream(credentialsResourcePath))
}

internal enum class QueryType {
    QUERY, EVENT
}

class DialogflowConnector(private val config: DialogflowAgentConfig) {

    private val sessionSettings: SessionsSettings
    private val projectId: String

    init {
        val credentials = ServiceAccountCredentials.fromStream(config.credentials)
        projectId = credentials.projectId
        sessionSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build()
    }

    private fun createQuery(input: String, type: QueryType): QueryInput {
        val query = QueryInput.newBuilder()

        when (type) {
            QueryType.EVENT -> query.setEvent(
                EventInput.newBuilder()
                    .setName(input)
                    .setLanguageCode(config.language).build()
            )
            QueryType.QUERY -> query.setText(TextInput.newBuilder()
                .setText(input)
                .setLanguageCode(config.language))
        }

        return query.build()
    }

    private fun detectIntent(query: QueryInput, sessionId: String, params: QueryParameters): QueryResult {
        val session = SessionName.of(projectId, sessionId)
        val client = SessionsClient.create(sessionSettings)
        try {
            return client.detectIntent(
                DetectIntentRequest.newBuilder()
                    .setQueryInput(query)
                    .setSession(session.toString())
                    .setQueryParams(params)
                    .build()
            ).queryResult
        } finally {
            client.close()
        }
    }

    fun detectIntentByQuery(request: BotRequest, params: QueryParameters) =
        detectIntent(createQuery(request.input, QueryType.QUERY), request.clientId, params)

    fun detectIntentByEvent(request: BotRequest, params: QueryParameters) =
        detectIntent(createQuery(request.input, QueryType.EVENT), request.clientId, params)
}