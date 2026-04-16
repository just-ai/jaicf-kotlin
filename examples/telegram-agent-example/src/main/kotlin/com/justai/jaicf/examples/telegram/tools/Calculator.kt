package com.justai.jaicf.examples.telegram.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.justai.jaicf.activator.llm.tool.llmTool

/**
 * Calculator function definition.
 * Use @JsonTypeName to override function name.
 */
@JsonClassDescription("Calculates math expressions")
data class Calculator(
    @field:JsonPropertyDescription("Math expression to evaluate")
    val expression: String
)

// Calculator function
val CalcTool = llmTool<Calculator> {
    // For demo purposes, return a random number
    // In production, you would evaluate the expression properly
    Math.random()
}
