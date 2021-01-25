package com.justai.jaicf.channel.alexa

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Response
import com.amazon.ask.model.SessionEndedRequest
import com.amazon.ask.model.interfaces.conversations.APIInvocationRequest
import com.amazon.ask.model.interfaces.playbackcontroller.NextCommandIssuedRequest
import com.amazon.ask.model.interfaces.playbackcontroller.PauseCommandIssuedRequest
import com.amazon.ask.model.interfaces.playbackcontroller.PlayCommandIssuedRequest
import com.amazon.ask.model.interfaces.playbackcontroller.PreviousCommandIssuedRequest
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.BotChannel
import com.justai.jaicf.channel.alexa.manager.AlexaBotContextManager
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.context.RequestContext
import java.util.*

class AlexaRequestHandler(
    override val botApi: BotApi,
    useDataStorage: Boolean = false
): BotChannel, RequestHandler {

    private val contextManager = useDataStorage.takeIf { it }?.let { AlexaBotContextManager() }

    override fun canHandle(input: HandlerInput?) = true

    override fun handle(input: HandlerInput?): Optional<Response> {
        if (input == null) return Optional.empty()

        val request = createRequest(input)
        val response = AlexaBotResponse(input.responseBuilder)
        val httpBotRequest = input.context as? HttpBotRequest

        botApi.process(
            request = request,
            reactions = AlexaReactions(response, input),
            contextManager = contextManager,
            requestContext = RequestContext(
                newSession = input.requestEnvelope.session != null && input.requestEnvelope.session.new,
                httpBotRequest = httpBotRequest
            )
        )

        return response.builder.build()
    }

    private fun createRequest(input: HandlerInput) = when (input.request) {
        is IntentRequest -> AlexaIntentRequest(input)
        else -> AlexaEventRequest(input, input.request.type)
    }
}