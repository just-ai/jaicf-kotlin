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
 *  main = "default-router" to BotEngine(main),
 *  routables = mapOf("sc1" to BotEngine(sc1), "sc2" to BotEngine(sc2))
 *  )
 * ```
 *
 * See more usage examples at examples/multilingual-bot.
 *
 * @param main a [BotEngine] implementation with name, this name will be used as this router's name
 * @param routables a map of engines with names to perform route from and to.][]
 *
 * @see BotRoutingApi scenario runtime api for routing.
 * */
class BotRoutingEngine(
    main: Pair<String, BotEngine>,
    routables: Map<String, BotEngine>,
) : BotEngine(MOCK_SCENARIO), WithLogger {

    val routerName = main.first
    val routerDefaultEngine = main.second
    internal val routables: MutableMap<String, BotEngine> = routables.toMutableMap()
    internal val children: MutableList<BotRoutingEngine> = mutableListOf()
    internal var parent: BotRoutingEngine? = null

    init {
        if (routables.isEmpty()) {
            error("Collection of routable engines must not be empty")
        }

        val routablesContextManagers = routables.values.distinctBy { it.defaultContextManager }
        if (routablesContextManagers.size > 1 || main.second.defaultContextManager != routablesContextManagers.first().defaultContextManager) {
            error("Collections of routable botEngines and main routing engine must have single shared context manager instance")
        }

        routables.values.forEach { engine ->
            if (engine is BotRoutingEngine) {
                children.add(engine)
                engine.parent = this
            }
        }
        this.routables[routerName] = routerDefaultEngine
    }

    private suspend fun processWithRouting(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
        botContext0: BotContext,
        routeRequest: RoutingRequest,
        executionContext0: ExecutionContext? = null,
    ) {

        val engineName = routeRequest.toEngine
        val lastRouter = routeRequest.fromRouter ?: routerName
        logger.info("Process route request: ${request.input} with routing for engine: $engineName and router: $lastRouter")

        var router = getRouterRecursive(lastRouter) ?: this
        if (engineName in router.childrenNames) {
            router = router.children.first { it.routerName == engineName }
        } else if (engineName == router.parent?.routerName) {
            router = router.parent!!
        }

        val engine = router.routables[engineName] ?: router.routerDefaultEngine
        val dialogContext = botContext0.getDialogContext(router.routerName, engineName)
        val botContext = botContext0
            .withCurrentRoutingEngine(router, engineName)
            .withNewDialogContext(dialogContext)

        val executionContext = executionContext0 
            ?: ExecutionContext(requestContext, null, botContext, request)

        try {
            engine.processRequest(request, reactions, requestContext, botContext, executionContext)
        } catch (e: BotRequestRerouteException) {
            botContext.saveDialogContext(lastRouter, engineName)
            processWithRouting(
                request = request,
                reactions = reactions,
                requestContext = requestContext,
                contextManager = contextManager,
                botContext0 = botContext,
                routeRequest = e.rerouteRequest,
                executionContext0 = executionContext
            )
        } finally {
            botContext.saveDialogContext(lastRouter, engineName)
            (contextManager ?: routerDefaultEngine.defaultContextManager).saveContext(
                botContext, request, response = null, requestContext)
        }
    }

    override suspend fun handle(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?
    ) {
        val botContext =
            (contextManager ?: routerDefaultEngine.defaultContextManager).loadContext(request, requestContext)
        val route = getCurrentRouting(botContext) ?: RoutingRequest(routerName, routerName)
        processWithRouting(request, reactions, requestContext, contextManager,botContext, route)
    }

    private fun getCurrentRouting(ctx: BotContext): RoutingRequest? = ctx.routingContext.routingStack.peek()

    private fun getRouterRecursive(router: String): BotRoutingEngine? {
        if (router == routerName) return this
        children.forEach { child ->
            child.getRouterRecursive(router)?.let { return it }
        }
        return null
    }

    private val childrenNames get() = children.map { it.routerName }

    companion object {
        private val MOCK_SCENARIO = Scenario { }
    }
}


private fun BotContext.withCurrentRoutingEngine(router: BotRoutingEngine, engineName: String) = apply {
    routingContext.currentEngine = engineName
    routingContext.currentRouter = router.routerName
    val routingRecord = RoutingRequest(engineName, router.routerName)
    try {
        val curr: RoutingRequest? = routingContext.routingStack.peek()
        if (curr?.toEngine != engineName) {
            routingContext.routingStack.push(routingRecord)
        }
    } catch (e: EmptyStackException) {
        routingContext.routingStack.push(routingRecord)
    }
}

private fun BotContext.withNewDialogContext(dialogContext: DialogContext): BotContext =
    copy(dialogContext = dialogContext).apply {
        result = this@withNewDialogContext.result
        client.putAll(this@withNewDialogContext.client)
        session.putAll(this@withNewDialogContext.session)
        temp.putAll(this@withNewDialogContext.temp)
    }

private fun BotContext.getDialogContext(router: String, engine: String): DialogContext =
    routingContext.dialogContextMap.getOrDefault("$router-$engine", DialogContext())

private fun BotContext.saveDialogContext(router: String, engine: String) {
    routingContext.dialogContextMap["$router-$engine"] = dialogContext
}
