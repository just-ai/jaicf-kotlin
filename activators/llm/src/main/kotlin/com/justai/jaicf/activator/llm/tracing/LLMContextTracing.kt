package com.justai.jaicf.activator.llm.tracing

import com.justai.jaicf.activator.llm.LLMContext
import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.completions.CompletionUsage
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionToolMessageParam

/**
 * Extension functions for LLMContext to manage tracing data
 */
fun LLMContext.startTracing(
    params: com.openai.models.chat.completions.ChatCompletionCreateParams,
    props: LLMProps
): Map<String, String> {
    if (!props.isTracingEnabled) return emptyMap()
    
    val messages = params.messages().map { message ->
        when (message) {
            is ChatCompletionUserMessageParam -> mapOf<String, Any>(
                TracingConstants.KEY_ROLE to "user",
                TracingConstants.KEY_CONTENT to (message.content()?.toString() ?: ""),
                TracingConstants.KEY_TOOL_CALLS to emptyList<Any>()
            )
            is ChatCompletionAssistantMessageParam -> mapOf<String, Any>(
                TracingConstants.KEY_ROLE to "assistant",
                TracingConstants.KEY_CONTENT to (message.content()?.toString() ?: ""),
                TracingConstants.KEY_TOOL_CALLS to (message.toolCalls()?.orElse(null) ?: emptyList<Any>())
            )
            is ChatCompletionSystemMessageParam -> mapOf<String, Any>(
                TracingConstants.KEY_ROLE to "system",
                TracingConstants.KEY_CONTENT to (message.content()?.toString() ?: ""),
                TracingConstants.KEY_TOOL_CALLS to emptyList<Any>()
            )
            is ChatCompletionToolMessageParam -> mapOf<String, Any>(
                TracingConstants.KEY_ROLE to "tool",
                TracingConstants.KEY_CONTENT to (message.content()?.toString() ?: ""),
                TracingConstants.KEY_TOOL_CALLS to emptyList<Any>()
            )
            else -> mapOf<String, Any>(
                TracingConstants.KEY_ROLE to "unknown",
                TracingConstants.KEY_CONTENT to "",
                TracingConstants.KEY_TOOL_CALLS to emptyList<Any>()
            )
        }
    }
    
    val manager = TracingManager.get()
    val runIds = manager.startLLMRun(context, request, props, messages)
    
    // Store run IDs in context for later use
    context.temp[TracingConstants.CONTEXT_LLM_RUN_IDS] = runIds
    
    return runIds
}

fun LLMContext.endTracing(
    completion: ChatCompletion,
    usage: CompletionUsage?
) {
    if (!props.isTracingEnabled) return
    
    val currentRunIds = context.temp[TracingConstants.CONTEXT_LLM_RUN_IDS] as? Map<String, String> ?: return
    if (currentRunIds.isEmpty()) return
    
    val manager = TracingManager.get()
    manager.endLLMRun(currentRunIds, completion, usage)
    
    // Clean up
    context.temp.remove(TracingConstants.CONTEXT_LLM_RUN_IDS)
}

fun LLMContext.startToolTracing(
    toolCall: ChatCompletionMessageToolCall,
    arguments: Any?
): Map<String, String> {
    if (!props.isTracingEnabled) return emptyMap()
    
    val currentRunIds = context.temp[TracingConstants.CONTEXT_LLM_RUN_IDS] as? Map<String, String> ?: return emptyMap()
    if (currentRunIds.isEmpty()) return emptyMap()
    
    val manager = TracingManager.get()
    val toolRunIds = manager.startToolRun(currentRunIds, toolCall, arguments)
    
    // Store tool run IDs in context
    context.temp[TracingConstants.CONTEXT_TOOL_RUN_IDS] = toolRunIds
    
    return toolRunIds
}

fun LLMContext.endToolTracing(
    toolRunIds: Map<String, String>,
    result: LLMToolResult
) {
    if (!props.isTracingEnabled) return
    
    val manager = TracingManager.get()
    manager.endToolRun(toolRunIds, result)
    
    // Clean up
    context.temp.remove(TracingConstants.CONTEXT_TOOL_RUN_IDS)
}

fun LLMContext.startChainTracing(
    name: String
): Map<String, String> {
    if (!props.isTracingEnabled) return emptyMap()
    
    val manager = TracingManager.get()
    val chainRunIds = manager.startChainRun(context, request, name)
    
    // Store chain run IDs in context
    context.temp[TracingConstants.CONTEXT_CHAIN_RUN_IDS] = chainRunIds
    
    return chainRunIds
}

fun LLMContext.endChainTracing(
    chainRunIds: Map<String, String>,
    outputs: Map<String, Any>
) {
    if (!props.isTracingEnabled) return
    
    val manager = TracingManager.get()
    manager.endChainRun(chainRunIds, outputs)
    
    // Clean up
    context.temp.remove(TracingConstants.CONTEXT_CHAIN_RUN_IDS)
}
