package com.justai.jaicf.activator.llm.test

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.agent.agentStateName
import com.justai.jaicf.test.model.ProcessResult
import org.junit.jupiter.api.Assertions.assertTrue

infix fun ProcessResult.agentResponds(name: String) = apply {
    val suffix = agentStateName(name)
    val state = botContext.dialogContext.currentContext
    assertTrue(state.endsWith(suffix), "scenario execution did end in state $state")
}

infix fun ProcessResult.agentResponds(agent: LLMAgent) = agentResponds(agent.name)
