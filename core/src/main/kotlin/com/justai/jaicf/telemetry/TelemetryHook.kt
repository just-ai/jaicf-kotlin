package com.justai.jaicf.telemetry

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.triggerBotHook

enum class TelemetryHookStage {
    START,
    FINISH,
    ERROR,
}

enum class BotRequestStage { START, END }

data class TelemetryProcessHook(
    override val context: BotContext,
    val request: BotRequest,
    val requestContext: RequestContext,
    val stage: BotRequestStage,
    val durationMs: Double = 0.0,
) : BotHook

interface TelemetryHook : BotHook {
    val stage: TelemetryHookStage
    val exception: Throwable?
        get() = null

    fun withStage(stage: TelemetryHookStage, exception: Throwable? = null): TelemetryHook
}


suspend inline fun <T> runWithTelemetry(
    hook: TelemetryHook,
    block: () -> T
): T {
    triggerBotHook(hook.withStage(TelemetryHookStage.START))
    return try {
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