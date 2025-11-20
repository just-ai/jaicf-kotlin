package com.justai.jaicf.channel.telegram.aggregation

import com.justai.jaicf.channel.telegram.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for AggregationStrategy implementations.
 */
class AggregationStrategyTest {

    @Test
    fun `DefaultAggregationStrategy always aggregates`() = runTest {
        val strategy = DefaultAggregationStrategy()

        val request1 = TelegramTestHelpers.createTextRequest(text = "first")
        val request2 = TelegramTestHelpers.createTextRequest(text = "second")
        val pending = listOf(request1)

        val result = strategy.shouldAggregate(
            chatId = 789L,
            newRequest = request2,
            pendingRequests = pending
        )

        assertTrue(result)
    }

    @Test
    fun `CommandAwareAggregationStrategy aggregates non-command messages`() = runTest {
        val strategy = CommandAwareAggregationStrategy()

        val request1 = TelegramTestHelpers.createTextRequest(text = "hello")
        val request2 = TelegramTestHelpers.createTextRequest(text = "world")
        val pending = listOf(request1)

        val result = strategy.shouldAggregate(
            chatId = 789L,
            newRequest = request2,
            pendingRequests = pending
        )

        assertTrue(result)
    }

    @Test
    fun `CommandAwareAggregationStrategy skips command messages`() = runTest {
        val strategy = CommandAwareAggregationStrategy()

        val request1 = TelegramTestHelpers.createTextRequest(text = "hello")
        val commandRequest = TelegramTestHelpers.createTextRequest(text = "/start")
        val pending = listOf(request1)

        val result = strategy.shouldAggregate(
            chatId = 789L,
            newRequest = commandRequest,
            pendingRequests = pending
        )

        assertFalse(result)
    }

    @Test
    fun `CommandAwareAggregationStrategy skips commands with leading whitespace`() = runTest {
        val strategy = CommandAwareAggregationStrategy()

        val request1 = TelegramTestHelpers.createTextRequest(text = "hello")
        val commandRequest = TelegramTestHelpers.createTextRequest(text = "  /help")
        val pending = listOf(request1)

        val result = strategy.shouldAggregate(
            chatId = 789L,
            newRequest = commandRequest,
            pendingRequests = pending
        )

        assertFalse(result)
    }

    @Test
    fun `CommandAwareAggregationStrategy enforces 10 item limit`() = runTest {
        val strategy = CommandAwareAggregationStrategy()

        // Create 10 pending requests
        val pending = (1..10).map {
            TelegramTestHelpers.createTextRequest(text = "message $it", messageId = it.toLong())
        }

        val newRequest = TelegramTestHelpers.createTextRequest(text = "new message", messageId = 11L)

        val result = strategy.shouldAggregate(
            chatId = 789L,
            newRequest = newRequest,
            pendingRequests = pending
        )

        assertFalse(result, "Should not aggregate when 10 items already pending")
    }

    @Test
    fun `createComposite returns single request as-is`() {
        val strategy = DefaultAggregationStrategy()
        val request = TelegramTestHelpers.createTextRequest(text = "single")

        val result = strategy.createComposite(listOf(request))

        assertEquals(request, result)
        assertFalse(result.isAggregated)
    }

    @Test
    fun `createComposite creates aggregated TelegramTextRequest`() {
        val strategy = DefaultAggregationStrategy()
        val req1 = TelegramTestHelpers.createTextRequest(text = "first", messageId = 1)
        val req2 = TelegramTestHelpers.createTextRequest(text = "second", messageId = 2)
        val req3 = TelegramTestHelpers.createTextRequest(text = "third", messageId = 3)

        val result = strategy.createComposite(listOf(req1, req2, req3))

        assertTrue(result.isAggregated)
        assertEquals(3, result.aggregated.size)
        assertEquals(req1, result.aggregated[0])
        assertEquals(req2, result.aggregated[1])
        assertEquals(req3, result.aggregated[2])

        // Result should be a copy of first request
        assertTrue(result is TelegramTextRequest)
        assertEquals("first", result.input)
        assertEquals(1L, result.message.messageId)
    }

