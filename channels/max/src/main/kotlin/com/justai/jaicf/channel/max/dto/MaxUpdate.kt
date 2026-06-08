package com.justai.jaicf.channel.max.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
    property = "update_type", defaultImpl = UnknownMaxUpdate::class)
@JsonSubTypes(
    JsonSubTypes.Type(value = MessageCreatedUpdate::class, name = "message_created"),
    JsonSubTypes.Type(value = MessageCallbackUpdate::class, name = "message_callback"),
    JsonSubTypes.Type(value = BotAddedUpdate::class, name = "bot_added"),
    JsonSubTypes.Type(value = BotStartedUpdate::class, name = "bot_started"),
    JsonSubTypes.Type(value = BotRemovedUpdate::class, name = "bot_removed")
)
sealed class MaxUpdate {
    abstract val timestamp: Long?
}

data class MessageCreatedUpdate(
    val message: MaxMessage,
    val userLocale: String? = null,
    override val timestamp: Long? = null
) : MaxUpdate()

data class MessageCallbackUpdate(
    val callback: MaxCallback,
    val message: MaxMessage? = null,
    val userLocale: String? = null,
    override val timestamp: Long? = null
) : MaxUpdate()

data class BotAddedUpdate(
    val chatId: Long,
    val user: MaxUser,
    val isChannel: Boolean = false,
    override val timestamp: Long? = null
) : MaxUpdate()

data class BotStartedUpdate(
    val chatId: Long,
    val user: MaxUser,
    val payload: String? = null,
    override val timestamp: Long? = null
) : MaxUpdate()

data class BotRemovedUpdate(
    val chatId: Long,
    val user: MaxUser,
    val isChannel: Boolean = false,
    override val timestamp: Long? = null
) : MaxUpdate()

class UnknownMaxUpdate : MaxUpdate() {
    override val timestamp: Long? = null
}
