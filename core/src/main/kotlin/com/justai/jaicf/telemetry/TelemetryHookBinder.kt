package com.justai.jaicf.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.ProcessContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.hook.AfterProcessHook
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BeforeProcessHook
import com.justai.jaicf.test.reactions.answer
import kotlinx.coroutines.withContext

internal enum class TelemetrySpanName(val value: String) {
    PROCESS("jaicf.process"),
    BOT_REQUEST("jaicf.bot.request"),
    ACTIVATION_BEFORE("jaicf.activation.before"),
    ACTION_ERROR("jaicf.action.error"),
    ERROR("jaicf.error"),
}

internal suspend fun BotEngine.runWithTelemetry(request: BotRequest, botContext: BotContext, block: suspend () -> Unit) {
    if (telemetryProvider == TelemetryProvider.NoOp) {
        block()
        return
    }
    val processAttributes = mapOf(
        JaicfTelemetryAttributes.REQUEST_TYPE to request.type.name,
        JaicfTelemetryAttributes.REQUEST_CLIENT_ID to request.clientId,
    )
    runWithTelemetry(telemetryProvider, TelemetrySpanName.PROCESS.value, processAttributes) {
        block()
    }
}

internal fun BotEngine.addTelemetryHooks() {
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
}

/**
 * Runs [block] with BOT_REQUEST span. Session id and attributes are set up before the span.
 */
internal suspend fun BotEngine.runBotRequestWithTelemetry(
    request: BotRequest,
    requestContext: RequestContext,
    botContext: BotContext,
    reactions: com.justai.jaicf.reactions.Reactions,
    block: suspend () -> Unit,
) {
    if (telemetryProvider == TelemetryProvider.NoOp) {
        block()
        return
    }
    val sessionId = botContext.getTelemetrySessionId().takeIf { it.isNotEmpty() }
        ?: java.util.UUID.randomUUID().toString().also { botContext.setTelemetrySessionId(it) }
    val attributes = mutableMapOf<String, Any?>(
        JaicfTelemetryAttributes.REQUEST_TYPE to request.type.name,
        JaicfTelemetryAttributes.REQUEST_INPUT to request.input,
        JaicfTelemetryAttributes.REQUEST_CLIENT_ID to request.clientId,
        JaicfTelemetryAttributes.SESSION_NEW to requestContext.newSession,
        "session.id" to sessionId,
    )
    attributes["gen_ai.conversation.id"] = sessionId
    runWithTelemetry(telemetryProvider, TelemetrySpanName.BOT_REQUEST.value, attributes) {
        block()
    }
}

/**
 * Runs [block] with handoff span context when present.
 * Note: handoff span is NOT closed here - it stays active for the entire processStates cycle.
 */
internal suspend fun executeActionWithTelemetry(
    processContext: ProcessContext,
    block: suspend () -> Unit,
) {
    val handoffSpan = processContext.botContext.getHandoffSpan()
    if (handoffSpan != null) {
        val parentBeforeHandoff = currentTelemetrySpan()
        withContext(HandoffContextElement(parentBeforeHandoff)) {
            withTelemetrySpan(handoffSpan) {
                block()
            }
        }
    } else {
        block()
    }
}

private object TelemetryHookProcessor {

    suspend fun handleBeforeProcess(telemetryProvider: TelemetryProvider, hook: BeforeProcessHook) {
        // No-op: activation spans are no longer stored in context
    }

    suspend fun handleAfterProcess(telemetryProvider: TelemetryProvider, hook: AfterProcessHook) {
        val attributes = mapOf(
            JaicfTelemetryAttributes.REQUEST_RESPONSE to hook.reactions.answer,
            JaicfTelemetryAttributes.ACTIVATOR to hook.activator.javaClass.name,
            JaicfTelemetryAttributes.CURRENT_STATE to hook.context.dialogContext.currentState
        )
        currentTelemetrySpan()?.apply {
            attributes.forEach { (k, v) -> setAttribute(k, v) }
        }
        hook.context.closeAndRemoveHandoffSpan()
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

    suspend fun TelemetryProvider.record(
        name: String,
        attributes: Map<String, Any?>,
        context: BotContext? = null,
    ) {
        val parent = currentTelemetrySpan()
        val span = createSpanOrNoOp(name, attributes, parent)
        span.use { }
    }
}
