package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.LLMScenarioTest
import com.justai.jaicf.activator.llm.agentResponds
import com.justai.jaicf.activator.llm.openai.OpenAITest
import com.justai.jaicf.examples.llm.tools.Calculator
import org.junit.jupiter.api.Test

@OpenAITest
class MultiAgentHandoffTest: LLMScenarioTest(HandoffScenario) {
    @Test
    fun `calculator hand offs math requests`() {
        query("two plus two") agentResponds HandoffCalculatorAgent callsTool Calculator::class
    }
}