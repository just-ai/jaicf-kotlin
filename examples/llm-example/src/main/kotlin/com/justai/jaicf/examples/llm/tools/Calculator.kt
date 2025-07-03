package com.justai.jaicf.examples.llm.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.justai.jaicf.activator.llm.llmTool

// Calculator function definition
@JsonClassDescription("Calculates math expression")
data class Calculator(val expression: String)

// Calculator function
val CalcTool = llmTool<Calculator> {
    "ERROR" // returns error for tests
}