package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.JaicpBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpResponse
import com.justai.jaicf.channel.jaicp.execution.ThreadPoolRequestExecutor
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.slf4j.MDCContext
import kotlin.coroutines.CoroutineContext


internal class Dispatcher(
    private val client: HttpClient,
    private val executor: ThreadPoolRequestExecutor
) : WithLogger, CoroutineScope {

    private val supervisor = SupervisorJob()
    override val coroutineContext: CoroutineContext = supervisor + MDCContext()
    private val pollingChannels = mutableListOf<PollingChannel>()
    private val sender = ResponseSender(client)

    fun registerPolling(channel: JaicpBotChannel, proxyUrl: String) = pollingChannels
        .add(PollingChannel(proxyUrl, channel, RequestPoller(client, proxyUrl)))
        .also { logger.info("Registered polling for channel $channel") }

    fun startPollingBlocking() = runBlocking {
        pollingChannels.map { channel ->
            launch(MDCContext()) {
                logger.info("Starting polling coroutine for channel ${channel.botChannel}")
                runPolling(channel)
            }
        }.joinAll()
    }

    private suspend fun runPolling(channel: PollingChannel) {
        channel.poller.getUpdates().collect { reqs ->
            reqs.forEach { req ->
                sendResponse(executor.executeAsync(req, channel.botChannel), channel.url)
            }
        }
    }

    private fun sendResponse(botResponse: Deferred<JaicpResponse>, url: String) {
        launch(Dispatchers.IO) {
            sender.send(url, botResponse)
        }
    }
}

