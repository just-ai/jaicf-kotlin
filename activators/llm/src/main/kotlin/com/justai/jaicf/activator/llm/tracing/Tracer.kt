package com.justai.jaicf.activator.llm.tracing

import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.completions.CompletionUsage

/**
 * Interface for LLM tracing implementations
 */
interface Tracer {

    /**
     * Whether this tracer is enabled
     */
    val isEnabled: Boolean

    /**
     * Name of this tracer
     */
    val name: String

    /**
     * Start tracing an LLM call
     */
    fun startLLMRun(
        context: BotContext,
        request: BotRequest,
        props: LLMProps,
        messages: List<Map<String, Any>>
    ): String

    /**
     * End tracing an LLM call
     */
    fun endLLMRun(
        runId: String,
        completion: ChatCompletion,
        usage: CompletionUsage?
    )

    /**
     * Start tracing a tool call
     */
    fun startToolRun(
        parentRunId: String,
        toolCall: ChatCompletionMessageToolCall,
        arguments: Any?
    ): String

    /**
     * End tracing a tool call
     */
    fun endToolRun(
        runId: String,
        result: LLMToolResult
    )

    /**
     * Start tracing a chain/sequence of operations
     */
    fun startChainRun(
        context: BotContext,
        request: BotRequest,
        name: String
    ): String

    /**
     * End tracing a chain
     */
    fun endChainRun(
        runId: String,
        outputs: Map<String, Any>
    )
}
