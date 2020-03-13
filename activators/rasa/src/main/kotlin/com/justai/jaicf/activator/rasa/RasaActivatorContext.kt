package com.justai.jaicf.activator.rasa

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.rasa.api.RasaParseMessageResponse
import com.justai.jaicf.context.ActivatorContext

data class RasaActivatorContext(
    val response: RasaParseMessageResponse
): IntentActivatorContext(
    confidence = response.intent.confidence,
    intent = response.intent.name
) {
    val slots = response.entities.map { it.entity to it }.toMap()
}

val ActivatorContext.rasa
    get() = this as? RasaActivatorContext