package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.telemetry.TelemetryHook
import com.justai.jaicf.telemetry.TelemetryHookStage
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.TelemetrySpan
import com.justai.jaicf.telemetry.closeAllTelemetrySpans
import com.justai.jaicf.telemetry.JaicfTelemetryAttributes
import com.justai.jaicf.telemetry.currentTelemetrySpan
import com.justai.jaicf.telemetry.getHandoffSpan
import com.justai.jaicf.telemetry.getTelemetrySessionId
import com.justai.jaicf.telemetry.getTelemetrySpan
import com.justai.jaicf.telemetry.handoffPending
import com.justai.jaicf.telemetry.handoffSpanParent
import com.justai.jaicf.telemetry.realOrNull
import com.justai.jaicf.telemetry.removeAllTelemetrySpan
import com.justai.jaicf.telemetry.removeTelemetrySpan
import com.justai.jaicf.telemetry.setTelemetrySpan

internal fun BotEngine.addLLMTelemetryHooks() {

    hooks.addHookAction<AnyErrorHook> {
        SpanProcessor.handleAnyError(this)
    }

    hooks.addHookAction<LLMHandoffHook> {
        SpanProcessor.handleHandoffStart(telemetryProvider, this)
    }

    addLifecycleHook<LLMLifecycleHook>(
        onStart = SpanProcessor::handleLifecycleStart,
        onFinish = SpanProcessor::handleLifecycleFinish,
        onError = SpanProcessor::handleLifecycleError
    )
}

private inline fun <reified T : TelemetryHook> BotEngine.addLifecycleHook(
    crossinline onStart: suspend (TelemetryProvider, T) -> Unit,
    crossinline onFinish: suspend (T) -> Unit,
    crossinline onError: suspend (T) -> Unit,
) {
    hooks.addHookAction<T> {
        when (stage) {
            TelemetryHookStage.START -> onStart(telemetryProvider, this)
            TelemetryHookStage.FINISH -> onFinish(this)
            TelemetryHookStage.ERROR -> onError(this)
        }
    }
}

private fun TelemetrySpan.setAttributes(attrs: Map<String, Any?>) {
    attrs.forEach { (k, v) -> setAttribute(k, v) }
}

private fun TelemetrySpan.recordError(exception: Throwable?, prefix: String, maxLength: Int = 500) {
    exception?.let { e ->
        setAttribute("$prefix.error.type", e::class.simpleName ?: "Unknown")
        e.message?.take(maxLength)?.let { msg ->
            setAttribute("$prefix.error.message", msg)
        }
        recordException(e)
    }
}

private object SpanProcessor {
    private val SKIP_TOOL_SPANS = setOf("handoff_to_agent")

    private fun shouldSkip(toolName: String) = toolName in SKIP_TOOL_SPANS

    private fun toolName(attributes: Map<String, Any?>) =
        attributes[LLMAttributes.TOOL_NAME] as? String ?: "unknown"

    private data class StartConfig(
        val parentSpanName: String? = null,
        val extraAttributes: (LLMLifecycleHook) -> Map<String, Any?> = { emptyMap() },
        val useSimpleAttributes: Boolean = false,
        val isToolType: Boolean = false,
    )

    private data class FinishConfig(
        val removeAfterClose: Boolean = false,
        val hasCompletionUsage: Boolean = false,
        val hasHandoffLogic: Boolean = false,
    )

    private data class ErrorConfig(
        val skip: Boolean = false,
        val errorPrefix: String = "llm",
        val errorMaxLength: Int = 500,
        val removeAfterClose: Boolean = false,
        val hasHandoffLogic: Boolean = false,
    )

    private data class LifecycleConfig(
        val start: StartConfig,
        val finish: FinishConfig = FinishConfig(),
        val error: ErrorConfig = ErrorConfig(),
    )

