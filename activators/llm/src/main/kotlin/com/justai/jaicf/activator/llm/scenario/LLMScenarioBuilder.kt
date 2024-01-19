package com.justai.jaicf.activator.llm.scenario

import com.justai.jaicf.activator.llm.LLMFunctionActivatorContext
import com.justai.jaicf.activator.llm.function.LLMFunction
import com.justai.jaicf.activator.llm.function.LLMFunctionParametersBuilder
import com.justai.jaicf.activator.llm.llmFunction
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.ScenarioGraphBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.plugin.StateDeclaration

@ScenarioDsl
@StateDeclaration
fun ScenarioGraphBuilder<*, *>.llmFunction(
    function: LLMFunction,
    block: LLMFunctionActivatorContext.() -> Unit
) {
    state(function.name, noContext = true) {
        activators {
            llmFunction(function)
        }

        action(llmFunction) {
            activator.apply(block)
        }
    }
}

@ScenarioDsl
@StateDeclaration
fun ScenarioGraphBuilder<*, *>.llmFunction(
    name: String,
    block: ActionContext<LLMFunctionActivatorContext, *, *>.() -> Unit
) {
    state(name, noContext = true) {
        activators {
            llmFunction(name)
        }

        action(llmFunction, block)
    }
}

@ScenarioDsl
@StateDeclaration
fun ScenarioGraphBuilder<*, *>.llmFunction(
    name: String,
    description: String,
    parameters: LLMFunctionParametersBuilder,
    block: ActionContext<LLMFunctionActivatorContext, *, *>.() -> Unit
) {
    state(name, noContext = true) {
        activators {
            llmFunction(name, description, parameters)
        }

        action(llmFunction, block)
    }
}