package com.justai.jaicf.logging

import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.helpers.logging.WithLogger

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
class Slf4jConversationLogger(
    logObfuscators: List<ConversationLogObfuscator> = emptyList()
) : ConversationLogger(logObfuscators),
    WithLogger {

    /**
     * @param executionContext current request's [ExecutionContext] with obfuscated input and reactions
     * */
    override fun doLog(executionContext: ExecutionContext) {
        val replies = executionContext.reactions.joinToString(separator = "\n\t\t", prefix = "\n\t\t") {
            it.toString().replace("\n", "\\n")
        }
        logger.info(
            """
            |
            |  Processing ${executionContext.request::class.simpleName} with input "${executionContext.input}"
            |  SelectedActivator: ${executionContext.activationContext?.activator?.name} with state ${executionContext.activationContext?.activation?.state}
            |  Reactions: $replies""".trimMargin()
        )
    }
}