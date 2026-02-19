package com.justai.jaicf.activator.llm.memory.transformers

import com.justai.jaicf.activator.llm.LLMMessage.assistant
import com.justai.jaicf.activator.llm.LLMMessage.developer
import com.justai.jaicf.activator.llm.LLMMessage.system
import com.justai.jaicf.activator.llm.LLMMessage.user
import com.openai.models.chat.completions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("withTokenLimit tests")
class TokenLimitTest {

    // Helper function to create assistant message with tool calls
    private fun toolCall(toolCallId: String, functionName: String, arguments: String) =
        ChatCompletionMessageParam.ofAssistant(
            ChatCompletionAssistantMessageParam.builder()
                .toolCalls(
                    listOf(
                        ChatCompletionMessageToolCall.builder()
                            .id(toolCallId)
                            .function(
                                ChatCompletionMessageToolCall.Function.builder()
                                    .name(functionName)
                                    .arguments(arguments)
                                    .build()
                            )
                            .build()
                    )
                )
                .build()
        )

    // Helper function to create tool result message
    private fun toolResult(toolCallId: String, content: String) =
        ChatCompletionMessageParam.ofTool(
            ChatCompletionToolMessageParam.builder()
                .toolCallId(toolCallId)
                .content(content)
                .build()
        )

    @Test
    @DisplayName("Should preserve system messages regardless of token limit")
    fun testSystemMessagesAlwaysPreserved() {
        val messages = listOf(
            system("You are a helpful assistant"),
            user("Hello"),
            assistant("Hi there!")
        )

        // Set a very low token limit
        val result = messages.withTokenLimit(maxTokens = 10)

        // System message should still be present
        assertTrue(result.isNotEmpty())
        assertTrue(result.first().isSystem())
    }

    @Test
    @DisplayName("Should preserve developer messages regardless of token limit")
    fun testDeveloperMessagesAlwaysPreserved() {
        val messages = listOf(
            developer("Important developer instructions"),
            user("Hello"),
            assistant("Hi there!")
        )

        // Set a very low token limit
        val result = messages.withTokenLimit(maxTokens = 10)

        // Developer message should still be present
        assertTrue(result.isNotEmpty())
        assertTrue(result.first().isDeveloper())
    }

    @Test
    @DisplayName("Should preserve both system and developer messages")
    fun testSystemAndDeveloperMessagesPreserved() {
        val messages = listOf(
            system("System instructions"),
            developer("Developer instructions"),
            user("Hello"),
            assistant("Hi!")
        )

        val result = messages.withTokenLimit(maxTokens = 50)

        assertTrue(result.size >= 2)
        assertTrue(result[0].isSystem())
        assertTrue(result[1].isDeveloper())
    }

    @Test
    @DisplayName("Should maintain proper user-assistant sequence")
    fun testUserAssistantSequence() {
        val messages = listOf(
            user("First message"),
            assistant("First response"),
            user("Second message"),
            assistant("Second response")
        )

        val result = messages.withTokenLimit(maxTokens = 100)

        // Should start with user message
        assertTrue(result.isNotEmpty())
        assertTrue(result.first().isUser())

        // Verify alternating pattern
        for (i in result.indices) {
            if (i % 2 == 0) {
                assertTrue(result[i].isUser(), "Message at index $i should be user")
            } else {
                assertTrue(result[i].isAssistant(), "Message at index $i should be assistant")
            }
        }
    }

    @Test
    @DisplayName("Should remove orphaned assistant messages at start")
    fun testRemoveOrphanedAssistantMessages() {
        val messages = listOf(
            assistant("Orphaned assistant message"),
            user("User message"),
            assistant("Valid assistant response")
        )

        val result = messages.withTokenLimit(maxTokens = 1000)

        // Should start with user message, not orphaned assistant
        assertTrue(result.first().isUser())
        assertEquals(2, result.size)
    }

