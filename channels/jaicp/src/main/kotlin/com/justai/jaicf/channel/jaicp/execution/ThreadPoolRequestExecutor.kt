package com.justai.jaicf.channel.jaicp.execution

import com.justai.jaicf.channel.BotChannel
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpMDC
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.processCompatible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class ThreadPoolRequestExecutor(nThreads: Int) : CoroutineScope {

    override val coroutineContext: CoroutineContext =
        Executors.newFixedThreadPool(nThreads).asCoroutineDispatcher()

    fun executeAsync(request: JaicpBotRequest, channel: BotChannel) = async(coroutineContext) {
        execute(request, channel)
    }

    fun executeSync(request: JaicpBotRequest, channel: BotChannel) = runBlocking {
        executeAsync(request, channel).await()
    }

    private fun execute(request: JaicpBotRequest, channel: BotChannel): JaicpBotResponse? {
        JaicpMDC.setFromRequest(request)
        return when (channel) {
            is JaicpNativeBotChannel -> executeNative(channel, request)
            is JaicpCompatibleBotChannel -> executeCompatible(channel, request)
            is JaicpCompatibleAsyncBotChannel -> {
                executeAsync(channel, request); null
            }
            else -> null
        }
    }

    private fun executeNative(channel: JaicpNativeBotChannel, request: JaicpBotRequest) =
        channel.process(request)

    private fun executeCompatible(channel: JaicpCompatibleBotChannel, request: JaicpBotRequest) =
        channel.processCompatible(request)

    private fun executeAsync(channel: JaicpCompatibleAsyncBotChannel, request: JaicpBotRequest) =
        channel.process(request.raw.asHttpBotRequest(request.stringify()))
}