package com.justai.jaicf.logging

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.helpers.logging.WithLogger

/**
 * A
 * */
interface ConversationLogger {

    /**
     * JAVADOC */
    val logObfuscator: ConversationLogObfuscator?

    /**
     * JAVADOC ME
     * */
    fun produce(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    )

}


/**
 * JAVADOC ME
 * */
class ConsoleConversationLogger(
    override val logObfuscator: ConversationLogObfuscator? = null
) : ConversationLogger,
    WithLogger {
    override fun produce(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ) {
        val input = logObfuscator
            ?.obfuscateInput(activationContext, botContext, request, loggingContext)
            ?: request.input
        val reactions = logObfuscator
            ?.obfuscateReactions(activationContext, botContext, request, loggingContext)
            ?: loggingContext.reactions

        logger.debug(
            """
            |
            |Processing ${request::class.simpleName} with input "${input}" from channel finished
            |SelectedActivator: ${activationContext?.activator?.name} with state ${activationContext?.activation?.state}
            |Reactions: $reactions""".trimMargin()
        )
    }
}
