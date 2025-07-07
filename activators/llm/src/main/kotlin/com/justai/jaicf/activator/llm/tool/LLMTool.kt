package com.justai.jaicf.activator.llm.tool

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.justai.jaicf.activator.llm.LLMActivatorContext
import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.sessionProperty
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.openai.core.JsonValue
import com.openai.models.FunctionDefinition
import com.openai.models.FunctionParameters
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.chat.completions.ChatCompletionTool
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

typealias LLMToolFunction<T> = LLMToolCallContext.(call: LLMToolCall<T>) -> Any?
typealias LLMToolConfirmationFunction<T> = LLMToolCallContext.(call: LLMToolCall<T>) -> Boolean

internal var BotContext.confirmToolCalls by sessionProperty { listOf<String>() }

data class LLMToolCallContext(
    val activator: LLMActivatorContext,
    val botContext: BotContext,
    val request: BotRequest,
)

data class LLMToolCall<T>(
    val name: String,
    val callId: String,
    val arguments: T,
    val origin: ChatCompletionMessageToolCall,
)

data class LLMToolResult(
    val callId: String,
    val name: String,
    val arguments: Any?,
    val result: Any?,
) {
    inline fun <reified T> arguments() = arguments as T
    inline fun <reified T> result() = result as T
}

open class LLMTool<T>(
    val definition: LLMToolDefinition<T>,
    val function: LLMToolFunction<T>,
) {
    val requiresConfirmation = this is WithConfirmation

    internal open fun arguments(call: ChatCompletionMessageToolCall) =
        MAPPER.readValue(call.function().arguments(), definition.parametersType)

    fun withConfirmation(block: LLMToolConfirmationFunction<T>): LLMTool<T> {
        return LLMTool(definition) { call ->
            block(call).ifTrue { function(call) } ?: "ERROR: user has declined this tool call"
        }
    }

    fun withConfirmation(message: String? = null): LLMTool<T> =
        WithConfirmation(this, message)

    internal class WithConfirmation<T>(
        tool: LLMTool<T>,
        message: String? = null,
    ) : LLMTool<T>(tool.definition, { call ->
        val confirmId = MAPPER
            .readValue(call.origin.function().arguments(), ObjectNode::class.java)
            .get(CONFIRM_FIELD)?.asText()

        if (confirmId.isNullOrEmpty() || !botContext.confirmToolCalls.contains(confirmId)) {
            botContext.confirmToolCalls += call.callId

            "CONFIRMATION REQUIRED! " +
                "Ask user to confirm this tool call ${message?.let { "using message like '$it'" }.orEmpty()}. " +
                "Only if the user confirms, call this tool again with '$CONFIRM_FIELD' = ${call.callId}"
        } else {
            tool.function(this, call).also {
                synchronized(request.clientId) {
                    botContext.confirmToolCalls = botContext.confirmToolCalls.filter {
                        it != confirmId
                    }
                }
            }
        }
    })

    companion object {
        const val CONFIRM_FIELD = "confirmToolCallId"
        val CONFIRM_PROPERTY = JsonValue.from(mapOf<String, String>(
            "type" to "string",
            "description" to "ID of confirmed tool call",
        ))

        val MAPPER = JsonMapper.builder()
            .addModule(kotlinModule())
            .addModule(Jdk8Module())
            .addModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build()
    }
}

sealed interface LLMToolDefinition<T> {
    val name: String
    val description: String?
    val parametersType: Class<T>

    class ClassDefinition<T>(
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

    data class SchemaDefinition<T>(
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
    name: String? = null,
    description: String? = null,
    noinline function: LLMToolFunction<T>
) = LLMTool(LLMToolDefinition.ClassDefinition(T::class.java, name, description), function)

inline fun <reified T> llmTool(
    name: String,
    description: String? = null,
    noinline parameters: JsonSchemaBuilder.() -> Unit,
    noinline function: LLMToolFunction<T>
) = LLMTool(LLMToolDefinition.SchemaDefinition(name, description, T::class.java, parameters), function)
