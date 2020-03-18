package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.jaicp.asJaicpBotRequest
import com.justai.jaicf.channel.jaicp.deserialized
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.create
import com.justai.jaicf.channel.jaicp.reactions.JaicpReactions
import com.justai.jaicf.helpers.logging.WithLogger
import java.time.OffsetDateTime


abstract class JaicpNativeChannel(
    override val botApi: BotApi
) : JaicpNativeBotChannel, WithLogger {

    internal abstract fun createRequest(request: JaicpBotRequest): BotRequest

    internal abstract fun createReactions(): JaicpReactions

    override fun process(input: String): String? {
        val request = input.asJaicpBotRequest()
        return process(request).deserialized()
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
    ) = JaicpBotResponse.create(request, reactions.collect(), processingTime, currentState)
}

