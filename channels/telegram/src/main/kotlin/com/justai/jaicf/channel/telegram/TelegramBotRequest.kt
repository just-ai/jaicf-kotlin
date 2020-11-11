package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.Contact
import com.github.kotlintelegrambot.entities.Location
import com.github.kotlintelegrambot.entities.Message
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest

val BotRequest.telegram
    get() = this as? TelegramBotRequest

val TelegramBotRequest.location
    get() = this as? TelegramLocationRequest

val TelegramBotRequest.contact
    get() = this as? TelegramContactRequest

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

internal val Message.clientId
    get() = from?.id?.toString() ?: chat.id.toString()