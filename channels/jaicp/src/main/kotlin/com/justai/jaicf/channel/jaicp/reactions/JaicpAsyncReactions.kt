package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import java.lang.IllegalArgumentException
import kotlin.jvm.Throws

/**
 * Switches to livechat operator if channel is connected to livechat in JAICP App Console.
 *
 * @param message a message sent to operator after switch.
 *
 * @throws IllegalArgumentException signal that no further execution is possible, as conversation was switched to livechat.
 * */
@Throws(IllegalArgumentException::class)
fun JaicpCompatibleAsyncReactions.switchToOperator(message: String) =
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
fun JaicpCompatibleAsyncReactions.switchToOperator(reply: LiveChatSwitchReply) =
    LiveChatInitRequest.create(loggingContext, reply)?.let {
        ChatAdapterConnector.getIfExists()?.initLiveChat(it)
    }