package com.justai.jaicf.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.ProcessContext
import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.hook.AfterProcessHook
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BeforeProcessHook
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookException
import com.justai.jaicf.hook.BotRequestHook
import com.justai.jaicf.test.reactions.answer

internal enum class TelemetrySpanName(val value: String) {
    SESSION("jaicf.session"),
    PROCESS("jaicf.process"),
    BOT_REQUEST("jaicf.bot.request"),
    ACTIVATION_BEFORE("jaicf.activation.before"),
    ACTION_ERROR("jaicf.action.error"),
    ERROR("jaicf.error"),
}

internal suspend inline fun <T : BotHook> BotEngine.withTelemetryHook(
    hook: T,
    spanName: String,
    crossinline block: suspend () -> Unit,
) {
    try {
        hooks.triggerHook(hook)
        val span = hook.context.getTelemetrySpan(spanName)
        withTelemetrySpan(span) {
            try {
                block()
            } finally {
                span?.close()
                hook.context.removeTelemetrySpan(spanName)
            }
        }
    } catch (e: BotHookException) {
        logger.debug("Hook interrupted a request processing", e)
    }
}

internal suspend fun BotEngine.runWithTelemetry(request: BotRequest, botContext: BotContext, block: suspend () -> Unit) {
    if (telemetryProvider == TelemetryProvider.NoOp) {
        block()
        return
    }
    val sessionSpan = botContext.getSessionSpan().takeIf { !it.isNoOp() }
    val sessionRoot = if (sessionSpan != null) {
        sessionSpan
    } else {
        val newSession = telemetryProvider.createSpanOrNoOp(
            TelemetrySpanName.SESSION.value,
            mapOf(JaicfTelemetryAttributes.CLIENT_ID to request.clientId),
            parent = null
        )
        newSession.realOrNull()?.let { botContext.setSessionSpan(it) }
        newSession
    }
    val processAttributes = mapOf(
        JaicfTelemetryAttributes.REQUEST_TYPE to request.type.name,
        JaicfTelemetryAttributes.REQUEST_CLIENT_ID to request.clientId,
    )
    val processSpan = telemetryProvider.createSpanOrNoOp(
        TelemetrySpanName.PROCESS.value,
        processAttributes,
        parent = sessionRoot.realOrNull()
    )
    withOptionalSpan(processSpan.realOrNull()) {
        try {
            block()
        } finally {
            processSpan.realOrNull()?.close()
        }
    }
}

internal fun BotEngine.addTelemetryHooks() {
    hooks.addHookAction<BotRequestHook> {
        TelemetryHookProcessor.handleBotRequest(telemetryProvider, this)
    }
    hooks.addHookAction<BeforeProcessHook> {
        TelemetryHookProcessor.handleBeforeProcess(telemetryProvider, this)
    }

    hooks.addHookAction<AfterProcessHook> {
        TelemetryHookProcessor.handleAfterProcess(telemetryProvider, this)
    }

    hooks.addHookAction<ActionErrorHook> {
        TelemetryHookProcessor.handleActionError(telemetryProvider, this)
    }

    hooks.addHookAction<AnyErrorHook> {
        TelemetryHookProcessor.handleAnyError(telemetryProvider, this)
    }

    hooks.addHookAction<CustomTelemetryHook> {
        TelemetryHookProcessor.handleCustomTelemetry(telemetryProvider, this)
    }
}

/**
 * Runs [block] with handoff span context when present.
 * Centralizes handoff/span logic; caller provides the action execution (withHook + executeAction).
 */
internal suspend fun executeActionWithTelemetry(
    processContext: ProcessContext,
    block: suspend () -> Unit,
) {
    withOptionalHandoffSpan(processContext.botContext) {
        block()
    }
}

private object TelemetryHookProcessor {

    suspend fun handleBotRequest(telemetryProvider: TelemetryProvider, hook: BotRequestHook) {
        if (telemetryProvider == TelemetryProvider.NoOp) return

        val sessionId = hook.context.getTelemetrySessionId()
            .takeIf { it.isNotEmpty() }
            ?: java.util.UUID.randomUUID().toString().also { hook.context.setTelemetrySessionId(it) }

        val attributes = mutableMapOf<String, Any?>(
            JaicfTelemetryAttributes.REQUEST_TYPE to hook.request.type.name,
            JaicfTelemetryAttributes.REQUEST_INPUT to hook.request.input,
            JaicfTelemetryAttributes.REQUEST_CLIENT_ID to hook.request.clientId,
            JaicfTelemetryAttributes.SESSION_NEW to hook.requestContext.newSession,
            "session.id" to sessionId,
        )
        attributes["gen_ai.conversation.id"] = sessionId

        val parentSpan = currentTelemetrySpan()
        val span = telemetryProvider.createSpanOrNoOp(TelemetrySpanName.BOT_REQUEST.value, attributes, parentSpan)
        span.realOrNull()?.let { hook.context.setTelemetrySpan(TelemetrySpanName.BOT_REQUEST.value, it) }
    }

