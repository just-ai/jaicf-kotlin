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
    constructor(transition: String, type: BargeInType) : this(transition, BargeInIntentData(type))

    companion object {
        val IGNORE = BargeInReplyData(".", BargeInType.IGNORE)
    }
}

@Serializable
data class BargeInIntentData(
    val type: BargeInType
)

@Serializable
enum class BargeInType {
    @SerialName("intent")
    INTENT,

    @SerialName("ignoreBargeIn")
    IGNORE
}
