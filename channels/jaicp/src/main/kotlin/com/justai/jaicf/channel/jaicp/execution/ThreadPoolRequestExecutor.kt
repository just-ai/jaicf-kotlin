package com.justai.jaicf.channel.jaicp.execution

import com.justai.jaicf.channel.BotChannel
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.*
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.fromRequest
import com.justai.jaicf.channel.jaicp.livechat.LiveChatEventAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
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

    private fun executeAsync(channel: JaicpCompatibleAsyncBotChannel, request: JaicpBotRequest) {
        if (!LiveChatEventAdapter.ensureAsyncLiveChatEvent(channel, request)) {
            channel.process(request.raw.asHttpBotRequest(request.stringify()))
        }
    }
}


private fun JaicpCompatibleBotChannel.processCompatible(
    botRequest: JaicpBotRequest
): JaicpBotResponse {
    val startTime = System.currentTimeMillis()
    val request = botRequest.raw.asHttpBotRequest(botRequest.stringify())
    val response = process(request)?.let { response ->
        val rawJson = JSON.decodeFromString<JsonObject>(response.output.toString())
        addRawReply(rawJson)
    } ?: throw RuntimeException("Failed to process compatible channel request")

    val processingTime = System.currentTimeMillis() - startTime
    return JaicpBotResponse.fromRequest(botRequest, response, processingTime)
}

private fun addRawReply(rawResponse: JsonElement) = buildJsonObject {
    putJsonArray("replies") {
        add(buildJsonObject {
            put("type", "raw")
            put("body", rawResponse)
        })
    }
}