    @Test
    fun `createComposite creates aggregated TelegramPhotosRequest`() {
        val strategy = DefaultAggregationStrategy()
        val photo1 = TelegramTestHelpers.mockPhotoSize(fileId = "photo_1")
        val photo2 = TelegramTestHelpers.mockPhotoSize(fileId = "photo_2")

        val req1 = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo1), messageId = 1)
        val req2 = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo2), messageId = 2)

        val result = strategy.createComposite(listOf(req1, req2))

        assertTrue(result.isAggregated)
        assertEquals(2, result.aggregated.size)
        assertTrue(result is TelegramPhotosRequest)
        assertEquals("photo_1", result.photos[0].fileId)
    }

    @Test
    fun `createComposite creates aggregated TelegramVideoRequest`() {
        val strategy = DefaultAggregationStrategy()
        val video1 = TelegramTestHelpers.mockVideo(fileId = "video_1")
        val video2 = TelegramTestHelpers.mockVideo(fileId = "video_2")

        val req1 = TelegramTestHelpers.createVideoRequest(video = video1, messageId = 1)
        val req2 = TelegramTestHelpers.createVideoRequest(video = video2, messageId = 2)

        val result = strategy.createComposite(listOf(req1, req2))

        assertTrue(result.isAggregated)
        assertEquals(2, result.aggregated.size)
        assertTrue(result is TelegramVideoRequest)
        assertEquals("video_1", result.video.fileId)
    }

    @Test
    fun `createComposite creates aggregated TelegramDocumentRequest`() {
        val strategy = DefaultAggregationStrategy()
        val doc1 = TelegramTestHelpers.mockDocument(fileId = "doc_1")
        val doc2 = TelegramTestHelpers.mockDocument(fileId = "doc_2")

        val req1 = TelegramTestHelpers.createDocumentRequest(document = doc1, messageId = 1)
        val req2 = TelegramTestHelpers.createDocumentRequest(document = doc2, messageId = 2)

        val result = strategy.createComposite(listOf(req1, req2))

        assertTrue(result.isAggregated)
        assertEquals(2, result.aggregated.size)
        assertTrue(result is TelegramDocumentRequest)
        assertEquals("doc_1", result.document.fileId)
    }

    @Test
    fun `createComposite handles mixed request types`() {
        val strategy = DefaultAggregationStrategy()
        val textReq = TelegramTestHelpers.createTextRequest(text = "text", messageId = 1)
        val photo = TelegramTestHelpers.mockPhotoSize(fileId = "photo_1")
        val photosReq = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo), messageId = 2)

        val result = strategy.createComposite(listOf(textReq, photosReq))

        assertTrue(result.isAggregated)
        assertEquals(2, result.aggregated.size)

        // Result should be a copy of first request (text)
        assertTrue(result is TelegramTextRequest)
        assertEquals("text", result.input)

        // But aggregated list contains both types
        assertTrue(result.aggregated[0] is TelegramTextRequest)
        assertTrue(result.aggregated[1] is TelegramPhotosRequest)
    }

    @Test
    fun `createComposite creates aggregated TelegramLocationRequest`() {
        val strategy = DefaultAggregationStrategy()
        val loc1 = TelegramTestHelpers.mockLocation(latitude = 55.7558f)
        val loc2 = TelegramTestHelpers.mockLocation(latitude = 40.7128f)

        val req1 = TelegramTestHelpers.createLocationRequest(location = loc1, messageId = 1)
        val req2 = TelegramTestHelpers.createLocationRequest(location = loc2, messageId = 2)

        val result = strategy.createComposite(listOf(req1, req2))

        assertTrue(result.isAggregated)
        assertEquals(2, result.aggregated.size)
        assertTrue(result is TelegramLocationRequest)
        assertEquals(55.7558f, result.location.latitude)
    }

    @Test
    fun `createComposite creates aggregated TelegramContactRequest`() {
        val strategy = DefaultAggregationStrategy()
        val contact1 = TelegramTestHelpers.mockContact(firstName = "John")
        val contact2 = TelegramTestHelpers.mockContact(firstName = "Jane")

        val req1 = TelegramTestHelpers.createContactRequest(contact = contact1, messageId = 1)
        val req2 = TelegramTestHelpers.createContactRequest(contact = contact2, messageId = 2)

        val result = strategy.createComposite(listOf(req1, req2))

        assertTrue(result.isAggregated)
        assertEquals(2, result.aggregated.size)
        assertTrue(result is TelegramContactRequest)
        assertEquals("John", result.contact.firstName)
    }
}
