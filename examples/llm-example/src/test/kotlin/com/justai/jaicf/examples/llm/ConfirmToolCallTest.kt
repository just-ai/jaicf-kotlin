package com.justai.jaicf.examples.llm


import com.justai.jaicf.activator.llm.LLMScenarioTest
import com.justai.jaicf.activator.llm.openai.OpenAITest
import com.justai.jaicf.activator.llm.testWithLLM
import org.junit.jupiter.api.Test

@OpenAITest
class ConfirmToolCallTest: LLMScenarioTest(AgentWithConfirmation) {
    private val sendMail = SendMail("Have a good day")

    @Test
    fun `agent requires confirmation before calling tool`() = testWithLLM {
        send(
            user = "Send mail with text: '${sendMail.message}'",
            agent = "Asks user to confirm sending the message"
        ) callsTool sendMail

        send(user = "Yes", agent = "Mail sent successfully") callsTool sendMail
    }
}