package com.justai.jaicf.channel.telegram

import com.justai.jaicf.reactions.Reactions
import me.ivmg.telegram.Bot

val Reactions.telegram
    get() = this as? TelegramReactions

class TelegramReactions(
    val api: Bot,
    request: TelegramBotRequest
): Reactions() {

    private val chatId = request.message.chat.id

    override fun say(text: String) {
        api.sendMessage(chatId, text)
    }

    override fun image(url: String) = image(url, null)

    fun image(url: String, caption: String? = null) {
        api.sendPhoto(chatId, url, caption)
    }

}