package com.justai.jaicf.activator.llm

import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.openai.models.chat.completions.ChatCompletionMessageParam

private val imgRegex = Regex("""https?://\S+?\.(png|jpe?g)""", RegexOption.IGNORE_CASE)

object LLMInputs {
    val TextOnly: LLMInputBuilder = {
        it.hasQuery().ifTrue { listOf(LLMMessage.user(it.input)) }
    }

    val WithImages: LLMInputBuilder = { request ->
        request.hasQuery().ifTrue {
            mutableListOf<ChatCompletionMessageParam>().apply {
                addAll(imgRegex.findAll(request.input).map { it.value }.map(LLMMessage::image))
                imgRegex.replace(request.input, "").trim().isNotBlank().ifTrue {
                    add(LLMMessage.user(request.input))
                }
            }.toList()
        }
    }
}