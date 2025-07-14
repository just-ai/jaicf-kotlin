package com.justai.jaicf.activator.llm.agent

import com.justai.jaicf.activator.llm.LLMActionBlock
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolParameters
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.Scenario
import com.openai.core.JsonValue

interface LLMAgentScenario : Scenario {
    val name: String
    val props: LLMPropsBuilder
    val onlyIf: ActivationRule.OnlyIfContext.() -> Boolean
    val action: LLMActionBlock

    fun handoffs(vararg agents: LLMAgentWithRole)

    fun withRole(role: String) = LLMAgentWithRole(this, role)

    fun withoutMemory(): LLMAgentScenario

    fun asTool(
        name: String = this.name,
        description: String? = (this as? LLMAgentWithRole)?.role,
        parameters: LLMToolParameters = DefaultAgentToolParams,
    ): LLMTool<JsonValue>

    fun <T> asTool(
        name: String = this.name,
        description: String? = (this as? LLMAgentWithRole)?.role,
        parameters: Class<T>,
    ): LLMTool<T>
}

private val DefaultAgentToolParams: LLMToolParameters = {
    str("input", "Request text", true)
}