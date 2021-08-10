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
 * @param main main engine to process requests.
 * @param routables a map of engines with names to perform route from and to.
 *
 * @see BotRoutingApi scenario runtime api for routing.
 * */
class BotRoutingEngine(
    main: Pair<String, BotEngine>,
    routables: Map<String, BotEngine>,
) : BotEngine(MOCK_SCENARIO), WithLogger {

    private var routerName = main.first
    private val routerDefaultEngine = main.second
    val routerDescriptor = BotRoutingEngineDescriptor(routerName, routerDefaultEngine, routables.toMutableMap())

    init {
        if (routables.isEmpty()) {
            error("Collection of routable engines must not be empty")
        }
        if (routables.values.distinctBy { it.defaultContextManager }.size > 1) {
            error("Collections of routable botEngines must have single shared context manager instance")
        }
        routables.values.forEach { engine ->
            if (engine is BotRoutingEngine) {
                routerDescriptor.children.add(engine.routerDescriptor)
                engine.routerDescriptor.parent = routerDescriptor
            }
        }
        routerDescriptor.routables[routerName] = routerDefaultEngine
    }

    private fun processWithRouting(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
        botContext0: BotContext,
        routeRequest: RoutingRequest,
        executionContext0: ExecutionContext? = null,
    ) {
        val engineName = routeRequest.engine
        logger.info("Process route request: ${request.input} with routing for engine: $engineName")
        val lastRouter = routeRequest.routeBackTo ?: routerName
        var descriptor = routerDescriptor.getDescriptorRecursiveOrError(lastRouter)

        if (engineName in descriptor.childrenNames) {
            descriptor = routerDescriptor.getChildDescriptorOrError(engineName)
        } else if (engineName == descriptor.parent?.routerName) {
            descriptor = descriptor.getParentDescriptorOrError()
        }

        val engine = descriptor.routables[engineName]
            ?: error("No engine found for key $engineName in router: ${descriptor.routerName}")
        val dialogContext = botContext0.getDialogContext(descriptor.routerName, engineName)
        val botContext = botContext0
            .withCurrentRoutingEngine(descriptor, engineName)
            .withNewDialogContext(dialogContext)


        logger.info("Routing stack: ${botContext.routingContext.routingStack}")
        logger.info("Router: ${botContext.routingContext.currentRouter}")
        logger.info("Engine: ${botContext.routingContext.currentEngine}")
        logger.info("Applied dialogContext: ${botContext.dialogContext}")
        val executionContext = executionContext0 ?: ExecutionContext(requestContext, null, botContext, request)

        try {
            engine.process(request, reactions, requestContext, botContext, executionContext)
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

    override fun process(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
    ) {
        val botContext =
            (contextManager ?: routerDefaultEngine.defaultContextManager).loadContext(request, requestContext)
        val route = getCurrentRouting(botContext) ?: RoutingRequest(routerName, routerName)
        processWithRouting(request, reactions, requestContext, contextManager, botContext, route)
    }

    private fun getCurrentRouting(ctx: BotContext): RoutingRequest? = ctx.routingContext.routingStack.peek()

    companion object {
        private val MOCK_SCENARIO = Scenario { }
    }
}


private fun BotContext.withCurrentRoutingEngine(descriptor: BotRoutingEngineDescriptor, engineName: String) = apply {
    routingContext.currentEngine = engineName
    routingContext.currentRouter = descriptor.routerName
    val routingRecord = RoutingRequest(engineName, descriptor.routerName)
    try {
        val curr: RoutingRequest? = routingContext.routingStack.peek()
        if (curr?.engine != engineName) {
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

data class BotRoutingEngineDescriptor(
    val routerName: String,
    val mainEngine: BotEngine,
    val routables: MutableMap<String, BotEngine> = mutableMapOf(),
    val children: MutableList<BotRoutingEngineDescriptor> = mutableListOf(),
) {
    internal var parent: BotRoutingEngineDescriptor? = null

    internal fun getChildDescriptorOrError(router: String): BotRoutingEngineDescriptor =
        children.find { it.routerName == router } ?: error("No nested router found for key: $router")

    internal fun getDescriptorRecursiveOrError(router: String): BotRoutingEngineDescriptor {
        return getRouterRecursive(router) ?: error("No nested router found for key: $router")
    }

    internal fun getParentDescriptorOrError(): BotRoutingEngineDescriptor =
        parent ?: error("No parent descriptor found for engine ${this.routerName}")

    private fun getRouterRecursive(router: String): BotRoutingEngineDescriptor? {
        if (router == routerName) return this
        children.forEach { child ->
            if (child.routerName == router) return child
            child.getRouterRecursive(router)?.let { return it }
        }
        return null
    }

    val childrenNames get() = children.map { it.routerName }
}

