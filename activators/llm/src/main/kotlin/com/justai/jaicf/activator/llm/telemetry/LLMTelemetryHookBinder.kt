package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.agent.handoffInfo
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BeforeActionHook
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.hook.BotRequestHook
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.TelemetrySpan
import com.justai.jaicf.telemetry.closeAllTelemetrySpans
import com.justai.jaicf.telemetry.currentTelemetrySpanBlocking
import com.justai.jaicf.telemetry.getTelemetrySpan
import com.justai.jaicf.telemetry.removeTelemetrySpan
import com.justai.jaicf.telemetry.setCurrentTelemetrySpan
import com.justai.jaicf.telemetry.setTelemetrySpan

fun BotEngine.installLLMActivatorTelemetryHooks() {
    hooks.addHookAction<BotRequestHook> {
        LLMTelemetryHookProcessor.handleBotRequest(this)
    }

    hooks.addHookAction<AnyErrorHook> {
        LLMTelemetryHookProcessor.handleAnyError(this)
    }

    hooks.addHookAction<AgentInvokeStartHook> {
        LLMTelemetryHookProcessor.handleAgentInvokeStart(telemetryProvider, this)
    }

    hooks.addHookAction<AgentInvokeFinishHook> {
        LLMTelemetryHookProcessor.handleAgentInvokeFinish(this)
    }

    hooks.addHookAction<BeforeLLMCallHook> {
        LLMTelemetryHookProcessor.handleBeforeLLMCall(telemetryProvider, this)
    }


    hooks.addHookAction<AfterLLMCallHook> {
        LLMTelemetryHookProcessor.handleAfterLLMCall(this)
    }

    hooks.addHookAction<ToolCallsStartHook> {
        LLMTelemetryHookProcessor.handleToolCallsStart(telemetryProvider, this)
    }

    hooks.addHookAction<ToolCallsFinishHook> {
        LLMTelemetryHookProcessor.handleToolCallsFinish(this)
    }

    hooks.addHookAction<ToolCallStartHook> {
        LLMTelemetryHookProcessor.handleToolCallStart(telemetryProvider, this)
    }

    hooks.addHookAction<ToolCallFinishHook> {
        LLMTelemetryHookProcessor.handleToolCallFinish(this)
    }

    hooks.addHookAction<ToolCallErrorHook> {
        LLMTelemetryHookProcessor.handleToolCallError(this)
    }

    hooks.addHookAction<BeforeActionHook> {
        LLMTelemetryHookProcessor.handleBeforeAction(this)
    }

    hooks.addHookAction<HandoffStartHook> {
        LLMTelemetryHookProcessor.handleHandoffStart(telemetryProvider, this)
    }

    hooks.addHookAction<ToolExecuteStartHook> {
        LLMTelemetryHookProcessor.handleToolExecuteStart(telemetryProvider, this)
    }

    hooks.addHookAction<ToolExecuteFinishHook> {
        LLMTelemetryHookProcessor.handleToolExecuteFinish(this)
    }

    hooks.addHookAction<ToolExecuteErrorHook> {
        LLMTelemetryHookProcessor.handleToolExecuteError(this)
    }
}

private object LLMTelemetryHookProcessor {

    private fun getSpan(context: BotContext, spanName: String): TelemetrySpan? {
        return context.getTelemetrySpan(spanName)
    }

    private fun setSpan(context: BotContext, spanName: String, span: TelemetrySpan) {
        context.setTelemetrySpan(spanName, span)
    }

    private fun removeSpan(context: BotContext, spanName: String) {
        context.removeTelemetrySpan(spanName)
    }

    private fun addCommonAttributes(span: TelemetrySpan, hook: BotActionHook) {
        span.setAttribute("jaicf.state.name", hook.state.path.toString())
        span.setAttribute("jaicf.state.current", hook.context.dialogContext.currentState)
        span.setAttribute("jaicf.client.id", hook.context.clientId)
        span.setAttribute("jaicf.request.type", hook.request.type.name)
        span.setAttribute("jaicf.activator.name", hook.activator.toString())
    }

    private fun closeAllSpans(context: BotContext) {
        context.closeAllTelemetrySpans()
    }

