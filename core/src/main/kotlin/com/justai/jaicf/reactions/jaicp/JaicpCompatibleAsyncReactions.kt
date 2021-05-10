package com.justai.jaicf.reactions.jaicp

import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.reactions.Reactions

/**
 * Base interface for all asynchronous JAICP Compatible channel reactions.
 * Implementations of this interface will contain extensions for asynchronous JAICP Reactions.
 *
 * @see com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
 * @property executionContext current request's [ExecutionContext]
 * @property liveChatProvider to support JAICP live chat integrations.
 * */
interface JaicpCompatibleAsyncReactions {
    val executionContext: ExecutionContext
    val liveChatProvider: JaicpLiveChatProvider?
}

val Reactions.jaicpAsync get() = this as? JaicpCompatibleAsyncReactions