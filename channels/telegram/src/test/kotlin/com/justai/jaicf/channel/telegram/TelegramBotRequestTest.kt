package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.files.*
import com.github.kotlintelegrambot.entities.payments.PreCheckoutQuery
import com.github.kotlintelegrambot.entities.payments.SuccessfulPayment
import com.github.kotlintelegrambot.entities.stickers.Sticker
import com.justai.jaicf.api.BotRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for TelegramBotRequest types and extension properties.
 */
class TelegramBotRequestTest {

    private fun createMockUpdate(messageId: Long = 1L): Update = mockk {
        every { updateId } returns 123456L
    }

    private fun createMockMessage(chatId: Long = 789L, text: String? = null): Message = mockk {
        every { messageId } returns 1L
        every { this@mockk.text } returns text
        every { date } returns System.currentTimeMillis()
        every { chat } returns Chat(chatId, "private")
    }

    private fun createMockUser(userId: Long = 999L): User = mockk {
        every { id } returns userId
        every { firstName } returns "Test"
        every { lastName } returns "User"
        every { username } returns "testuser"
        every { isBot } returns false
    }

    @Test
    fun `test TelegramTextRequest creation and properties`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L, text = "Hello")

        val request = TelegramTextRequest(update, message)

        assertEquals("12345", request.clientId)
        assertEquals("Hello", request.input)
        assertEquals(12345L, request.chatId)
        assertEquals(update, request.update)
        assertEquals(message, request.message)
    }

    @Test
    fun `test TelegramQueryRequest creation and properties`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val callbackData = "button_clicked"

        val request = TelegramQueryRequest(update, message, callbackData)

        assertEquals("12345", request.clientId)
        assertEquals(callbackData, request.input)
        assertEquals(12345L, request.chatId)
    }

    @Test
    fun `test TelegramLocationRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val location = mockk<Location> {
            every { latitude } returns 55.7558f
            every { longitude } returns 37.6173f
        }

        val request = TelegramLocationRequest(update, message, location)

        assertEquals("12345", request.clientId)
        assertEquals(TelegramEvent.LOCATION, request.input)
        assertEquals(location, request.location)
    }

    @Test
    fun `test TelegramContactRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val contact = mockk<Contact> {
            every { phoneNumber } returns "+1234567890"
            every { firstName } returns "John"
        }

        val request = TelegramContactRequest(update, message, contact)

        assertEquals(TelegramEvent.CONTACT, request.input)
        assertEquals(contact, request.contact)
    }

    @Test
    fun `test TelegramAudioRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val audio = mockk<Audio> {
            every { fileId } returns "audio_file_123"
            every { duration } returns 180
        }

        val request = TelegramAudioRequest(update, message, audio)

        assertEquals(TelegramEvent.AUDIO, request.input)
        assertEquals(audio, request.audio)
    }

    @Test
    fun `test TelegramDocumentRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val document = mockk<Document> {
            every { fileId } returns "doc_file_123"
            every { fileName } returns "test.pdf"
        }

        val request = TelegramDocumentRequest(update, message, document)

        assertEquals(TelegramEvent.DOCUMENT, request.input)
        assertEquals(document, request.document)
    }

    @Test
    fun `test TelegramAnimationRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val animation = mockk<Animation> {
            every { fileId } returns "anim_file_123"
            every { duration } returns 5
        }

        val request = TelegramAnimationRequest(update, message, animation)

        assertEquals(TelegramEvent.ANIMATION, request.input)
        assertEquals(animation, request.animation)
    }

    @Test
    fun `test TelegramGameRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val game = mockk<Game> {
            every { title } returns "Test Game"
        }

        val request = TelegramGameRequest(update, message, game)

        assertEquals(TelegramEvent.GAME, request.input)
        assertEquals(game, request.game)
    }

    @Test
    fun `test TelegramPhotosRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val photos = listOf(
            mockk<PhotoSize> {
                every { fileId } returns "photo1"
                every { width } returns 800
                every { height } returns 600
            }
        )

        val request = TelegramPhotosRequest(update, message, photos)

        assertEquals(TelegramEvent.PHOTOS, request.input)
        assertEquals(photos, request.photos)
    }

    @Test
    fun `test TelegramStickerRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val sticker = mockk<Sticker> {
            every { fileId } returns "sticker_123"
        }

        val request = TelegramStickerRequest(update, message, sticker)

        assertEquals(TelegramEvent.STICKER, request.input)
        assertEquals(sticker, request.sticker)
    }

    @Test
    fun `test TelegramVideoRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val video = mockk<Video> {
            every { fileId } returns "video_123"
            every { duration } returns 120
        }

        val request = TelegramVideoRequest(update, message, video)

        assertEquals(TelegramEvent.VIDEO, request.input)
        assertEquals(video, request.video)
    }

    @Test
    fun `test TelegramVideoNoteRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val videoNote = mockk<VideoNote> {
            every { fileId } returns "videonote_123"
            every { duration } returns 30
        }

        val request = TelegramVideoNoteRequest(update, message, videoNote)

        assertEquals(TelegramEvent.VIDEO_NOTE, request.input)
        assertEquals(videoNote, request.videoNote)
    }

    @Test
    fun `test TelegramVoiceRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage(chatId = 12345L)
        val voice = mockk<Voice> {
            every { fileId } returns "voice_123"
            every { duration } returns 45
        }

        val request = TelegramVoiceRequest(update, message, voice)

        assertEquals(TelegramEvent.VOICE, request.input)
        assertEquals(voice, request.voice)
    }

    @Test
    fun `test TelegramPreCheckoutRequest creation`() {
        val update = createMockUpdate()
        val user = createMockUser(userId = 555L)
        val preCheckoutQuery = mockk<PreCheckoutQuery> {
            every { id } returns "query_123"
            every { from } returns user
            every { currency } returns "USD"
            every { totalAmount } returns java.math.BigInteger.valueOf(1000)
            every { invoicePayload } returns "payload"
        }

        val request = TelegramPreCheckoutRequest(update, preCheckoutQuery)

        assertEquals("555", request.clientId)
        assertEquals(TelegramEvent.PRE_CHECKOUT, request.input)
        assertEquals(preCheckoutQuery, request.preCheckoutQuery)
        assertNotNull(request.message)
        assertEquals(555L, request.message.chat.id)
    }

    // Note: TelegramSuccessfulPaymentRequest test omitted due to complex BigInteger types

    @Test
    fun `test telegram extension property on BotRequest`() {
        val update = createMockUpdate()
        val message = createMockMessage()
        val telegramRequest: BotRequest = TelegramTextRequest(update, message)

        val result: TelegramBotRequest? = telegramRequest.telegram
        assertNotNull(result)
    }

    @Test
    fun `test text extension property`() {
        val update = createMockUpdate()
        val message = createMockMessage(text = "test")
        val request: TelegramBotRequest = TelegramTextRequest(update, message)

        val result: TelegramTextRequest? = request.text
        assertNotNull(result)
    }

    @Test
    fun `test callback extension property`() {
        val update = createMockUpdate()
        val message = createMockMessage()
        val request: TelegramBotRequest = TelegramQueryRequest(update, message, "data")

        val result: TelegramQueryRequest? = request.callback
        assertNotNull(result)
    }

    @Test
    fun `test location extension property`() {
        val update = createMockUpdate()
        val message = createMockMessage()
        val location = mockk<Location>()
        val request: TelegramBotRequest = TelegramLocationRequest(update, message, location)

        val result: TelegramLocationRequest? = request.location
        assertNotNull(result)
    }

    @Test
    fun `test extension properties return null for wrong type`() {
        val update = createMockUpdate()
        val message = createMockMessage(text = "test")
        val request: TelegramBotRequest = TelegramTextRequest(update, message)

        // Text request should not be callback
        assertNull(request.callback)
        assertNull(request.location)
        assertNull(request.contact)
        assertNull(request.audio)
    }

    @Test
    fun `test clientId extraction from message`() {
        val message = createMockMessage(chatId = 99999L)
        assertEquals("99999", message.clientId)
    }

    @Test
    fun `test TelegramInvocationEventRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage()

        val request = TelegramInvocationEventRequest(
            update = update,
            message = message,
            clientId = "client123",
            input = "test_event",
            requestData = """{"key": "value"}"""
        )

        assertEquals("client123", request.clientId)
        assertEquals("test_event", request.input)
        assertEquals("""{"key": "value"}""", request.requestData)
        assertEquals(update, request.update)
        assertEquals(message, request.message)
    }

    @Test
    fun `test TelegramInvocationQueryRequest creation`() {
        val update = createMockUpdate()
        val message = createMockMessage()

        val request = TelegramInvocationQueryRequest(
            update = update,
            message = message,
            clientId = "client456",
            input = "test query",
            requestData = """{"data": "test"}"""
        )

        assertEquals("client456", request.clientId)
        assertEquals("test query", request.input)
        assertEquals("""{"data": "test"}""", request.requestData)
    }

    // Tests for aggregated requests and new extension properties

    @Test
    fun `test isAggregated returns false for non-aggregated request`() {
        val request = TelegramTestHelpers.createTextRequest()

        assertEquals(false, request.isAggregated)
        assertEquals(0, request.aggregated.size)
    }

    @Test
    fun `test isAggregated returns true for aggregated request`() {
        val req1 = TelegramTestHelpers.createTextRequest(text = "hello")
        val req2 = TelegramTestHelpers.createTextRequest(text = "world")
        val aggregated = TelegramTestHelpers.createTextRequest(
            text = "hello",
            aggregated = listOf(req1, req2)
        )

        assertEquals(true, aggregated.isAggregated)
        assertEquals(2, aggregated.aggregated.size)
    }

    @Test
    fun `test allItems returns single item for non-aggregated request`() {
        val request = TelegramTestHelpers.createTextRequest(text = "test")

        val items = request.allItems

        assertEquals(1, items.size)
        assertEquals(request, items[0])
    }

    @Test
    fun `test allItems returns aggregated list for aggregated request`() {
        val req1 = TelegramTestHelpers.createTextRequest(text = "one", messageId = 1)
        val req2 = TelegramTestHelpers.createTextRequest(text = "two", messageId = 2)
        val req3 = TelegramTestHelpers.createTextRequest(text = "three", messageId = 3)
        val aggregated = TelegramTestHelpers.createTextRequest(
            text = "one",
            aggregated = listOf(req1, req2, req3)
        )

        val items = aggregated.allItems

        assertEquals(3, items.size)
        assertEquals(req1, items[0])
        assertEquals(req2, items[1])
        assertEquals(req3, items[2])
    }

    @Test
    fun `test itemsOfType filters by type correctly`() {
        val textReq1 = TelegramTestHelpers.createTextRequest(text = "hello", messageId = 1)
        val textReq2 = TelegramTestHelpers.createTextRequest(text = "world", messageId = 2)
        val photosReq = TelegramTestHelpers.createPhotosRequest(messageId = 3)
        val videoReq = TelegramTestHelpers.createVideoRequest(messageId = 4)

        val aggregated = TelegramTestHelpers.createTextRequest(
            text = "hello",
            aggregated = listOf(textReq1, photosReq, textReq2, videoReq)
        )

        val textItems = aggregated.itemsOfType<TelegramTextRequest>()
        val photoItems = aggregated.itemsOfType<TelegramPhotosRequest>()
        val videoItems = aggregated.itemsOfType<TelegramVideoRequest>()

        assertEquals(2, textItems.size)
        assertEquals(1, photoItems.size)
        assertEquals(1, videoItems.size)
        assertEquals(textReq1, textItems[0])
        assertEquals(textReq2, textItems[1])
        assertEquals(photosReq, photoItems[0])
        assertEquals(videoReq, videoItems[0])
    }

    @Test
    fun `test allTexts collects from non-aggregated request`() {
        val request = TelegramTestHelpers.createTextRequest(text = "single message")

        val texts = request.allTexts

        assertEquals(1, texts.size)
        assertEquals("single message", texts[0])
    }

    @Test
    fun `test allTexts collects from aggregated request`() {
        val req1 = TelegramTestHelpers.createTextRequest(text = "first", messageId = 1)
        val req2 = TelegramTestHelpers.createTextRequest(text = "second", messageId = 2)
        val req3 = TelegramTestHelpers.createTextRequest(text = "third", messageId = 3)
        val aggregated = TelegramTestHelpers.createTextRequest(
            text = "first",
            aggregated = listOf(req1, req2, req3)
        )

        val texts = aggregated.allTexts

        assertEquals(3, texts.size)
        assertEquals("first", texts[0])
        assertEquals("second", texts[1])
        assertEquals("third", texts[2])
    }

    @Test
    fun `test allPhotos collects from non-aggregated request`() {
        val photo = TelegramTestHelpers.mockPhotoSize(fileId = "photo_1")
        val request = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo))

        val photos = request.allPhotos

        assertEquals(1, photos.size)
        assertEquals("photo_1", photos[0].fileId)
    }

    @Test
    fun `test allPhotos collects from aggregated request`() {
        val photo1 = TelegramTestHelpers.mockPhotoSize(fileId = "photo_1")
        val photo2 = TelegramTestHelpers.mockPhotoSize(fileId = "photo_2")
        val photo3 = TelegramTestHelpers.mockPhotoSize(fileId = "photo_3")

        val req1 = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo1), messageId = 1)
        val req2 = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo2), messageId = 2)
        val req3 = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo3), messageId = 3)

        val aggregated = TelegramTestHelpers.createPhotosRequest(
            photos = listOf(photo1),
            aggregated = listOf(req1, req2, req3)
        )

        val photos = aggregated.allPhotos

        assertEquals(3, photos.size)
        assertEquals("photo_1", photos[0].fileId)
        assertEquals("photo_2", photos[1].fileId)
        assertEquals("photo_3", photos[2].fileId)
    }

    @Test
    fun `test allPhotos from mixed request types`() {
        val photo1 = TelegramTestHelpers.mockPhotoSize(fileId = "photo_1")
        val photo2 = TelegramTestHelpers.mockPhotoSize(fileId = "photo_2")

        val textReq = TelegramTestHelpers.createTextRequest(text = "text", messageId = 1)
        val photosReq1 = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo1), messageId = 2)
        val photosReq2 = TelegramTestHelpers.createPhotosRequest(photos = listOf(photo2), messageId = 3)

        val aggregated = TelegramTestHelpers.createTextRequest(
            text = "text",
            aggregated = listOf(textReq, photosReq1, photosReq2)
        )

        val photos = aggregated.allPhotos

        assertEquals(2, photos.size)
        assertEquals("photo_1", photos[0].fileId)
        assertEquals("photo_2", photos[1].fileId)
    }

    @Test
    fun `test allVideos collects from aggregated request`() {
        val video1 = TelegramTestHelpers.mockVideo(fileId = "video_1")
        val video2 = TelegramTestHelpers.mockVideo(fileId = "video_2")

        val req1 = TelegramTestHelpers.createVideoRequest(video = video1, messageId = 1)
        val req2 = TelegramTestHelpers.createVideoRequest(video = video2, messageId = 2)

        val aggregated = TelegramTestHelpers.createVideoRequest(
            video = video1,
            aggregated = listOf(req1, req2)
        )

        val videos = aggregated.allVideos

        assertEquals(2, videos.size)
        assertEquals("video_1", videos[0].fileId)
        assertEquals("video_2", videos[1].fileId)
    }

    @Test
    fun `test allDocuments collects from aggregated request`() {
        val doc1 = TelegramTestHelpers.mockDocument(fileId = "doc_1", fileName = "file1.pdf")
        val doc2 = TelegramTestHelpers.mockDocument(fileId = "doc_2", fileName = "file2.pdf")

        val req1 = TelegramTestHelpers.createDocumentRequest(document = doc1, messageId = 1)
        val req2 = TelegramTestHelpers.createDocumentRequest(document = doc2, messageId = 2)

        val aggregated = TelegramTestHelpers.createDocumentRequest(
            document = doc1,
            aggregated = listOf(req1, req2)
        )

        val documents = aggregated.allDocuments

        assertEquals(2, documents.size)
        assertEquals("file1.pdf", documents[0].fileName)
        assertEquals("file2.pdf", documents[1].fileName)
    }

    @Test
    fun `test allLocations collects from aggregated request`() {
        val loc1 = TelegramTestHelpers.mockLocation(latitude = 55.7558f, longitude = 37.6173f)
        val loc2 = TelegramTestHelpers.mockLocation(latitude = 40.7128f, longitude = -74.0060f)

        val req1 = TelegramTestHelpers.createLocationRequest(location = loc1, messageId = 1)
        val req2 = TelegramTestHelpers.createLocationRequest(location = loc2, messageId = 2)

        val aggregated = TelegramTestHelpers.createLocationRequest(
            location = loc1,
            aggregated = listOf(req1, req2)
        )

        val locations = aggregated.allLocations

        assertEquals(2, locations.size)
        assertEquals(55.7558f, locations[0].latitude)
        assertEquals(40.7128f, locations[1].latitude)
    }

    @Test
    fun `test allContacts collects from aggregated request`() {
        val contact1 = TelegramTestHelpers.mockContact(firstName = "John", phoneNumber = "+1111")
        val contact2 = TelegramTestHelpers.mockContact(firstName = "Jane", phoneNumber = "+2222")

        val req1 = TelegramTestHelpers.createContactRequest(contact = contact1, messageId = 1)
        val req2 = TelegramTestHelpers.createContactRequest(contact = contact2, messageId = 2)

        val aggregated = TelegramTestHelpers.createContactRequest(
            contact = contact1,
            aggregated = listOf(req1, req2)
        )

        val contacts = aggregated.allContacts

        assertEquals(2, contacts.size)
        assertEquals("John", contacts[0].firstName)
        assertEquals("Jane", contacts[1].firstName)
    }

    @Test
    fun `test empty all* properties return empty lists`() {
        val textRequest = TelegramTestHelpers.createTextRequest(text = "only text")

        // Text request should not have photos, videos, etc.
        assertEquals(0, textRequest.allPhotos.size)
        assertEquals(0, textRequest.allVideos.size)
        assertEquals(0, textRequest.allDocuments.size)
        assertEquals(0, textRequest.allLocations.size)
        assertEquals(0, textRequest.allContacts.size)

        // But should have text
        assertEquals(1, textRequest.allTexts.size)
    }
}
