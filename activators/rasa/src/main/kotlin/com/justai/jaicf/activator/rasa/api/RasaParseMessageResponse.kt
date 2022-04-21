package com.justai.jaicf.activator.rasa.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RasaParseMessageResponse(
    val text: String,
    val intent: Intent? = null,
    val entities: List<Entity>? = null,

    @SerialName("intent_ranking")
    val ranking: List<Intent>? = null
)

@Serializable
data class Entity(
    val start: Int,
    val end: Int,
    val confidence: Float? = null,
    val value: String,
    val entity: String
)

@Serializable
data class Intent(
    val confidence: Float,
    val name: String
)