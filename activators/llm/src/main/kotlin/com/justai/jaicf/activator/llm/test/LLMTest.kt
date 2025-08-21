package com.justai.jaicf.activator.llm.test

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonTypeName
import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.llmMemory
import com.justai.jaicf.activator.llm.withProps
import com.justai.jaicf.activator.llm.withSystemMessage
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.justai.jaicf.test.BotTest
import com.justai.jaicf.test.model.ProcessResult
import com.justai.jaicf.test.reactions.TestReactions
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
    val error: Optional<String>,
    val actualResponse: String,
    val expectedResponse: String,
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
            ((context.testResult.error.getOrNull() ?: "LLM test failed")
                + "\n"
                + "Expected response: ${context.testResult.expectedResponse}"
                + "\n"
                + "Actual response: ${context.testResult.actualResponse}")
        )
        return context.processResult
    }

    fun BotTest.chat(user: String, agent: String) = process(
        """
      You are a testing controller that (1) sends a request to a testing agent and (2) evaluates the agent's reply.

      === Task to send ===
      $user

      === Expectation ===
      $agent

      === Evaluation Rules ===
      1) Normalize:
         - For comparisons, use case-insensitive matching.
         - Collapse multiple spaces.

      2) Short-literal heuristic (HIGH PRIORITY):
         - If Expectation is a short literal (≤ 2 words OR ≤ 10 characters) and contains only letters/quotes,
           then PASS if REPLY either:
             a) starts with that literal (after normalization), OR
             b) contains it as a standalone word/phrase.
         - Extras like polite follow-ups are allowed unless Expectation says "only"/"nothing else".

      3) General rules (apply when #2 doesn’t trigger):
         - "exactly"/"verbatim" ⇒ strict equality (trim whitespace).
         - "starts with" ⇒ REPLY must start with the phrase.
         - "contains"/"include(s)" ⇒ substring presence.
         - "one of [A|B|C]" ⇒ accept any.
         - "regex:" ⇒ treat remainder as pattern.
         - Lists ⇒ accept prose or bullets; PASS if ≥3 distinct items or a clear broad range with examples.
         - Semantic intent (greetings, confirmations, listing capabilities) ⇒ accept close paraphrases.
         - Forbid rules ("must not ...") ⇒ FAIL if violated.

      4) Decision policy:
         - Prefer PASS if the core requirement is satisfied; tolerate harmless extra text.

      5) Output ONLY this JSON (matching LLMTestResult):
         {
           "passed": <true|false>,
           "error": <string|null>,
           "actualResponse": <string>,
           "expectedResponse": <string>
         }

      Now execute:
      - Send the Task to the agent, capture REPLY verbatim.
      - Evaluate using the rules above.
      - Return ONLY the JSON object.
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
    props: LLMPropsBuilder = DefaultLLMProps,
    block: LLMTest.() -> Unit
) {
    var result: ProcessResult?

    val testAgent = LLMAgent("testAgent", props.withProps {
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