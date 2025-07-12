package com.justai.jaicf.activator.llm.builder

import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolWithConfirmation
import com.openai.core.JsonValue
import com.openai.models.FunctionParameters
import com.openai.models.chat.completions.ChatCompletionCreateParams
import kotlin.collections.plus
import kotlin.collections.toMutableMap
import kotlin.jvm.optionals.getOrNull

internal fun ChatCompletionCreateParams.Builder.build(props: LLMProps): ChatCompletionCreateParams {
    val params = build()
    if (props.tools.isNullOrEmpty()) return params

    params.tools().getOrNull()?.mapIndexed { index, tool ->
        val function = tool.function()
        val propTool = props.tools[index]

        val parameters = function._parameters().let { parameters ->
            parameters.asKnown().getOrNull()?._additionalProperties() ?: parameters.asObject().getOrNull()
        } ?: emptyMap()

        val properties = parameters["properties"]?.asObject()?.get() ?: emptyMap()

        if (properties.isEmpty() || propTool.requiresConfirmation || propTool.definition.name != function.name() || propTool.definition.description != function.description().getOrNull()) {
            tool.toBuilder().function(
                function.toBuilder()
                    .strict(!propTool.requiresConfirmation && !properties.isEmpty())
                    .name(propTool.definition.name)
                    .description(propTool.definition.description ?: "")
                    .apply {
                        if (properties.isEmpty()) {
                            parameters(FunctionParameters.builder().build())
                        }
                        if (propTool.requiresConfirmation) {
                            parameters(JsonValue.from(parameters.toMutableMap().apply {
                                put("properties", JsonValue.from(
                                    getValue("properties").asObject().get().plus(
                                        LLMToolWithConfirmation.WithLLMConfirmation.CONFIRM_PARAM
                                    )
                                ))
                            }))
                        }
                    }.build()
            ).build()
        } else {
            tool
        }
    }?.also(::tools)

    return build()
}