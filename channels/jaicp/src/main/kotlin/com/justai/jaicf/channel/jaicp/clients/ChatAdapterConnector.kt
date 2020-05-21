package com.justai.jaicf.channel.jaicp.clients

import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking


class ChatAdapterClient(
        private val url: String
) : WithLogger {

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

    fun listChannels(accessToken: String) = runBlocking {
        try {
            client.get<List<ChannelConfig>>("$url/restapi/external-bot/$accessToken/channels")
        } catch (e: ClientRequestException) {
            throw error("Invalid access token: $e")
        }
    }
}