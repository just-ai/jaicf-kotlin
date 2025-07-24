package com.justai.jaicf.activator.llm.vectorstore

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson

private val DefaultHTTPClient = HttpClient {
    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        }
    }
}

abstract class HTTPVectorStore(
    val client: HttpClient = DefaultHTTPClient
): LLMVectorStore