package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.files.*
import com.github.kotlintelegrambot.entities.payments.PreCheckoutQuery
import com.github.kotlintelegrambot.entities.stickers.Sticker
import io.mockk.every
import io.mockk.mockk

/**
 * Helper functions for creating test fixtures for Telegram channel tests.
 */
object TelegramTestHelpers {

    /**
     * Creates a mock Update with minimal required fields.
     */
    fun mockUpdate(updateId: Long = 123456L): Update = mockk {
        every { this@mockk.updateId } returns updateId
    }

    /**
     * Creates a mock Message with specified parameters.
     */
    fun mockMessage(
        chatId: Long = 789L,
        messageId: Long = 1L,
        text: String? = null,
        mediaGroupId: String? = null,
        caption: String? = null
    ): Message = mockk {
        every { this@mockk.messageId } returns messageId
        every { this@mockk.text } returns text
        every { this@mockk.mediaGroupId } returns mediaGroupId
        every { this@mockk.caption } returns caption
        every { date } returns System.currentTimeMillis()
        every { chat } returns Chat(chatId, "private")
        every { from } returns mockUser()
    }

    /**
     * Creates a mock User.
     */
    fun mockUser(userId: Long = 999L, username: String? = "testuser"): User = mockk {
        every { id } returns userId
        every { isBot } returns false
        every { firstName } returns "Test"
        every { this@mockk.username } returns username
    }

    /**
     * Creates a mock PhotoSize.
     */
    fun mockPhotoSize(
        fileId: String = "photo_123",
        width: Int = 800,
        height: Int = 600,
        fileSize: Int? = 1024
    ): PhotoSize = mockk {
        every { this@mockk.fileId } returns fileId
        every { this@mockk.width } returns width
        every { this@mockk.height } returns height
        every { this@mockk.fileSize } returns fileSize
    }

    /**
     * Creates a mock Video.
     */
    fun mockVideo(
        fileId: String = "video_123",
        width: Int = 1920,
        height: Int = 1080,
        duration: Int = 60
    ): Video = mockk {
        every { this@mockk.fileId } returns fileId
        every { this@mockk.width } returns width
        every { this@mockk.height } returns height
        every { this@mockk.duration } returns duration
    }

    /**
     * Creates a mock Document.
     */
    fun mockDocument(
        fileId: String = "doc_123",
        fileName: String? = "test.pdf"
    ): Document = mockk {
        every { this@mockk.fileId } returns fileId
        every { this@mockk.fileName } returns fileName
    }

    /**
     * Creates a mock Audio.
     */
    fun mockAudio(fileId: String = "audio_123", duration: Int = 180): Audio = mockk {
        every { this@mockk.fileId } returns fileId
        every { this@mockk.duration } returns duration
    }

    /**
     * Creates a mock Voice.
     */
    fun mockVoice(fileId: String = "voice_123", duration: Int = 5): Voice = mockk {
        every { this@mockk.fileId } returns fileId
        every { this@mockk.duration } returns duration
    }

    /**
     * Creates a mock VideoNote.
     */
    fun mockVideoNote(fileId: String = "videonote_123", duration: Int = 10): VideoNote = mockk {
        every { this@mockk.fileId } returns fileId
        every { this@mockk.duration } returns duration
        every { length } returns 240
    }

    /**
     * Creates a mock Sticker.
     */
    fun mockSticker(fileId: String = "sticker_123", emoji: String? = "😀"): Sticker = mockk {
        every { this@mockk.fileId } returns fileId
        every { this@mockk.emoji } returns emoji
        every { width } returns 512
        every { height } returns 512
    }

    /**
     * Creates a mock Animation.
     */
    fun mockAnimation(fileId: String = "animation_123"): Animation = mockk {
        every { this@mockk.fileId } returns fileId
        every { width } returns 320
        every { height } returns 240
        every { duration } returns 3
    }

    /**
     * Creates a mock Location.
     */
    fun mockLocation(latitude: Float = 55.7558f, longitude: Float = 37.6173f): Location = mockk {
        every { this@mockk.latitude } returns latitude
        every { this@mockk.longitude } returns longitude
    }

