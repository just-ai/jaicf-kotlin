package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorChannelConfiguredException
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorsOnlineException
import com.justai.jaicf.channel.jaicp.reactions.reaction.SwitchReaction
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

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
    val switchRequest = LiveChatInitRequest.create(executionContext, reply) ?: return null
    val liveChatProvider = liveChatProvider as? ChatAdapterConnector ?: return null
    liveChatProvider.initLiveChat(switchRequest)
    return SwitchReaction.fromReply(switchRequest.switchData, executionContext.botContext.dialogContext.currentState)
        .also { executionContext.reactions.add(it) }
}
