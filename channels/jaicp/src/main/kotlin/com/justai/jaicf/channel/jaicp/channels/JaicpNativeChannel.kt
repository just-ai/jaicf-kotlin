package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.reactions.JaicpReactions
import com.justai.jaicf.helpers.logging.WithLogger
import kotlinx.serialization.json.JsonElement
import java.time.OffsetDateTime


abstract class JaicpNativeChannel(
    override val botApi: BotApi
) : JaicpNativeBotChannel, WithLogger {

    internal abstract fun createRequest(request: JaicpBotRequest): BotRequest

    internal abstract fun createReactions(): JaicpReactions

    override fun process(input: String): String? {
        val request = JSON.parse(JaicpBotRequest.serializer(), input)
        val response = process(request)
        return JSON.stringify(JaicpBotResponse.serializer(), response)
    }

    override fun process(request: JaicpBotRequest): JaicpBotResponse {
        val start = OffsetDateTime.now().toEpochSecond()

        val channelRequest = createRequest(request)
        logger.debug("Processing query: ${request.query} or event: ${request.event}")

        val reactions = createReactions()
        botApi.process(channelRequest, reactions)

        val executionTime = OffsetDateTime.now().toEpochSecond() - start
        return answer(reactions, request, reactions.getCurrentState(), executionTime)
    }


    private fun answer(
        reactions: JaicpReactions,
        request: JaicpBotRequest,
        currentState: String,
        processingTime: Long
    ) = makeResponse(reactions.collect(), request, currentState, processingTime)

    private fun makeResponse(
        response: JsonElement,
        jaicpBotRequest: JaicpBotRequest,
        currentState: String,
        processingTime: Long
    ) = JaicpBotResponse(
        data = response,
        botId = jaicpBotRequest.botId,
        accountId = jaicpBotRequest.botId,
        channelType = jaicpBotRequest.channelType,
        channelBotId = jaicpBotRequest.channelBotId,
        channelUserId = jaicpBotRequest.channelUserId,
        questionId = jaicpBotRequest.questionId,
        query = jaicpBotRequest.query ?: "",
        timestamp = OffsetDateTime.now().toEpochSecond(),
        currentState = currentState,
        processingTime = processingTime
    )
}

