package com.justai.jaicf.logging

import com.justai.jaicf.api.BotRequest


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
     * @return list of obfuscated [LoggingReaction]
     * */
    fun obfuscateReactions(loggingContext: LoggingContext): MutableList<LoggingReaction> = loggingContext.reactions
}
