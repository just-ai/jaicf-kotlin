package com.justai.jaicf.activator.llm.client.openai

import com.justai.jaicf.activator.llm.client.LLMClient
import com.justai.jaicf.activator.llm.client.LLMRequest
import com.justai.jaicf.activator.llm.client.LLMResponse
import com.justai.jaicf.activator.llm.LLMSettings
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class OpenAIClient(
    private val settings: LLMSettings,
    logLevel: LogLevel = LogLevel.INFO,
) : LLMClient {
    private val client = HttpClient(Apache) {
        expectSuccess = true
        install(Logging) {
            logger = Logger.DEFAULT
            level = logLevel
        }
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
        engine {
            socketTimeout = 30_000
        }
    }

    override fun chatCompletion(request: LLMRequest): LLMResponse = runBlocking {
        client.post("${settings.baseUrl}/chat/completions") {
            header("Authorization", "Bearer ${settings.apiKey}")
            contentType(ContentType.Application.Json)
            body = request
        }
    }
}