package com.justai.jaicf.activator.llm.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@JsonIgnoreProperties(ignoreUnknown = true)
data class LLMResponse(
    val id: String,
    val usage: Usage,
    val choices: List<Choice>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Usage(
        @JsonProperty("prompt_tokens")
        val promptTokens: Int,
        @JsonProperty("completion_tokens")
        val completionTokens: Int,
        @JsonProperty("total_tokens")
        val totalTokens: Int
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Choice(
        val message: Message
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Message(
            val content: String?,
            @JsonProperty("function_call")
            val functionCall: FunctionCall?
        ) {
            fun toRequestMessage() = when {
                functionCall != null -> LLMRequest.FunctionMessage(functionCall)
                content != null -> LLMRequest.Message.assistant(content)
                else -> throw IllegalArgumentException("Message type is not supported")
            }
        }
    }

    data class FunctionCall(
        val name: String,
        val arguments: String,
    )

    inline fun <reified T> parse(): T =
        mapper.readValue(choices.first().message.content, T::class.java)

    companion object {
        val mapper = jacksonObjectMapper()
    }
}