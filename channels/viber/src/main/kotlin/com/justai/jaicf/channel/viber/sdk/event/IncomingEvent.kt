package com.justai.jaicf.channel.viber.sdk.event

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.justai.jaicf.channel.viber.sdk.message.Message
import com.justai.jaicf.channel.viber.sdk.profile.UserProfile

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "event")
@JsonSubTypes(
    JsonSubTypes.Type(value = IncomingMessageEvent::class, name = "message"),
    JsonSubTypes.Type(value = IncomingWebhookEvent::class, name = "webhook"),
    JsonSubTypes.Type(value = IncomingSeenEvent::class, name = "seen"),
    JsonSubTypes.Type(value = IncomingDeliveredEvent::class, name = "delivered"),
    JsonSubTypes.Type(value = IncomingSubscribedEvent::class, name = "subscribed"),
    JsonSubTypes.Type(value = IncomingUnsubscribedEvent::class, name = "unsubscribed"),
    JsonSubTypes.Type(value = IncomingConversationStartedEvent::class, name = "conversation_started"),
    JsonSubTypes.Type(value = IncomingFailedEvent::class, name = "failed")
)
sealed class IncomingEvent {
    abstract val timestamp: Long?
    abstract val chatHostname: String?
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#conversation-started
 */
data class IncomingConversationStartedEvent(
    val user: UserProfile,
    val type: String,
    val messageToken: Long? = null,
    val subscribed: Boolean? = null,
    val context: String? = null,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null,
    val sender: UserProfile = user
) : IncomingEvent()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#message-receipts-callbacks
 */
data class IncomingDeliveredEvent(
    val userId: String,
    val messageToken: Long? = null,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null,
    val sender: UserProfile? = null
) : IncomingEvent()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#failed-callback
 */
data class IncomingFailedEvent(
    val userId: String,
    @JsonProperty("desc")
    val description: String,
    val messageToken: Long? = null,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null,
    val sender: UserProfile? = null
) : IncomingEvent()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#receive-message-from-user
 */
data class IncomingMessageEvent(
    val message: Message,
    val sender: UserProfile,
    val messageToken: Long? = null,
    @JsonProperty("silent")
    val receivedAsSilent: Boolean? = null,
    val replyType: String? = null,
    val chatId: String? = null,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null
) : IncomingEvent()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#message-receipts-callbacks
 */
data class IncomingSeenEvent(
    val userId: String,
    val messageToken: Long? = null,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null,
    val sender: UserProfile? = null
) : IncomingEvent()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#subscribed
 */
data class IncomingSubscribedEvent(
    val user: UserProfile,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null,
    val messageToken: Long? = null,
    val sender: UserProfile = user
) : IncomingEvent()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#unsubscribed
 */
data class IncomingUnsubscribedEvent(
    val userId: String,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null,
    val messageToken: Long? = null,
    val sender: UserProfile? = null
) : IncomingEvent()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#set-webhook-response
 */
data class IncomingWebhookEvent(
    val messageToken: Long? = null,
    override val timestamp: Long? = null,
    override val chatHostname: String? = null
) : IncomingEvent()

val IncomingEvent.senderId: String
    get() = when (this) {
        is IncomingSeenEvent -> userId
        is IncomingUnsubscribedEvent -> userId
        is IncomingConversationStartedEvent -> user.id
        is IncomingDeliveredEvent -> userId
        is IncomingFailedEvent -> userId
        is IncomingMessageEvent -> sender.id
        is IncomingSubscribedEvent -> user.id
        is IncomingWebhookEvent -> error("No sender for webhook event")
    }

val IncomingEvent.sender: UserProfile?
    get() = when (this) {
        is IncomingConversationStartedEvent -> user
        is IncomingMessageEvent -> sender
        is IncomingSubscribedEvent -> user
        is IncomingDeliveredEvent -> sender
        is IncomingFailedEvent -> sender
        is IncomingSeenEvent -> sender
        is IncomingUnsubscribedEvent -> sender
        is IncomingWebhookEvent -> error("No sender for webhook event")
    }
