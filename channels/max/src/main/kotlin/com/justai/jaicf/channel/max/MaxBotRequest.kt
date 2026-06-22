package com.justai.jaicf.channel.max

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.max.dto.BotAddedUpdate
import com.justai.jaicf.channel.max.dto.BotRemovedUpdate
import com.justai.jaicf.channel.max.dto.BotStartedUpdate
import com.justai.jaicf.channel.max.dto.MaxCallback
import com.justai.jaicf.channel.max.dto.MaxMessage

interface MaxBotRequest : BotRequest {
    /** Chat id replies are sent to. */
    val chatId: Long
}

val BotRequest.max get() = this as? MaxBotRequest

val MaxBotRequest.text get() = this as? MaxTextRequest
val MaxBotRequest.contact get() = this as? MaxContactRequest
val MaxBotRequest.audio get() = this as? MaxAudioRequest
val MaxBotRequest.callback get() = this as? MaxCallbackRequest
val MaxBotRequest.botAdded get() = this as? MaxBotAddedRequest
val MaxBotRequest.botStarted get() = this as? MaxBotStartedRequest
val MaxBotRequest.botRemoved get() = this as? MaxBotRemovedRequest

data class MaxTextRequest(
    val message: MaxMessage
) : MaxBotRequest, QueryBotRequest(clientId = message.clientId(), input = message.body.text.orEmpty()) {
    override val chatId = message.recipient.chatId
}

data class MaxContactRequest(
    val message: MaxMessage
) : MaxBotRequest, EventBotRequest(clientId = message.clientId(), input = MaxEvent.CONTACT) {
    override val chatId = message.recipient.chatId
}

data class MaxAudioRequest(
    val message: MaxMessage
) : MaxBotRequest, EventBotRequest(clientId = message.clientId(), input = MaxEvent.AUDIO) {
    override val chatId = message.recipient.chatId
}

data class MaxCallbackRequest(
    val callbackData: MaxCallback,
    val message: MaxMessage
) : MaxBotRequest, QueryBotRequest(clientId = callbackData.user.userId.toString(), input = callbackData.payload.orEmpty()) {
    override val chatId = message.recipient.chatId
}

data class MaxBotAddedRequest(
    val update: BotAddedUpdate
) : MaxBotRequest, EventBotRequest(clientId = update.user.userId.toString(), input = MaxEvent.BOT_ADDED) {
    override val chatId = update.chatId
}

data class MaxBotStartedRequest(
    val update: BotStartedUpdate
) : MaxBotRequest, EventBotRequest(clientId = update.user.userId.toString(), input = MaxEvent.BOT_STARTED) {
    override val chatId = update.chatId
}

data class MaxBotRemovedRequest(
    val update: BotRemovedUpdate
) : MaxBotRequest, EventBotRequest(clientId = update.user.userId.toString(), input = MaxEvent.BOT_REMOVED) {
    override val chatId = update.chatId
}

private fun MaxMessage.clientId() = (sender?.userId ?: recipient.userId
    ?: error("Max message has neither sender nor recipient user id")).toString()
