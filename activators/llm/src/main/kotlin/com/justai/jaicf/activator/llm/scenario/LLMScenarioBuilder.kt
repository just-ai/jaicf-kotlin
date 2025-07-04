package com.justai.jaicf.activator.llm.scenario

import com.justai.jaicf.activator.llm.*
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.ScenarioGraphBuilder
import com.justai.jaicf.plugin.StateBody
import com.justai.jaicf.plugin.StateDeclaration
import com.justai.jaicf.plugin.StateName


@ScenarioDsl
@StateDeclaration
fun ScenarioGraphBuilder<*, *>.llmState(
    @StateName state: String,
    props: LLMPropsBuilder = DefaultLLMProps,
    noContext: Boolean = false,
    modal: Boolean = false,
    @StateBody block: LLMActionBlock = DefaultLLMActionBlock,
) {
    state(state, noContext, modal) {
        activators {
            catchAll()
        }

        llmAction(props, block)
    }
}
