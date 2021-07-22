package com.justai.jaicf.api.routing

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.state.StatePath
import java.util.*

/**
 * JAVADOC ME
 * */
data class NewRouteException(val targetEngineName: String) : RuntimeException()

/**
 * JAVADOC ME
 * */
class BotRoutingApi(internal val botContext: BotContext) : WithLogger {

    /**
     * JAVADOC ME
     * */
    fun route(engineName: String, targetState: String? = null) {
        changeBot(engineName, targetState)
        throw NewRouteException(engineName)
    }

    /**
     * JAVADOC ME
     * */
    fun routeBack() {
        try {
            val routingContext = botContext.routingContext
            val target = routingContext.routingEngineStack.pop()
            val curr = routingContext.routingEngineStack.pop()
            logger.info("Routing request back from engine: $curr to engine: $target")
            routingContext.dialogContextMap[routingContext.currentEngine] = botContext.dialogContext
            throw NewRouteException(target)
        } catch (e: EmptyStackException) {
            logger.warn("Failed to change route back as there is no engines left in stack")
        }

    }

    /**
     * JAVADOC ME
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
     * JAVADOC ME
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
 * JAVADOC ME
 * */
data class BotRoutingContext(
    val dialogContextMap: MutableMap<String, DialogContext> = mutableMapOf(),
    val routingEngineStack: Stack<String> = Stack(),
    var targetState: String? = null,
    var currentEngine: String = BotRoutingEngine.DEFAULT_ROUTE_NAME,
    var staticallySelectedEngine: String? = null,
)

/**
 * JAVADOC ME
 * */
internal val BotContext.routingContext: BotRoutingContext
    get() = (client[BOT_ROUTING_CONTEXT_KEY] as? BotRoutingContext)
        ?: BotRoutingContext().also { client[BOT_ROUTING_CONTEXT_KEY] = it }

/**
 * JAVADOC ME
 * */
val DefaultActionContext.routing: BotRoutingApi
    get() = BotRoutingApi(context)


private const val BOT_ROUTING_CONTEXT_KEY = "com/justai/jaicf/api/routing/context"