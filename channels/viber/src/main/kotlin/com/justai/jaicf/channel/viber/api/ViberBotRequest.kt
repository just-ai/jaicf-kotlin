package com.justai.jaicf.channel.viber.api

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.invocationapi.InvocationEventRequest
import com.justai.jaicf.channel.invocationapi.InvocationQueryRequest
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.viber.sdk.event.IncomingConversationStartedEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingDeliveredEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingFailedEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingMessageEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingSeenEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingSubscribedEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingUnsubscribedEvent
import com.justai.jaicf.channel.viber.sdk.message.ContactMessage
import com.justai.jaicf.channel.viber.sdk.message.FileMessage
import com.justai.jaicf.channel.viber.sdk.message.KeyboardMessage
import com.justai.jaicf.channel.viber.sdk.message.LocationMessage
import com.justai.jaicf.channel.viber.sdk.message.Message
import com.justai.jaicf.channel.viber.sdk.message.PictureMessage
import com.justai.jaicf.channel.viber.sdk.message.RichMediaMessage
import com.justai.jaicf.channel.viber.sdk.message.StickerMessage
import com.justai.jaicf.channel.viber.sdk.message.TextMessage
import com.justai.jaicf.channel.viber.sdk.message.UrlMessage
import com.justai.jaicf.channel.viber.sdk.message.VideoMessage

val BotRequest.viber get() = this as? ViberBotRequest

val ViberBotRequest.seen get() = this as? ViberSeenRequest
val ViberBotRequest.delivered get() = this as? ViberDeliveredRequest
val ViberBotRequest.subscribed get() = this as? ViberSubscribedRequest
val ViberBotRequest.unsubscribed get() = this as? ViberUnsubscribedRequest
val ViberBotRequest.conversationStarted get() = this as? ViberConversationStartedRequest
val ViberBotRequest.failed get() = this as? ViberFailedRequest
val ViberBotRequest.message get() = this as? ViberMessageRequest
val ViberBotRequest.richMedia get() = this as? ViberRichMediaMessageRequest
val ViberBotRequest.text get() = this as? ViberTextMessageRequest
val ViberBotRequest.contact get() = this as? ViberContactMessageRequest
val ViberBotRequest.file get() = this as? ViberFileMessageRequest
val ViberBotRequest.location get() = this as? ViberLocationMessageRequest
val ViberBotRequest.image get() = this as? ViberImageMessageRequest
val ViberBotRequest.sticker get() = this as? ViberStickerMessageRequest
val ViberBotRequest.url get() = this as? ViberUrlMessageRequest
val ViberBotRequest.video get() = this as? ViberVideoMessageRequest

interface ViberBotRequest : BotRequest {
    val event: IncomingEvent
}

data class ViberSeenRequest(
    override val event: IncomingSeenEvent
) : ViberBotRequest, EventBotRequest(event.userId, ViberEvent.SEEN)

data class ViberDeliveredRequest(
    override val event: IncomingDeliveredEvent
) : ViberBotRequest, EventBotRequest(event.userId, ViberEvent.DELIVERED)

data class ViberSubscribedRequest(
    override val event: IncomingSubscribedEvent
) : ViberBotRequest, EventBotRequest(event.user.id, ViberEvent.SUBSCRIBED)

data class ViberUnsubscribedRequest(
    override val event: IncomingUnsubscribedEvent
) : ViberBotRequest, EventBotRequest(event.userId, ViberEvent.UNSUBSCRIBED)

data class ViberConversationStartedRequest(
    override val event: IncomingConversationStartedEvent
) : ViberBotRequest, EventBotRequest(event.user.id, ViberEvent.CONVERSATION_STARTED)

data class ViberFailedRequest(
    override val event: IncomingFailedEvent
) : ViberBotRequest, EventBotRequest(event.userId, ViberEvent.FAILED)

interface ViberMessageRequest : ViberBotRequest {
    val message: Message
}

data class ViberRichMediaMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: RichMediaMessage = event.message as RichMediaMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.RICH_MEDIA_MESSAGE)

data class ViberTextMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: TextMessage = event.message as TextMessage
) : ViberMessageRequest, QueryBotRequest(event.sender.id, requireNotNull(message.text))

data class ViberContactMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: ContactMessage = event.message as ContactMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.CONTACT_MESSAGE)

data class ViberFileMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: FileMessage = event.message as FileMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.FILE_MESSAGE)

data class ViberLocationMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: LocationMessage = event.message as LocationMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.LOCATION_MESSAGE)

data class ViberImageMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: PictureMessage = event.message as PictureMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.IMAGE_MESSAGE)

data class ViberStickerMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: StickerMessage = event.message as StickerMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.STICKER_MESSAGE)

data class ViberUrlMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: UrlMessage = event.message as UrlMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.URL_MESSAGE)

data class ViberVideoMessageRequest(
    override val event: IncomingMessageEvent,
    override val message: VideoMessage = event.message as VideoMessage
) : ViberMessageRequest, EventBotRequest(event.sender.id, ViberEvent.VIDEO_MESSAGE)

interface ViberInvocationRequest : ViberBotRequest, InvocationRequest {
    companion object {
        fun create(r: InvocationRequest, event: IncomingEvent): ViberInvocationRequest? = when (r) {
            is InvocationEventRequest -> ViberInvocationEventRequest(event, r.clientId, r.input, r.requestData)
            is InvocationQueryRequest -> ViberInvocationQueryRequest(event, r.clientId, r.input, r.requestData)
            else -> null
        }
    }
}

data class ViberInvocationEventRequest(
    override val event: IncomingEvent,
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : ViberInvocationRequest, InvocationEventRequest(clientId, input, requestData)

data class ViberInvocationQueryRequest(
    override val event: IncomingEvent,
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : ViberInvocationRequest, InvocationQueryRequest(clientId, input, requestData)


internal fun IncomingEvent.toBotRequest() = when (this) {
    is IncomingSeenEvent -> ViberSeenRequest(this)
    is IncomingDeliveredEvent -> ViberDeliveredRequest(this)
    is IncomingSubscribedEvent -> ViberSubscribedRequest(this)
    is IncomingConversationStartedEvent -> ViberConversationStartedRequest(this)
    is IncomingFailedEvent -> ViberFailedRequest(this)
    is IncomingUnsubscribedEvent -> ViberUnsubscribedRequest(this)
    is IncomingMessageEvent -> this.toBotRequest()
    else -> error("Unsupported incoming event $this")
}

internal fun IncomingMessageEvent.toBotRequest(): ViberBotRequest = when (this.message) {
    is RichMediaMessage -> ViberRichMediaMessageRequest(this)
    is TextMessage -> ViberTextMessageRequest(this)
    is ContactMessage -> ViberContactMessageRequest(this)
    is FileMessage -> ViberFileMessageRequest(this)
    is LocationMessage -> ViberLocationMessageRequest(this)
    is PictureMessage -> ViberImageMessageRequest(this)
    is StickerMessage -> ViberStickerMessageRequest(this)
    is UrlMessage -> ViberUrlMessageRequest(this)
    is VideoMessage -> ViberVideoMessageRequest(this)
    is KeyboardMessage -> error("Unsupported type")
}

val ViberTextMessageRequest.isReceivedAsPublic: Boolean
    get() = !isReceivedAsSilent

val ViberTextMessageRequest.isReceivedAsSilent: Boolean
    get() = event.receivedAsSilent ?: false
