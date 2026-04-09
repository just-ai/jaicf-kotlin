package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.BotEngine
import org.slf4j.LoggerFactory
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.TelemetrySpan
import com.justai.jaicf.telemetry.closeHandoffSpan
import com.justai.jaicf.telemetry.currentTelemetrySpan
import com.justai.jaicf.telemetry.handoffSpanParent
import com.justai.jaicf.telemetry.isNoOp
import com.justai.jaicf.telemetry.realOrNull
import com.justai.jaicf.telemetry.setHandoffSpan

internal fun BotEngine.addLLMTelemetryHooks() {
    hooks.addHookAction<AnyErrorHook> {
        currentTelemetrySpan()?.recordError("jaicf", exception)
        context.closeHandoffSpan()
    }

    hooks.addHookAction<LLMHandoffHook> {
        val from = attributes[LLMAttributes.HANDOFF_FROM_AGENT]
        val to = attributes[LLMAttributes.HANDOFF_TO_AGENT]
        val spanName = "${LLMSpanName.Handoff} $from -> $to"

        val handoffParent = handoffSpanParent(context)
        LoggerFactory.getLogger("jaicf.telemetry.debug").info(
            "[TELEMETRY] LLMHandoffHook spanName=$spanName handoffParent=${handoffParent?.let { if (it.isNoOp()) "NoOp" else "${it::class.simpleName}#${System.identityHashCode(it)}" } ?: "null"}"
        )
        val span = telemetryProvider.createSpanOrNoOp(spanName, attributes, handoffParent)
        span.realOrNull()?.let { real ->
            real.addCommonAttributes(this)
            context.setHandoffSpan(real)
        }
    }
}

private fun TelemetrySpan.recordError(prefix: String, exception: Throwable?, maxLength: Int = 500) {
    exception?.let { e ->
        setAttribute("$prefix.error.type", e::class.simpleName ?: "Unknown")
        e.message?.take(maxLength)?.let { msg ->
            setAttribute("$prefix.error.message", msg)
        }
        recordException(e)
    }
}

private fun TelemetrySpan.addCommonAttributes(hook: LLMHandoffHook) {
    setAttribute(com.justai.jaicf.telemetry.JaicfTelemetryAttributes.STATE_NAME, hook.state.path.toString())
    setAttribute(com.justai.jaicf.telemetry.JaicfTelemetryAttributes.STATE_CURRENT, hook.context.dialogContext.currentState)
    setAttribute(com.justai.jaicf.telemetry.JaicfTelemetryAttributes.CLIENT_ID_ALT, hook.context.clientId)
    setAttribute(com.justai.jaicf.telemetry.JaicfTelemetryAttributes.REQUEST_TYPE, hook.request.type.name)
    setAttribute(com.justai.jaicf.telemetry.JaicfTelemetryAttributes.ACTIVATOR_NAME, hook.activator.toString())
}
