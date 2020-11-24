package com.justai.jaicf

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.strict.ButtonActivator
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.*
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.hook.*
import com.justai.jaicf.logging.ConversationLogger
import com.justai.jaicf.logging.LoggingContext
import com.justai.jaicf.logging.Slf4jConversationLogger
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
 *      RegexActivator
 *    )
 *  )
 * ```
 *
 * @param model bot scenario model. Every bot should serve some scenario that implements a business logic of the bot.
 * @param defaultContextManager the default manager that manages a bot's context during the request execution. Can be overriden by the channel itself fot every user's request.
 * @param activators an array of used activator that can handle a request. Note that an order is matter: lower activators won't be called if top-level activator handles a request and a corresponding state is found in scenario.
 * @param activationSelector a selector that is used for selecting the most relevant [ActivationSelector] from all possible.
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
    activators: Array<ActivatorFactory> = emptyArray(),
    private val activationSelector: ActivationSelector = ActivationSelector.default,
    private val slotReactor: SlotReactor? = null,
    private val conversationLoggers: Array<ConversationLogger> = arrayOf(Slf4jConversationLogger())
) : BotApi, WithLogger {

    private val activators = activators.map { it.create(model) }.addBuiltinActivators()

    private fun List<Activator>.addBuiltinActivators(): List<Activator> {
        fun MutableList<Activator>.removeIfPresent(a: Activator) = removeIf { it.name == a.name }
        fun MutableList<Activator>.pushToTheEnd(a: Activator) = find { it.name == a.name } ?: add(a)

        val builtinActivators =
            listOf(BaseEventActivator, BaseIntentActivator, CatchAllActivator).map { it.create(model) }
        val strictActivators = mutableListOf(ButtonActivator).map { it.create(model) }

        return strictActivators + toMutableList().apply {
            builtinActivators.forEach {
                removeIfPresent(it)
                pushToTheEnd(it)
            }
        }
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
        val manager = contextManager ?: defaultContextManager
        val botContext = manager.loadContext(request)
        val loggingContext = LoggingContext(requestContext.httpBotRequest, null, botContext, request)
        reactions.loggingContext = loggingContext
        reactions.botContext = botContext

        processContext(botContext, requestContext)

        withHook(BotRequestHook(botContext, request, reactions)) {
            processRequest(botContext, request, requestContext, reactions, loggingContext)
        }

        botContext.cleanTempData()
        conversationLoggers.forEach { it.obfuscateAndLog(loggingContext) }
        saveContext(manager, botContext, request, reactions)
    }

    private fun processRequest(
        botContext: BotContext,
        request: BotRequest,
        requestContext: RequestContext,
        reactions: Reactions,
        loggingContext: LoggingContext
    ) {
        val slotFillingContext = if (isActiveSlotFilling(botContext)) {
            getSlotFillingContext(botContext)!!
        } else {
            selectActivation(botContext, request)?.let {
                startSlotFilling(botContext, it)
            } ?: run {
                logger.warn("No state selected to handle a request $request")
                return
            }
        }

        with(slotFillingContext) {
            val res = activator.fillSlots(request, reactions, botContext, activatorContext, slotReactor)

            when (res) {
                is SlotFillingInProgress -> return
                is SlotFillingInterrupted -> {
                    cancelSlotFilling(botContext)
                    processRequest(botContext, request, requestContext, reactions, loggingContext)
                }
                is SlotFillingFinished -> {
                    val activation = finishSlotFilling(botContext, res)
                    loggingContext.activationContext = activation
                    processStates(ProcessContext(
                        request,
                        reactions,
                        requestContext,
                        botContext,
                        activation,
                        loggingContext
                    ))
                }
            }
        }
    }

    internal fun getActivatorForName(activatorName: String) = activators.find { it.name == activatorName }

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

    private fun selectActivation(botContext: BotContext, request: BotRequest): ActivationContext? {
        activators.filter { it.canHandle(request) }.forEach { a ->
            val activation = a.activate(botContext, request, activationSelector)
            if (activation != null) {
                return ActivationContext(a, activation)
            }
        }

        return null
    }

    private fun processStates(context: ProcessContext) = with(context) {
        val activator = activationContext.activation.context
        val dc = botContext.dialogContext
        dc.nextState = activationContext.activation.state

        withHook(BeforeProcessHook(botContext, request, reactions, activator)) {
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