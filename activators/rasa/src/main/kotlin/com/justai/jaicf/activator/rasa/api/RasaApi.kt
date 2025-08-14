package com.justai.jaicf.activator.rasa.api

import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class RasaApi(
    private val uri: String,
    logLevel: LogLevel = LogLevel.INFO,
    httpClient: HttpClientEngine = CIO.create()
) : WithLogger {

    internal val Json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = HttpClient(httpClient) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json)
        }

        install(Logging) {
            level = logLevel
        }
    }

    fun parseMessage(request: RasaParseMessageRequest): String? = runBlocking {
        try {
            client.post("$uri/model/parse".toUrl()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            logger.error("Cannot parse $request", e)
            null
        }

    }
}