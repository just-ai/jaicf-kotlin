package com.justai.jaicf.activator.caila.client

import com.justai.jaicf.activator.caila.dto.CailaInferenceResults
import com.justai.jaicf.activator.caila.dto.EntitiesLookupResults
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class CailaKtorClient(
    accessToken: String,
    url: String
) : WithLogger,
    CailaHttpClient {

    private val simpleInferenceUrl = "$url/$accessToken/nlu/inference".toUrl()
    private val client = HttpClient(CIO) { expectSuccess = true }
    private val json = Json(JsonConfiguration.Stable.copy(strictMode = false, encodeDefaults = false))

    override fun simpleInference(query: String): CailaInferenceResults? {
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

    override fun entitiesLookup(query: String): EntitiesLookupResults? {
        try {
            return runBlocking { entitiesLookupAsync(query) }
        } catch (ex: Exception) {
            logger.warn("Failed on cailapub entities lookup processing", ex)
        }
        return null
    }

    private suspend fun entitiesLookupAsync(query: String, showAll: Boolean = true): EntitiesLookupResults {
        val response = client.get<String>(simpleInferenceUrl) {
            parameter("query", query)
            parameter("showAll", showAll)
        }
        logger.info(response)
        return json.parse(EntitiesLookupResults.serializer(), response)
    }
}