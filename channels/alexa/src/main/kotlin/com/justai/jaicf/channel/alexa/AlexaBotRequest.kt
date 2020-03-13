package com.justai.jaicf.channel.alexa

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.IntentRequest
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.IntentBotRequest

val BotRequest.alexa
    get() = this as? AlexaBotRequest

interface AlexaBotRequest: BotRequest {
    val handlerInput: HandlerInput
}

data class AlexaIntentRequest(
    override val handlerInput: HandlerInput
): AlexaBotRequest, IntentBotRequest(
    clientId = handlerInput.requestEnvelope.context.system.user.userId,
    input = (handlerInput.request as IntentRequest).intent.name
) {
    val intentRequest: IntentRequest = handlerInput.request as IntentRequest
}

data class AlexaEventRequest(
    override val handlerInput: HandlerInput,
    val event: String
): AlexaBotRequest, EventBotRequest(
    clientId = handlerInput.requestEnvelope.context.system.user.userId,
    input = event
)