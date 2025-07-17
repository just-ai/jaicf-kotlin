package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionMessage
import com.openai.models.chat.completions.ChatCompletionMessageToolCall

sealed class LLMEvent(val type: Type) {
    enum class Type {
        Start,
        Chunk,
        Delta,
        ContentDelta,
        Content,
        ToolCalls,
        ToolCallResults,
        Message,
        Finish,
    }

    class Start : LLMEvent(Type.Start)
    data class Chunk(val chunk: ChatCompletionChunk) : LLMEvent(Type.Chunk)
    data class Delta(val delta: ChatCompletionChunk.Choice.Delta) : LLMEvent(Type.Delta)
    data class ContentDelta(val delta: String) : LLMEvent(Type.ContentDelta)
    data class Content(val content: String) : LLMEvent(Type.Content)
    data class ToolCalls(val calls: List<ChatCompletionMessageToolCall>) : LLMEvent(Type.ToolCalls)
    data class ToolCallResults(val results: List<LLMToolResult>) : LLMEvent(Type.ToolCallResults)
    data class Message(val message: ChatCompletionMessage) : LLMEvent(Type.Message)
    data class Finish(val reason: ChatCompletionChunk.Choice.FinishReason.Value) : LLMEvent(Type.Finish)
}