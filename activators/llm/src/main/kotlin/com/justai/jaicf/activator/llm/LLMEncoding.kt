package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.client.LLMRequest
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding

object LLMEncoding {
    private val registry = Encodings.newDefaultEncodingRegistry()

    private fun countTokens(request: LLMRequest, encoding: Encoding?) =
        (request.messages.joinToString("\n") + "\n" + request.functions?.toString().orEmpty()).let { text ->
            encoding?.countTokens(text) ?: text.length
        }

    fun trim(
        request: LLMRequest,
        model: LLMSettings.Model,
        minTokens: Int = model.maxContextLength / 4
    ): LLMRequest {
        val enc = registry.getEncodingForModel(model.name).orElse(null)
        val messages = request.messages.toMutableList()
        var tokens = countTokens(request, enc)
        while (tokens > model.maxContextLength - minTokens) {
            val index = messages.indexOfFirst { !it.role.isSystem }
            if (index == -1) {
                break
            } else {
                messages.removeAt(index)
                tokens = countTokens(request.copy(messages = messages), enc)
            }
        }
        if (messages.none { !it.role.isSystem }) {
            throw IllegalArgumentException("Messages history is too big for this model")
        }
        return request.copy(messages = messages)
    }
}