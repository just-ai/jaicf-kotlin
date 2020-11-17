package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Deprecated("Deprecated long poll api. Will be deleted with all references in further releases.")
internal data class JaicpPollingResponse(
    @SerialName("questionId")
    private val requestId: String,
    private val response: JaicpBotResponse
)