    /**
     * Creates a mock Contact.
     */
    fun mockContact(firstName: String = "John", phoneNumber: String = "+1234567890"): Contact = mockk {
        every { this@mockk.firstName } returns firstName
        every { this@mockk.phoneNumber } returns phoneNumber
    }

    /**
     * Creates a mock Game.
     */
    fun mockGame(title: String = "Test Game"): Game = mockk {
        every { this@mockk.title } returns title
        every { description } returns "A test game"
    }

    // Factory functions for creating TelegramBotRequest instances

    /**
     * Creates a TelegramTextRequest with optional aggregated requests.
     */
    fun createTextRequest(
        chatId: Long = 789L,
        text: String = "test message",
        updateId: Long = 123456L,
        messageId: Long = 1L,
        aggregated: List<TelegramBotRequest> = emptyList()
    ): TelegramTextRequest {
        val update = mockUpdate(updateId)
        val message = mockMessage(chatId = chatId, messageId = messageId, text = text)
        return TelegramTextRequest(update, message, aggregated)
    }

    /**
     * Creates a TelegramPhotosRequest with optional aggregated requests.
     */
    fun createPhotosRequest(
        chatId: Long = 789L,
        photos: List<PhotoSize> = listOf(mockPhotoSize()),
        mediaGroupId: String? = null,
        caption: String? = null,
        updateId: Long = 123456L,
        messageId: Long = 1L,
        aggregated: List<TelegramBotRequest> = emptyList()
    ): TelegramPhotosRequest {
        val update = mockUpdate(updateId)
        val message = mockMessage(
            chatId = chatId,
            messageId = messageId,
            mediaGroupId = mediaGroupId,
            caption = caption
        )
        every { message.photo } returns photos
        return TelegramPhotosRequest(update, message, photos, aggregated)
    }

    /**
     * Creates a TelegramVideoRequest with optional aggregated requests.
     */
    fun createVideoRequest(
        chatId: Long = 789L,
        video: Video = mockVideo(),
        caption: String? = null,
        updateId: Long = 123456L,
        messageId: Long = 1L,
        aggregated: List<TelegramBotRequest> = emptyList()
    ): TelegramVideoRequest {
        val update = mockUpdate(updateId)
        val message = mockMessage(chatId = chatId, messageId = messageId, caption = caption)
        every { message.video } returns video
        return TelegramVideoRequest(update, message, video, aggregated)
    }

    /**
     * Creates a TelegramDocumentRequest with optional aggregated requests.
     */
    fun createDocumentRequest(
        chatId: Long = 789L,
        document: Document = mockDocument(),
        caption: String? = null,
        updateId: Long = 123456L,
        messageId: Long = 1L,
        aggregated: List<TelegramBotRequest> = emptyList()
    ): TelegramDocumentRequest {
        val update = mockUpdate(updateId)
        val message = mockMessage(chatId = chatId, messageId = messageId, caption = caption)
        every { message.document } returns document
        return TelegramDocumentRequest(update, message, document, aggregated)
    }

    /**
     * Creates a TelegramLocationRequest with optional aggregated requests.
     */
    fun createLocationRequest(
        chatId: Long = 789L,
        location: Location = mockLocation(),
        updateId: Long = 123456L,
        messageId: Long = 1L,
        aggregated: List<TelegramBotRequest> = emptyList()
    ): TelegramLocationRequest {
        val update = mockUpdate(updateId)
        val message = mockMessage(chatId = chatId, messageId = messageId)
        every { message.location } returns location
        return TelegramLocationRequest(update, message, location, aggregated)
    }

    /**
     * Creates a TelegramContactRequest with optional aggregated requests.
     */
    fun createContactRequest(
        chatId: Long = 789L,
        contact: Contact = mockContact(),
        updateId: Long = 123456L,
        messageId: Long = 1L,
        aggregated: List<TelegramBotRequest> = emptyList()
    ): TelegramContactRequest {
        val update = mockUpdate(updateId)
        val message = mockMessage(chatId = chatId, messageId = messageId)
        every { message.contact } returns contact
        return TelegramContactRequest(update, message, contact, aggregated)
    }
}
