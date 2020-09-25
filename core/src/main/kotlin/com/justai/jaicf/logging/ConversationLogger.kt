package com.justai.jaicf.logging

/**
 * Main abstraction for class, which will perform dialog logging to any service or console.
 * It supports log obfuscation by [ConversationLogObfuscator] to hide sensitive data.
 *
 * @property logObfuscators implementations of [ConversationLogObfuscator], hiding sensitive data in request and reactions.
 *
 * @see Slf4jConversationLogger
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

    private fun obfuscateInput(loggingContext: LoggingContext) =
        logObfuscators.fold(loggingContext) { context, obfuscator ->
            context.copy(input = obfuscator.obfuscateInput(context))
        }.input


    private fun obfuscateReactions(loggingContext: LoggingContext) =
        logObfuscators.fold(loggingContext) { context, obfuscator ->
            context.copy(reactions = obfuscator.obfuscateReactions(context))
        }.reactions
}

