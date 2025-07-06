package com.justai.jaicf.activator.llm

import com.justai.jaicf.context.BotContext
import com.openai.models.chat.completions.ChatCompletionMessageParam as Message
import kotlin.collections.emptyList

typealias MessagesTransform = (List<Message>) -> List<Message>

class LLMMemory(
    private val botContext: BotContext,
    val key: String,
    private val transform: MessagesTransform? = null,
) : AbstractList<Message>() {
    private val messages =
        botContext.session.getOrDefault(key, emptyList<Message>()) as List<Message>

    private var list = messages

    init {
        transform(transform)
    }

    val initial
        get() = transform?.invoke(emptyList()) ?: emptyList()

    override val size
        get() = list.size

    override fun get(index: Int) = list[index]

    fun set(messages: List<Message>) {
        list = messages
        botContext.session[key] = messages
    }

    fun transform(transform: MessagesTransform?) = apply {
        list = transform?.invoke(list) ?: list
    }
}

fun BotContext.llmMemory(key: String, transform: MessagesTransform? = null) =
    LLMMemory(this, key, transform)

fun withSystemMessage(message: String): MessagesTransform =
    withSystemMessage({message})

fun withSystemMessage(producer: () -> String): MessagesTransform = { messages ->
    val msg = LLMMessage.system(producer.invoke())
    if (messages.isEmpty()) {
        listOf(msg)
    } else {
        messages.toMutableList().apply {
            val idx = indexOfFirst { it.isSystem() }
            if (idx != -1) set(idx, msg)
            else add(msg)
        }
    }
}

fun List<Message>.ifLLMMemory(block: (memory: LLMMemory) -> Unit) =
    apply {
        if (this is LLMMemory) {
            block(this)
        }
    }

fun List<Message>?.transform(
    transform: MessagesTransform,
) = let { messages ->
    if (messages == null || messages !is LLMMemory) {
        transform.invoke(messages.orEmpty())
    } else {
        messages.transform(transform)
    }
}