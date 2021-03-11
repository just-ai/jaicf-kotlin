package com.justai.jaicf.channel.jaicp.dto.bargein

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("bargeInIntentStatus")
data class BargeInRequest(
    @SerialName("barginTrans")
    val bargeInTransition: BargeInTransition,
    val recognitionResult: RecognitionResult
) {
    @Serializable
    data class BargeInTransition(
        @SerialName("trans")
        val transition: String
    )

    @Serializable
    data class RecognitionResult(
        val text: String,
        val resultType: String
    )
}