package com.justai.jaicf.activator.llm.tool

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.LLMContext
import com.justai.jaicf.activator.llm.builder.JsonSchemaBuilder
import com.justai.jaicf.activator.llm.telemetry.GenAIAttributes
import com.justai.jaicf.activator.llm.telemetry.LLMAttributes
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.telemetry.getTelemetrySessionId
import com.justai.jaicf.telemetry.runWithTelemetry
import com.openai.models.chat.completions.ChatCompletionMessageToolCall

typealias LLMToolFunction<T> = suspend LLMToolCallContext<T>.() -> Any?
typealias LLMToolParameters = JsonSchemaBuilder.() -> Unit

data class LLMToolCallContext<T>(
    val context: BotContext,
    val request: BotRequest,
    val llm: LLMContext,
    val call: LLMToolCall<T>,
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
    val requiresConfirmation = this is LLMToolWithConfirmation

    internal open fun arguments(call: ChatCompletionMessageToolCall) =
        ArgumentsMapper.readValue(call.function().arguments(), definition.parametersType)

    open fun withConfirmation(block: LLMToolConfirmationFunction<T>) =
        LLMToolWithConfirmation.WithCustomConfirmation(this, block)

    open fun withConfirmation(message: String? = null) =
        LLMToolWithConfirmation.WithLLMConfirmation(this, message)

    val withoutConfirmation by lazy {
        withConfirmation { true }
    }

    suspend fun <R> withToolTelemetrySpan(
        context: LLMToolCallContext<T>,
        block: suspend LLMToolCallContext<T>.() -> R
    ): R {
        val toolName = context.call.name
        val spanName = "${GenAIAttributes.OPERATION_EXECUTE_TOOL} $toolName"
        val baseAttributes = mutableMapOf<String, Any?>(
            GenAIAttributes.OPERATION_NAME to GenAIAttributes.OPERATION_EXECUTE_TOOL,
            GenAIAttributes.TOOL_NAME to toolName,
            LLMAttributes.TOOL_NAME to toolName,
            LLMAttributes.TOOL_CALL_ID to context.call.callId,
            LLMAttributes.TOOL_ARGUMENTS to (context.call.arguments?.toString() ?: "")
        )
        context.context.getTelemetrySessionId().takeIf { it.isNotEmpty() }?.let {
            baseAttributes[GenAIAttributes.CONVERSATION_ID] = it
        }
        val provider = BotEngine.current()?.telemetryProvider ?: com.justai.jaicf.telemetry.TelemetryProvider.NoOp
        return runWithTelemetry(provider, spanName, baseAttributes) {
            context.block()
        }
    }

    companion object {
        val ArgumentsMapper = JsonMapper.builder()
            .addModule(kotlinModule())
            .addModule(Jdk8Module())
            .addModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build()
    }
}

inline fun <reified T> llmTool(
    noinline function: LLMToolFunction<T>
) = LLMTool(LLMToolDefinition.FromClass(T::class.java), function)

inline fun <reified T> llmTool(
    name: String? = null,
    description: String? = null,
    noinline function: LLMToolFunction<T>
) = LLMTool(LLMToolDefinition.FromClass(T::class.java, name, description), function)

inline fun <reified T> llmTool(
    name: String? = null,
    description: String? = null,
    noinline parameters: LLMToolParameters,
    noinline function: LLMToolFunction<T>
) = LLMTool(LLMToolDefinition.CustomSchema(T::class.java, name, description, parameters), function)
