package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.LLMToolCallsHook
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.logging.Reaction

data class LLMToolCallReaction(
    override val fromState: String,
    val toolCallResult: LLMToolResult,
) : Reaction(fromState) {
    companion object {
        fun fromHook(hook: LLMToolCallsHook) = hook.toolCallResults.map {
            LLMToolCallReaction(hook.state.path.toString(), it)
        }
    }
}