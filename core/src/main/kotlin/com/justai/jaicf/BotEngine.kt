package com.justai.jaicf

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.*
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.hook.*
import com.justai.jaicf.logging.Slf4jConversationLogger
import com.justai.jaicf.logging.ConversationLogger
import com.justai.jaicf.logging.LoggingContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions
import com.justai.jaicf.slotfilling.*

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
 * @param defaultContextManager the default manager that manages a bot's context during the request execution. Can be overriden by the channel itself fot every user's request.
 * @param activators an array of used activator that can handle a request. Note that an order is matter: lower activators won't be called if top-level activator handles a request and a corresponding state is found in scenario.
 * @param slotReactor an entity to react to filling specified slot.
 * @param conversationLoggers an array conversation loggers, all of which will log conversation information after request is processed.
 *
 * @see BotApi
 * @see com.justai.jaicf.builder.ScenarioBuilder
 * @see BotContextManager
 * @see BotContext
 * @see ActivatorFactory
 * @see SlotReactor
 * @see ConversationLogger
 */
class BotEngine(
    val model: ScenarioModel,
    val defaultContextManager: BotContextManager = InMemoryBotContextManager,
    activators: Array<ActivatorFactory>,
    private val slotReactor: SlotReactor? = null,
    private val conversationLoggers: Array<ConversationLogger> = arrayOf(Slf4jConversationLogger())
) : BotApi, WithLogger {

    private val activators = activators.map { a ->
        a.create(model)
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

    override fun process(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?
    ) {
        val cm = contextManager ?: defaultContextManager
        val botContext = cm.loadContext(request)
        val loggingContext = LoggingContext(requestContext.httpBotRequest, null, botContext, request)
        reactions.loggingContext = loggingContext
        reactions.botContext = botContext

        processContext(botContext, requestContext)

        withHook(BotRequestHook(botContext, request, reactions)) {
            val state = checkStrictTransitions(botContext, request)
            val skippedActivators = mutableListOf<ActivatorContext>()

            var activation: ActivationContext? = null
            if (!botContext.isActiveSlotFilling()) {
                activation = state
                    ?.let { ActivationContext(null, Activation(state, StrictActivatorContext())) }
                    ?: selectActivation(botContext, request, skippedActivators)
            }
            activation = fillSlots(activation, botContext, request, reactions, cm, state, skippedActivators).apply {
                if (first) return
            }.second
            loggingContext.activationContext = activation

            if (activation?.activation == null) {
                logger.warn("No state selected to handle a request $request")
            } else {
                val context = ProcessContext(
                    request,
                    reactions,
                    requestContext,
                    botContext,
                    activation,
                    skippedActivators,
                    loggingContext
                )

                processStates(context)
                saveContext(cm, botContext, request, reactions)

            }
            conversationLoggers.forEach { it.obfuscateAndLog(loggingContext) }
            botContext.cleanTempData()
        }
    }

    private fun fillSlots(
        selectedActivator: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        reactions: Reactions,
        cm: BotContextManager,
        state: String?,
        skippedActivators: MutableList<ActivatorContext>
    ): Pair<Boolean, ActivationContext?> {
        val slotFillingActivatorName = botContext.getSlotFillingActivator()
        val isSlotFillingSession = slotFillingActivatorName != null
        var activationContext = selectedActivator
        var shouldReturn = false

        val res = when (val a = activationContext?.activator ?: getActivatorForName(slotFillingActivatorName)) {
            null -> SlotFillingSkipped
            else -> a.fillSlots(
                request,
                reactions,
                botContext,
                activationContext?.activation?.context,
                slotReactor
            )
        }
        if (res is SlotFillingInProgress) {
            if (!isSlotFillingSession) {
                botContext.startSlotFilling(activationContext?.activator?.name)
                botContext.dialogContext.nextState = activationContext?.activation?.state
                saveContext(cm, botContext, request, reactions)
            }
            saveContext(cm, botContext, request, reactions)
            conversationLoggers.forEach { it.obfuscateAndLog(reactions.loggingContext) }
            shouldReturn = true
        }
        if (res is SlotFillingFinished) {
            botContext.finishSlotFilling()
            activationContext = ActivationContext(
                activator = getActivatorForName(slotFillingActivatorName),
                activation = Activation(
                    botContext.dialogContext.nextState,
                    res.activatorContext
                )
            )
        }
        if (res is SlotFillingInterrupted) {
            botContext.finishSlotFilling()
            activationContext = state
                ?.let { ActivationContext(null, Activation(
                    state,
                    StrictActivatorContext()
                )) }
                ?: selectActivation(botContext, request, skippedActivators)
        }
        return shouldReturn to activationContext
    }

    private fun getActivatorForName(activatorName: String?): Activator? {
        activatorName ?: return null
        return activators.find { it.name == activatorName }
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
        if (!request.hasQuery()) {
            return null
        }

        val dc = botContext.dialogContext
        val transition = dc.transitions[request.input.toLowerCase()]
        dc.transitions.clear()
        return transition
    }

    private fun selectActivation(
        botContext: BotContext,
        request: BotRequest,
        skippedActivators: MutableList<ActivatorContext>
    ): ActivationContext? {

        activators.filter { it.canHandle(request) }.forEach { a ->
            val activation = a.activate(botContext, request)
            if (activation != null) {
                if (activation.state != null) {
                    return ActivationContext(a, activation)
                } else {
                    skippedActivators.add(activation.context)
                }
            }
        }

        return null
    }

    private fun processStates(context: ProcessContext) = with(context) {
        val activator = activationContext.activation.context
        val dc = botContext.dialogContext
        dc.nextState = activationContext.activation.state

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
                                logger.trace("Executing state: $state")
                                state.action.execute(this)
                                withHook(AfterActionHook(botContext, request, reactions, activator, state))
                            } catch (e: Exception) {
                                logger.error("Action exception on state ${dc.currentState}", e)
                                hooks.triggerHook(ActionErrorHook(botContext, request, reactions, activator, state, e))
                            }
                        }
                    }
                }
            }

            dc.nextContext(model)
            withHook(AfterProcessHook(botContext, request, reactions, activationContext.activation.context))
        }
    }

    private fun saveContext(
        cm: BotContextManager,
        botContext: BotContext,
        request: BotRequest,
        reactions: Reactions
    ) {
        cm.saveContext(
            botContext, request, when (reactions) {
                is ResponseReactions<*> -> reactions.response
                else -> null
            }
        )
    }
}