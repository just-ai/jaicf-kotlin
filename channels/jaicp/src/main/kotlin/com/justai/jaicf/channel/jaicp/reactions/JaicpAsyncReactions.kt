package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.exceptions.TerminalReactionException
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

/**
 * Switches to livechat operator if channel is connected to livechat in JAICP App Console.
 *
 * @param message a message sent to operator after switch.
 *
 * @throws TerminalReactionException to signal that no further execution is possible, as conversation was switched to livechat.
 * */
fun JaicpCompatibleAsyncReactions.switchToOperator(message: String): Nothing =
    switchToOperator(LiveChatSwitchReply(firstMessage = message))


/**
 * Switches to livechat operator if channel is connected to livechat in JAICP App Console.
 *
 * @param reply object with all switch parameters.
 *
 * @see LiveChatSwitchReply
 *
 * @throws TerminalReactionException to signal that no further execution is possible, as conversation was switched to livechat.
 * */
fun JaicpCompatibleAsyncReactions.switchToOperator(reply: LiveChatSwitchReply): Nothing {
    LiveChatInitRequest.create(loggingContext, reply)?.let {
        ChatAdapterConnector.getIfExists()?.initLiveChat(it)
    }
    throw TerminalReactionException
}