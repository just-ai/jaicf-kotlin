package com.justai.jaicf.activator.rasa.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RasaParseMessageRequest(
    val text: String,

    @SerialName("message_id")
    val messageId: String? = null
)