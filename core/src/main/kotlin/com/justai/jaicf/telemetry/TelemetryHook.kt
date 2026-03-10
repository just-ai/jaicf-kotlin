package com.justai.jaicf.telemetry

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.triggerBotHook

enum class TelemetryHookStage {
    START,
    FINISH,
    ERROR,
}

interface TelemetryHook : BotHook {
    val stage: TelemetryHookStage
    val exception: Throwable?
        get() = null

    fun withStage(stage: TelemetryHookStage, exception: Throwable? = null): TelemetryHook
}

data class CustomTelemetryHook(
    override val context: BotContext,
    val spanName: String,
    val attributes: Map<String, Any?> = emptyMap(),
    override val stage: TelemetryHookStage = TelemetryHookStage.START,
    override val exception: Throwable? = null,
) : TelemetryHook {
    override fun withStage(stage: TelemetryHookStage, exception: Throwable?) =
        copy(stage = stage, exception = exception)
}


suspend inline fun <T> runWithTelemetry(
    hook: TelemetryHook,
    spanName: String? = null,
    crossinline block: suspend () -> T
): T {
    triggerBotHook(hook.withStage(TelemetryHookStage.START))
    val effectiveSpanName = spanName ?: (hook as? CustomTelemetryHook)?.spanName
    val span = effectiveSpanName?.let { hook.context.getTelemetrySpan(it) }
    return withTelemetrySpan(span) {
        try {
            block()
        } catch (e: Throwable) {
            if (e !is TelemetrySkipException) {
                triggerBotHook(hook.withStage(TelemetryHookStage.ERROR, e))
            }
            throw e
        } finally {
            triggerBotHook(hook.withStage(TelemetryHookStage.FINISH))
        }
    }
}