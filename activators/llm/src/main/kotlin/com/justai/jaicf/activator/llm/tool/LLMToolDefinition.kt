package com.justai.jaicf.activator.llm.tool

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonTypeName
import com.openai.models.FunctionDefinition
import com.openai.models.FunctionParameters
import com.openai.models.chat.completions.ChatCompletionTool

sealed interface LLMToolDefinition<T> {
    val name: String
    val description: String?
    val parametersType: Class<T>

    class FromClass<T>(
        override val parametersType: Class<T>,
        name: String? = null,
        description: String? = null,
    ) : LLMToolDefinition<T> {
        override val name: String = name
            ?: parametersType.getAnnotation(JsonTypeName::class.java)?.value
            ?: parametersType.simpleName

        override val description: String? =
            description ?: parametersType.getAnnotation(JsonClassDescription::class.java)?.value
    }

    data class CustomSchema<T>(
        override val name: String,
        override val description: String? = null,
        override val parametersType: Class<T>,
        private val parameters: JsonSchemaBuilder.() -> Unit
    ) : LLMToolDefinition<T> {
        val asChatCompletionTool
            get() = ChatCompletionTool.builder().function(asFunctionDefinition).build()

        val asFunctionDefinition: FunctionDefinition
            get() = FunctionDefinition.builder().apply {
                strict(false)
                name(name)
                description?.also { description(it) }
                parameters(
                    FunctionParameters.builder().additionalProperties(
                    JsonSchemaBuilder().apply(parameters).build()
                ).build())
            }.build()
    }
}