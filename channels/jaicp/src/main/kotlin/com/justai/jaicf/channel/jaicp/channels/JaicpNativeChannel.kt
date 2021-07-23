package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.asJaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.fromRequest
import com.justai.jaicf.channel.jaicp.reactions.JaicpReactions
import com.justai.jaicf.channel.jaicp.serialized
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.logging.WithLogger
import kotlin.system.measureTimeMillis

abstract class JaicpNativeChannel(
    override val botApi: BotApi,
    private val mutedEvents: List<String> = emptyList()
) : JaicpNativeBotChannel, WithLogger {

    abstract fun createRequest(request: JaicpBotRequest): BotRequest

    abstract fun createReactions(): JaicpReactions

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val botRequest = request.receiveText().asJaicpBotRequest()
        return process(botRequest).serialized().asJsonHttpBotResponse()
    }

    override fun process(request: JaicpBotRequest): JaicpBotResponse {
        val reactions = createReactions()
        val executionTime = measureTimeMillis {
            val channelRequest = createRequest(request)
            if (isMuted(channelRequest))
                return@measureTimeMillis

            logger.debug("Processing query: ${request.query} or event: ${request.event}")
            botApi.process(channelRequest, reactions, RequestContext.DEFAULT)
        }
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

    private fun isMuted(botRequest: BotRequest) =
        botRequest.type == BotRequestType.EVENT && botRequest.input in mutedEvents
}

