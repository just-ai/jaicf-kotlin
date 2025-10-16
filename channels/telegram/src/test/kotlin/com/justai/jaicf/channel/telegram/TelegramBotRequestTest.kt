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
}
