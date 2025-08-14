package com.justai.jaicf.activator.llm.tracing

import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.completions.CompletionUsage
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull

/**
 * OpenTelemetry tracer implementation with real API integration
 */
class OpenTelemetryTracer(
    private val config: OpenTelemetryConfig
) : Tracer {
    
    companion object {
        private val logger = LoggerFactory.getLogger(OpenTelemetryTracer::class.java)
        
        fun init(config: OpenTelemetryConfig): OpenTelemetryTracer {
            return OpenTelemetryTracer(config)
        }
    }

    private val client = OpenTelemetryClient(config)
    private val activeSpans = mutableMapOf<String, Any>()

    override val isEnabled: Boolean = config.enabled
    override val name: String = TracingConstants.TRACER_OPENTELEMETRY

    override fun startLLMRun(
        context: BotContext,
        request: BotRequest,
        props: LLMProps,
        messages: List<Map<String, Any>>
    ): String {
        if (!isEnabled) return ""
        
        val runId = "otel_${System.currentTimeMillis()}_${context.clientId}"
        
        val attributes = mapOf(
            "model" to (props.model ?: "unknown"),
            "temperature" to (props.temperature ?: 0.0),
            "max_tokens" to (props.maxTokens ?: 0),
            "messages_count" to messages.size,
            "bot_context_id" to context.clientId,
            "request_id" to request.toString(),
            "channel" to request.javaClass.simpleName,
            "session_id" to context.clientId,
            "client_id" to context.clientId
        )
        
        val span = client.startLLMSpan("LLM Call", attributes)
        activeSpans[runId] = span
        
        logger.info("OpenTelemetry: Started LLM run $runId")
        logger.debug("OpenTelemetry: LLM run inputs - model: ${props.model}, messages: $messages")
        
        return runId
    }

    override fun endLLMRun(
        runId: String,
        completion: ChatCompletion,
        usage: CompletionUsage?
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        val span = activeSpans.remove(runId) ?: return
        
        val firstChoice = completion.choices().firstOrNull()
        val content = firstChoice?.message()?.content()?.toString()
        val finishReason = firstChoice?.finishReason()?.toString()
        
        // Add completion data as span events
        client.addEvent(span, "completion.received", mapOf(
            "content" to (content ?: ""),
            "finish_reason" to (finishReason ?: ""),
            "choices_count" to completion.choices().size,
            "model" to completion.model()
        ))
        
        if (usage != null) {
            client.addEvent(span, "usage.stats", mapOf(
                "usage_available" to true
            ))
        }
        
        client.endSpan(span)
        
        logger.info("OpenTelemetry: Ended LLM run $runId")
        logger.debug("OpenTelemetry: LLM run outputs - content: $content, finish_reason: $finishReason, usage: $usage")
    }

    override fun startToolRun(
        parentRunId: String,
        toolCall: ChatCompletionMessageToolCall,
        arguments: Any?
    ): String {
        if (!isEnabled) return ""
        
        val runId = "otel_tool_${System.currentTimeMillis()}_${toolCall.id()}"
        val parentSpan = activeSpans[parentRunId]
        
        if (parentSpan == null) {
            logger.warn("OpenTelemetry: Parent span not found for tool run $runId")
            return ""
        }
        
        val attributes = mapOf(
            "tool_name" to toolCall.function().name(),
            "tool_id" to toolCall.id(),
            "arguments" to (arguments?.toString() ?: "")
        )
        
        val span = client.startToolSpan("Tool Call: ${toolCall.function().name()}", parentSpan, attributes)
        activeSpans[runId] = span
        
        logger.info("OpenTelemetry: Started tool run $runId for tool ${toolCall.function().name()}")
        logger.debug("OpenTelemetry: Tool run inputs - arguments: $arguments")
        
        return runId
    }

    override fun endToolRun(
        runId: String,
        result: LLMToolResult
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        val span = activeSpans.remove(runId) ?: return
        
        client.addEvent(span, "tool.result", mapOf(
            "tool_name" to result.name,
            "success" to (result.result !is String || !result.result.toString().startsWith("Error:"))
        ))
        
        client.endSpan(span)
        
        logger.info("OpenTelemetry: Ended tool run $runId")
        logger.debug("OpenTelemetry: Tool run outputs - result: $result")
    }

    override fun startChainRun(
        context: BotContext,
        request: BotRequest,
        name: String
    ): String {
        if (!isEnabled) return ""
        
        val runId = "otel_chain_${System.currentTimeMillis()}_${context.clientId}"
        
        val attributes = mapOf(
            "chain_name" to name,
            "bot_context_id" to context.clientId,
            "request_id" to request.toString(),
            "channel" to request.javaClass.simpleName,
            "session_id" to context.clientId,
            "client_id" to context.clientId
        )
        
        val span = client.startChainSpan("Chain: $name", attributes)
        activeSpans[runId] = span
        
        logger.info("OpenTelemetry: Started chain run $runId: $name")
        
        return runId
    }

    override fun endChainRun(
        runId: String,
        outputs: Map<String, Any>
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        val span = activeSpans.remove(runId) ?: return
        
        client.addEvent(span, "chain.completed", outputs)
        client.endSpan(span)
        
        logger.info("OpenTelemetry: Ended chain run $runId")
        logger.debug("OpenTelemetry: Chain run outputs: $outputs")
    }
}
