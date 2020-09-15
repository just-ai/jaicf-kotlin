package com.justai.jaicf.logging

import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.reactions.Reaction

/**
 * Simple implementation of [ConversationLogger]. It logs basic scenario activity information after request is processed.
 *
 * Example console output:
 * ```
 * Processing QueryBotRequest with input "/start" finished
 * SelectedActivator: regexActivator with state /start
 * Reactions: [answer "Hi! Here's some questions I can help you with." from state /start]
 * ```
 *
 * @see [ConversationLogger]
 * @see [ConversationLogObfuscator]
 * */
class ConsoleConversationLogger(
    logObfuscators: List<ConversationLogObfuscator> = listOf()
) : ConversationLogger(logObfuscators),
    WithLogger {

    /**
     * @param loggingContext current request's [LoggingContext] with obfuscated input and reactions
     * */
    override fun doLog(loggingContext: LoggingContext) {
        logger.debug(
            """
            |
            |Processing ${loggingContext.request::class.simpleName} with input "${loggingContext.input}" finished
            |SelectedActivator: ${loggingContext.activationContext?.activator?.name} with state ${loggingContext.activationContext?.activation?.toState}
            |Reactions: ${loggingContext.reactions}""".trimMargin()
        )
    }
}