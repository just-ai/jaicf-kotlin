package com.justai.jaicf.channel.jaicp.dto.bargein

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BargeInResponse(
    val interrupt: Boolean
)

@Serializable
data class BargeInReplyData(
    @SerialName("bargin_transition")
    val bargeInTransition: String,
    val bargeInIntent: BargeInIntentData
) {
    constructor(transition: String) : this(transition, BargeInIntentData(BargeInType.INTENT))
}

@Serializable
data class BargeInIntentData(
    val type: BargeInType
)

@Serializable
enum class BargeInType {
    @SerialName("intent")
    INTENT
}
