package com.justai.jaicf.activator.llm.memory.transformers

import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.chat.completions.ChatCompletionToolMessageParam

internal data class ToolCallSpec(val id: String, val name: String, val arguments: String = "{}")

internal fun toolCall(vararg calls: ToolCallSpec) =
    ChatCompletionMessageParam.ofAssistant(
        ChatCompletionAssistantMessageParam.builder()
            .toolCalls(calls.map { call ->
                ChatCompletionMessageToolCall.ofFunction(
                    ChatCompletionMessageFunctionToolCall.builder()
                        .id(call.id)
                        .function(
                            ChatCompletionMessageFunctionToolCall.Function.builder()
                                .name(call.name)
                                .arguments(call.arguments)
                                .build()
                        )
                        .build()
                )
            }).build()
    )

internal fun toolCall(toolCallId: String, toolName: String, arguments: String = "{}") =
    toolCall(ToolCallSpec(toolCallId, toolName, arguments))

internal fun toolResult(toolCallId: String, content: String) =
    ChatCompletionMessageParam.ofTool(
        ChatCompletionToolMessageParam.builder()
            .toolCallId(toolCallId)
            .content(content)
            .build()
    )