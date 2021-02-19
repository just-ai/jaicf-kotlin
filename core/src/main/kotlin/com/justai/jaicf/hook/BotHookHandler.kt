package com.justai.jaicf.hook

import kotlin.reflect.KClass

internal typealias BotHookAction<T> = (T) -> Unit

/**
 * Holds a collection of [BotHook] handlers.
 * @see BotHook
 */
class BotHookHandler {

    val actions = mutableMapOf<KClass<out BotHook>, MutableList<BotHookAction<in BotHook>>>()

    /**
     * Adds a listener for specified [BotHook]
     *
     * @param action a block that will be invoked once specified [BotHook] was triggered.
     * @see BotHook
     */
    inline fun <reified T: BotHook> addHookAction(noinline action: T.() -> Unit) {
        val hookAction = { hook: T -> hook.action() }

        @Suppress("UNCHECKED_CAST")
        actions.computeIfAbsent(T::class) { mutableListOf() }.add(hookAction as BotHookAction<in BotHook>)
    }

    /**
     * Invokes all listeners that are registered for a specified [BotHook].
     * Mainly used by bot engine to trigger hook for every phase of request processing.
     * You are free to create and trigger your own [BotHook] using the [BotHookHandler] as an event bus for your bot.
     *
     * @param hook a particular [BotHook] to be triggered
     */
    fun triggerHook(hook: BotHook) {
        val actions = actions[hook::class]
        actions?.forEach { action ->
            try {
                action.invoke(hook)
            } catch (e: BotHookException) {
                throw e
            } catch (e: Exception) {}
        }
    }
}