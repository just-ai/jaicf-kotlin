package com.justai.jaicf.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.reactions.Reactions
import java.util.*

/**
 * This class provides a [BotEngine] implementation with BotRouting feature.
 * BotRouting provides runtime functionality to change a [BotEngine] used to process client requests.
 *
 *
 * Scenario usage example:
 * ```
 * private val main = Scenario {
 *  fallback {
 *      if (request.input == "sc1") routing.route("sc1")
 *      else if (request.input == "sc2") routing.route("sc2")
 *      else reactions.say("Hello from MAIN bot!")
 *  }
 * }
 *
 * private val sc1 = Scenario {
 *  fallback {
 *      reactions.say("Hello from SC1")
 *  }
 * }
 *
 * private val sc2 = Scenario {
 *  fallback {
 *      reactions.say("Hello from SC1")
 *  }
 * }
 * private val router = BotRoutingEngine(
 *  main = BotEngine(main),
 *  routables = mapOf("sc1" to BotEngine(sc1), "sc2" to BotEngine(sc2))
 *  )
 * ```
 *
 * See more usage examples at examples/multilingual-bot.
 *
 * @param main main engine to process requests.
 * @param routables a map of engines with names to perform route from and to.
 *
 * @see BotRoutingApi scenario runtime api for routing.
 * */
class BotRoutingEngine(
    private val main: BotEngine,
    routables: Map<String, BotEngine>,
) : BotEngine(MOCK_SCENARIO), WithLogger {

    private val routables: MutableMap<String, BotEngine> = routables.toMutableMap()

    init {
        if (routables.isEmpty()) {
            error("Collection of routable engines must not be empty")
        }
        if (routables.values.distinctBy { it.defaultContextManager }.size > 1) {
            error("Collections of routable botEngines must have single shared context manager instance")
        }

        this.routables[DEFAULT_ROUTE_NAME] = main
    }

    private fun processWithRouting(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
        botContext0: BotContext,
        engineName: String,
        executionContext0: ExecutionContext? = null,
    ) {
        var botContext = botContext0
        val executionContext = executionContext0 ?: ExecutionContext(requestContext, null, botContext, request)
        setCurrentRoutingEngine(botContext, engineName)
        botContext = applyNewDialogContext(botContext, engineName)

        try {
            logger.info("Process route request: ${request.input} with routing for engine: $engineName")
            val engine = routables[engineName] ?: error("No engine with name: $engineName")
            engine.process(request, reactions, requestContext, botContext, executionContext)
        } catch (e: BotRequestRerouteException) {
            botContext.routingContext.dialogContextMap[engineName] = botContext.dialogContext
            processWithRouting(
                request = request,
                reactions = reactions,
                requestContext = requestContext,
                contextManager = contextManager,
                botContext0 = botContext,
                engineName = e.targetEngineName,
                executionContext0 = executionContext
            )
        } finally {
            botContext.routingContext.dialogContextMap[engineName] = botContext.dialogContext
            (contextManager ?: main.defaultContextManager).saveContext(botContext,
                request,
                response = null,
                requestContext)
        }
    }

    private fun setCurrentRoutingEngine(botContext: BotContext, engineName: String) {
        botContext.routingContext.currentEngine = engineName
        try {
            val curr = botContext.routingContext.routingEngineStack.peek()
            if (curr != engineName) {
                botContext.routingContext.routingEngineStack.push(engineName)
            }
        } catch (e: EmptyStackException) {
            botContext.routingContext.routingEngineStack.push(engineName)
        }
    }

    private fun applyNewDialogContext(ctx: BotContext, engineName: String): BotContext =
        ctx.copy(dialogContext = ctx.routingContext.dialogContextMap.getOrDefault(engineName, DialogContext())).apply {
            result = ctx.result
            client.putAll(ctx.client)
            session.putAll(ctx.session)
            temp.putAll(ctx.temp)
        }

    override fun process(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
    ) {
        val botContext = (contextManager ?: main.defaultContextManager).loadContext(request, requestContext)
        val route = getCurrentRouting(botContext) ?: DEFAULT_ROUTE_NAME
        processWithRouting(request, reactions, requestContext, contextManager, botContext, route)
    }

    private fun getCurrentRouting(ctx: BotContext): String? = try {
        ctx.routingContext.routingEngineStack.peek()
    } catch (e: EmptyStackException) {
        null
    }

    companion object {
        internal const val DEFAULT_ROUTE_NAME = "main"
        private val MOCK_SCENARIO = Scenario { }
    }
}