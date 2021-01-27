package com.justai.jaicf.hook

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.exceptions.BotException
import com.justai.jaicf.exceptions.BotExecutionException
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
interface BotHook

interface BotProcessHook: BotHook {
    val context: BotContext
    val request: BotRequest
    val reactions: Reactions
}

interface BotStatesProcessHook: BotProcessHook {
    val activator: ActivatorContext
}

interface BotActionHook: BotStatesProcessHook {
    val state: State
}

data class BotRequestHook(
    val context: BotContext,
    val request: BotRequest,
    val reactions: Reactions
): BotHook

data class BeforeProcessHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext
): BotStatesProcessHook

data class AfterProcessHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext
): BotStatesProcessHook

data class BeforeActionHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val state: State
): BotActionHook

data class AfterActionHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val state: State
): BotActionHook

data class ActionErrorHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val state: State,
    val exception: BotException
): BotActionHook

data class AnyErrorHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    val exception: BotExecutionException,
): BotProcessHook