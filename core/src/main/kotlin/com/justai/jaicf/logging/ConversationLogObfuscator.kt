package com.justai.jaicf.logging

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ExecutionContext


/**
 * Base interface for logging content obfuscators.
 * Implementations of this interface should be used to hide sensitive data during logging in [ConversationLogger].
 *
 * @see com.justai.jaicf.logging.ConversationLogger
 * @see com.justai.jaicf.logging.Slf4jConversationLogger
 * */
interface ConversationLogObfuscator {

    /**
     * Obfuscates user input from [BotRequest] in [ConversationLogger] implementation.
     *
     * @param executionContext current request's [ExecutionContext]
     *
     * @return obfuscated input
     * */
    fun obfuscateInput(executionContext: ExecutionContext): String

    /**
     * Obfuscates bot reactions in [ConversationLogger] implementation.
     *
     * @param executionContext current request's [ExecutionContext]
     *
     * @return list of obfuscated [Reaction]
     * */
    fun obfuscateReactions(executionContext: ExecutionContext): MutableList<Reaction> = executionContext.reactions
}
