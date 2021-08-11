package com.justai.jaicf.api.routing

import com.justai.jaicf.api.routing.BotRoutingContext.*
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.state.StatePath
import java.util.*

/**
 * An exception thrown to re-route current BotRequest to specified engine via [rerouteRequest]
 *
 * @param [rerouteRequest] a request with specified engine in [BotRoutingEngine] routables map.
 * */
data class BotRequestRerouteException(val rerouteRequest: RoutingRequest) : RuntimeException()

/**
 * An exception thrown when there is no previous engine to route back.
 * */
class NoRouteBackException : RuntimeException()

/**
 * This class provides BotRouting API for changing execution engine for client requests in channel.
 * Must be used with [BotRoutingEngine] with defined routables.
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
 * see more usage examples at examples/multilingual-bot
 * */
class BotRoutingApi(internal val botContext: BotContext) : WithLogger {

    /**
     * Route current bot request to specified [engineName]. Next requests will be also send to [engineName].
     *
     * @param engineName target engine name specified in routables map in [BotRoutingEngine.routables]
     * @param targetState target state for scenario in [engineName].
     *
     * @throws BotRequestRerouteException to reroute request using [BotRoutingEngine]
     * @return [Nothing] as no request execution possible after invoking this method
     *
     * @see BotRoutingEngine
     * */
    fun route(engineName: String, targetState: String? = null): Nothing {
        changeEngine(engineName, targetState)
        throw BotRequestRerouteException(RoutingRequest(engineName, botContext.routingContext.currentRouter))
    }

    /**
     * Route current bot request back to previous bot engine
     *
     * @throws BotRequestRerouteException to reroute request using [BotRoutingEngine]
     * @return [Nothing] as no request execution possible after invoking this method
     *
     * @see BotRoutingEngine
     * */
    fun routeBack(): Nothing {
        val routingContext = botContext.routingContext
        try {
            val curr = routingContext.routingStack.pop()
            val target = routingContext.routingStack.pop()
            logger.info("Routing request back from engine: $curr to engine: $target")
            throw BotRequestRerouteException(target)
        } catch (e: NoSuchElementException) {
            logger.warn("Failed to change route back as there is no engines left in stack")
            routingContext.routingStack.push(RoutingRequest(requireNotNull(routingContext.currentEngine), routingContext.currentRouter))
            throw NoRouteBackException()
        }
    }

    /**
     * Route client all next requests to specified [engineName].
     *
     * @param engineName target engine name specified in routables map in [BotRoutingEngine]
     * @param targetState target state for scenario in [engineName].
     *
     * @see BotRoutingEngine
     * */
    fun changeEngine(engineName: String, targetState: String? = null) {
        targetState?.let {
            val dialogContext = botContext.routingContext.dialogContextMap.getOrDefault(engineName, DialogContext())
            val currentState = StatePath.parse(dialogContext.currentState)
            val resolved = currentState.resolve(targetState).toString()
            botContext.routingContext.targetState = resolved
        }

        botContext.routingContext.routingStack.push(RoutingRequest(engineName, botContext.routingContext.currentRouter))
    }

    /**
     * Route client next requests back to previous bot engine
     *
     * @see BotRoutingEngine
     * */
    fun changeEngineBack() {
        val routingContext = botContext.routingContext
        val curr = routingContext.routingStack.pop()
        val target = routingContext.routingStack.peek()
        if (target == null) {
            routingContext.routingStack.push(curr)
            logger.warn("Failed to change bot engine back as there is no engines left in stack")
            throw NoRouteBackException()
        } else {
            logger.info("Changing execution back from engine: $curr to engine: $target")
        }
    }

    /**
     * Checks if there is an engine in stack we can route to
     * */
    fun hasPreviousEngineInStack(): Boolean = botContext.routingContext.routingStack.size > 1

    /**
     * Cleans routing context. Deletes dialog contexts for other bots visited by client and removes engines from stack.
     * */
    fun cleanRoutingContext() {
        botContext.routingContext.routingStack.clear()
        botContext.routingContext.dialogContextMap.clear()
    }
}

/**
 * A context used in [BotRoutingEngine] and [BotRoutingApi] to store routing state.
 *
 * @see BotRoutingApi
 * @see BotRoutingEngine
 * */
data class BotRoutingContext(
    val dialogContextMap: MutableMap<String, DialogContext> = mutableMapOf(),
    val routingStack: ArrayDeque<RoutingRequest> = ArrayDeque(),
    var targetState: String? = null,
    var currentEngine: String? = null,
    var currentRouter: String? = null,
)

/**
 * Request to route execution from one engine to another using [BotRoutingEngine]
 *
 * @param toEngine target engine to process next or current request
 * @param fromEngine engine request is sent from.
 *
 * @see BotRoutingEngine
 * @see BotRoutingApi
 * @see BotRoutingContext
 * @see BotRequestRerouteException
 * */
data class RoutingRequest(
    val toEngine: String,
    val fromEngine: String? = null,
)

/**
 * A helpful extension to get [BotRoutingContext] from BotContext.
 *
 * @see BotRoutingContext
 * @see BotRoutingApi
 * @see BotRoutingEngine
 * */
val BotContext.routingContext: BotRoutingContext
    get() = (client[BOT_ROUTING_CONTEXT_KEY] as? BotRoutingContext)
        ?: BotRoutingContext().also { client[BOT_ROUTING_CONTEXT_KEY] = it }

/**
 * A extension for [BotRoutingApi] to be used in scenario.
 *
 * @see [BotRoutingApi]
 * */
val DefaultActionContext.routing: BotRoutingApi
    get() = BotRoutingApi(context)


private const val BOT_ROUTING_CONTEXT_KEY = "com/justai/jaicf/api/routing/context"
