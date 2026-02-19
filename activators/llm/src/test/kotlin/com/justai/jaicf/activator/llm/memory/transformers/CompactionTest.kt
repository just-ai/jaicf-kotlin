package com.justai.jaicf.activator.llm.memory.transformers

import com.justai.jaicf.activator.llm.LLMMessage.assistant
import com.justai.jaicf.activator.llm.LLMMessage.developer
import com.justai.jaicf.activator.llm.LLMMessage.system
import com.justai.jaicf.activator.llm.LLMMessage.user
import com.justai.jaicf.activator.llm.openai.OpenAITest
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.chat.completions.ChatCompletionToolMessageParam
import kotlin.jvm.optionals.getOrNull
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private fun toolCallAssistant(toolCallId: String, toolName: String, arguments: String = "{}") =
    ChatCompletionMessageParam.ofAssistant(
        ChatCompletionAssistantMessageParam.builder()
            .toolCalls(listOf(
                ChatCompletionMessageToolCall.builder()
                    .id(toolCallId)
                    .function(
                        ChatCompletionMessageToolCall.Function.builder()
                            .name(toolName)
                            .arguments(arguments)
                            .build()
                    )
                    .build()
            ))
            .build()
    )

private fun toolResult(toolCallId: String, content: String) =
    ChatCompletionMessageParam.ofTool(
        ChatCompletionToolMessageParam.builder()
            .toolCallId(toolCallId)
            .content(content)
            .build()
    )

private fun List<ChatCompletionMessageParam>.hasSummary() =
    any { it.isAssistant() && it.asAssistant().name().getOrNull() == CONVERSATION_SUMMARY_MESSAGE }


@OpenAITest
@DisplayName("withCompaction tests")
class CompactionTest {

    private val conversationAboveThreshold = listOf(
        user("Hello, my name is Alice and I am planning a trip to Tokyo next month with a budget of 150 USD per night."),
        assistant("Nice to meet you Alice! I can help you find hotels in Tokyo within your budget."),
        user("I prefer to stay near Shinjuku station."),
        assistant("Great choice! Shinjuku has many hotels in that price range."),
    )

    @Test
    @DisplayName("Should not compact when total tokens are within limit")
    fun testNoCompactionBelowThreshold() {
        val messages = listOf(user("Hi"), assistant("Hello!"))
        val result = messages.withCompaction(maxTokens = 10_000)
        assertSame(messages, result)
        assertFalse(result.hasSummary())
    }

    @Test
    @DisplayName("Should preserve messages unchanged when below threshold")
    fun testMessagesUnchangedBelowThreshold() {
        val messages = listOf(
            system("You are a helpful assistant."),
            user("Hi"),
            assistant("Hello!")
        )
        val result = messages.withCompaction(maxTokens = 10_000)
        assertEquals(3, result.size)
        assertTrue(result[0].isSystem())
        assertTrue(result[1].isUser())
        assertTrue(result[2].isAssistant())
        assertFalse(result.hasSummary())
    }

    @Test
    @DisplayName("Should return original list instance when compaction is not triggered")
    fun testReturnsSameInstanceWhenNotCompacted() {
        val messages = listOf(
            system("You are a helpful assistant."),
            user("What is the capital of France?"),
            assistant("The capital of France is Paris."),
        )
        val result = messages.withCompaction(maxTokens = 10_000)
        assertSame(messages, result)
    }

    @Test
    @DisplayName("Should preserve system and developer messages after compaction")
    fun testSystemAndDeveloperMessagesPreservedAfterCompaction() {
        val messages = listOf(system("You are a helpful assistant."), developer("Dev notes.")) +
            conversationAboveThreshold
        val result = messages.withCompaction(maxTokens = 10)

        assertTrue(result[0].isSystem())
        assertTrue(result[1].isDeveloper())
        assertTrue(result.hasSummary())
    }

    @Test
    @DisplayName("Should compact conversation into a single assistant summary message")
    fun testCompactionProducesSingleSummary() {
        val result = conversationAboveThreshold.withCompaction(maxTokens = 10)

        assertTrue(result.hasSummary())
        assertEquals(1, result.filter { it.isAssistant() }.size)
    }

    @Test
    @DisplayName("Should drop tool result messages before summarizing")
    fun testToolResultsDroppedBeforeCompaction() {
        val messages = listOf(
            user("What is the weather in Paris?"),
            toolCallAssistant("call_1", "get_weather", "{\"city\":\"Paris\"}"),
            toolResult("call_1", "Sunny, 22 degrees"),
            assistant("It is sunny and 22 degrees in Paris."),
            user("And in Tokyo?"),
            toolCallAssistant("call_2", "get_weather", "{\"city\":\"Tokyo\"}"),
            toolResult("call_2", "Cloudy, 18 degrees"),
            assistant("It is cloudy and 18 degrees in Tokyo."),
        )
        val result = messages.withCompaction(maxTokens = 10)

        assertTrue(result.none { it.isTool() })
        assertTrue(result.hasSummary())
    }

    @Test
    @DisplayName("Should drop tool-call-only assistant messages before summarizing")
    fun testToolCallOnlyAssistantMessagesDropped() {
        val messages = listOf(
            user("What is the weather in Paris?"),
            toolCallAssistant("call_1", "get_weather", "{\"city\":\"Paris\"}"),
            toolResult("call_1", "Sunny, 22 degrees"),
            assistant("It is sunny and 22 degrees in Paris."),
            user("And in Tokyo?"),
            toolCallAssistant("call_2", "get_weather", "{\"city\":\"Tokyo\"}"),
            toolResult("call_2", "Cloudy, 18 degrees"),
            assistant("It is cloudy and 18 degrees in Tokyo."),
        )
        val result = messages.withCompaction(maxTokens = 10)

        assertTrue(result.none { it.isAssistant() && it.asAssistant().toolCalls().isPresent })
        assertTrue(result.hasSummary())
    }

    @Test
    @DisplayName("Should keep text from assistant message that has both content and tool calls")
    fun testAssistantWithTextAndToolCallsPreservesText() {
        val messages = listOf(
            user("Search for something and explain."),
            ChatCompletionMessageParam.ofAssistant(
                ChatCompletionAssistantMessageParam.builder()
                    .content("Let me search for that right away.")
                    .toolCalls(listOf(
                        ChatCompletionMessageToolCall.builder()
                            .id("call_3")
                            .function(
                                ChatCompletionMessageToolCall.Function.builder()
                                    .name("search")
                                    .arguments("{\"q\":\"something\"}")
                                    .build()
                            )
                            .build()
                    ))
                    .build()
            ),
            toolResult("call_3", "Some result"),
            assistant("Here is what I found based on the search."),
            user("Can you summarize that for me please?"),
            assistant("Sure, here is the summary of the search results."),
        )
        val result = messages.withCompaction(maxTokens = 10)

        assertTrue(result.hasSummary())
    }
}
