package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.test.LLMScenarioTest
import com.justai.jaicf.activator.llm.test.testWithLLM
import org.junit.jupiter.api.Test
import java.util.Optional

class AgentWithGoalTest: LLMScenarioTest(AgentWithGoal) {
    @Test
    fun `agent achieves goal`() = testWithLLM({model = "gpt-4o-mini"}) {
        send(user = "Hello", agent = "Greets and asks firstname")
        send(user = "John", agent = "Asks the lastname")
        send(user = "Doe", agent = "Asks for age")
        query("Thirty") returnsResult Goal("John", "Doe", Optional.of(30))
    }
}