package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorChannelConfiguredException
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorsOnlineException
import com.justai.jaicf.logging.Reaction
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import kotlinx.serialization.json.JsonObject

/**
 * Switches to livechat operator if channel is connected to livechat in JAICP App Console.
 *
 * @param message a message sent to operator after switch.
 *
 * @throws NoOperatorsOnlineException when no livechat operators are available
 * @throws NoOperatorChannelConfiguredException when current channel has no livechat configured
 * */
fun JaicpCompatibleAsyncReactions.switchToLiveChat(message: String) =
    switchToLiveChat(LiveChatSwitchReply(firstMessage = message))


/**
 * Switches to livechat operator if channel is connected to livechat in JAICP App Console.
 *
 * @param reply object with all switch parameters.
 *
 * @see LiveChatSwitchReply
 *
 * @throws NoOperatorsOnlineException when no livechat operators are available
 * @throws NoOperatorChannelConfiguredException when current channel has no livechat configured
 * */
fun JaicpCompatibleAsyncReactions.switchToLiveChat(reply: LiveChatSwitchReply): SwitchReaction? {
    val switchRequest = LiveChatInitRequest.create(loggingContext, reply) ?: return null
    val connector = ChatAdapterConnector.getIfExists() ?: return null
    connector.initLiveChat(switchRequest)
    return SwitchReaction.fromReply(switchRequest.switchData, loggingContext.botContext.dialogContext.currentState)
        .also { loggingContext.reactions.add(it) }
}

data class SwitchReaction(
    val firstMessage: String? = null,
    val closeChatPhrases: List<String> = emptyList(),
    val appendCloseChatButton: Boolean = false,
    val ignoreOffline: Boolean = false,
    val oneTimeMessage: Boolean = false,
    val destination: String? = null,
    val lastMessage: String? = null,
    val attributes: JsonObject? = null,
    val hiddenAttributes: JsonObject? = null,
    val sendMessagesToOperator: Boolean = false,
    val sendMessageHistoryAmount: Int? = null,
    override val fromState: String
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
            fromState = state
        )
    }
}