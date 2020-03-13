package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JaicpPollingResponse(
    @SerialName("questionId")
    private val requestId: String,
    private val response: JaicpBotResponse
)