package com.justai.jaicf.channel.jaicp.reactions.reaction

import com.justai.jaicf.channel.jaicp.dto.ProviderConfig
import com.justai.jaicf.channel.jaicp.dto.SmsReply
import com.justai.jaicf.logging.Reaction

data class SmsReaction(
    val text: String? = null,
    val phoneNumber: String? = null,
    val provider: ProviderConfig? = null,
    override val fromState: String
) : Reaction(fromState) {
    companion object {
        fun fromReply(smsReply: SmsReply, state: String) = SmsReaction(
            text = smsReply.text,
            phoneNumber = smsReply.phoneNumber,
            provider = smsReply.provider,
            fromState = state
        )
    }
}