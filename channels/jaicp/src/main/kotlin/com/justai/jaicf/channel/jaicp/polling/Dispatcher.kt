package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.*
import com.justai.jaicf.channel.jaicp.JaicpMDC
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingResponse
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.slf4j.MDCContext


internal class Dispatcher(
    client: HttpClient,
    private val requestProcessor: (JaicpBotRequest, JaicpBotChannel) -> JaicpBotResponse?
) :
    WithLogger,
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val pollingChannels = mutableListOf<PollingChannel>()
    private val poller = RequestPoller(client)
    private val sender = ResponseSender(client)

    fun registerPolling(channel: JaicpBotChannel, proxyUrl: String) {
        pollingChannels.add(PollingChannel(proxyUrl, channel))
            .also { logger.info("Registered polling for channel $channel") }
    }

    fun startPollingBlocking() {
        val jobs = pollingChannels.map { channel ->
            launch(MDCContext()) {
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
            JaicpMDC.setFromRequest(request)
            requestProcessor.invoke(request, channel.botChannel)?.let { response ->
                sendResponse(response, channel.url)
            }
        }
    }

    private fun sendResponse(botResponse: JaicpBotResponse, url: String) = launch {
        sender.processResponse(url, JaicpPollingResponse(botResponse.questionId, botResponse))
    }
}

