package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.*
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingResponse
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
        botApi: BotApi,
        channelToken: String
    ) = launch {
        factory.createAndRun(botApi, "$proxyUrl/$channelToken/${factory.channelType}".toUrl()).also {
            logger.info("Running external polling channel ${factory.channelType}")
        }
    }


    fun registerPolling(
        factory: JaicpChannelFactory,
        channel: JaicpBotChannel,
        channelToken: String
    ) = pollingChannels.add(
        PollingChannel(
            "$proxyUrl/$channelToken/${factory.channelType}".toUrl(),
             channel
        ).also {
            logger.info("Registered polling for channel $channel")
        }
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
            val request = rawRequest.asJaicpPollingRequest().request
            when (val botChannel = channel.botChannel) {
                is JaicpNativeBotChannel -> processNativeChannel(botChannel, channel.url, request)
                is JaicpCompatibleBotChannel -> processCompatibleChannel(botChannel, channel.url, request)
                is JaicpCompatibleAsyncBotChannel -> processAsyncChannel(botChannel, request)
            }
        }
    }

    private fun processAsyncChannel(
        channel: JaicpCompatibleAsyncBotChannel,
        request: JaicpBotRequest
    ) = channel.process(request.rawRequest.toString().asHttpBotRequest())

    private fun processNativeChannel(
        channel: JaicpNativeBotChannel,
        url: String,
        request: JaicpBotRequest
    ) = sendResponse(
        botResponse = channel.process(request),
        url = url
    )

    private fun processCompatibleChannel(
        channel: JaicpCompatibleBotChannel,
        url: String,
        request: JaicpBotRequest
    ) = sendResponse(
        botResponse = channel.processCompatible(request),
        url = url
    )

    private fun sendResponse(
        botResponse: JaicpBotResponse,
        url: String
    ) = launch {
        sender.processResponse(
            url,
            JaicpPollingResponse(botResponse.questionId, botResponse)
        )
    }
}

