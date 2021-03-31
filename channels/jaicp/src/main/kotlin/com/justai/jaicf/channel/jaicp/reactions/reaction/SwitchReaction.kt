package com.justai.jaicf.channel.jaicp.reactions.reaction

import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.logging.Reaction
import kotlinx.serialization.json.JsonElement

data class SwitchReaction(
    val firstMessage: String? = null,
    val closeChatPhrases: List<String> = emptyList(),
    val appendCloseChatButton: Boolean = false,
    val ignoreOffline: Boolean = false,
    val oneTimeMessage: Boolean = false,
    val destination: String? = null,
    val lastMessage: String? = null,
    val attributes: Map<String, String>? = emptyMap(),
    val hiddenAttributes: Map<String, String>? = emptyMap(),
    val sendMessagesToOperator: Boolean = false,
    val sendMessageHistoryAmount: Int? = null,
    override val fromState: String,
    val customData: JsonElement? = null,
) : Reaction(fromState) {
    companion object {
        fun fromReply(switchReply: LiveChatSwitchReply, state: String) = SwitchReaction(
            firstMessage = switchReply.firstMessage,
            closeChatPhrases = switchReply.closeChatPhrases,
            appendCloseChatButton = switchReply.appendCloseChatButton,
            ignoreOffline = switchReply.ignoreOffline,
            oneTimeMessage = switchReply.oneTimeMessage,
            destination = switchReply.destination,
            lastMessage = switchReply.lastMessage,
            attributes = switchReply.attributes,
            hiddenAttributes = switchReply.hiddenAttributes,
            sendMessageHistoryAmount = switchReply.sendMessageHistoryAmount,
            sendMessagesToOperator = switchReply.sendMessagesToOperator,
            fromState = state,
            customData = switchReply.customData
        )
    }
}