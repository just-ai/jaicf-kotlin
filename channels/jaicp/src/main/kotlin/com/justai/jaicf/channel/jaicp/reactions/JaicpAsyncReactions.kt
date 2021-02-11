package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorChannelConfiguredException
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorsOnlineException
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

/**
 * Switches to livechat operator if channel is connected to livechat in JAICP App Console.
 *
 * @param message a message sent to operator after switch.
 *
 * @throws NoOperatorsOnlineException when no livechat operators are available
 * @throws NoOperatorChannelConfiguredException when current channel has no livechat configured
 * */
@Throws(NoOperatorsOnlineException::class, NoOperatorChannelConfiguredException::class)
fun JaicpCompatibleAsyncReactions.switchToOperator(message: String) =
    switchToOperator(LiveChatSwitchReply(firstMessage = message))


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
@Throws(NoOperatorsOnlineException::class, NoOperatorChannelConfiguredException::class)
fun JaicpCompatibleAsyncReactions.switchToOperator(reply: LiveChatSwitchReply) =
    LiveChatInitRequest.create(loggingContext, reply)?.let {
        ChatAdapterConnector.getIfExists()?.initLiveChat(it)
    }