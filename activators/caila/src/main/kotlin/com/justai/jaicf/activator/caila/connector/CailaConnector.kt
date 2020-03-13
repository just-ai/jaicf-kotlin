package com.justai.jaicf.activator.caila.connector

import com.justai.jaicf.activator.caila.dto.CailaInferenceResults
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

internal class CailaConnector(
    projectId: String,
    url: String
) : WithLogger {

    private val simpleInferenceUrl = "$url/$projectId/nlu/inference".toUrl()
    private val client = HttpClient(CIO) { expectSuccess = true }
    private val json = Json(JsonConfiguration.Stable.copy(strictMode = false, encodeDefaults = false))

    fun simpleInference(query: String): CailaInferenceResults? {
        try {
            return runBlocking { simpleInferenceAsync(query) }
        } catch (ex: Exception) {
            logger.warn("Failed on cailapub activator processing", ex)
        }
        return null
    }

    private suspend fun simpleInferenceAsync(query: String): CailaInferenceResults {
        val response = client.get<String>(simpleInferenceUrl) {
            parameter("query", query)
        }
        logger.info(response)
        return json.parse(CailaInferenceResults.serializer(), response)
    }
}