package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.telemetry.LLMSpanName.ToolCall
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.telemetry.TelemetryHookStage
import com.justai.jaicf.telemetry.TelemetryHook
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.TelemetrySpan
import com.justai.jaicf.telemetry.closeAllTelemetrySpans
import com.justai.jaicf.telemetry.currentTelemetrySpan
import com.justai.jaicf.telemetry.getHandoffSpan
import com.justai.jaicf.telemetry.getTelemetrySpan
import com.justai.jaicf.telemetry.removeAllTelemetrySpan
import com.justai.jaicf.telemetry.removeTelemetrySpan
import com.justai.jaicf.telemetry.setCurrentTelemetrySpan
import com.justai.jaicf.telemetry.setTelemetrySpan

internal fun BotEngine.addLLMTelemetryHooks() {

    hooks.addHookAction<AnyErrorHook> {
        SpanProcessor.handleAnyError(this)
    }

    hooks.addHookAction<LLMHandoffHook> {
        SpanProcessor.handleHandoffStart(telemetryProvider, this)
    }

    addLifecycleHook<LLMActionHook>(
        onStart = SpanProcessor::handleAgentInvokeStart,
        onFinish = SpanProcessor::handleAgentInvokeFinish,
        onError = SpanProcessor::handleAgentInvokeError
    )

    addLifecycleHook<LLMCallHook>(
        onStart = SpanProcessor::handleLLMCallStart,
        onFinish = SpanProcessor::handleLLMCallFinish,
        onError = SpanProcessor::handleLLMCallError
    )

    addLifecycleHook<LLMStreamingHook>(
        onStart = SpanProcessor::handleStreamingStart,
        onFinish = SpanProcessor::handleStreamingFinish
    )

    addLifecycleHook<LLMToolCallsHook>(
        onStart = SpanProcessor::handleToolCallsStart,
        onFinish = SpanProcessor::handleToolCallsFinish
    )

    addLifecycleHook<LLMToolCallHook>(
        onStart = SpanProcessor::handleToolCallStart,
        onFinish = SpanProcessor::handleToolCallFinish,
        onError = SpanProcessor::handleToolCallError
    )

    addLifecycleHook<LLMToolExecuteHook>(
        onStart = SpanProcessor::handleToolExecuteStart,
        onFinish = SpanProcessor::handleToolExecuteFinish,
        onError = SpanProcessor::handleToolExecuteError
    )
}

