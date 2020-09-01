package com.justai.jaicf.logging

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.reactions.Reaction


/**
 * Base interface for logging content obfuscators.
 * Implementations of this interface should be used to hide sensitive data during logging in [ConversationLogger].
 *
 * @see com.justai.jaicf.logging.ConversationLogger
 * */
interface ConversationLogObfuscator {

    /**
     * Obfuscates user input from [BotRequest] in [ConversationLogger] implementation.
     *
     * @return obfuscated input
     * */
    fun obfuscateInput(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ): String

    /**
     * Obfuscates bot reactions in [ConversationLogger] implementation.
     *
     * @return list of obfuscated [Reaction]
     * */
    fun obfuscateReactions(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ): MutableList<Reaction>
}
