package com.justai.jaicf.channel.telegram

import com.pengrad.telegrambot.model.Animation
import com.pengrad.telegrambot.model.Audio
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Contact
import com.pengrad.telegrambot.model.Document
import com.pengrad.telegrambot.model.Game
import com.pengrad.telegrambot.model.Location
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.PhotoSize
import com.pengrad.telegrambot.model.Sticker
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.Video
import com.pengrad.telegrambot.model.VideoNote
import com.pengrad.telegrambot.model.Voice
import com.pengrad.telegrambot.model.PreCheckoutQuery
import com.pengrad.telegrambot.model.SuccessfulPayment
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.invocationapi.InvocationEventRequest
import com.justai.jaicf.channel.invocationapi.InvocationQueryRequest
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import kotlin.random.Random

val BotRequest.telegram get() = this as? TelegramBotRequest

val TelegramBotRequest.text get() = this as? TelegramTextRequest
val TelegramBotRequest.callback get() = this as? TelegramQueryRequest
val TelegramBotRequest.location get() = this as? TelegramLocationRequest
val TelegramBotRequest.contact get() = this as? TelegramContactRequest
val TelegramBotRequest.audio get() = this as? TelegramAudioRequest
val TelegramBotRequest.document get() = this as? TelegramDocumentRequest
val TelegramBotRequest.animation get() = this as? TelegramAnimationRequest
val TelegramBotRequest.game get() = this as? TelegramGameRequest
val TelegramBotRequest.photos get() = this as? TelegramPhotosRequest
val TelegramBotRequest.sticker get() = this as? TelegramStickerRequest
val TelegramBotRequest.video get() = this as? TelegramVideoRequest
val TelegramBotRequest.videoNote get() = this as? TelegramVideoNoteRequest
val TelegramBotRequest.voice get() = this as? TelegramVoiceRequest
val TelegramBotRequest.preCheckout get() = this as? TelegramPreCheckoutRequest
val TelegramBotRequest.successfulPayment get() = this as? TelegramSuccessfulPaymentRequest

internal val Message.clientId get() = chat().id().toString()

interface TelegramBotRequest : BotRequest {
    val update: Update
    val message: Message
    val chatId: Long get() = message.chat().id()
}

data class TelegramTextRequest(
    override val update: Update,
    override val message: Message
) : TelegramBotRequest, QueryBotRequest(clientId = message.clientId, input = message.text() ?: "")

data class TelegramQueryRequest(
    override val update: Update,
    override val message: Message,
    val data: String
) : TelegramBotRequest, QueryBotRequest(clientId = message.clientId, input = data)

data class TelegramLocationRequest(
    override val update: Update,
    override val message: Message,
    val location: Location
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.LOCATION)

data class TelegramContactRequest(
    override val update: Update,
    override val message: Message,
    val contact: Contact
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.CONTACT)

data class TelegramAudioRequest(
    override val update: Update,
    override val message: Message,
    val audio: Audio
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.AUDIO)

data class TelegramDocumentRequest(
    override val update: Update,
    override val message: Message,
    val document: Document
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.DOCUMENT)

data class TelegramAnimationRequest(
    override val update: Update,
    override val message: Message,
    val animation: Animation
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.ANIMATION)

data class TelegramGameRequest(
    override val update: Update,
    override val message: Message,
    val game: Game
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.GAME)

data class TelegramPhotosRequest(
    override val update: Update,
    override val message: Message,
    val photos: Array<PhotoSize>
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.PHOTOS) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TelegramPhotosRequest

        if (update != other.update) return false
        if (message != other.message) return false
        if (!photos.contentEquals(other.photos)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = update.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + photos.contentHashCode()
        return result
    }
}

data class TelegramStickerRequest(
    override val update: Update,
    override val message: Message,
    val sticker: Sticker
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.STICKER)

data class TelegramVideoRequest(
    override val update: Update,
    override val message: Message,
    val video: Video
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.VIDEO)

data class TelegramVideoNoteRequest(
    override val update: Update,
    override val message: Message,
    val videoNote: VideoNote
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.VIDEO_NOTE)

data class TelegramVoiceRequest(
    override val update: Update,
    override val message: Message,
    val voice: Voice
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.VOICE)

data class TelegramPreCheckoutRequest(
    override val update: Update,
    val preCheckoutQuery: PreCheckoutQuery
) : TelegramBotRequest, EventBotRequest(clientId = preCheckoutQuery.from().id().toString(), input = TelegramEvent.PRE_CHECKOUT) {
    // For PreCheckoutQuery, there's no associated message in the update
    // We provide a minimal stub implementation to satisfy the interface
    override val message: Message
        get() = throw UnsupportedOperationException("PreCheckoutQuery does not have an associated message")

    override val chatId: Long
        get() = preCheckoutQuery.from().id()
}

data class TelegramSuccessfulPaymentRequest(
    override val update: Update,
    override val message: Message,
    val successfulPayment: SuccessfulPayment
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.SUCCESSFUL_PAYMENT)


interface TelegramInvocationRequest : TelegramBotRequest, InvocationRequest {
    companion object {
        fun create(r: InvocationRequest, update: Update, message: Message): TelegramInvocationRequest? = when (r) {
            is InvocationEventRequest -> TelegramInvocationEventRequest(update, message, r.clientId, r.input, r.requestData)
            is InvocationQueryRequest -> TelegramInvocationQueryRequest(update, message, r.clientId, r.input, r.requestData)
            else -> null
        }
    }
}

data class TelegramInvocationEventRequest(
    override val update: Update,
    override val message: Message,
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : TelegramInvocationRequest, InvocationEventRequest(clientId, input, requestData)

data class TelegramInvocationQueryRequest(
    override val update: Update,
    override val message: Message,
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : TelegramInvocationRequest, InvocationQueryRequest(clientId, input, requestData)
