package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.client.LLMRequest
import com.justai.jaicf.activator.llm.function.LLMFunction
import com.knuddels.jtokkit.api.ModelType

private const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
private val DEFAULT_MODEL = LLMSettings.Model("gpt-3.5-turbo-1106", 16_000)

data class LLMSettings(
    val apiKey: String = System.getenv("OPENAI_API_KEY"),
    val baseUrl: String = System.getenv("OPENAI_API_BASE_URL") ?: DEFAULT_BASE_URL,
    val model: Model? = null,
    val temperature: Float? = null,
    val maxTokens: Int? = null,
    val topP: Float? = null,
    val frequencyPenalty: Float? = null,
    val presencePenalty: Float? = null,
    val responseFormat: LLMRequest.ResponseFormat? = null,
    val functions: List<LLMFunction>? = null,
    val functionCall: LLMRequest.FunctionCall? = null,
) {
    val isJsonFormat
        get() = responseFormat?.type === LLMRequest.ResponseFormat.Type.json_object

    fun createChatRequest(messages: List<LLMRequest.Message>) =
        LLMRequest(
            model = model?.name.orEmpty(),
            temperature = temperature,
            maxTokens = maxTokens,
            topP = topP,
            frequencyPenalty = frequencyPenalty,
            presencePenalty = presencePenalty,
            responseFormat = responseFormat,
            functions = functions,
            functionCall = functionCall,
            messages = messages,
        )

    fun assign(from: LLMSettings) = copy(
        model = from.model ?: model ?: DEFAULT_MODEL,
        temperature = from.temperature ?: temperature,
        maxTokens = from.maxTokens ?: maxTokens,
        topP = from.topP ?: topP,
        frequencyPenalty = from.frequencyPenalty ?: frequencyPenalty,
        presencePenalty = from.presencePenalty ?: presencePenalty,
        responseFormat = from.responseFormat ?: responseFormat,
        functions = from.functions ?: functions,
        functionCall = from.functionCall ?: functionCall,
    )

    data class Model(
        val name: String,
        val maxContextLength: Int,
    ) {
        companion object {
            fun fromName(name: String) = ModelType.fromName(name)
                .orElse(null)?.let { type ->
                    Model(name, type.maxContextLength)
                } ?: throw IllegalArgumentException("Model $name was not found")
        }
    }
}