    suspend fun handleBeforeProcess(telemetryProvider: TelemetryProvider, hook: BeforeProcessHook) {
        val activationSpan = hook.context.getTelemetrySpan(TelemetrySpanName.ACTIVATION_BEFORE.value)
        activationSpan?.close()
        hook.context.removeTelemetrySpan(TelemetrySpanName.ACTIVATION_BEFORE.value)
    }

    fun handleAfterProcess(telemetryProvider: TelemetryProvider, hook: AfterProcessHook) {
        val attributes = mapOf(
            JaicfTelemetryAttributes.REQUEST_RESPONSE to hook.reactions.answer,
            JaicfTelemetryAttributes.ACTIVATOR to hook.activator.javaClass.name,
            JaicfTelemetryAttributes.CURRENT_STATE to hook.context.dialogContext.currentState
        )
        hook.context.getTelemetrySpan(TelemetrySpanName.BOT_REQUEST.value)?.apply {
            attributes.forEach { (k, v) -> setAttribute(k, v) }
        }
    }

    suspend fun handleActionError(telemetryProvider: TelemetryProvider, hook: ActionErrorHook) {
        val name = TelemetrySpanName.ACTION_ERROR.value
        val attributes = mutableMapOf(
            JaicfTelemetryAttributes.REQUEST_CLIENT_ID to hook.request.clientId,
            JaicfTelemetryAttributes.STATE to hook.state.path,
            JaicfTelemetryAttributes.STATE_PATH to hook.state.path.toString(),
            JaicfTelemetryAttributes.ACTIVATOR to hook.activator.javaClass.name,
            JaicfTelemetryAttributes.ERROR_TYPE to hook.exception::class.qualifiedName,
            JaicfTelemetryAttributes.ERROR_MESSAGE to hook.exception.message,
            JaicfTelemetryAttributes.ERROR_STATE to hook.state.path
        )
        telemetryProvider.record(name, attributes, hook.context)
        currentTelemetrySpan()?.recordException(hook.exception)
    }

    suspend fun handleAnyError(telemetryProvider: TelemetryProvider, hook: AnyErrorHook) {
        val name = TelemetrySpanName.ERROR.value
        val attributes = mutableMapOf<String, Any?>(
            JaicfTelemetryAttributes.REQUEST_CLIENT_ID to hook.request.clientId,
            JaicfTelemetryAttributes.ERROR_TYPE to hook.exception::class.qualifiedName,
            JaicfTelemetryAttributes.ERROR_MESSAGE to hook.exception.message,
            JaicfTelemetryAttributes.CURRENT_STATE to hook.context.dialogContext.currentState
        )
        telemetryProvider.record(name, attributes, hook.context)
        currentTelemetrySpan()?.recordException(hook.exception)
    }

    suspend fun handleCustomTelemetry(telemetryProvider: TelemetryProvider, hook: CustomTelemetryHook) {
        when (hook.stage) {
            TelemetryHookStage.START -> {
                val parent = currentTelemetrySpan()
                    ?: hook.context.getTelemetrySpan(TelemetrySpanName.BOT_REQUEST.value)
                val span = telemetryProvider.createSpanOrNoOp(hook.spanName, hook.attributes, parent)
                span.realOrNull()?.let { hook.context.setTelemetrySpan(hook.spanName, it) }
            }

            TelemetryHookStage.FINISH, TelemetryHookStage.ERROR -> {
                val span = hook.context.getTelemetrySpan(hook.spanName) ?: return
                hook.exception?.let { span.recordException(it) }
                span.close()
                hook.context.removeTelemetrySpan(hook.spanName)
            }
        }
    }

    suspend fun TelemetryProvider.record(
        name: String,
        attributes: Map<String, Any?>,
        context: BotContext? = null,
    ) {
        val parent = currentTelemetrySpan()
        val span = createSpanOrNoOp(name, attributes, parent)
        span.use { span ->
            // Span is automatically closed by use block
            // No need to set it in context for short-lived spans
        }
    }
}
