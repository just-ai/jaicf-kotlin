package com.justai.jaicf.channel.jaicp.dto.bargein

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BargeInInterruptData(
    val interrupt: Boolean
)

@Serializable
data class BargeInReplyData(
    @SerialName("bargin_transition")
    val bargeInTransition: String,
    val bargeInIntent: BargeInIntentData
) {
    constructor(transition: String) : this(transition, BargeInIntentData(BargeInIntentType.INTENT))
}

@Serializable
data class BargeInIntentData(
    val type: BargeInIntentType
)

@Serializable
enum class BargeInIntentType {
    @SerialName("intent")
    INTENT
}
