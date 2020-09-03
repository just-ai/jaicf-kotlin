package com.justai.jaicf.logging

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.helpers.logging.WithLogger

/**
 * Main abstraction for class, which will perform dialog logging to any service or console.
 * It supports log obfuscation by [ConversationLogObfuscator] to hide sensitive data.
 *
 * @property logObfuscator implementation
 *
 * @see ConsoleConversationLogger
 * */
interface ConversationLogger {

    /**
     * Implementation of [ConversationLogObfuscator], which will be used to obfuscate sensitive data in request and reactions.
     * */
    val logObfuscator: ConversationLogObfuscator?

    /**
     * Produces log to console or external consumer.
     * */
    fun produce(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    )
}

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
    override val logObfuscator: ConversationLogObfuscator? = null
) : ConversationLogger,
    WithLogger {

    /*
    * Produces log to console.
    * */
    override fun produce(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ) {
        val input = getObfuscatedInput(activationContext, botContext, request, loggingContext)
        val reactions = getObfuscatedReactions(activationContext, botContext, request, loggingContext)
        logger.debug(
            """
            |
            |Processing ${request::class.simpleName} with input "${input}" finished
            |SelectedActivator: ${activationContext?.activator?.name} with state ${activationContext?.activation?.state}
            |Reactions: $reactions""".trimMargin()
        )
    }
}


fun ConversationLogger.getObfuscatedInput(
    activationContext: ActivationContext?,
    botContext: BotContext,
    request: BotRequest,
    loggingContext: LoggingContext
) = logObfuscator?.obfuscateInput(activationContext, botContext, request, loggingContext) ?: request.input

fun ConversationLogger.getObfuscatedReactions(
    activationContext: ActivationContext?,
    botContext: BotContext,
    request: BotRequest,
    loggingContext: LoggingContext
) = logObfuscator
    ?.obfuscateReactions(activationContext, botContext, request, loggingContext)
    ?: loggingContext.reactions

