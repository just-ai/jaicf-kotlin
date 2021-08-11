package com.justai.jaicf.api.routing.handlers

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.routing.BotRouterDescriptor
import com.justai.jaicf.api.routing.BotRoutingApi
import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.api.routing.RoutingRequest
import com.justai.jaicf.api.routing.handlers.BotRoutingErrorHandler.FailFast
import com.justai.jaicf.api.routing.handlers.BotRoutingErrorHandler.RollbackToMainEngine
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.reactions.Reactions


/**
 * Error handler for [BotRoutingEngine].
 *
 * @see FailFast implementation of [BotRoutingErrorHandler]. It throws errors, therefore user request won't be processed.
 * @see RollbackToMainEngine implementation of [BotRoutingErrorHandler]. It finds last successful [BotEngine] request can be routed to process request.
 *
 * @see BotRoutingEngine
 * @see BotRoutingApi
 * */
abstract class BotRoutingErrorHandler : WithLogger {

    /**
     * Invoked when [BotRoutingEngine] tries to use nested [BotRoutingEngine] with [missingRouterName] and cannot find it.
     *
     * @param missingRouterName name of missing [BotRoutingEngine] request should be processed in
     * @param context current context of routing request
     * */
    abstract fun handleMissingRouter(
        missingRouterName: String,
        context: BotRoutingErrorHandlerContext,
    ): BotRouterDescriptor

    /**
     * Invoked when [BotRoutingEngine] tries to use [BotEngine] with [missingEngineName] and cannot find it.
     *
     * @param missingEngineName name of missing [BotEngine] request should be routed to
     * @param context current context of routing request
     * */
    abstract fun handleMissingEngine(
        missingEngineName: String,
        context: BotRoutingErrorHandlerContext,
    ): BotEngine

    object FailFast : BotRoutingErrorHandler() {
        override fun handleMissingRouter(
            missingRouterName: String,
            context: BotRoutingErrorHandlerContext,
        ): Nothing = error("No router named $missingRouterName found")

        override fun handleMissingEngine(
            missingEngineName: String,
            context: BotRoutingErrorHandlerContext,
        ): Nothing = error("No engine named $missingEngineName found in router ${context.routerDescriptor}")
    }

    object RollbackToMainEngine : BotRoutingErrorHandler() {
        override fun handleMissingRouter(
            missingRouterName: String,
            context: BotRoutingErrorHandlerContext,
        ): BotRouterDescriptor = requireNotNull(context.routerDescriptor).also {
            logger.info("No router found for name $missingRouterName. Rolling back to ${it.routerName}")
        }

        override fun handleMissingEngine(missingEngineName: String, context: BotRoutingErrorHandlerContext): BotEngine =
            requireNotNull(context.routerDescriptor).mainEngine.also {
                logger.info("No engine found for name $missingEngineName. Rolling back to ${context.routerDescriptor?.routerName}")
            }

    }
}

/**
 * Context for error handling in [BotRoutingEngine]
 *
 * @see BotRoutingEngine
 * @see BotRoutingApi
 * */
data class BotRoutingErrorHandlerContext(
    val request: BotRequest,
    val reactions: Reactions,
    val requestContext: RequestContext,
    val contextManager: BotContextManager?,
    val botContext: BotContext,
    val routeRequest: RoutingRequest,
    val executionContext0: ExecutionContext?,
    var routerDescriptor: BotRouterDescriptor?,
)


