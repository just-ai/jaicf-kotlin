package com.justai.jaicf.channel.jaicp.execution

import com.justai.jaicf.channel.BotChannel
import com.justai.jaicf.channel.http.ContentType
import com.justai.jaicf.channel.http.HttpStatusCode
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationEventRequest
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpMDC
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponseWithStatus
import com.justai.jaicf.channel.jaicp.dto.fromRequest
import com.justai.jaicf.channel.jaicp.dto.withStatus
import com.justai.jaicf.channel.jaicp.invocationapi.InvocationRequestData
import com.justai.jaicf.context.RequestContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
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

    private fun execute(
        request: JaicpBotRequest,
        channel: BotChannel
    ): JaicpBotResponseWithStatus {
        JaicpMDC.setFromRequest(request)
        return when (channel) {
            is JaicpNativeBotChannel -> executeNative(channel, request)
            is JaicpCompatibleBotChannel -> executeCompatible(channel, request)
            is JaicpCompatibleAsyncBotChannel -> executeAsync(channel, request)
            else -> error("Not supported channel type ${channel.javaClass}")
        }
    }

    private fun executeNative(channel: JaicpNativeBotChannel, request: JaicpBotRequest) =
        channel.process(request)

    private fun executeCompatible(channel: JaicpCompatibleBotChannel, request: JaicpBotRequest) =
        channel.processCompatible(request)

    private fun executeAsync(
        channel: JaicpCompatibleAsyncBotChannel,
        request: JaicpBotRequest
    ): JaicpBotResponseWithStatus {
        if (tryProcessAsExternalInvocation(channel, request)) {
            return JaicpBotResponseWithStatus(null, statusCode = HttpStatusCode.ACCEPTED)
        }
        val httpBotResponse = channel.process(request.asHttpBotRequest())

        val response = if (httpBotResponse.contentType == ContentType.PLAIN_TEXT) {
            JsonNull
        } else {
            val rawJson = JSON.decodeFromString<JsonObject>(httpBotResponse.output.toString())
            addRawReply(rawJson)
        }

        return JaicpBotResponse.fromRequest(request, response)
            .withStatus(httpBotResponse.statusCode, httpBotResponse.output.toString())
    }
}

private fun JaicpCompatibleBotChannel.processCompatible(
    botRequest: JaicpBotRequest
): JaicpBotResponseWithStatus {
    val startTime = System.currentTimeMillis()
    val request = botRequest.asHttpBotRequest()
    val httpBotResponse = process(request)

    val processingTime = System.currentTimeMillis() - startTime

    val response = if (httpBotResponse.contentType == ContentType.PLAIN_TEXT) {
        JsonNull
    } else {
        val rawJson = JSON.decodeFromString<JsonObject>(httpBotResponse.output.toString())
        addRawReply(rawJson)
    }

    return JaicpBotResponse.fromRequest(botRequest, response, processingTime)
        .withStatus(httpBotResponse.statusCode, httpBotResponse.output.toString())
}

private fun addRawReply(rawResponse: JsonElement) = buildJsonObject {
    putJsonArray("replies") {
        add(buildJsonObject {
            put("type", "raw")
            put("body", rawResponse)
        })
    }
}

private fun tryProcessAsExternalInvocation(
    channel: JaicpCompatibleAsyncBotChannel,
    request: JaicpBotRequest
): Boolean {
    if (channel !is InvocableBotChannel) return false
    if (!request.isExternalInvocationRequest()) return false
    val event = request.event ?: return false
    val data = try {
        JSON.decodeFromString(InvocationRequestData.serializer(), request.raw)
    } catch (e: Exception) {
        return false
    }

    channel.processInvocation(
        request = InvocationEventRequest(data.chatId, event, request.raw),
        requestContext = RequestContext.fromHttp(request.asHttpBotRequest())
    )
    return true
}
