package com.justai.jaicf.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.reactions.Reactions
import java.util.*

/**
 * JAVADOC ME
 * */
class BotRoutingEngine(
    private val main: BotEngine,
    routables: Map<String, BotEngine>,
    private val staticRouteSelector: BotRoutingStaticBuilder.() -> String? = { null },
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

    private fun selectStatically(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
        ctx: BotContext,
    ) = BotRoutingStaticBuilder(request, reactions, requestContext, contextManager, routables)
        .staticRouteSelector()?.also {
            ctx.routingContext.staticallySelectedEngine = it
        }

    private fun getDynamicRouting(ctx: BotContext): String? = try {
        ctx.routingContext.routingEngineStack.peek()
    } catch (e: EmptyStackException) {
        null
    }

    private fun getStaticRouting(ctx: BotContext): String? = ctx.routingContext.staticallySelectedEngine

    private fun selectRoute(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
        ctx: BotContext,
    ): RoutingResult {
        getDynamicRouting(ctx)?.let { return RoutingResult(it, false) }
        selectStatically(request, reactions, requestContext, contextManager, ctx)?.let {
            return RoutingResult(it, true)
        }
        getStaticRouting(ctx)?.let { return RoutingResult(it, true) }

        return RoutingResult(DEFAULT_ROUTE_NAME, false)
    }

    private fun processWithRouting(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext,
        contextManager: BotContextManager?,
        ctx: BotContext,
        routingResult: RoutingResult,
    ) {
        var botContext = ctx
        val engineName = routingResult.route
        val isStatic = routingResult.isStatic

        try {
            val curr = botContext.routingContext.routingEngineStack.peek()
            if (curr != engineName) {
                botContext.routingContext.routingEngineStack.push(engineName)
            }
        } catch (e: EmptyStackException) {
            if (!isStatic) {
                botContext.routingContext.routingEngineStack.push(engineName)
            }
        }

        val manager = (contextManager ?: main.defaultContextManager)
        if (botContext.routingContext.currentEngine != engineName) {
            botContext = applyNewDialogContext(botContext, engineName)
            botContext.routingContext.currentEngine = engineName
        }

        if (!isStatic) {
            botContext.routingContext.staticallySelectedEngine = null
        }

        try {
            logger.info("Process route request: $request with routing for engine: $engineName")
            logger.info("DialogContext: ${botContext.dialogContext}")
            logger.info("RoutingContext: ${botContext.routingContext}")
            val engine = routables[engineName] ?: error("No engine with name: $engineName")
            engine.process(request, reactions, requestContext, botContext)
        } catch (e: NewRouteException) {
            processWithRouting(
                request = request,
                reactions = reactions,
                requestContext = requestContext,
                contextManager = contextManager,
                ctx = botContext,
                routingResult = RoutingResult(e.targetEngineName, false)
            )
        } finally {
            manager.saveContext(botContext, request, response = null, requestContext)
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
        val ctx = (contextManager ?: main.defaultContextManager).loadContext(request, requestContext)
        val route = selectRoute(request, reactions, requestContext, contextManager, ctx)
        processWithRouting(request, reactions, requestContext, contextManager, ctx, route)
    }

    private data class RoutingResult(
        val route: String,
        val isStatic: Boolean,
    )

    companion object {
        internal const val DEFAULT_ROUTE_NAME = "main"
        private val MOCK_SCENARIO = Scenario { }
    }
}


