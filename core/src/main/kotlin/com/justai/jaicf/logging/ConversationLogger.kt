package com.justai.jaicf.logging

import com.justai.jaicf.context.ExecutionContext

/**
 * Main abstraction for class, which will perform dialog logging to any service or console.
 * It supports log obfuscation by [ConversationLogObfuscator] to hide sensitive data.
 *
 * @property logObfuscators implementations of [ConversationLogObfuscator], hiding sensitive data in request and reactions.
 *
 * @see Slf4jConversationLogger
 * */
abstract class ConversationLogger(
    private val logObfuscators: List<ConversationLogObfuscator> = emptyList()
) {
    /**
     * Logs to console or external service.
     *
     * @param executionContext current request's [ExecutionContext] with obfuscated input and reactions
     *
     * @see Reaction
     * @see ExecutionContext
     * @see ConversationLogObfuscator
     * */
    abstract fun doLog(executionContext: ExecutionContext)

    internal fun obfuscateAndLog(executionContext: ExecutionContext) = doLog(
        executionContext.copy(
            input = obfuscateInput(executionContext),
            reactions = obfuscateReactions(executionContext)
        )
    )

    private fun obfuscateInput(executionContext: ExecutionContext) =
        logObfuscators.fold(executionContext) { context, obfuscator ->
            context.copy(input = obfuscator.obfuscateInput(context))
        }.input


    private fun obfuscateReactions(executionContext: ExecutionContext) =
        logObfuscators.fold(executionContext) { context, obfuscator ->
            context.copy(reactions = obfuscator.obfuscateReactions(context))
        }.reactions
}

