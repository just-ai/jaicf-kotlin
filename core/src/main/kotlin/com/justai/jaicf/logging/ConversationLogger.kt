package com.justai.jaicf.logging

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.reactions.Reaction

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
     *
     * @param activationContext current request's [ActivationContext]
     * @param botContext a current user's [BotContext]
     * @param request a current user's [BotRequest]
     * @param loggingContext current request's [LoggingContext]
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

    /**
     * Produces log to console.
     * @param activationContext current request's [ActivationContext]
     * @param botContext a current user's [BotContext]
     * @param request a current user's [BotRequest]
     * @param loggingContext current request's [LoggingContext]
     * */
    override fun produce(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ) {
        val input = extractObfuscatedInput(activationContext, botContext, request, loggingContext)
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

/**
 * Extracts input in [BotRequest] and obfuscates it.
 *
 * @return obfuscated input from [BotRequest].
 * */
fun ConversationLogger.extractObfuscatedInput(
    activationContext: ActivationContext?,
    botContext: BotContext,
    request: BotRequest,
    loggingContext: LoggingContext
) = logObfuscator?.obfuscateInput(activationContext, botContext, request, loggingContext) ?: request.input


/**
 * Obfuscates content in [Reaction]s.
 *
 * @return list of reactions with obfuscated content (text, imageUrl, or others)
 * */
fun ConversationLogger.getObfuscatedReactions(
    activationContext: ActivationContext?,
    botContext: BotContext,
    request: BotRequest,
    loggingContext: LoggingContext
) = logObfuscator
    ?.obfuscateReactions(activationContext, botContext, request, loggingContext)
    ?: loggingContext.reactions

