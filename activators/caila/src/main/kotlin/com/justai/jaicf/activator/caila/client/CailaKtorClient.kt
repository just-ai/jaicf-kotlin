package com.justai.jaicf.activator.caila.client

import com.justai.jaicf.activator.caila.DEFAULT_CAILA_URL
import com.justai.jaicf.activator.caila.dto.*
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class CailaKtorClient(
    override val accessToken: String,
    override val url: String = DEFAULT_CAILA_URL
) : WithLogger,
    CailaHttpClient {

    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
    private val json = Json(JsonConfiguration.Stable.copy(strictMode = false, encodeDefaults = false))

    override fun simpleInference(query: String): CailaInferenceResultData? {
        try {
            return runBlocking { simpleInferenceAsync(query) }
        } catch (ex: Exception) {
            logger.warn("Failed on cailapub activator processing", ex)
        }
        return null
    }

    private suspend fun simpleInferenceAsync(query: String): CailaInferenceResultData? {
        val response = client.get<String>(inferenceUrl) {
            parameter("query", query)
        }
        logger.info(response)
        val intent = json.parseJson(response).jsonObject["intent"] ?: return null
        if (intent.isNull) {
            return null
        }
        return json.parse(CailaInferenceResultData.serializer(), response)
    }

    override fun entitiesLookup(query: String): CailaEntitiesLookupResults? {
        try {
            return runBlocking { entitiesLookupAsync(query) }
        } catch (ex: Exception) {
            logger.warn("Failed on cailapub entities lookup processing", ex)
        }
        return null
    }

    private suspend fun entitiesLookupAsync(query: String, showAll: Boolean = true): CailaEntitiesLookupResults {
        val response = client.get<String>(entitiesLookupUrl) {
            parameter("query", query)
            parameter("showAll", showAll)
        }
        logger.info(response)
        return json.parse(CailaEntitiesLookupResults.serializer(), response)
    }


    override fun analyze(query: String): CailaAnalyzeResponseData? {
        val requestData = CailaAnalyzeRequestData(
            CailaInferenceRequestData(
                CailaPhraseMarkupData(
                    text = query,
                    entities = emptyList()
                ),
                knownSlots = emptyList()
            ), true
        )

        try {
            return runBlocking { analyzeAsync(requestData) }
        } catch (ex: Exception) {
            logger.warn("Failed on cailapub analyze processing", ex)
        }
        return null
    }

    private suspend fun analyzeAsync(requestData: CailaAnalyzeRequestData): CailaAnalyzeResponseData {
        val response = client.post<String>(analyzeUrl) {
            contentType(ContentType.Application.Json)
            body = requestData
        }
        logger.info(response)
        return json.parse(CailaAnalyzeResponseData.serializer(), response)
    }
}