package com.justai.jaicf.activator.llm.tracing

import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.completions.CompletionUsage
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * LangSmith tracer implementation with real API integration
 */
class LangSmithTracer(
    private val config: LangSmithConfig
) : Tracer {
    
    companion object {
        private val logger = LoggerFactory.getLogger(LangSmithTracer::class.java)
        
        fun init(): LangSmithTracer {
            val config = LangSmithConfig.fromEnvironment()
            return LangSmithTracer(config)
        }
    }

    private val client = LangSmithClient(config)

    override val isEnabled: Boolean = config.enabled
    override val name: String = TracingConstants.TRACER_LANGSMITH

    override fun startLLMRun(
        context: BotContext,
        request: BotRequest,
        props: LLMProps,
        messages: List<Map<String, Any>>
    ): String {
        if (!isEnabled) return ""
        val runId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        
        val inputs = mapOf(
            "model" to (props.model ?: "unknown"),
            "temperature" to (props.temperature ?: 0.0),
            "max_tokens" to (props.maxTokens ?: 0),
            "messages_count" to messages.size,
            "messages" to messages,
            "bot_context_id" to context.clientId,
            "request_id" to request.toString(),
            "channel" to (request.javaClass.simpleName),
            "session_id" to context.clientId,
            "client_id" to context.clientId
        )
        
        // Create run in LangSmith
        val success = client.createRun(
            runId = runId,
            name = "LLM Call",
            runType = "llm",
            inputs = inputs,
            startTime = startTime
        )
        
        if (success) {
            logger.info("LangSmith: Started LLM run $runId")
            logger.debug("LangSmith: LLM run inputs - model: ${props.model}, messages: $messages")
        } else {
            logger.warn("LangSmith: Failed to create LLM run $runId")
            logger.debug("LangSmith: Failed inputs - model: ${props.model}, messages_count: ${messages.size}")
        }
        
        return runId
    }

    override fun endLLMRun(
        runId: String,
        completion: ChatCompletion,
        usage: CompletionUsage?
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        val firstChoice = completion.choices().firstOrNull()
        val content = firstChoice?.message()?.content()?.toString()
        val finishReason = firstChoice?.finishReason()?.toString()
        
        val outputs = mapOf(
            "content" to (content ?: ""),
            "finish_reason" to (finishReason ?: ""),
            "choices_count" to completion.choices().size,
            "model" to completion.model(),
            "created" to completion.created()
        )
        
        // Update run in LangSmith
        val success = client.updateRun(
            runId = runId,
            outputs = outputs
        )
        
        if (success) {
            logger.info("LangSmith: Ended LLM run $runId")
            logger.debug("LangSmith: LLM run outputs - content: $content, finish_reason: $finishReason, usage: $usage")
        } else {
            logger.warn("LangSmith: Failed to update LLM run $runId")
        }
    }

    override fun startToolRun(
        parentRunId: String,
        toolCall: ChatCompletionMessageToolCall,
        arguments: Any?
    ): String {
        if (!isEnabled) return ""
        
        val runId = "langsmith_tool_${System.currentTimeMillis()}_${toolCall.id()}"
        val startTime = System.currentTimeMillis()
        
        val inputs = mapOf(
            "tool_name" to toolCall.function().name(),
            "tool_id" to toolCall.id(),
            "arguments" to (arguments ?: ""),
            "parent_run_id" to parentRunId
        )
        
        // Create child run in LangSmith
        val success = client.createChildRun(
            runId = runId,
            parentRunId = parentRunId,
            name = "Tool Call: ${toolCall.function().name()}",
            runType = "tool",
            inputs = inputs,
            startTime = startTime
        )
        
        if (success) {
            logger.info("LangSmith: Started tool run $runId for tool ${toolCall.function().name()}")
            logger.debug("LangSmith: Tool run inputs - arguments: $arguments")
        } else {
            logger.warn("LangSmith: Failed to create tool run $runId")
        }
        
        return runId
    }

    override fun endToolRun(
        runId: String,
        result: LLMToolResult
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        val outputs = mapOf(
            "tool_name" to result.name,
            "tool_result" to (result.result ?: ""),
            "success" to (result.result !is String || !result.result.toString().startsWith("Error:"))
        )
        
        // Update tool run in LangSmith
        val success = client.updateRun(
            runId = runId,
            outputs = outputs
        )
        
        if (success) {
            logger.info("LangSmith: Ended tool run $runId")
            logger.debug("LangSmith: Tool run outputs - result: $result")
        } else {
            logger.warn("LangSmith: Failed to update tool run $runId")
        }
    }

    override fun startChainRun(
        context: BotContext,
        request: BotRequest,
        name: String
    ): String {
        if (!isEnabled) return ""

        val runId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        
        val inputs = mapOf(
            "chain_name" to name,
            "bot_context_id" to context.clientId,
            "request_id" to request.toString(),
            "channel" to (request.javaClass.simpleName),
            "session_id" to context.clientId,
            "client_id" to context.clientId
        )
        
        // Create run in LangSmith
        val success = client.createRun(
            runId = runId,
            name = "Chain: $name",
            runType = "chain",
            inputs = inputs,
            startTime = startTime
        )
        
        if (success) {
            logger.info("LangSmith: Started chain run $runId: $name")
        } else {
            logger.warn("LangSmith: Failed to create chain run $runId")
        }
        
        return runId
    }

    override fun endChainRun(
        runId: String,
        outputs: Map<String, Any>
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        // Update chain run in LangSmith
        val success = client.updateRun(
            runId = runId,
            outputs = outputs
        )
        
        if (success) {
            logger.info("LangSmith: Ended chain run $runId")
            logger.debug("LangSmith: Chain run outputs: $outputs")
        } else {
            logger.warn("LangSmith: Failed to update chain run $runId")
        }
    }
}
