package com.justai.jaicf.channel.jaicp.http

import com.justai.jaicf.channel.jaicp.JSON
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*

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
        install(JsonFeature) {
            serializer = KotlinxSerializer(JSON)
        }
    }
}
