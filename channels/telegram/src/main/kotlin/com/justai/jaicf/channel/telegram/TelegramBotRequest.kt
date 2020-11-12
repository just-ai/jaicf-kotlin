package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.stickers.Sticker
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest

val BotRequest.telegram
    get() = this as? TelegramBotRequest

val TelegramBotRequest.location
    get() = this as? TelegramLocationRequest

val TelegramBotRequest.contact
    get() = this as? TelegramContactRequest

internal val Message.clientId
    get() = from?.id?.toString() ?: chat.id.toString()

interface TelegramBotRequest: BotRequest {
    val message: Message

    val chatId: Long
        get() = message.chat.id
}

data class TelegramTextRequest(
    override val message: Message
): TelegramBotRequest, QueryBotRequest(
    clientId = message.clientId,
    input = message.text.toString()
)

data class TelegramQueryRequest(
    override val message: Message,
    val data: String
): TelegramBotRequest, QueryBotRequest(
    clientId = message.clientId,
    input = data
)

data class TelegramLocationRequest(
    override val message: Message,
    val location: Location
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.LOCATION
)

data class TelegramContactRequest(
    override val message: Message,
    val contact: Contact
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.CONTACT
)

data class TelegramAudioRequest(
    override val message: Message,
    val audio: Audio
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.AUDIO
)

data class TelegramDocumentRequest(
    override val message: Message,
    val document: Document
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.DOCUMENT
)

data class TelegramAnimationRequest(
    override val message: Message,
    val animation: Animation
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.ANIMATION
)

data class TelegramGameRequest(
    override val message: Message,
    val game: Game
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.GAME
)

data class TelegramPhotosRequest(
    override val message: Message,
    val photos: List<PhotoSize>
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.PHOTOS
)

data class TelegramStickerRequest(
    override val message: Message,
    val sticker: Sticker
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.STICKER
)

data class TelegramVideoRequest(
    override val message: Message,
    val video: Video
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.VIDEO
)

data class TelegramVideoNoteRequest(
    override val message: Message,
    val videoNote: VideoNote
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.VIDEO_NOTE
)

data class TelegramVoiceRequest(
    override val message: Message,
    val voice: Voice
): TelegramBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = TelegramEvent.VOICE
)