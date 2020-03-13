package com.justai.jaicf.channel.alexa.activator

import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.Slot
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

data class AlexaIntentActivatorContext(
    val intentRequest: IntentRequest
): IntentActivatorContext(
    confidence = 1f,
    intent = intentRequest.intent.name
) {
    val slots: Map<String, Slot> = intentRequest.intent.slots
}

val ActivatorContext.alexaIntent
    get() = this as? AlexaIntentActivatorContext