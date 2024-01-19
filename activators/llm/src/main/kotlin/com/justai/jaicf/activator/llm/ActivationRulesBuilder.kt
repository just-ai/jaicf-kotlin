package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.llm.function.LLMFunction
import com.justai.jaicf.activator.llm.function.LLMFunctionParametersBuilder
import com.justai.jaicf.builder.ActivationRulesBuilder

class LLMFunctionActivationRule(val function: LLMFunction) : EventByNameActivationRule(LLMEvent.FUNCTION_CALL)

fun ActivationRulesBuilder.llmMessage() =
    event(LLMEvent.MESSAGE)

fun ActivationRulesBuilder.llmFunction(name: String) =
    event(LLMEvent.FUNCTION_CALL).onlyIf {
        activator.llmFunction?.name == name
    }

fun ActivationRulesBuilder.llmFunction(function: LLMFunction) =
    rule(LLMFunctionActivationRule(function)).onlyIf {
        activator.llmFunction?.name == function.name
    }

fun ActivationRulesBuilder.llmFunction(
    name: String,
    description: String,
    parameters: LLMFunctionParametersBuilder
) = llmFunction(LLMFunction.create(name, description, parameters))