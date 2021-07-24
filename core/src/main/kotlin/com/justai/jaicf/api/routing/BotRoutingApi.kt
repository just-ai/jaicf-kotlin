package com.justai.jaicf.api.routing

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.state.StatePath
import java.util.*

/**
 * An exception thrown to re-route current BotRequest to specified [targetEngineName]
 *
 * @param [targetEngineName] an engine with specified name provided in [BotRoutingEngine] routables map.
 * */
internal data class BotRequestRerouteException(val targetEngineName: String) : RuntimeException()

/**
 * An exception thrown when there is no previous engine to route back.
 * */
internal object NoRouteBackException : RuntimeException()

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
 *  main = BotEngine(main),
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
     * @param engineName target engine name specified in routables map in [BotRoutingEngine]
     * @param targetState target state for scenario in [engineName].
     *
     * @throws BotRequestRerouteException to reroute request using [BotRoutingEngine]
     * @return [Nothing] as no request execution possible after invoking this method
     *
     * @see BotRoutingEngine
     * */
    fun route(engineName: String, targetState: String? = null): Nothing {
        changeBot(engineName, targetState)
        throw BotRequestRerouteException(engineName)
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
        try {
            val routingContext = botContext.routingContext
            val target = routingContext.routingEngineStack.pop()
            val curr = routingContext.routingEngineStack.pop()
            logger.info("Routing request back from engine: $curr to engine: $target")
            routingContext.dialogContextMap[routingContext.currentEngine] = botContext.dialogContext
            throw BotRequestRerouteException(target)
        } catch (e: EmptyStackException) {
            logger.warn("Failed to change route back as there is no engines left in stack")
            throw NoRouteBackException
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
    fun changeBot(engineName: String, targetState: String? = null) {
        targetState?.let {
            val dialogContext = botContext.routingContext.dialogContextMap.getOrDefault(engineName, DialogContext())
            val currentState = StatePath.parse(dialogContext.currentState)
            val resolved = currentState.resolve(targetState).toString()
            botContext.routingContext.targetState = resolved
        }

        botContext.routingContext.routingEngineStack.push(engineName)
        botContext.routingContext.dialogContextMap[botContext.routingContext.currentEngine] = botContext.dialogContext
    }

    /**
     * Route client next requests back to previous bot engine
     *
     * @see BotRoutingEngine
     * */
    fun changeBotBack() {
        try {
            val routingContext = botContext.routingContext
            val curr = routingContext.routingEngineStack.pop()
            val target = routingContext.routingEngineStack.pop()
            routingContext.dialogContextMap[routingContext.currentEngine] = botContext.dialogContext
            logger.info("Changing execution back from engine: $curr to engine: $target")
        } catch (e: EmptyStackException) {
            logger.warn("Failed to change bot engine back as there is no engines left in stack")
        }

    }
}

/**
 * A context used in [BotRoutingEngine] and [BotRoutingApi] to store routing state.
 *
 * @see BotRoutingApi
 * @see BotRoutingEngine
 * */
internal data class BotRoutingContext(
    val dialogContextMap: MutableMap<String, DialogContext> = mutableMapOf(),
    val routingEngineStack: Stack<String> = Stack(),
    var targetState: String? = null,
    var currentEngine: String = BotRoutingEngine.DEFAULT_ROUTE_NAME,
    var staticallySelectedEngine: String? = null,
)

/**
 * A helpful extension to get [BotRoutingContext] from BotEngine.
 *
 * @see BotRoutingContext
 * @see BotRoutingApi
 * @see BotRoutingEngine
 * */
internal val BotContext.routingContext: BotRoutingContext
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