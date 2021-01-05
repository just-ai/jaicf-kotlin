package com.justai.jaicf.reactions.jaicp

import com.justai.jaicf.logging.LoggingContext
import com.justai.jaicf.reactions.Reactions

/**
 * Base interface for all asynchronous JAICP Compatible channel reactions.
 * Implementations of this interface will contain extensions for asynchronous JAICP Reactions.
 *
 * @see com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
 * */
interface JaicpCompatibleAsyncReactions {
    val loggingContext: LoggingContext
}

val Reactions.jaicpAsync get() = this as? JaicpCompatibleAsyncReactions