    private fun config(
        parentSpanName: String? = null,
        extraAttributes: (LLMLifecycleHook) -> Map<String, Any?> = { emptyMap() },
        useSimpleAttributes: Boolean = false,
        isToolType: Boolean = false,
        removeAfterClose: Boolean = false,
        hasCompletionUsage: Boolean = false,
        hasHandoffLogic: Boolean = false,
        errorSkip: Boolean = false,
        errorPrefix: String = "llm",
        errorMaxLength: Int = 500,
    ) = LifecycleConfig(
        start = StartConfig(parentSpanName, extraAttributes, useSimpleAttributes, isToolType),
        finish = FinishConfig(removeAfterClose, hasCompletionUsage, hasHandoffLogic),
        error = ErrorConfig(errorSkip, errorPrefix, errorMaxLength, removeAfterClose, hasHandoffLogic),
    )

    private fun genAIConversationId(context: BotContext): String? {
        val id = context.getTelemetrySessionId()
        return id.takeIf { it.isNotEmpty() }
    }

    private val lifecycleConfigs = mapOf(
        LLMSpanType.ACTION_INVOKE to config(
            extraAttributes = { hook ->
                val attrs = mutableMapOf<String, Any?>(
                    LLMAttributes.AGENT_STATE to (hook.state?.path?.toString() ?: ""),
                    LLMAttributes.AGENT_INPUT_LENGTH to hook.request.input.length,
                    GenAIAttributes.OPERATION_NAME to GenAIAttributes.OPERATION_INVOKE_AGENT,
                    GenAIAttributes.AGENT_NAME to (hook.state?.path?.toString() ?: "unknown"),
                )
                genAIConversationId(hook.context)?.let { attrs[GenAIAttributes.CONVERSATION_ID] = it }
                attrs
            },
            hasHandoffLogic = true,
            errorPrefix = LLMAttributes.ERROR_PREFIX_AGENT,
        ),
        LLMSpanType.LLM_CALL to config(
            parentSpanName = LLMSpanName.ActionInvoke,
            extraAttributes = { hook ->
                val model = hook.attributes[LLMAttributes.MODEL] as? String ?: ""
                val attrs = mutableMapOf<String, Any?>(
                    GenAIAttributes.OPERATION_NAME to GenAIAttributes.OPERATION_CHAT,
                    GenAIAttributes.REQUEST_MODEL to model.ifBlank { null },
                )
                genAIConversationId(hook.context)?.let { attrs[GenAIAttributes.CONVERSATION_ID] = it }
                attrs
            },
            hasCompletionUsage = true,
            errorPrefix = LLMAttributes.ERROR_PREFIX_CALL,
        ),
        LLMSpanType.STREAMING to config(
            parentSpanName = LLMSpanName.LLMCall,
            errorSkip = true,
        ),
        LLMSpanType.TOOL_CALLS to config(errorSkip = true),
        LLMSpanType.TOOL_CALL to config(
            isToolType = true,
            extraAttributes = { hook ->
                val toolName = hook.attributes[LLMAttributes.TOOL_NAME] as? String ?: "unknown"
                val callId = hook.attributes[LLMAttributes.TOOL_CALL_ID] as? String
                mutableMapOf<String, Any?>(
                    GenAIAttributes.OPERATION_NAME to GenAIAttributes.OPERATION_EXECUTE_TOOL,
                    GenAIAttributes.TOOL_NAME to toolName,
                ).apply {
                    callId?.let { put(GenAIAttributes.TOOL_CALL_ID, it) }
                    genAIConversationId(hook.context)?.let { put(GenAIAttributes.CONVERSATION_ID, it) }
                }
            },
            removeAfterClose = true,
            errorPrefix = LLMAttributes.ERROR_PREFIX_TOOL,
            errorMaxLength = 200,
        ),
        LLMSpanType.TOOL_EXECUTE to config(
            isToolType = true,
            useSimpleAttributes = true,
            extraAttributes = { hook ->
                val toolName = hook.attributes[LLMAttributes.TOOL_NAME] as? String ?: "unknown"
                val callId = hook.attributes[LLMAttributes.TOOL_CALL_ID] as? String
                mutableMapOf<String, Any?>(
                    GenAIAttributes.OPERATION_NAME to GenAIAttributes.OPERATION_EXECUTE_TOOL,
                    GenAIAttributes.TOOL_NAME to toolName,
                ).apply {
                    callId?.let { put(GenAIAttributes.TOOL_CALL_ID, it) }
                    genAIConversationId(hook.context)?.let { put(GenAIAttributes.CONVERSATION_ID, it) }
                }
            },
            removeAfterClose = true,
            errorPrefix = LLMAttributes.ERROR_PREFIX_TOOL,
            errorMaxLength = 200,
        ),
    )

