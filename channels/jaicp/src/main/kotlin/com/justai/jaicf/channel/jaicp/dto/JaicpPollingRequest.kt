package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class JaicpPollingRequest(
    @SerialName("questionId")
    val requestId: String,
    @SerialName("rawRequest")
    val request: JaicpBotRequest
)