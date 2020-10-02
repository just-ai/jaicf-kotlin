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
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.context.RequestContext
import java.util.*

class AlexaRequestHandler(
    override val botApi: BotApi
): BotChannel, RequestHandler {

    override fun canHandle(input: HandlerInput?) = true

    override fun handle(input: HandlerInput?): Optional<Response> {
        val request = createRequest(input!!) ?: return Optional.empty()
        val response = AlexaBotResponse(input.responseBuilder)
        val httpBotRequest = (input.context as HttpBotRequest)

        botApi.process(
            request,
            AlexaReactions(response, input),
            RequestContext(newSession = input.requestEnvelope.session != null && input.requestEnvelope.session.new, httpBotRequest = httpBotRequest)
        )

        return response.builder.build()
    }

    private fun createRequest(input: HandlerInput): AlexaBotRequest? {
        if (input.request is IntentRequest) {
            return AlexaIntentRequest(input)
        }

        return when (input.request) {
            is LaunchRequest -> AlexaEvent.LAUNCH
            is SessionEndedRequest -> AlexaEvent.SESSION_ENDED
            is PauseCommandIssuedRequest -> AlexaEvent.PAUSE
            is NextCommandIssuedRequest -> AlexaEvent.NEXT
            is PreviousCommandIssuedRequest -> AlexaEvent.PREV
            is PlayCommandIssuedRequest -> AlexaEvent.PLAY
            is APIInvocationRequest -> (input.request as APIInvocationRequest).apiRequest.name

            else -> null
        }?.let {
            AlexaEventRequest(input, it)
        }
    }
}