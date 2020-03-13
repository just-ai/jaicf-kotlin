package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBotChannel
import com.justai.jaicf.channel.jaicp.JaicpChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpExternalPollingChannelFactory
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingResponse
import com.justai.jaicf.channel.jaicp.dto.create
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray

private fun deserializeBotRequest(rawRequest: String) = JSON.parse(
    JaicpPollingRequest.serializer(), rawRequest
)

class Dispatcher(private val proxyUrl: String) :
    WithLogger,
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val pollingChannels = mutableListOf<PollingChannel>()
    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.INFO
        }
        expectSuccess = true
        engine {
            endpoint {
                connectTimeout = 10000
                requestTimeout = 35000
                keepAliveTime = 35000
            }
        }
    }

    private val poller = RequestPoller(client)
    private val sender = ResponseSender(client)

    fun runChannel(
        factory: JaicpExternalPollingChannelFactory,
        botApi: BotApi
    ) = launch { factory.createAndRun(botApi, "$proxyUrl/${factory.channelType}".toUrl()) }

    fun registerPolling(
        factory: JaicpChannelFactory,
        channel: JaicpBotChannel
    ) = pollingChannels.add(
        PollingChannel(
            "$proxyUrl/${factory.channelType}".toUrl(),
            channel
        )
    )

    fun startPollingBlocking() {
        val jobs = pollingChannels.map { channel ->
            launch {
                logger.info("Starting polling coroutine for channel ${channel.botChannel}")
                runPollingForChannel(channel)
            }
        }
        runBlocking { jobs.joinAll() }
    }


    private suspend fun runPollingForChannel(channel: PollingChannel) {
        poller.getUpdates(channel.url).collect { rawRequest ->
            logger.info("Received bot request: $rawRequest")
            val botRequest = deserializeBotRequest(rawRequest)
            when (val botChannel = channel.botChannel) {
                is JaicpNativeBotChannel -> processNative(botChannel, channel.url, botRequest)
                is JaicpCompatibleBotChannel -> processCompatible(botChannel, channel.url, botRequest)
            }
        }
    }

    private fun processNative(
        channel: JaicpNativeBotChannel,
        url: String,
        pollingRequest: JaicpPollingRequest
    ) {
        val botRequest = pollingRequest.request
        val botResponse = channel.process(botRequest)

        sendResponse(botResponse, url)
    }

    private fun processCompatible(
        channel: JaicpCompatibleBotChannel,
        url: String,
        pollingRequest: JaicpPollingRequest
    ) {
        val rawRequest = pollingRequest.request.rawRequest.toString()
        channel.process(rawRequest)?.let { json ->
            val rawJson = JSON.parseJson(json)
            val rawResponse = addRawReply(rawJson)

            val botResponse = JaicpBotResponse.create(pollingRequest.request, rawResponse)
            sendResponse(botResponse, url)
        }
    }

    private fun sendResponse(
        botResponse: JaicpBotResponse,
        url: String
    ) = launch {
        sender.processResponse(
            url,
            JaicpPollingResponse(botResponse.questionId, botResponse)
        )
    }

    private fun addRawReply(rawResponse: JsonElement) = json {
        "replies" to jsonArray {
            +(json {
                "type" to "raw"
                "text" to rawResponse
            })
        }
    }
}
