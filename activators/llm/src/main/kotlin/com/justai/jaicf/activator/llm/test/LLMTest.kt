package com.justai.jaicf.activator.llm.test

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonTypeName
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.llmMemory
import com.justai.jaicf.activator.llm.withSystemMessage
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.justai.jaicf.test.BotTest
import com.justai.jaicf.test.model.ProcessResult
import com.justai.jaicf.test.reactions.TestReactions
import com.openai.models.ChatModel
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.Optional
import kotlin.jvm.optionals.getOrNull


private val INSTRUCTIONS = """
    You are an agent that tests another conversational agent (testing agent).
    Testing agent has its own memory, so you can converse in a context-aware manner.
    I will write you instructions for next request to the testing agent, and you should converse with this agent regarding these instructions.
    Once you decided that the agent passed or failed a test regarding my instructions, return a JSON result of assertion.
""".trimIndent()

@JsonTypeName("sendRequest")
@JsonClassDescription("Send natural language text request to the testing agent")
private data class LLMTestRequest(val input: String)

data class LLMTestResult(
    val passed: Boolean,
    @field:JsonPropertyDescription("Description of error if test was failed by testing agent")
    val error: Optional<String>
)

class LLMTestRequestContext(
    override val newSession: Boolean = false,
) : RequestContext(newSession, null) {
    lateinit var testResult: LLMTestResult
    lateinit var processResult: ProcessResult
}

class LLMTest(testAgent: LLMAgent) {
    private val testEngine = testAgent.asBot(InMemoryBotContextManager())

    private fun BotTest.process(input: String): ProcessResult {
        val reactions = TestReactions()
        val context = LLMTestRequestContext()
        val request = QueryBotRequest(clientId, input)

        testEngine.process(request, reactions, context)
        assertTrue(
            context.testResult.passed,
            context.testResult.error.getOrNull() ?: "LLM test failed"
        )
        return context.processResult
    }

    fun BotTest.chat(user: String, agent: String) = process(
        """
            Create and send to agent a request that satisfies these instructions: "$user"
            Agent must respond with reply that satisfies next: "$agent"
        """.trimIndent()
    )

    fun BotTest.send(user: String, agent: String) = process(
        """
            Send to agent a message "$user"
            Agent must respond with reply that satisfies next: "$agent"
        """.trimIndent()
    )
}

fun BotTest.testWithLLM(
    props: LLMPropsBuilder? = { model = ChatModel.GPT_4O_MINI.asString() },
    block: LLMTest.() -> Unit
) {
    var result: ProcessResult?

    val testAgent = LLMAgent("testAgent", {
        props?.invoke(this)

        setParallelToolCalls(false)
        setResponseFormat(LLMTestResult::class.java)

        messages = messages.isNullOrEmpty().ifTrue {
            llmMemory("testAgent", withSystemMessage(INSTRUCTIONS))
        } ?: messages.withSystemMessage("testAgent", INSTRUCTIONS)

        tool<LLMTestRequest> {
            val res = process(QueryBotRequest(clientId, call.arguments.input))
            result = res
            res.answer
        }
    }) {
        result = null
        llm.awaitStructuredContent<LLMTestResult>().also {
            val requestContext = reactions.executionContext.requestContext as LLMTestRequestContext
            requestContext.testResult = it
            requestContext.processResult = checkNotNull(result) { "Agent was not invoked" }
        }
    }

    block.invoke(LLMTest(testAgent))
}