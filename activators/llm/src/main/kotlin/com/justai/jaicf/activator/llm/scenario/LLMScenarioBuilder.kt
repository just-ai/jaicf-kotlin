package com.justai.jaicf.activator.llm.scenario

import com.justai.jaicf.activator.llm.*
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.ScenarioGraphBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.plugin.StateBody
import com.justai.jaicf.plugin.StateDeclaration
import com.justai.jaicf.plugin.StateName


@ScenarioDsl
@StateDeclaration
fun ScenarioGraphBuilder<*, *>.llmChat(
    @StateName state: String,
    props: LLMPropsBuilder = DefaultLLMProps,
    @StateBody block: LLMActionBlock = DefaultLLMActionBlock,
) {
    state(state) {
        activators {
            llmActivator(props)
        }

        action(llm, block)
    }
}
