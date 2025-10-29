package com.justai.jaicf.plugins.caila.publish.internal.http

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal object HttpClientFactory {
    fun create(
        logLevel: LogLevel,
        connectTimeoutMs: Int = 10_000,
        requestTimeoutMs: Int = 35_000,
        keepAliveTimeMs: Int = 35_000,
    ) = HttpClient(CIO) {
        expectSuccess = true
        engine {
            endpoint {
                connectTimeout = connectTimeoutMs.toLong()
                requestTimeout = requestTimeoutMs.toLong()
                keepAliveTime = keepAliveTimeMs.toLong()
            }
        }
        install(Logging) {
            this.logger = Logger.DEFAULT
            this.level = logLevel
        }
        install(ContentNegotiation) {
            json(
                Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = false }
            )
        }
    }
}
