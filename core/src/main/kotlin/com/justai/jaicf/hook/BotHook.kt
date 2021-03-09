package com.justai.jaicf.hook

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.MutableBotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.exceptions.BotException
import com.justai.jaicf.exceptions.BotExecutionException
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.state.State
import com.justai.jaicf.reactions.Reactions

/**
 * A base interface for every bot hook.
 * Bot hook represents a particular phase of the user's request processing.
 * You can handle the hook by registering a listener via handle method. For example:
 *
 * ```
 * object HelloWorldScenario: Scenario() {
 *   init {
 *     handle<BotRequestHook> {
 *       println("HelloWorldScenario - on request")
 *     }
 *   }
 * }
 * ```
 *
 * It's useful if you need to log, append some data or transparently change the behaviour of the dialogue in some cases.
 */
interface BotHook {
    val context: BotContext
}

interface BotPreProcessHook : BotHook, WithLogger {
    override val context: BotContext
    val request: BotRequest
    val reactions: Reactions

    fun setRequestInput(input: String): Unit = (request as? MutableBotRequest)?.setInput(input)
        ?: logger.trace("Request ${request::class.simpleName} does not inherit MutableBotRequest, therefore input setters are unavailable")
}

interface BotProcessHook: BotHook {
    override val context: BotContext
    val request: BotRequest
    val reactions: Reactions
    val activator: ActivatorContext
}

interface BotActionHook : BotProcessHook {
    val state: State
}

data class BotRequestHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions
) : BotPreProcessHook

data class BeforeProcessHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext
) : BotProcessHook

data class AfterProcessHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext
) : BotProcessHook

data class BeforeActionHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val state: State
) : BotActionHook

data class AfterActionHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val state: State
) : BotActionHook

data class ActionErrorHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val state: State,
    val exception: BotExecutionException
) : BotActionHook

data class BeforeActivationHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions
) : BotPreProcessHook, WithLogger

data class AnyErrorHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    val exception: BotException,
) : BotPreProcessHook