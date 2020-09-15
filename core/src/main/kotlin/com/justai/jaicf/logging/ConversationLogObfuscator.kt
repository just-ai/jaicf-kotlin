package com.justai.jaicf.logging

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.reactions.Reaction


/**
 * Base interface for logging content obfuscators.
 * Implementations of this interface should be used to hide sensitive data during logging in [ConversationLogger].
 *
 * @see com.justai.jaicf.logging.ConversationLogger
 * @see com.justai.jaicf.logging.ConsoleConversationLogger
 * */
interface ConversationLogObfuscator {

    /**
     * Obfuscates user input from [BotRequest] in [ConversationLogger] implementation.
     *
     * @param loggingContext current request's [LoggingContext]
     *
     * @return obfuscated input
     * */
    fun obfuscateInput(loggingContext: LoggingContext): String

    /**
     * Obfuscates bot reactions in [ConversationLogger] implementation.
     *
     * @param loggingContext current request's [LoggingContext]
     *
     * @return list of obfuscated [Reaction]
     * */
    fun obfuscateReactions(loggingContext: LoggingContext): MutableList<Reaction> = loggingContext.reactions
}
