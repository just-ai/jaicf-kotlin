package com.justai.jaicf.activator.llm.tool

import com.fasterxml.jackson.annotation.JsonTypeName
import com.justai.jaicf.activator.llm.LLMActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.models.FunctionDefinition
import com.openai.models.FunctionParameters
import com.openai.models.chat.completions.ChatCompletionTool

typealias LLMToolFunction<T> = LLMToolCallContext.(call: LLMToolCall<T>) -> Any?

data class LLMToolCallContext(
    val activator: LLMActivatorContext,
    val botContext: BotContext,
    val request: BotRequest,
)

data class LLMToolCall<T>(
    val name: String,
    val callId: String,
    val arguments: T,
)

data class LLMTool<T>(
    val definition: LLMToolDefinition<T>,
    val function: LLMToolFunction<T>,
)

sealed interface LLMToolDefinition<T> {
    val name: String
    val parametersType: Class<*>

    class ClassDefinition<T>(override val parametersType: Class<T>) : LLMToolDefinition<T> {
        override val name: String =
            parametersType.getAnnotation(JsonTypeName::class.java)?.value ?: parametersType.simpleName
    }

    data class SchemaDefinition<T>(
        override val name: String,
        val description: String? = null,
        override val parametersType: Class<*>,
        val parameters: JsonSchemaBuilder.() -> Unit
    ) : LLMToolDefinition<T> {
        val asChatCompletionTool
            get() = ChatCompletionTool.builder().function(asFunctionDefinition).build()

        val asFunctionDefinition: FunctionDefinition
            get() = FunctionDefinition.builder().apply {
                strict(true)
                name(name)
                description?.also { description(it) }
                parameters(FunctionParameters.builder().additionalProperties(
                    JsonSchemaBuilder().apply(parameters).build()
                ).build())
            }.build()
    }
}

inline fun <reified T> llmTool(
    noinline function: LLMToolFunction<T>
) = LLMTool(LLMToolDefinition.ClassDefinition(T::class.java), function)

inline fun <reified T> llmTool(
    name: String,
    description: String? = null,
    noinline parameters: JsonSchemaBuilder.() -> Unit,
    noinline function: LLMToolFunction<T>
) = LLMTool(LLMToolDefinition.SchemaDefinition(name, description, T::class.java, parameters), function)

data class LLMToolResult(
    val callId: String,
    val name: String,
    val arguments: Any,
    val result: Any?,
) {
    inline fun <reified T> arguments() = arguments as T
    inline fun <reified T> result() = result as T
}