    private suspend fun createSpan(
        provider: TelemetryProvider,
        context: BotContext,
        name: String,
        attributes: Map<String, Any?>,
        parentOverride: TelemetrySpan? = null,
    ): TelemetrySpan {
        val parent = sequenceOf(
            parentOverride,
            currentTelemetrySpan(),
            context.getHandoffSpan(),
            context.getTelemetrySpan(JaicfTelemetryAttributes.BOT_REQUEST_SPAN),
        ).mapNotNull { it.realOrNull() }
            .firstOrNull()
        return provider.createSpanOrNoOp(name, attributes, parent)
    }

    private fun TelemetrySpan.addCommonAttributes(hook: BotActionHook) {
        setAttribute(JaicfTelemetryAttributes.STATE_NAME, hook.state.path.toString())
        setAttribute(JaicfTelemetryAttributes.STATE_CURRENT, hook.context.dialogContext.currentState)
        setAttribute(JaicfTelemetryAttributes.CLIENT_ID_ALT, hook.context.clientId)
        setAttribute(JaicfTelemetryAttributes.REQUEST_TYPE, hook.request.type.name)
        setAttribute(JaicfTelemetryAttributes.ACTIVATOR_NAME, hook.activator.toString())
    }

    private fun TelemetrySpan.addLifecycleAttributes(hook: LLMLifecycleHook) {
        hook.state?.let { setAttribute(JaicfTelemetryAttributes.STATE_NAME, it.path.toString()) }
        setAttribute(JaicfTelemetryAttributes.STATE_CURRENT, hook.context.dialogContext.currentState)
        setAttribute(JaicfTelemetryAttributes.CLIENT_ID_ALT, hook.context.clientId)
        setAttribute(JaicfTelemetryAttributes.REQUEST_TYPE, hook.request.type.name)
        hook.activator?.let { setAttribute(JaicfTelemetryAttributes.ACTIVATOR_NAME, it.toString()) }
    }

    private fun TelemetrySpan.addSimpleAttributes(context: BotContext, request: BotRequest) {
        setAttribute(JaicfTelemetryAttributes.CLIENT_ID_ALT, context.clientId)
        setAttribute(JaicfTelemetryAttributes.REQUEST_TYPE, request.type.name)
    }

    private fun closeHandoffSpans(context: BotContext) {
        context.getHandoffSpan()?.apply {
            close()
            context.removeAllTelemetrySpan(LLMSpanName.Handoff)
        }
    }

    private suspend fun resolveParent(hook: LLMLifecycleHook, parentSpanName: String?): TelemetrySpan? {
        val fromContext = parentSpanName?.let { hook.context.getTelemetrySpan(it) }
        if (fromContext != null) return fromContext
        val handoff = hook.context.getHandoffSpan()
        if (handoff != null) return handoff
        return currentTelemetrySpan()
            ?: hook.context.getTelemetrySpan(JaicfTelemetryAttributes.BOT_REQUEST_SPAN)
    }

