package com.justai.jaicf.activator.llm.memory.transformers

import com.justai.jaicf.activator.llm.LLMMessage
import com.justai.jaicf.activator.llm.memory.MessagesTransform
import com.justai.jaicf.activator.llm.memory.transform
import com.openai.models.chat.completions.ChatCompletionMessageParam
import kotlin.jvm.optionals.getOrNull

/**
 * Creates a [MessagesTransform] that ensures a system message with the given static [message] is present in the list.
 * If a system message already exists, it is replaced; otherwise, the message is appended.
 *
 * @param message the static system message content.
 * @return a [MessagesTransform] that inserts or replaces the system message.
 */
fun withSystemMessage(message: String): MessagesTransform =
    withSystemMessage { message }

/**
 * Creates a [MessagesTransform] that ensures a system message produced by [producer] is present in the list.
 * If the list is empty, a new list with only the system message is returned.
 * If a system message already exists, it is replaced at its position; otherwise, the message is appended.
 *
 * @param producer a lambda that returns the system message content on each invocation.
 * @return a [MessagesTransform] that inserts or replaces the system message.
 */
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

/**
 * Transforms this list of messages by inserting or updating a named system message with the given [message] content.
 * If a system message with the same [name] already exists, it is replaced in place.
 * Otherwise, the new message is inserted right after the last existing system message.
 *
 * @param name the name identifier for the system message.
 * @param message the system message content.
 * @return a new list with the named system message inserted or updated.
 */
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