package com.justai.jaicf.activator.rasa

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.rasa.api.Entity
import com.justai.jaicf.activator.rasa.api.Intent
import com.justai.jaicf.context.ActivatorContext
import kotlinx.serialization.json.JsonObject

data class RasaActivatorContext(
    val rasaIntent: Intent,
    val entities: List<Entity>,
    val rawResponse: JsonObject
): IntentActivatorContext(
    confidence = rasaIntent.confidence,
    intent = rasaIntent.name
) {
    val slots = entities.map { it.entity to it }.toMap()
}

val ActivatorContext.rasa
    get() = this as? RasaActivatorContext