    @Test
    @DisplayName("Should handle empty message list")
    fun testEmptyMessageList() {
        val messages = emptyList<ChatCompletionMessageParam>()
        val result = messages.withTokenLimit(maxTokens = 100)
        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Should handle null message list")
    fun testNullMessageList() {
        val messages: List<ChatCompletionMessageParam>? = null
        val result = messages.withTokenLimit(maxTokens = 100)
        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Should trim oldest messages first")
    fun testTrimOldestMessagesFirst() {
        val messages = listOf(
            user("Oldest message"),
            assistant("Old response"),
            user("Recent message"),
            assistant("Recent response")
        )

        // Set a limit that can only fit the last two messages
        val result = messages.withTokenLimit(maxTokens = 30)

        // Should contain recent messages
        assertTrue(result.any { msg ->
            msg.isUser() && msg.asUser().content().toString().contains("Recent")
        })
    }

    @Test
    @DisplayName("Should handle messages with name field")
    fun testMessagesWithNameField() {
        val messages = listOf(
            system {
                content("System message")
                name("system_1")
            },
            user {
                content("User message")
                name("john_doe")
            },
            assistant {
                content("Assistant message")
                name("assistant_1")
            }
        )

        val result = messages.withTokenLimit(maxTokens = 1000)

        assertEquals(3, result.size)
        assertTrue(result[0].asSystem().name().isPresent)
        assertTrue(result[1].asUser().name().isPresent)
        assertTrue(result[2].asAssistant().name().isPresent)
    }

    @Test
    @DisplayName("Should return only system messages when they exceed token limit")
    fun testOnlySystemMessagesWhenExceedingLimit() {
        val messages = listOf(
            system("Very long system message that contains a lot of tokens and exceeds the maximum limit"),
            user("Hello")
        )

        // Set limit lower than system message tokens
        val result = messages.withTokenLimit(maxTokens = 5)

        // Should only contain system message
        assertEquals(1, result.size)
        assertTrue(result.first().isSystem())
    }

    @Test
    @DisplayName("Should use withTokenLimit function directly")
    fun testWithTokenLimitFunction() {
        val messages = listOf(
            user("Hello"),
            assistant("Hi there!")
        )

        val transformer = withTokenLimit(maxTokens = 1000)
        val result = transformer(messages)

        assertEquals(2, result.size)
    }

    @Test
    @DisplayName("Should preserve message order within categories")
    fun testMessageOrderPreserved() {
        val messages = listOf(
            system("System 1"),
            developer("Developer 1"),
            user("User 1"),
            assistant("Assistant 1"),
            user("User 2"),
            assistant("Assistant 2")
        )

        val result = messages.withTokenLimit(maxTokens = 1000)

        // Verify system/developer messages come first
        assertTrue(result[0].isSystem())
        assertTrue(result[1].isDeveloper())

        // Verify conversation messages follow
        assertTrue(result[2].isUser())
        assertTrue(result[2].asUser().content().toString().contains("User 1"))
    }

    @Test
    @DisplayName("Should handle all messages within token limit")
    fun testAllMessagesWithinLimit() {
        val messages = listOf(
            user("Hi"),
            assistant("Hello!")
        )

        val result = messages.withTokenLimit(maxTokens = 10000)

        // All messages should be retained
        assertEquals(2, result.size)
    }

    @Test
    @DisplayName("Should handle multiple system messages")
    fun testMultipleSystemMessages() {
        val messages = listOf(
            system("System 1"),
            system("System 2"),
            user("Hello"),
            assistant("Hi!")
        )

        val result = messages.withTokenLimit(maxTokens = 50)

        // Both system messages should be preserved
        val systemMessages = result.filter { it.isSystem() }
        assertEquals(2, systemMessages.size)
    }

    @Test
    @DisplayName("Should handle tight token limit with conversation")
    fun testTightTokenLimitWithConversation() {
        val messages = listOf(
            user("Message one"),
            assistant("Response one"),
            user("Message two"),
            assistant("Response two"),
            user("Message three"),
            assistant("Response three")
        )

        // Set a tight limit that should only fit the last pair
        val result = messages.withTokenLimit(maxTokens = 25)

        // Should have trimmed older messages
        assertTrue(result.size < messages.size)
        // Should start with user message
        assertTrue(result.first().isUser())
        // Should end with assistant message
        assertTrue(result.last().isAssistant())
    }

    @Test
    @DisplayName("Should preserve system messages at start even with conversation trimming")
    fun testSystemMessagesFirstAfterTrimming() {
        val messages = listOf(
            system("System"),
            user("Old message"),
            assistant("Old response"),
            user("New message"),
            assistant("New response")
        )

        val result = messages.withTokenLimit(maxTokens = 40)

        // System message should be first even after trimming conversation
        assertTrue(result.first().isSystem())
        // Should have trimmed old conversation but kept new
        assertTrue(result.any { msg ->
            msg.isUser() && msg.asUser().content().toString().contains("New")
        })
    }

    @Test
    @DisplayName("Should keep tool call and tool result pairs together")
    fun testToolCallPairsKeptTogether() {
        val toolCallId = "call_123"
        val messages = listOf(
            user("What's the weather?"),
            toolCall(toolCallId, "get_weather", "{\"location\":\"NYC\"}"),
            toolResult(toolCallId, "Sunny, 75°F"),
            assistant("It's sunny and 75°F in NYC")
        )

        val result = messages.withTokenLimit(maxTokens = 1000)

        assertEquals(4, result.size)
        assertTrue(result[1].isAssistant())
        assertTrue(result[2].isTool())

        // Verify the tool result matches the tool call
        val assistantMsg = result[1].asAssistant()
        val toolMsg = result[2].asTool()
        assertEquals(toolCallId, assistantMsg.toolCalls().get().first().id())
        assertEquals(toolCallId, toolMsg.toolCallId())
    }

    @Test
    @DisplayName("Should drop tool call pair if it doesn't fit within token limit")
    fun testDropToolCallPairWhenExceedsLimit() {
        val toolCallId = "call_123"
        val messages = listOf(
            user("Very short question"),
            toolCall(
                toolCallId,
                "very_long_function_name_that_takes_many_tokens",
                "{\"param\":\"very long parameter value that takes many tokens\"}"
            ),
            toolResult(toolCallId, "Result")
        )

        // Set a very tight token limit
        val result = messages.withTokenLimit(maxTokens = 20)

        // Should only include the user message, not the tool call pair
        assertTrue(result.size <= 2)
        if (result.isNotEmpty()) {
            assertTrue(result.first().isUser())
        }
    }

    @Test
    @DisplayName("Should handle multiple tool calls in single assistant message")
    fun testMultipleToolCalls() {
        val toolCallId1 = "call_123"
        val toolCallId2 = "call_456"

        val messages = listOf(
            user("Get weather and time"),
            ChatCompletionMessageParam.ofAssistant(
                ChatCompletionAssistantMessageParam.builder()
                    .toolCalls(
                        listOf(
                            ChatCompletionMessageToolCall.builder()
                                .id(toolCallId1)
                                .function(
                                    ChatCompletionMessageToolCall.Function.builder()
                                        .name("get_weather")
                                        .arguments("{\"location\":\"NYC\"}")
                                        .build()
                                )
                                .build(),
                            ChatCompletionMessageToolCall.builder()
                                .id(toolCallId2)
                                .function(
                                    ChatCompletionMessageToolCall.Function.builder()
                                        .name("get_time")
                                        .arguments("{\"timezone\":\"EST\"}")
                                        .build()
                                )
                                .build()
                        )
                    )
                    .build()
            ),
            toolResult(toolCallId1, "Sunny, 75°F"),
            toolResult(toolCallId2, "3:45 PM EST")
        )

        val result = messages.withTokenLimit(maxTokens = 1000)

        assertEquals(4, result.size)
        assertTrue(result[1].asAssistant().toolCalls().get().size == 2)
        assertTrue(result[2].isTool())
        assertTrue(result[3].isTool())
    }

    @Test
    @DisplayName("Should preserve tool call pairs even with tight token limits")
    fun testToolCallPairsPreservedWithTightLimit() {
        val toolCallId1 = "call_old"
        val toolCallId2 = "call_new"

        val messages = listOf(
            user("Old question"),
            toolCall(toolCallId1, "old_function", "{}"),
            toolResult(toolCallId1, "Old result"),
            user("New question"),
            toolCall(toolCallId2, "new_function", "{}"),
            toolResult(toolCallId2, "New result")
        )

        // Set limit to only fit the recent tool call pair
        val result = messages.withTokenLimit(maxTokens = 50)

        // Should have the recent tool call pair
        assertTrue(result.any { it.isTool() })

        // Verify it's the new tool call, not the old one
        val toolMessages = result.filter { it.isTool() }
        if (toolMessages.isNotEmpty()) {
            assertTrue(toolMessages.any { it.asTool().toolCallId() == toolCallId2 })
        }
    }

    @Test
    @DisplayName("Should not include orphaned tool results without matching assistant message")
    fun testOrphanedToolResultsRemoved() {
        val toolCallId = "call_123"
        val messages = listOf(
            user("Question"),
            toolCall(toolCallId, "function", "{}"),
            toolResult(toolCallId, "Result"),
            toolResult("orphaned_call", "Orphaned result"),
            user("Another question"),
            assistant("Answer")
        )

        val result = messages.withTokenLimit(maxTokens = 1000)

        // Count tool results
        val toolResults = result.filter { it.isTool() }

        // Should only have the valid tool result, not the orphaned one
        assertEquals(1, toolResults.size)
        assertEquals(toolCallId, toolResults.first().asTool().toolCallId())
    }

    @Test
    @DisplayName("Should handle conversation with mixed tool calls and regular messages")
    fun testMixedToolCallsAndRegularMessages() {
        val toolCallId = "call_123"

        val messages = listOf(
            system("You are a helpful assistant"),
            user("Hello"),
            assistant("Hi! How can I help?"),
            user("What's the weather?"),
            toolCall(toolCallId, "get_weather", "{\"location\":\"NYC\"}"),
            toolResult(toolCallId, "Sunny, 75°F"),
            assistant("It's sunny and 75°F"),
            user("Thanks!"),
            assistant("You're welcome!")
        )

        val result = messages.withTokenLimit(maxTokens = 1000)

        // All messages should be retained with high limit
        assertEquals(9, result.size)

        // System message should be first
        assertTrue(result.first().isSystem())

        // Tool call and result should be present and paired
        assertTrue(result.any { it.isAssistant() && it.asAssistant().toolCalls().isPresent })
        assertTrue(result.any { it.isTool() })
    }

    @Test
    @DisplayName("Should trim old messages but preserve recent tool call pairs")
    fun testTrimOldButPreserveRecentToolCalls() {
        val oldToolCallId = "call_old"
        val recentToolCallId = "call_recent"

        val messages = listOf(
            user("Old message 1"),
            assistant("Old response 1"),
            user("Old message 2"),
            toolCall(oldToolCallId, "old_func", "{}"),
            toolResult(oldToolCallId, "Old tool result"),
            assistant("Old response 2"),
            user("Recent question"),
            toolCall(recentToolCallId, "recent_func", "{}"),
            toolResult(recentToolCallId, "Recent result"),
            assistant("Recent response")
        )

        // Set moderate limit
        val result = messages.withTokenLimit(maxTokens = 80)

        // Should have trimmed old messages
        assertTrue(result.size < messages.size)

        // Should contain the recent tool call
        val toolMessages = result.filter { it.isTool() }
        if (toolMessages.isNotEmpty()) {
            assertTrue(toolMessages.any { it.asTool().toolCallId() == recentToolCallId })
        }
    }
}
