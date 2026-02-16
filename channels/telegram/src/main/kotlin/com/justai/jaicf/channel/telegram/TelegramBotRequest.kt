package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Contact
import com.github.kotlintelegrambot.entities.Game
import com.github.kotlintelegrambot.entities.Location
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.files.Animation
import com.github.kotlintelegrambot.entities.files.Audio
import com.github.kotlintelegrambot.entities.files.Document
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.entities.files.Video
import com.github.kotlintelegrambot.entities.files.VideoNote
import com.github.kotlintelegrambot.entities.files.Voice
import com.github.kotlintelegrambot.entities.payments.PreCheckoutQuery
import com.github.kotlintelegrambot.entities.payments.SuccessfulPayment
import com.github.kotlintelegrambot.entities.stickers.Sticker
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.invocationapi.InvocationEventRequest
import com.justai.jaicf.channel.invocationapi.InvocationQueryRequest
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import kotlin.random.Random

val BotRequest.telegram get() = this as? TelegramBotRequest

val TelegramBotRequest.allItems: List<TelegramBotRequest> get() =
    aggregated.ifEmpty { listOf(this) }

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

val TelegramBotRequest.allPhotos: List<PhotoSize> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allPhotos }
    } else {
        (this as? TelegramPhotosRequest)?.photos ?: emptyList()
    }
}
val TelegramBotRequest.allTexts: List<String> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allTexts }
    } else {
        (this as? TelegramTextRequest)?.input?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allVideos: List<Video> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allVideos }
    } else {
        (this as? TelegramVideoRequest)?.video?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allDocuments: List<Document> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allDocuments }
    } else {
        (this as? TelegramDocumentRequest)?.document?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allAudios: List<Audio> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allAudios }
    } else {
        (this as? TelegramAudioRequest)?.audio?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allVoices: List<Voice> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allVoices }
    } else {
        (this as? TelegramVoiceRequest)?.voice?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allVideoNotes: List<VideoNote> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allVideoNotes }
    } else {
        (this as? TelegramVideoNoteRequest)?.videoNote?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allStickers: List<Sticker> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allStickers }
    } else {
        (this as? TelegramStickerRequest)?.sticker?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allAnimations: List<Animation> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allAnimations }
    } else {
        (this as? TelegramAnimationRequest)?.animation?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allLocations: List<Location> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allLocations }
    } else {
        (this as? TelegramLocationRequest)?.location?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allContacts: List<Contact> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allContacts }
    } else {
        (this as? TelegramContactRequest)?.contact?.let { listOf(it) } ?: emptyList()
    }
}
val TelegramBotRequest.allGames: List<Game> get() {
    return if (aggregated.isNotEmpty()) {
        aggregated.flatMap { it.allGames }
    } else {
        (this as? TelegramGameRequest)?.game?.let { listOf(it) } ?: emptyList()
    }
}

inline fun <reified T : TelegramBotRequest> TelegramBotRequest.itemsOfType(): List<T> =
    allItems.filterIsInstance<T>()

internal val Message.clientId get() = chat.id.toString()

interface TelegramBotRequest : BotRequest {
    val update: Update
    val message: Message
    val aggregated: List<TelegramBotRequest>
    val chatId: Long get() = message.chat.id
    val isAggregated: Boolean get() = aggregated.isNotEmpty()
}

data class TelegramTextRequest(
    override val update: Update,
    override val message: Message,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, QueryBotRequest(clientId = message.clientId, input = message.text.toString())

data class TelegramQueryRequest(
    override val update: Update,
    override val message: Message,
    val data: String,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, QueryBotRequest(clientId = message.clientId, input = data)

data class TelegramLocationRequest(
    override val update: Update,
    override val message: Message,
    val location: Location,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.LOCATION)

data class TelegramContactRequest(
    override val update: Update,
    override val message: Message,
    val contact: Contact,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.CONTACT)

data class TelegramAudioRequest(
    override val update: Update,
    override val message: Message,
    val audio: Audio,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.AUDIO)

data class TelegramDocumentRequest(
    override val update: Update,
    override val message: Message,
    val document: Document,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.DOCUMENT)

data class TelegramAnimationRequest(
    override val update: Update,
    override val message: Message,
    val animation: Animation,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.ANIMATION)

data class TelegramGameRequest(
    override val update: Update,
    override val message: Message,
    val game: Game,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.GAME)

data class TelegramPhotosRequest(
    override val update: Update,
    override val message: Message,
    val photos: List<PhotoSize>,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.PHOTOS)

data class TelegramStickerRequest(
    override val update: Update,
    override val message: Message,
    val sticker: Sticker,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.STICKER)

data class TelegramVideoRequest(
    override val update: Update,
    override val message: Message,
    val video: Video,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.VIDEO)

data class TelegramVideoNoteRequest(
    override val update: Update,
    override val message: Message,
    val videoNote: VideoNote,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.VIDEO_NOTE)

data class TelegramVoiceRequest(
    override val update: Update,
    override val message: Message,
    val voice: Voice,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = message.clientId, input = TelegramEvent.VOICE)

data class TelegramPreCheckoutRequest(
    override val update: Update,
    val preCheckoutQuery: PreCheckoutQuery,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramBotRequest, EventBotRequest(clientId = preCheckoutQuery.from.id.toString(), input = TelegramEvent.PRE_CHECKOUT) {
    override val message = Message(
        messageId = Random.nextLong(),
        from = preCheckoutQuery.from,
        date = System.currentTimeMillis(),
        chat = Chat(preCheckoutQuery.from.id, "private")
    )
}

data class TelegramSuccessfulPaymentRequest(
    override val update: Update,
    override val message: Message,
    val successfulPayment: SuccessfulPayment,
    override val aggregated: List<TelegramBotRequest> = emptyList()
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
    override val requestData: String,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramInvocationRequest, InvocationEventRequest(clientId, input, requestData)

data class TelegramInvocationQueryRequest(
    override val update: Update,
    override val message: Message,
    override val clientId: String,
    override val input: String,
    override val requestData: String,
    override val aggregated: List<TelegramBotRequest> = emptyList()
) : TelegramInvocationRequest, InvocationQueryRequest(clientId, input, requestData)
