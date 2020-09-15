package com.justai.jaicf.logging

import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.reactions.Reaction

/**
 * Main abstraction for class, which will perform dialog logging to any service or console.
 * It supports log obfuscation by [ConversationLogObfuscator] to hide sensitive data.
 *
 * @property logObfuscators implementations of [ConversationLogObfuscator], hiding sensitive data in request and reactions.
 *
 * @see ConsoleConversationLogger
 * */
abstract class ConversationLogger(private val logObfuscators: List<ConversationLogObfuscator>) {
    /**
     * Logs to console or external service.
     *
     * @param loggingContext current request's [LoggingContext] with obfuscated input and reactions
     *
     * @see Reaction
     * @see LoggingContext
     * @see ConversationLogObfuscator
     * */
    abstract fun doLog(loggingContext: LoggingContext)

    internal fun doLogInternal(loggingContext: LoggingContext) = doLog(
        loggingContext.copy(
            input = obfuscateInput(loggingContext),
            reactions = obfuscateReactions(loggingContext)
        )
    )

    private fun obfuscateInput(loggingContext: LoggingContext): String {
        var lc = loggingContext
        logObfuscators.forEach {
            lc = lc.copy(input = it.obfuscateInput(lc))
        }
        return lc.input
    }


    private fun obfuscateReactions(loggingContext: LoggingContext): MutableList<Reaction> {
        var lc = loggingContext
        logObfuscators.forEach {
            lc = lc.copy(reactions = it.obfuscateReactions(lc))
        }
        return lc.reactions
    }
}