    private fun createSpanWithParent(
        telemetryProvider: TelemetryProvider,
        name: String,
        attributes: Map<String, Any?>,
        parentSpan: TelemetrySpan?
    ): TelemetrySpan {
        return try {
            telemetryProvider.createSpan(name, attributes, parentSpan)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
    }

    fun handleAgentInvokeStart(telemetryProvider: TelemetryProvider, hook: AgentInvokeStartHook) {
        // Если есть активный Handoff span, делаем его родителем, иначе используем текущий JAICF span
        val parent = getSpan(hook.context, LLMSpanName.Handoff) ?: currentTelemetrySpanBlocking(hook.context)
        val attributes = hook.attributes.toMutableMap().apply {
            put("llm.agent.state", hook.state.path.toString())
            put("llm.agent.input.length", hook.request.input.length)
        }
        val span = try {
            telemetryProvider.createSpan(LLMSpanName.AgentInvoke, attributes, parent)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
        if (span != TelemetrySpan.NoOp) {
            addCommonAttributes(span, hook)
            setSpan(hook.context, LLMSpanName.AgentInvoke, span)
            // Устанавливаем как текущий span для дочерних спанов (LLM Call, ToolCalls)
            hook.context.setCurrentTelemetrySpan(span)
        }
    }

    fun handleAgentInvokeFinish(hook: AgentInvokeFinishHook) {
        getSpan(hook.context, LLMSpanName.AgentInvoke)?.apply {
            hook.attributes.forEach { (k, v) -> setAttribute(k, v) }
            close()
            removeSpan(hook.context, LLMSpanName.AgentInvoke)
        }

        // Если Handoff span был активен для этого AgentInvoke, закрываем его после завершения агента
        getSpan(hook.context, LLMSpanName.Handoff)?.apply {
            close()
            removeSpan(hook.context, LLMSpanName.Handoff)
        }

        // Восстанавливаем родительский span (jaicf.process.start или jaicf.action.start) как текущий
        val parentSpan = hook.context.getTelemetrySpan("jaicf.action.start") 
            ?: hook.context.getTelemetrySpan("jaicf.process.start")
        hook.context.setCurrentTelemetrySpan(parentSpan)
    }

    fun handleBeforeLLMCall(telemetryProvider: TelemetryProvider, hook: BeforeLLMCallHook) {
        // Получаем Agent span как parent
        val parentSpan = getSpan(hook.context, LLMSpanName.AgentInvoke) ?: currentTelemetrySpanBlocking(hook.context)
        val span = createSpanWithParent(telemetryProvider, LLMSpanName.LLMCall, hook.attributes, parentSpan)
        if (span != TelemetrySpan.NoOp) {
            addCommonAttributes(span, hook)
            setSpan(hook.context, LLMSpanName.LLMCall, span)
            // Устанавливаем как текущий span для дочерних спанов (Streaming, ToolCalls)
            hook.context.setCurrentTelemetrySpan(span)
        }
    }


    fun handleAfterLLMCall(hook: AfterLLMCallHook) {
        getSpan(hook.context, LLMSpanName.LLMCall)?.apply {
            hook.attributes.forEach { (k, v) -> setAttribute(k, v) }

            hook.completionUsage?.let { usage ->
                setAttribute("llm.tokens.prompt", usage.promptTokens())
                setAttribute("llm.tokens.completion", usage.completionTokens())
                setAttribute("llm.tokens.total", usage.totalTokens())
            }

            close()
            removeSpan(hook.context, LLMSpanName.LLMCall)
        }
        // Восстанавливаем Agent span как текущий
        val parentSpan = getSpan(hook.context, LLMSpanName.AgentInvoke)
        hook.context.setCurrentTelemetrySpan(parentSpan)
    }

    fun handleToolCallsStart(telemetryProvider: TelemetryProvider, hook: ToolCallsStartHook) {
        // Получаем LLM Call span как parent
        val parentSpan = getSpan(hook.context, LLMSpanName.LLMCall) ?: currentTelemetrySpanBlocking(hook.context)
        val span = createSpanWithParent(telemetryProvider, LLMSpanName.ToolCalls, hook.attributes, parentSpan)
        if (span != TelemetrySpan.NoOp) {
            addCommonAttributes(span, hook)
            setSpan(hook.context, LLMSpanName.ToolCalls, span)
            // Устанавливаем как текущий span для дочерних ToolCall спанов
            hook.context.setCurrentTelemetrySpan(span)
        }
    }

    fun handleToolCallsFinish(hook: ToolCallsFinishHook) {
        getSpan(hook.context, LLMSpanName.ToolCalls)?.apply {
            hook.attributes.forEach { (k, v) -> setAttribute(k, v) }
            close()
            removeSpan(hook.context, LLMSpanName.ToolCalls)
        }
        // Восстанавливаем LLM Call span как текущий
        val parentSpan = getSpan(hook.context, LLMSpanName.LLMCall)
        hook.context.setCurrentTelemetrySpan(parentSpan)
    }

    fun handleToolCallStart(telemetryProvider: TelemetryProvider, hook: ToolCallStartHook) {
        val toolName = hook.attributes["llm.tool.name"] as? String ?: "unknown"
        val parentSpan = getSpan(hook.context, LLMSpanName.ToolCalls) ?: currentTelemetrySpanBlocking(hook.context)
        val span = createSpanWithParent(telemetryProvider, LLMSpanName.ToolCall, hook.attributes, parentSpan)
        if (span != TelemetrySpan.NoOp) {
            addCommonAttributes(span, hook)
            setSpan(hook.context, "${LLMSpanName.ToolCall}:$toolName", span)
        }
    }

    fun handleToolCallFinish(hook: ToolCallFinishHook) {
        val toolName = hook.attributes["llm.tool.name"] as? String ?: "unknown"
        getSpan(hook.context, "${LLMSpanName.ToolCall}:$toolName")?.apply {
            hook.attributes.forEach { (k, v) -> setAttribute(k, v) }
            close()
            removeSpan(hook.context, "${LLMSpanName.ToolCall}:$toolName")
        }
    }

    fun handleToolCallError(hook: ToolCallErrorHook) {
        val toolName = hook.attributes["llm.tool.name"] as? String ?: "unknown"
        getSpan(hook.context, "${LLMSpanName.ToolCall}:$toolName")?.apply {
            hook.attributes.forEach { (k, v) -> setAttribute(k, v) }
            hook.attributes["llm.tool.error"]?.let { error ->
                val errorType = error::class.simpleName ?: "Unknown"
                setAttribute("llm.tool.error.type", errorType)
                if (error is Throwable) {
                    error.message?.take(200)?.let { msg ->
                        setAttribute("llm.tool.error.message", msg)
                    }
                }
            }
            close()
            removeSpan(hook.context, "${LLMSpanName.ToolCall}:$toolName")
        }
    }

    fun handleHandoffStart(telemetryProvider: TelemetryProvider, hook: HandoffStartHook) {
        val parentSpan = currentTelemetrySpanBlocking(hook.context)
        val span = createSpanWithParent(telemetryProvider, LLMSpanName.Handoff, hook.attributes, parentSpan)
        if (span != TelemetrySpan.NoOp) {
            addCommonAttributes(span, hook)
            setSpan(hook.context, LLMSpanName.Handoff, span)
        }
    }

    fun handleBeforeAction(hook: BeforeActionHook) {
        val handoffInfo = hook.context.handoffInfo
        if (handoffInfo != null) {
            currentTelemetrySpanBlocking(hook.context)?.apply {
                handoffInfo.forEach { (k, v) -> setAttribute(k, v) }
            }
            hook.context.handoffInfo = null
        }
    }

    fun handleToolExecuteStart(telemetryProvider: TelemetryProvider, hook: ToolExecuteStartHook) {
        val toolName = hook.attributes["llm.tool.name"] as? String ?: "unknown"
        val callId = hook.attributes["llm.tool.call_id"] as? String ?: ""
        val spanName = "llm.tool.execute:$toolName:$callId"

        val parentSpan = currentTelemetrySpanBlocking(hook.context)
        val span = createSpanWithParent(telemetryProvider, "llm.tool.execute.$toolName", hook.attributes, parentSpan)
        if (span != TelemetrySpan.NoOp) {
            span.setAttribute("jaicf.client.id", hook.context.clientId)
            span.setAttribute("jaicf.request.type", hook.request.type.name)
            hook.attributes.forEach { (k, v) -> span.setAttribute(k, v) }
            setSpan(hook.context, spanName, span)
        }
    }

    fun handleToolExecuteFinish(hook: ToolExecuteFinishHook) {
        val toolName = hook.attributes["llm.tool.name"] as? String ?: "unknown"
        val callId = hook.attributes["llm.tool.call_id"] as? String ?: ""
        val spanName = "llm.tool.execute:$toolName:$callId"
        getSpan(hook.context, spanName)?.apply {
            hook.attributes.forEach { (k, v) -> setAttribute(k, v) }
            close()
            removeSpan(hook.context, spanName)
        }
    }

    fun handleToolExecuteError(hook: ToolExecuteErrorHook) {
        val toolName = hook.attributes["llm.tool.name"] as? String ?: "unknown"
        val callId = hook.attributes["llm.tool.call_id"] as? String ?: ""
        val spanName = "llm.tool.execute:$toolName:$callId"
        getSpan(hook.context, spanName)?.apply {
            hook.attributes.forEach { (k, v) -> setAttribute(k, v) }
            hook.attributes["llm.tool.error"]?.let { error ->
                val errorType = error::class.simpleName ?: "Unknown"
                setAttribute("llm.tool.error.type", errorType)
                if (error is Throwable) {
                    recordException(error)
                    error.message?.take(200)?.let { msg ->
                        setAttribute("llm.tool.error.message", msg)
                    }
                }
            }
            close()
            removeSpan(hook.context, spanName)
        }
    }

    fun handleBotRequest(hook: BotRequestHook) {
        //closeAllSpans(hook.context)
    }

    fun handleAnyError(hook: AnyErrorHook) {
        currentTelemetrySpanBlocking(hook.context)?.apply {
            setAttribute("jaicf.error.type", hook.exception::class.simpleName ?: "Unknown")
            hook.exception.message?.take(500)?.let { msg ->
                setAttribute("jaicf.error.message", msg)
            }
        }
        closeAllSpans(hook.context)
    }
}
