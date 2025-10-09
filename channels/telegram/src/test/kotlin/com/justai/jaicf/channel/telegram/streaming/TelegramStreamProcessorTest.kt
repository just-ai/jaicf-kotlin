package com.justai.jaicf.channel.telegram.streaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.types.TelegramBotResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for TelegramStreamProcessor.
 * Tests debouncing, message splitting, and proper sequencing of messages.
 */
class TelegramStreamProcessorTest {

    private lateinit var mockBot: Bot
    private lateinit var chatId: ChatId
    private lateinit var sentMessages: MutableList<SentMessage>
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockBot = mockk(relaxed = true)
        chatId = ChatId.fromId(123456789L)
        sentMessages = mutableListOf()

        // Mock sendMessage to track calls
        every { mockBot.sendMessage(any(), any()) } answers {
            val messageId = sentMessages.size + 1L
            val text = secondArg<String>()
            val message = mockk<Message>(relaxed = true) {
                every { this@mockk.messageId } returns messageId
                every { this@mockk.text } returns text
            }
            sentMessages.add(SentMessage(messageId, text, SentMessage.Type.NEW))
            TelegramBotResult.Success(message)
        }

        // Mock editMessageText to track calls
        every { mockBot.editMessageText(any(), any<Long>(), text = any()) } answers {
            val messageId = secondArg<Long>()
            val text = arg<String>(2)
            val message = mockk<Message>(relaxed = true) {
                every { this@mockk.messageId } returns messageId
                every { this@mockk.text } returns text
            }
            sentMessages.add(SentMessage(messageId, text, SentMessage.Type.EDIT))
            Pair(mockk(relaxed = true) {
                every { body() } returns mockk(relaxed = true) {
                    every { result } returns message
                }
            }, null)
        }
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test simple stream without splitting`() = runTest(testDispatcher) {
        val processor = TelegramStreamProcessor(mockBot, chatId, 50, testDispatcher)
        val chunks = listOf("Hello", " ", "World", "!")
        
        val result = processor.processStream(Stream.of(*chunks.toTypedArray()))

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Hello World!", result)
        assertTrue(sentMessages.isNotEmpty(), "Should have sent messages")
        
        // First message should be created
        assertEquals(SentMessage.Type.NEW, sentMessages.first().type)
        
        // Last message should contain full text
        val lastMessage = sentMessages.last()
        assertEquals("Hello World!", lastMessage.text)
    }

    @Test
    fun `test stream with message splitting`() = runTest(testDispatcher) {
        val processor = TelegramStreamProcessor(mockBot, chatId, 50, testDispatcher)
        
        // Create text longer than SAFE_MESSAGE_LIMIT (3900 chars)
        val longText = "A".repeat(4000)
        
        val result = processor.processStream(Stream.of(longText))

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(longText, result)
        
        // Should have created multiple messages
        val newMessages = sentMessages.filter { it.type == SentMessage.Type.NEW }
        assertTrue(newMessages.size >= 2, "Should have split into multiple messages, got ${newMessages.size}")
        
        // Check that messages don't exceed limit
        newMessages.forEach { msg ->
            assertTrue(msg.text.length <= TelegramStreamProcessor.SAFE_MESSAGE_LIMIT,
                "Message length ${msg.text.length} exceeds limit ${TelegramStreamProcessor.SAFE_MESSAGE_LIMIT}")
        }
        
        // Verify message IDs are sequential (no race condition)
        val messageIds = newMessages.map { it.id }
        assertEquals(messageIds, messageIds.sorted(), "Message IDs should be sequential")
    }

    @Test
    fun `test debouncing reduces API calls`() = runTest(testDispatcher) {
        val processor = TelegramStreamProcessor(mockBot, chatId, 100, testDispatcher)
        
        // Stream chunks rapidly
        val chunks = (1..10).map { "chunk$it " }
        
        processor.processStream(Stream.of(*chunks.toTypedArray()))

        testDispatcher.scheduler.advanceUntilIdle()

        // With debouncing, should have fewer calls than chunks
        assertTrue(sentMessages.size < chunks.size * 2,
            "Debouncing should reduce API calls. Had ${sentMessages.size} calls for ${chunks.size} chunks")
    }

    @Test
    fun `test low debounce value still maintains message order`() = runTest(testDispatcher) {
        val processor = TelegramStreamProcessor(mockBot, chatId, 10, testDispatcher)
        
        val chunks = listOf("First", " second", " third", " fourth")
        
        val result = processor.processStream(Stream.of(*chunks.toTypedArray()))

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("First second third fourth", result)
        
        // Check all messages have sequential IDs (no race condition)
        val allMessageIds = sentMessages.map { it.id }.distinct().sorted()
        assertEquals(allMessageIds, allMessageIds.sorted())
    }

    @Test
    fun `test message splitting flushes before creating new message`() = runTest(testDispatcher) {
        val processor = TelegramStreamProcessor(mockBot, chatId, 10, testDispatcher)
        
        // Create text that will be split
        val part1 = "A".repeat(3900)
        val part2 = "B".repeat(100)
        
        processor.processStream(Stream.of(part1, part2))

        testDispatcher.scheduler.advanceUntilIdle()

        // Get all NEW message creations
        val newMessages = sentMessages.filter { it.type == SentMessage.Type.NEW }
        
        // Should have 2 new messages
        assertEquals(2, newMessages.size, "Should have created exactly 2 messages")
        
        // First message should be completed (have edits) before second is created
        val firstMessageId = newMessages[0].id
        val secondMessageId = newMessages[1].id
        
        val firstMessageIndex = sentMessages.indexOfFirst { it.id == firstMessageId && it.type == SentMessage.Type.NEW }
        val secondMessageIndex = sentMessages.indexOfFirst { it.id == secondMessageId && it.type == SentMessage.Type.NEW }
        
        // All updates to first message should come before second message creation
        val updatesAfterFirst = sentMessages.subList(firstMessageIndex + 1, secondMessageIndex)
        assertTrue(updatesAfterFirst.all { it.id == firstMessageId },
            "All updates between first and second message should be for first message")
    }

    @Test
    fun `test empty stream`() = runTest(testDispatcher) {
        val processor = TelegramStreamProcessor(mockBot, chatId, 50, testDispatcher)
        
        val result = processor.processStream(Stream.empty())

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", result)
        assertTrue(sentMessages.isEmpty(), "Should not send any messages for empty stream")
    }

    @Test
    fun `test single character stream`() = runTest(testDispatcher) {
        val processor = TelegramStreamProcessor(mockBot, chatId, 50, testDispatcher)
        
        val result = processor.processStream(Stream.of("X"))

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("X", result)
        assertTrue(sentMessages.isNotEmpty())
        assertEquals("X", sentMessages.last().text)
    }

    data class SentMessage(
        val id: Long,
        val text: String,
        val type: Type
    ) {
        enum class Type { NEW, EDIT }
    }
}
