package com.justai.jaicf.channel.telegram

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import me.ivmg.telegram.entities.Contact
import me.ivmg.telegram.entities.Location
import me.ivmg.telegram.entities.Message
import me.ivmg.telegram.entities.payment.PreCheckoutQuery

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
    clientId = message.chat.id.toString(),
    input = message.text!!
)

data class TelegramQueryRequest(
    override val message: Message,
    val data: String
): TelegramBotRequest, QueryBotRequest(
    clientId = message.chat.id.toString(),
    input = message.text!!
)

data class TelegramLocationRequest(
    override val message: Message,
    val location: Location
): TelegramBotRequest, EventBotRequest(
    clientId = message.chat.id.toString(),
    input = TelegramEvent.LOCATION
)

data class TelegramContactRequest(
    override val message: Message,
    val contact: Contact
): TelegramBotRequest, EventBotRequest(
    clientId = message.chat.id.toString(),
    input = TelegramEvent.CONTACT
)