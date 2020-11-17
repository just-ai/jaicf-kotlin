package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Deprecated("Deprecated long poll api. Will be deleted with all references in further releases.")
internal data class JaicpPollingRequest(
    @SerialName("questionId")
    val requestId: String,
    @SerialName("rawRequest")
    val request: JaicpBotRequest
)