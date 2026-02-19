package com.justai.jaicf.activator.llm.memory.transformers

import com.justai.jaicf.activator.llm.LLMMessage
import com.justai.jaicf.activator.llm.memory.MessagesTransform
import com.justai.jaicf.activator.llm.memory.transform
import com.openai.models.chat.completions.ChatCompletionMessageParam
import kotlin.jvm.optionals.getOrNull

fun withSystemMessage(message: String): MessagesTransform =
    withSystemMessage { message }

fun withSystemMessage(producer: () -> String): MessagesTransform = { messages ->
    val msg = LLMMessage.system(producer.invoke())
    if (messages.isEmpty()) {
        listOf(msg)
    } else {
        messages.toMutableList().apply {
            val idx = indexOfFirst { it.isSystem() }
            if (idx != -1) set(idx, msg)
            else add(msg)
        }.toList()
    }
}

fun List<ChatCompletionMessageParam>?.withSystemMessage(name: String, message: String) = transform { messages ->
    val msg = LLMMessage.system {
        name(name)
        content(message)
    }
    val idx = messages.indexOfLast { it.isSystem() && it.asSystem().name().getOrNull() == name }
    messages.toMutableList().apply {
        if (idx != -1) {
            set(idx, msg)
        } else {
            add(indexOfLast { it.isSystem() } + 1, msg)
        }
    }.toList()
}