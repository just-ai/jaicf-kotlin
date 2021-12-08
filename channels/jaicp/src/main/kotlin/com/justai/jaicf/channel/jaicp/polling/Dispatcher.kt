package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.JaicpBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpResponse
import com.justai.jaicf.channel.jaicp.execution.JaicpRequestExecutor
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext


internal class Dispatcher(
    private val client: HttpClient,
    private val jaicpExecutor: JaicpRequestExecutor,
) : WithLogger {

    private val pollingChannels = mutableListOf<PollingChannel>()
    private val sender = ResponseSender(client)

    fun registerPolling(channel: JaicpBotChannel, proxyUrl: String) = pollingChannels
        .add(PollingChannel(
            url = proxyUrl,
            botChannel = channel,
            poller = RequestPoller(client, proxyUrl, jaicpExecutor.coroutineContext)
        ))
        .also { logger.info("Registered polling for channel $channel") }

    fun startPollingBlocking() = runBlocking {
        logger.info("Start blocking polling for ${pollingChannels.size} channels")
        startPolling().joinAll()
    }

    fun stopPolling() {
        logger.info("Stop polling for ${pollingChannels.size} channels")
        pollingChannels.forEach { it.poller.stopPolling() }
    }

    private suspend fun PollingChannel.registerUpdatesCollector() = poller.getUpdates().collect { reqs ->
        reqs.forEach { req ->
            sendResponse(jaicpExecutor.executeAsync(req, botChannel), url)
        }
    }

    private fun sendResponse(botResponse: Deferred<JaicpResponse>, url: String) {
        CoroutineScope(jaicpExecutor.coroutineContext).launch {
            sender.send(url, botResponse)
        }
    }

    fun startPolling(): List<Job> {
        logger.info("Start polling for ${pollingChannels.size} channels")
        return pollingChannels.map { channel ->
            CoroutineScope(jaicpExecutor.coroutineContext + MDCContext()).launch {
                logger.info("Starting polling coroutine for channel ${channel.botChannel}")
                channel.poller.startPolling()
                channel.registerUpdatesCollector()
            }
        }
    }
}

