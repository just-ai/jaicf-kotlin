package com.justai.jaicf.activator.llm.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.justai.jaicf.activator.llm.function.LLMFunction
import com.justai.jaicf.activator.llm.llmJsonMapper

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LLMRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Float? = null,
    @JsonProperty("max_tokens")
    val maxTokens: Int? = null,
    @JsonProperty("top_p")
    val topP: Float? = null,
    @JsonProperty("frequency_penalty")
    val frequencyPenalty: Float? = null,
    @JsonProperty("presence_penalty")
    val presencePenalty: Float? = null,
    @JsonProperty("response_format")
    val responseFormat: ResponseFormat? = null,
    val functions: List<LLMFunction>? = null,
    @JsonProperty("function_call")
    val functionCall: FunctionCall? = null,
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    open class Message(
        val role: LLMMessageRole,
        open val content: String? = null
    ) {

        override fun toString() = content.orEmpty()

        companion object {
            fun system(text: String) = Message(LLMMessageRole.system, text)
            fun user(text: String) = Message(LLMMessageRole.user, text)
            fun assistant(text: String) = Message(LLMMessageRole.assistant, text)
            fun function(name: String, result: String) = FunctionResultMessage(name, result)
        }
    }

    data class FunctionMessage(
        @JsonProperty("function_call")
        val functionCall: LLMResponse.FunctionCall
    ) : Message(LLMMessageRole.assistant) {
        override fun toString() =
            llmJsonMapper.writeValueAsString(functionCall)
    }

    data class FunctionResultMessage(
        val name: String,
        override val content: String
    ) : Message(LLMMessageRole.assistant, content) {
        override fun toString() =
            llmJsonMapper.writeValueAsString(this)
    }

    data class ResponseFormat(
        val type: Type
    ) {
        enum class Type {
            text, json_object
        }

        companion object {
            val text = ResponseFormat(Type.text)
            val json = ResponseFormat(Type.json_object)
        }
    }

    data class FunctionCall(
        val name: String
    )
}