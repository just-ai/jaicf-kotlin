package com.justai.jaicf.channel.telegram

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import me.ivmg.telegram.entities.Contact
import me.ivmg.telegram.entities.Location
import me.ivmg.telegram.entities.Message

val BotRequest.telegram
    get() = this as? TelegramBotRequest


interface TelegramBotRequest: BotRequest {
    val message: Message
}

data class TelegramTextRequest(
    override val message: Message
): TelegramBotRequest, QueryBotRequest(
    clientId = message.chat.id.toString(),
    input = message.text!!
)

data class TelegramLocationRequest(
    override val message: Message,
    val location: Location
): TelegramBotRequest, EventBotRequest(
    clientId = message.chat.id.toString(),
    input = TelegramEvent.EVENT_LOCATION
)

data class TelegramContactRequest(
    override val message: Message,
    val contact: Contact
): TelegramBotRequest, EventBotRequest(
    clientId = message.chat.id.toString(),
    input = TelegramEvent.EVENT_CONTACT
)