private inline fun <reified T : TelemetryHook> BotEngine.addLifecycleHook(
    crossinline onStart: (TelemetryProvider, T) -> Unit,
    crossinline onFinish: (T) -> Unit,
    crossinline onError: (T) -> Unit = {}
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
        attributes["llm.tool.name"] as? String ?: "unknown"

    private fun toolSpanName(toolName: String) = "$ToolCall:$toolName"

    private fun createSpan(
        provider: TelemetryProvider,
        context: BotContext,
        name: String,
        attributes: Map<String, Any?>
    ): TelemetrySpan {
        val parent = currentTelemetrySpan(context)
        return try {
            provider.createSpan(name, attributes, parent)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
    }

    private fun TelemetrySpan.addCommonAttributes(hook: BotActionHook) {
        setAttribute("jaicf.state.name", hook.state.path.toString())
        setAttribute("jaicf.state.current", hook.context.dialogContext.currentState)
        setAttribute("jaicf.client.id", hook.context.clientId)
        setAttribute("jaicf.request.type", hook.request.type.name)
        setAttribute("jaicf.activator.name", hook.activator.toString())
    }

    private fun TelemetrySpan.addSimpleAttributes(context: BotContext, request: BotRequest) {
        setAttribute("jaicf.client.id", context.clientId)
        setAttribute("jaicf.request.type", request.type.name)
    }

    private fun startSpan(
        provider: TelemetryProvider,
        hook: LLMLifecycleHook,
        spanName: String,
        extraAttributes: Map<String, Any?> = emptyMap()
    ) {
        val attrs = hook.attributes + extraAttributes
        val span = createSpan(provider, hook.context, spanName, attrs)
        if (span != TelemetrySpan.NoOp) {
            span.addCommonAttributes(hook)
            hook.context.setTelemetrySpan(spanName, span)
            hook.context.setCurrentTelemetrySpan(span)
        }
    }

    private fun finishSpan(context: BotContext, spanName: String, attributes: Map<String, Any?>) {
        context.getTelemetrySpan(spanName)?.apply {
            setAttributes(attributes)
            close()
        }
    }

    private fun closeHandoffSpans(context: BotContext) {
        context.getHandoffSpan()?.apply {
            close()
            context.removeAllTelemetrySpan(LLMSpanName.Handoff)
        }
    }

    fun handleAgentInvokeStart(provider: TelemetryProvider, hook: LLMActionHook) {
        startSpan(
            provider, hook, LLMSpanName.ActionInvoke, mapOf(
                "llm.agent.state" to hook.state.path.toString(),
                "llm.agent.input.length" to hook.request.input.length
            )
        )
    }

    fun handleAgentInvokeFinish(hook: LLMActionHook) {
        finishSpan(hook.context, LLMSpanName.ActionInvoke, hook.attributes)
        closeHandoffSpans(hook.context)
    }

    fun handleAgentInvokeError(hook: LLMActionHook) {
        hook.context.getTelemetrySpan(LLMSpanName.ActionInvoke)?.apply {
            setAttributes(hook.attributes)
            recordError(hook.exception, "llm.agent")
            close()
        }
        closeHandoffSpans(hook.context)
    }

    fun handleLLMCallStart(provider: TelemetryProvider, hook: LLMCallHook) =
        startSpan(provider, hook, LLMSpanName.LLMCall)

    fun handleLLMCallFinish(hook: LLMCallHook) {
        hook.context.getTelemetrySpan(LLMSpanName.LLMCall)?.apply {
            setAttributes(hook.attributes)
            hook.completionUsage?.let { usage ->
                setAttribute("llm.tokens.usage.prompt", usage.promptTokens())
                setAttribute("llm.tokens.usage.completion", usage.completionTokens())
                setAttribute("llm.tokens.usage.total", usage.totalTokens())
            }
            close()
        }
    }

    fun handleLLMCallError(hook: LLMCallHook) {
        hook.context.getTelemetrySpan(LLMSpanName.LLMCall)?.apply {
            setAttributes(hook.attributes)
            recordError(hook.exception, "llm.call")
            close()
        }
    }

    fun handleStreamingStart(provider: TelemetryProvider, hook: LLMStreamingHook) =
        startSpan(provider, hook, LLMSpanName.Streaming)

    fun handleStreamingFinish(hook: LLMStreamingHook) {
        val span = hook.context.getTelemetrySpan(LLMSpanName.Streaming)?.apply {
            setAttributes(hook.attributes)
            close()
        }
        hook.context.setCurrentTelemetrySpan(span)
    }

    fun handleToolCallsStart(provider: TelemetryProvider, hook: LLMToolCallsHook) =
        startSpan(provider, hook, LLMSpanName.ToolCalls)

    fun handleToolCallsFinish(hook: LLMToolCallsHook) =
        finishSpan(hook.context, LLMSpanName.ToolCalls, hook.attributes)

    fun handleToolCallStart(provider: TelemetryProvider, hook: LLMToolCallHook) {
        val name = toolName(hook.attributes)
        if (shouldSkip(name)) return
        startSpan(provider, hook, toolSpanName(name))
    }

    fun handleToolCallFinish(hook: LLMToolCallHook) {
        val name = toolName(hook.attributes)
        if (shouldSkip(name)) return

        val spanName = toolSpanName(name)
        val span = hook.context.getTelemetrySpan(spanName)?.apply {
            setAttributes(hook.attributes)
            close()
            hook.context.removeTelemetrySpan(spanName)
        }
        hook.context.setCurrentTelemetrySpan(span)
    }

    fun handleToolCallError(hook: LLMToolCallHook) {
        val name = toolName(hook.attributes)
        if (shouldSkip(name)) return

        val spanName = toolSpanName(name)
        hook.context.getTelemetrySpan(spanName)?.apply {
            setAttributes(hook.attributes)
            recordError(hook.exception, "llm.tool", maxLength = 200)
            close()
            hook.context.removeTelemetrySpan(spanName)
        }
    }

    fun handleHandoffStart(provider: TelemetryProvider, hook: LLMHandoffHook) {
        val from = hook.attributes["llm.handoff.from.agent"]
        val to = hook.attributes["llm.handoff.to.agent"]
        val spanName = "${LLMSpanName.Handoff} $from -> $to"

        val span = createSpan(provider, hook.context, spanName, hook.attributes)
        if (span != TelemetrySpan.NoOp) {
            span.addCommonAttributes(hook)
            hook.context.setTelemetrySpan(spanName, span)
            hook.context.setCurrentTelemetrySpan(span)
        }
    }

    fun handleToolExecuteStart(provider: TelemetryProvider, hook: LLMToolExecuteHook) {
        val name = toolName(hook.attributes)
        if (shouldSkip(name)) return

        val spanName = toolSpanName(name)
        val span = createSpan(provider, hook.context, spanName, hook.attributes)
        if (span != TelemetrySpan.NoOp) {
            span.addSimpleAttributes(hook.context, hook.request)
            span.setAttributes(hook.attributes)
            hook.context.setTelemetrySpan(spanName, span)
            hook.context.setCurrentTelemetrySpan(span)
        }
    }

    fun handleToolExecuteFinish(hook: LLMToolExecuteHook) {
        val name = toolName(hook.attributes)
        if (shouldSkip(name)) return

        val spanName = toolSpanName(name)
        hook.context.getTelemetrySpan(spanName)?.apply {
            setAttributes(hook.attributes)
            close()
            hook.context.removeTelemetrySpan(spanName)
        }
    }

    fun handleToolExecuteError(hook: LLMToolExecuteHook) {
        val name = toolName(hook.attributes)
        if (shouldSkip(name)) return

        val spanName = toolSpanName(name)
        hook.context.getTelemetrySpan(spanName)?.apply {
            setAttributes(hook.attributes)
            recordError(hook.exception, "llm.tool", maxLength = 200)
            close()
            hook.context.removeTelemetrySpan(spanName)
        }
    }

    fun handleAnyError(hook: AnyErrorHook) {
        currentTelemetrySpan(hook.context)?.apply {
            recordError(hook.exception, "jaicf")
        }
        hook.context.closeAllTelemetrySpans()
    }
}