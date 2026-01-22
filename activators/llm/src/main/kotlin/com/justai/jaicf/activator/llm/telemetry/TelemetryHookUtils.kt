@file:Suppress("LESS_VISIBLE_TYPE_ACCESS_IN_INLINE_WARNING")

package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.tool.LLMToolInterruptionException
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.HookStage
import com.justai.jaicf.hook.TelemetryHook
import com.justai.jaicf.model.state.State

suspend fun triggerHook(hook: BotHook) {
    BotEngine.current()?.hooks?.triggerHook(hook)
}

suspend fun currentState(context: BotContext): State? =
    BotEngine.current()?.model?.states?.get(context.dialogContext.currentState)

suspend inline fun triggerHook(context: BotContext, hookFactory: (State) -> BotHook) {
    currentState(context)?.let { state ->
        triggerHook(hookFactory(state))
    }
}

suspend inline fun <T> withTelemetryHook(
    hook: TelemetryHook,
    block: () -> T
): T {
    val engine = BotEngine.current()
    engine?.hooks?.triggerHook(hook.withStage(HookStage.START))
    return try {
        block()
    } catch (e: Throwable) {
        if (e !is LLMToolInterruptionException) {
            engine?.hooks?.triggerHook(hook.withStage(HookStage.ERROR, e))
        }
        throw e
    } finally {
        engine?.hooks?.triggerHook(hook.withStage(HookStage.FINISH))
    }
}