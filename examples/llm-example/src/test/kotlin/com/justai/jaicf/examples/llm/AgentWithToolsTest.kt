package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.test.LLMScenarioTest
import com.justai.jaicf.activator.llm.test.OpenAITest
import com.justai.jaicf.examples.llm.tools.Calculator
import org.junit.jupiter.api.Test

@OpenAITest
class AgentWithToolsTest : LLMScenarioTest(AgentWithTools) {

    @Test
    fun `uses calculator tool`() {
        query("two plus two") callsTool Calculator("2+2")
    }
}