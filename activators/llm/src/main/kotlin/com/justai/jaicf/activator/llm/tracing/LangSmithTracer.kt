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
 * LangSmith tracer implementation
 * Note: This is a simplified implementation. In a real scenario, you would use the LangSmith SDK
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

    override val isEnabled: Boolean = config.enabled
    override val name: String = TracingConstants.TRACER_LANGSMITH

    override fun startLLMRun(
        context: BotContext,
        request: BotRequest,
        props: LLMProps,
        messages: List<Map<String, Any>>
    ): String {
        if (!isEnabled) return ""
        
        val runId = "langsmith_${System.currentTimeMillis()}_${context.clientId}"
        
        logger.info("LangSmith: Started LLM run $runId")
        logger.debug("LangSmith: LLM run inputs - model: ${props.model}, messages: $messages")
        
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
        
        logger.info("LangSmith: Ended LLM run $runId")
        logger.debug("LangSmith: LLM run outputs - content: $content, finish_reason: $finishReason, usage: $usage")
    }

    override fun startToolRun(
        parentRunId: String,
        toolCall: ChatCompletionMessageToolCall,
        arguments: Any?
    ): String {
        if (!isEnabled) return ""
        
        val runId = "langsmith_tool_${System.currentTimeMillis()}_${toolCall.id()}"
        
        logger.info("LangSmith: Started tool run $runId for tool ${toolCall.function().name()}")
        logger.debug("LangSmith: Tool run inputs - arguments: $arguments")
        
        return runId
    }

    override fun endToolRun(
        runId: String,
        result: LLMToolResult
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        logger.info("LangSmith: Ended tool run $runId")
        logger.debug("LangSmith: Tool run outputs - result: $result")
    }

    override fun startChainRun(
        context: BotContext,
        request: BotRequest,
        name: String
    ): String {
        if (!isEnabled) return ""
        
        val runId = "langsmith_chain_${System.currentTimeMillis()}_${context.clientId}"
        
        logger.info("LangSmith: Started chain run $runId: $name")
        
        return runId
    }

    override fun endChainRun(
        runId: String,
        outputs: Map<String, Any>
    ) {
        if (!isEnabled || runId.isEmpty()) return
        
        logger.info("LangSmith: Ended chain run $runId")
        logger.debug("LangSmith: Chain run outputs: $outputs")
    }
}
