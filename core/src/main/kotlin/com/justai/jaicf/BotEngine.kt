package com.justai.jaicf

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.*
import com.justai.jaicf.context.ProcessContext
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.hook.*
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import java.lang.RuntimeException

/**
 * Default [BotApi] implementation.
 * You can use it passing the [ScenarioModel] of your bot, [BotContextManager] that manages the bot's state data and an array of [ActivatorFactory]. See params description below.
 *
 * Here is an example of usage:
 *
 * ```
 *  val helloWorldBot = BotEngine(
 *    model = HelloWorldScenario.model,
 *    activators = arrayOf(
 *      RegexActivator,
 *      BaseEventActivator,
 *      CatchAllActivator
 *    )
 *  )
 * ```
 *
 * @param model bot scenario model. Every bot should serve some scenario that implements a business logic of the bot.
 * @param contextManager manages a bot's context during the request execution
 * @param activators an array of used activator that can handle a request. Note that an order is matter: lower activators won't be called if top-level activator handles a request and a corresponding state is found in scenario.
 *
 * @see BotApi
 * @see com.justai.jaicf.builder.ScenarioBuilder
 * @see BotContextManager
 * @see BotContext
 * @see ActivatorFactory
 */
class BotEngine(
    val model: ScenarioModel,
    val contextManager: BotContextManager = InMemoryBotContextManager,
    activators: Array<ActivatorFactory>
): BotApi, WithLogger {

    private val activators = activators.map {
            a -> a.create(model)
    }

    /**
     * A [BotHook] handler.
     * You can register your own listener that handles particular phase of the request execution process.
     *
     * @see BotHook
     */
    val hooks = BotHookHandler().also { handler ->
        handler.actions.putAll(model.hooks)
    }

    override fun process(request: BotRequest, reactions: Reactions, requestContext: RequestContext) {
        val botContext = contextManager.loadContext(request.clientId)
        reactions.botContext = botContext

        processContext(botContext, requestContext)

        withHook(BotRequestHook(botContext, request, reactions)) {

            val state = checkStrictTransitions(botContext, request)
            val skippedActivators = mutableListOf<ActivatorContext>()

            val activation = state
                ?.let { Activation(state, StrictActivatorContext()) }
                ?: selectActivation(botContext, request, skippedActivators)

            if (activation == null) {
                logger.warn("No state selected to handle a request $request")
            } else {
                val context = ProcessContext(
                    request,
                    reactions,
                    requestContext,
                    botContext,
                    activation,
                    skippedActivators
                )

                processStates(context)
                contextManager.saveContext(botContext)
            }
        }
    }

    private inline fun withHook(hook: BotHook, block: () -> Unit = {}) {
        try {
            hooks.triggerHook(hook)
            block.invoke()
        } catch (e: BotHookException) {
            logger.error("Hook $hook interrupted a request processing", e)
        }
    }

    private fun processContext(botContext: BotContext, requestContext: RequestContext) {
        if (requestContext.newSession) {
            botContext.cleanSessionData()
        }
    }

    private fun checkStrictTransitions(botContext: BotContext, request: BotRequest): String? {
        return request.hasQuery().takeIf { true }?.let {
            val dc = botContext.dialogContext
            val transition = dc.transitions[request.input.toLowerCase()]
            dc.transitions.clear()
            return transition
        }
    }

    private fun selectActivation(
        botContext: BotContext,
        request: BotRequest,
        skippedActivators: MutableList<ActivatorContext>
    ): Activation? {

        activators.filter { it.canHandle(request) }.forEach { a ->
            val activation = a.activate(botContext, request)
            if (activation != null) {
                if (activation.state != null) {
                    return activation
                } else {
                    skippedActivators.add(activation.context)
                }
            }
        }

        return null
    }

    private fun processStates(context: ProcessContext) = with(context) {
        val activator = activation.context
        val dc = botContext.dialogContext
        dc.nextState = activation.state

        withHook(BeforeProcessHook(context.botContext, request, reactions, activator)) {

            while (dc.nextState() != null) {
                val state = model.states[dc.currentState]

                if (state == null) {
                    logger.warn("No state to execute ${dc.currentState}")
                } else {
                    dc.nextContext(model)

                    withHook(BeforeActionHook(botContext, request, reactions, activator, state)) {

                        if (state.action == null) {
                            logger.warn("No action on state ${dc.currentState}")
                        } else {
                            try {
                                state.action.execute(this)
                                withHook(AfterActionHook(botContext, request, reactions, activator, state))

                            } catch (e: Exception) {
                                hooks.triggerHook(ActionErrorHook(botContext, request, reactions, activator, state, e))
                                throw RuntimeException("Error on state " + dc.currentState, e)
                            }
                        }
                    }
                }
            }

            dc.nextContext(model)

            withHook(AfterProcessHook(botContext, request, reactions, activation.context))
        }
    }
}