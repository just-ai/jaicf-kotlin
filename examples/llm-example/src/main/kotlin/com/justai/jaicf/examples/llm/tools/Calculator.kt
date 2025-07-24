package com.justai.jaicf.examples.llm.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.justai.jaicf.activator.llm.tool.llmTool

/**
 * Calculator function definition.
 * Use @JsonTypeName to override function name.
 */
//@JsonTypeName("calc")
@JsonClassDescription("Calculates math expressions")
data class Calculator(
    @field:JsonPropertyDescription("Math expression")
    val expression: String
)

// Calculator function
val CalcTool = llmTool<Calculator> {
    Math.random() // for testing
}