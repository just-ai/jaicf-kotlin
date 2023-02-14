package com.justai.jaicf.channel.jaicp.reactions.reaction

import com.justai.jaicf.channel.jaicp.dto.SmsReply
import com.justai.jaicf.logging.Reaction
import kotlinx.serialization.json.JsonObject

data class SmsReaction(
    val text: String? = null,
    val destination: String? = null,
    val providerData: JsonObject? = null,
    override val fromState: String
) : Reaction(fromState) {
    companion object {
        fun fromReply(smsReply: SmsReply, state: String) = SmsReaction(
            text = smsReply.text,
            destination = smsReply.destination,
            providerData = smsReply.providerData,
            fromState = state
        )
    }
}