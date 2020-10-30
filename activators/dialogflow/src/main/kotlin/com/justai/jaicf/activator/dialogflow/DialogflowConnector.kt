package com.justai.jaicf.activator.dialogflow

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.*
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import java.io.InputStream

data class DialogflowAgentConfig(
    val language: String,
    val credentials: InputStream
) {
    constructor(language: String, credentialsResourcePath: String)
            : this(language, DialogflowAgentConfig::class.java.getResourceAsStream(credentialsResourcePath))
}

class DialogflowConnector(private val config: DialogflowAgentConfig) {

    private val sessionSettings: SessionsSettings
    private val contextsSettings: ContextsSettings
    private val projectId: String

    init {
        val credentials = ServiceAccountCredentials.fromStream(config.credentials)
        projectId = credentials.projectId
        sessionSettings = SessionsSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()
        contextsSettings = ContextsSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()
    }

    private fun createQuery(request: BotRequest): QueryInput.Builder? {
        val query = QueryInput.newBuilder()

        return when (request.type) {
            BotRequestType.EVENT -> query.setEvent(
                EventInput.newBuilder()
                    .setName(request.input)
                    .setLanguageCode(config.language).build()
            )

            BotRequestType.QUERY -> query.setText(TextInput.newBuilder()
                .setText(request.input)
                .setLanguageCode(config.language))

            else -> null
        }
    }

    private fun detectIntent(query: QueryInput, session: SessionName, params: QueryParameters): QueryResult {
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

    fun detectIntent(request: BotRequest, params: QueryParameters) = createQuery(request)?.let {
        detectIntent(it.build(), request.sessionName, params)
    }

    fun deleteAllContexts(request: BotRequest) {
        val client = ContextsClient.create(contextsSettings)
        try {
            client.deleteAllContexts(request.sessionName)
        } finally {
            client.close()
        }
    }

    private val BotRequest.sessionName
        get() = SessionName.of(projectId, clientId)
}