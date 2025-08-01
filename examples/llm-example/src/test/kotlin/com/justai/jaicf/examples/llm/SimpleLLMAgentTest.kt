package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.createLLMProps
import com.justai.jaicf.activator.llm.test.openai.OpenAITest
import com.justai.jaicf.activator.llm.test.testWithLLM
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

@OpenAITest
class SimpleLLMAgentTest: ScenarioTest(SimpleLLMAgent) {
    private val props = createLLMProps {
        model = "gpt-4o-mini"
        temperature = 1.0
    }

    @Test
    fun `greets the user`() = testWithLLM(props) {
        chat(user = "Greets an agent", agent = "Says 'Hello' back")
    }

    @Test
    fun `writes about their skills`() = testWithLLM(props) {
        chat(user = "Asks what agent can do", agent = "Lists all their skills")
        chat(user = "Asks to describe first skill", agent = "Responds with detailed description of first skill")
    }
}