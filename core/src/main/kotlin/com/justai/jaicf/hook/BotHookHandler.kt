package com.justai.jaicf.hook

import com.justai.jaicf.model.state.StatePath
import kotlinx.coroutines.CancellationException
import kotlin.reflect.KClass

data class BotHookListener<T : BotHook>(
    val klass: KClass<T>,
    val action: (T) -> Unit,
    val availableFrom: StatePath = StatePath.root(),
    val exceptFrom: Set<StatePath> = setOf<StatePath>()
) {
    fun execute(hook: T) {
        val current = hook.context.dialogContext.currentContext.toString()
        val isAvailable = current.startsWith(availableFrom.toString())
        val isException = exceptFrom.any { current.startsWith(it.toString()) }

        if (isAvailable && !isException) {
            action.invoke(hook)
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
    inline fun <reified T : BotHook> addHookAction(noinline action: T.() -> Unit) {
        val listener = BotHookListener<T>(T::class, { hook: T -> hook.action() })
        @Suppress("UNCHECKED_CAST")
        actions.computeIfAbsent(T::class) { mutableListOf() }.add(listener as BotHookListener<BotHook>)
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
                listener.execute(hook)
            } catch (e: BotHookException) {
                throw e
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    internal inline fun <reified T : BotHook> hasHook() = actions[T::class] != null
}