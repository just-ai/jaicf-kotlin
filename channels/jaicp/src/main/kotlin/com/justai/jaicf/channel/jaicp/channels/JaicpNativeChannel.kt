package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.asJaicpBotRequest
import com.justai.jaicf.channel.jaicp.deserialized
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.fromRequest
import com.justai.jaicf.channel.jaicp.logging.toEpochMillis
import com.justai.jaicf.channel.jaicp.reactions.JaicpReactions
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.logging.WithLogger
import java.time.OffsetDateTime


abstract class JaicpNativeChannel(
    override val botApi: BotApi
) : JaicpNativeBotChannel, WithLogger {

    internal abstract fun createRequest(request: JaicpBotRequest): BotRequest

    internal abstract fun createReactions(): JaicpReactions

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = request.receiveText().asJaicpBotRequest()
        return process(botRequest).deserialized().asJsonHttpBotResponse()
    }

    override fun process(request: JaicpBotRequest): JaicpBotResponse {
        val start = OffsetDateTime.now().toEpochMillis()

        val channelRequest = createRequest(request)
        logger.debug("Processing query: ${request.query} or event: ${request.event}")

        val reactions = createReactions()
        botApi.process(channelRequest, reactions, RequestContext.DEFAULT)

        val executionTime = OffsetDateTime.now().toEpochMillis() - start
        return answer(reactions, request, reactions.getCurrentState(), executionTime)
    }

    private fun answer(
        reactions: JaicpReactions,
        request: JaicpBotRequest,
        currentState: String,
        processingTime: Long
    ) = JaicpBotResponse.fromRequest(
        jaicpBotRequest = request,
        rawResponse = reactions.collect(),
        processingTime = processingTime,
        currentState = currentState
    )
}

