package com.justai.jaicf.test.context

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.ProcessContext
import com.justai.jaicf.reactions.Reactions

/**
 * A [ActionContext] subclass used during the unit test execution.
 * Every action block of the scenario will be executed in this context during the unit test.
 * This class contains some functions that determines a smart random numbers generation and others helpers.
 */
data class TestActionContext<A: ActivatorContext, B: BotRequest, R: Reactions>(
    override val context: BotContext,
    override val activator: A,
    override val request: B,
    override val reactions: R,
    private val requestContext: TestRequestContext
) : ActionContext<A, B, R>(context, activator, request, reactions) {

    internal fun nextRandomInt() = requestContext.randomNumbers.poll() ?: 0
    override fun random(min: Int, max: Int) = min

    /**
     * Finds a named variable passed from test.
     * @param name a name of the variable to find
     * @return variable value or null if nothing was found
     */
    fun getVar(name: String) = requestContext.variables[name]
}

/**
 * Indicates if scenario is running in test mode
 */
fun ActionContext<*, *, *>.isTestMode() = this is TestActionContext

/**
 * Runs this block of code only if the scenario is running in test mode
 * @param block a block of code to be ran in test mode only
 */
fun <A: ActivatorContext, B: BotRequest, R: Reactions, T> ActionContext<A, B, R>.runInTest(
    block: TestActionContext<A, B, R>.() -> T
) = (this as? TestActionContext)?.block()
