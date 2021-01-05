package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.SwitchReply
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.exceptions.TerminalReactionException
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

val Reactions.jaicpAsync get() = this as? JaicpCompatibleAsyncReactions

/**
 * JAVADOC ME
 * */
fun JaicpCompatibleAsyncReactions.switch(message: String): Nothing = switch(SwitchReply(firstMessage = message))


/**
 * JAVADOC ME
 * Works only if channel connected via JAICP Connector (Webhook or Polling);
 * */
fun JaicpCompatibleAsyncReactions.switch(reply: SwitchReply): Nothing {
    LiveChatInitRequest.create(loggingContext, reply)?.let {
        ChatAdapterConnector.getIfExists()?.initLiveChat(it)
    }
    throw TerminalReactionException
}