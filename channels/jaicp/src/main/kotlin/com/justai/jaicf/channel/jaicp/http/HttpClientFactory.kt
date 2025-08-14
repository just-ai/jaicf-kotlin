package com.justai.jaicf.channel.jaicp.http

import com.justai.jaicf.channel.jaicp.JSON
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

internal object HttpClientFactory {
    fun create(logLevel: LogLevel) = HttpClient(CIO) {
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
        install(ContentNegotiation) {
            json(JSON)
        }
    }
}
