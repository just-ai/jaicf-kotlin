package com.justai.jaicf.channel.jaicp.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging

internal object HttpClientFactory {
    fun withLogLevel(logLevel: LogLevel) = HttpClient(CIO) {
        expectSuccess = true
        engine {
            endpoint {
                connectTimeout = 10000
                requestTimeout = 35000
                keepAliveTime = 35000
            }
        }
        install(Logging) {
            this.logger = Logger.DEFAULT
            this.level = logLevel
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
}
