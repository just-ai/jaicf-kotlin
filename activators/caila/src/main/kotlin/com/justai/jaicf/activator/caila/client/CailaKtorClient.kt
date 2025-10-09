package com.justai.jaicf.activator.caila.client

import com.justai.jaicf.activator.caila.DEFAULT_CAILA_URL
import com.justai.jaicf.activator.caila.JSON
import com.justai.jaicf.activator.caila.dto.*
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject

class CailaKtorClient(
    override val accessToken: String,
    override val url: String = DEFAULT_CAILA_URL,
    override val inferenceNBest: Int,
    logLevel: LogLevel = LogLevel.INFO,
    engine: HttpClientEngine = CIO.create()
) : WithLogger,
    CailaHttpClient {

    private val client = HttpClient(engine) {
        expectSuccess = true
        install(Logging) {
            logger = Logger.DEFAULT
            level = logLevel
        }
        install(ContentNegotiation) {
            json(JSON)
        }
    }

    override fun simpleInference(query: String): CailaInferenceResultData? {
        try {
            return runBlocking { simpleInferenceAsync(query) }
        } catch (ex: Exception) {
            logger.warn("Failed on cailapub activator processing", ex)
        }
        return null
    }

    private suspend fun simpleInferenceAsync(query: String): CailaInferenceResultData? {
        val response: String = client.get(inferenceUrl) {
            parameter("query", query)
        }.body()
        logger.info(response)
        JSON.parseToJsonElement(response).jsonObject["intent"] ?: return null
        return JSON.decodeFromString(CailaInferenceResultData.serializer(), response)
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
        val response: String = client.get(entitiesLookupUrl) {
            parameter("query", query)
            parameter("showAll", showAll)
        }.body()
        logger.info(response)
        return JSON.decodeFromString(CailaEntitiesLookupResults.serializer(), response)
    }


    override fun analyze(query: String): CailaAnalyzeResponseData? {
        val requestData = CailaAnalyzeRequestData(
            CailaInferenceRequestData(
                CailaPhraseMarkupData(text = query, entities = mutableListOf()),
                knownSlots = emptyList(),
                nBest = inferenceNBest
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
        val response: String = client.post(analyzeUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestData)
        }.body()
        logger.info(response)
        return JSON.decodeFromString(CailaAnalyzeResponseData.serializer(), response)
    }
}