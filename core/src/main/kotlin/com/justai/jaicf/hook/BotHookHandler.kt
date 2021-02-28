package com.justai.jaicf.hook

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.state.StatePath
import kotlin.reflect.KClass

class BotHookListener<in T: BotHook>(
    val action: (T) -> Unit,
    val isAvailable: (T) -> Boolean
) {
    companion object {
        class Builder<T: BotHook>(val action: (T) -> Unit) {
            private val availableFrom = mutableSetOf<StatePath>()
            private val exceptFrom = mutableSetOf<StatePath>()

            fun addAvailableFrom(state: StatePath) {
                availableFrom += state
            }

            fun addExceptFrom(state: StatePath) {
                exceptFrom += state
            }

            fun build(): BotHookListener<T> {
                return BotHookListener(action) { hook ->
                    val current = hook.context.dialogContext.currentContext.toString()
                    val isAvailable = availableFrom.any { current.startsWith(it.toString()) }
                    val isException = exceptFrom.any { current.startsWith(it.toString()) }
                    isAvailable && !isException
                }
            }
        }
    }
}

/**
 * Holds a collection of [BotHook] handlers.
 * @see BotHook
 */
class BotHookHandler {

    val actions = mutableMapOf<KClass<out BotHook>, MutableList<BotHookListener<BotHook>>>()

    /**
     * Adds a listener for specified [BotHook]
     *
     * @param action a block that will be invoked once specified [BotHook] was triggered.
     * @see BotHook
     */
    inline fun <reified T: BotHook> addHookAction(noinline action: T.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val hookAction = { hook: T -> hook.action() } as (BotHook) -> Unit
        actions.computeIfAbsent(T::class) { mutableListOf() }.add(BotHookListener(hookAction, { true }))
    }

    /**
     * Invokes all listeners that are registered for a specified [BotHook].
     * Mainly used by bot engine to trigger hook for every phase of request processing.
     * You are free to create and trigger your own [BotHook] using the [BotHookHandler] as an event bus for your bot.
     *
     * @param hook a particular [BotHook] to be triggered
     */
    fun triggerHook(hook: BotHook) {
        actions[hook::class]?.forEach { listener ->
            try {
                if (listener.isAvailable(hook)) {
                    listener.action(hook)
                }
            } catch (e: BotHookException) {
                throw e
            } catch (e: Exception) {
            }
        }
    }
}