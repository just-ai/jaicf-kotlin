package com.justai.jaicf.channel.jaicp.livechat.exceptions

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.reactions.switchToLiveChat
import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

/**
 * An exception thrown by [switchToLiveChat] reaction when no operators are available to switch conversation to operator.
 * [LiveChatSwitchReply.ignoreOffline] parameter can be used in switch reply to ignore if operators are offline.
 *
 * @see [JaicpCompatibleAsyncReactions] base interface for jaicp asynchronous reactions
 * @see [LiveChatSwitchReply] full object with livechat switch parameters
 * @see [switchToLiveChat] jaicpAsync reaction
 * */
class NoOperatorsOnlineException(request: JaicpBotRequest) : RuntimeException() {
    override val message: String = "No operators online for channel ${request.channelBotId}"
}

/**
 * An exception thrown by [switchToLiveChat] reaction when livechat is not configured for current channel.
 *
 * @see [JaicpCompatibleAsyncReactions] base interface for jaicp asynchronous reactions
 * @see [switchToLiveChat] jaicpAsync reaction
 * */
class NoOperatorChannelConfiguredException(request: JaicpBotRequest) : RuntimeException() {
    override val message: String = "No operator channel configured for channel ${request.channelBotId}"
}
