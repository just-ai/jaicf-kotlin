package com.justai.jaicf.activator.llm.test

import com.openai.core.http.StreamResponse
import com.openai.models.chat.completions.ChatCompletionChunk
import java.util.stream.Collectors


fun ChatCompletionChunk.toContent(): String {
    return this.choices().firstOrNull()?.delta()?.content()?.orElse("") ?: ""
}

fun StreamResponse<ChatCompletionChunk>.toContent(): String {
    return this.stream().map { it.toContent() }.collect(Collectors.joining())
}