    suspend fun handleLifecycleStart(provider: TelemetryProvider, hook: LLMLifecycleHook) {
        val config = lifecycleConfigs[hook.type] ?: return
        val start = config.start
        if (start.isToolType && shouldSkip(toolName(hook.attributes))) return

        val spanDisplayName = hook.resolveSpanName()
        val storageKey = hook.getStorageKey()
        val parent = resolveParent(hook, start.parentSpanName)
        val attrs = hook.attributes + start.extraAttributes(hook)

        val span = createSpan(provider, hook.context, spanDisplayName, attrs, parent)
        span.realOrNull()?.let {
            if (start.useSimpleAttributes) {
                it.addSimpleAttributes(hook.context, hook.request)
                it.setAttributes(hook.attributes)
            } else {
                it.addLifecycleAttributes(hook)
            }
            hook.context.setTelemetrySpan(storageKey, it)
        }
    }

    fun handleLifecycleFinish(hook: LLMLifecycleHook) {
        val config = lifecycleConfigs[hook.type] ?: return
        val finish = config.finish
        if (config.start.isToolType && shouldSkip(toolName(hook.attributes))) return

        val storageKey = hook.getStorageKey()
        val span = hook.context.getTelemetrySpan(storageKey) ?: return
        span.setAttributes(hook.attributes)
        if (finish.hasCompletionUsage) {
            hook.completionUsage?.let { usage ->
                span.setAttribute(LLMAttributes.TOKENS_USAGE_PROMPT, usage.promptTokens())
                span.setAttribute(LLMAttributes.TOKENS_USAGE_COMPLETION, usage.completionTokens())
                span.setAttribute(LLMAttributes.TOKENS_USAGE_TOTAL, usage.totalTokens())
                span.setAttribute(GenAIAttributes.USAGE_INPUT_TOKENS, usage.promptTokens())
                span.setAttribute(GenAIAttributes.USAGE_OUTPUT_TOKENS, usage.completionTokens())
            }
        }
        span.close()
        if (finish.removeAfterClose) hook.context.removeTelemetrySpan(storageKey)
        if (finish.hasHandoffLogic) {
            if (hook.context.handoffPending) hook.context.handoffPending = false
            else closeHandoffSpans(hook.context)
        }
    }

    fun handleLifecycleError(hook: LLMLifecycleHook) {
        val config = lifecycleConfigs[hook.type] ?: return
        val error = config.error
        if (error.skip) return
        if (config.start.isToolType && shouldSkip(toolName(hook.attributes))) return

        val storageKey = hook.getStorageKey()
        val span = hook.context.getTelemetrySpan(storageKey) ?: return
        span.setAttributes(hook.attributes)
        span.recordError(hook.exception, error.errorPrefix, maxLength = error.errorMaxLength)
        span.close()
        if (error.removeAfterClose) hook.context.removeTelemetrySpan(storageKey)
        if (error.hasHandoffLogic) {
            if (hook.context.handoffPending) hook.context.handoffPending = false
            else closeHandoffSpans(hook.context)
        }
    }

    suspend fun handleHandoffStart(provider: TelemetryProvider, hook: LLMHandoffHook) {
        val from = hook.attributes[LLMAttributes.HANDOFF_FROM_AGENT]
        val to = hook.attributes[LLMAttributes.HANDOFF_TO_AGENT]
        val spanName = "${LLMSpanName.Handoff} $from -> $to"

        val handoffParent = handoffSpanParent(hook.context)
        val span = createSpan(provider, hook.context, spanName, hook.attributes, handoffParent)
        span.realOrNull()?.let {
            it.addCommonAttributes(hook)
            hook.context.setTelemetrySpan(spanName, it)
            hook.context.handoffPending = true
        }
    }

    suspend fun handleAnyError(hook: AnyErrorHook) {
        currentTelemetrySpan()?.apply {
            recordError(hook.exception, "jaicf")
        }
        hook.context.closeAllTelemetrySpans()
    }
}
