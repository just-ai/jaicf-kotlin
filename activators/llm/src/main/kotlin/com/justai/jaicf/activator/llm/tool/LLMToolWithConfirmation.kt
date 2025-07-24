package com.justai.jaicf.activator.llm.tool

import com.fasterxml.jackson.databind.node.ObjectNode
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.sessionProperty
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.openai.core.JsonValue

typealias LLMToolConfirmationFunction<T> = LLMToolCallContext<T>.() -> Boolean

internal var BotContext.confirmToolCalls by sessionProperty { listOf<String>() }

sealed class LLMToolWithConfirmation<T>(
    private val tool: LLMTool<T>,
    function: LLMToolFunction<T>
): LLMTool<T>(tool.definition, function) {

    override fun withConfirmation(message: String?) =
        tool.withConfirmation(message)

    override fun withConfirmation(block: LLMToolConfirmationFunction<T>) =
        tool.withConfirmation(block)

    class WithCustomConfirmation<T>(
        tool: LLMTool<T>,
        block: LLMToolConfirmationFunction<T>,
    ): LLMToolWithConfirmation<T>(tool, {
        block().ifTrue {
            val context = this
            suspend {
                tool.function.invoke(context)
            }
        } ?: throw Exception("user has declined this tool call")
    })

    class WithLLMConfirmation<T>(
        tool: LLMTool<T>,
        message: String? = null,
    ): LLMToolWithConfirmation<T>(tool, {
        val confirmId = ArgumentsMapper
            .readValue(call.origin.function().arguments(), ObjectNode::class.java)
            .get(CONFIRM_FIELD)?.asText()

        if (confirmId.isNullOrEmpty() || !context.confirmToolCalls.contains(confirmId)) {
            context.confirmToolCalls += call.callId

            "CONFIRMATION REQUIRED! " +
                "Ask user to confirm this tool call ${message?.let { "using message like '$it'" }.orEmpty()}. " +
                "Only if the user confirms, call this tool again with '$CONFIRM_FIELD' = ${call.callId}"
        } else {
            tool.function(this).also {
                synchronized(request.clientId) {
                    context.confirmToolCalls = context.confirmToolCalls.filter {
                        it != confirmId
                    }
                }
            }
        }
    }) {
        internal companion object {
            const val CONFIRM_FIELD = "confirmToolCallId"
            val CONFIRM_PROPERTY = JsonValue.from(mapOf<String, String>(
                "type" to "string",
                "description" to "ID of confirmed tool call",
            ))
            val CONFIRM_PARAM: Pair<String, JsonValue> =
                CONFIRM_FIELD to JsonValue.from(CONFIRM_PROPERTY)
        }
    }
}