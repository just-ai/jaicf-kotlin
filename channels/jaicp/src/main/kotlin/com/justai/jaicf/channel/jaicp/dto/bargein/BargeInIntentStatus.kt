package com.justai.jaicf.channel.jaicp.dto.bargein

import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.channel.jaicp.dto.TelephonyBotRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class BargeInIntentStatus(
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

fun TelephonyBotRequest.getBargeInIntentStatus(): BargeInIntentStatus? =
    jaicp.rawRequest["bargeInIntentStatus"]?.let { JSON.decodeFromJsonElement<BargeInIntentStatus>(it) }

fun TelephonyBotRequest?.isBargeInEvent() =
    this != null && type == BotRequestType.EVENT && input == TelephonyEvents.BARGE_IN_EVENT
