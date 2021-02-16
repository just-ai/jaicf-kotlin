package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInInterruptData
import com.justai.jaicf.channel.jaicp.dto.bargein.SimpleBargeInData
import com.justai.jaicf.channel.jaicp.toJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@SerialName("responseData")
@Serializable
internal class JaicpResponseData private constructor(
    val replies: List<JsonElement>,
    val answer: String,
    val dialer: JaicpDialerData? = null,
    val bargeIn: SimpleBargeInData? = null,
    val bargeInInterrupt: BargeInInterruptData? = null,
    val sessionId: String
) {
    internal constructor(
        replies: List<Reply>,
        dialer: JaicpDialerData?,
        bargeInData: SimpleBargeInData?,
        bargeInInterrupt: BargeInInterruptData?,
        sessionId: String
    ) : this(
        replies = replies.map { it.serialized().toJson() },
        answer = replies.filterIsInstance<TextReply>().joinToString("\n\n") { it.text },
        dialer = dialer,
        bargeIn = bargeInData,
        bargeInInterrupt = bargeInInterrupt,
        sessionId = sessionId
    )
}


