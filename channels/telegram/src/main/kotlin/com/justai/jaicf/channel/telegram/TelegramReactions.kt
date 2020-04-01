package com.justai.jaicf.channel.telegram

import com.justai.jaicf.reactions.Reactions
import me.ivmg.telegram.Bot
import me.ivmg.telegram.entities.*

val Reactions.telegram
    get() = this as? TelegramReactions

class TelegramReactions(
    val api: Bot,
    val request: TelegramBotRequest
): Reactions() {

    val chatId = request.chatId

    override fun say(text: String) {
        api.sendMessage(chatId, text)
    }

    fun say(text: String, inlineButtons: List<String>) = api.sendMessage(
        chatId,
        text,
        replyMarkup = InlineKeyboardMarkup(
            listOf(inlineButtons.map { InlineKeyboardButton(it, callbackData = it) })
        )
    )

    fun say(
        text: String,
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = sendMessage(text, parseMode, disableWebPagePreview, disableNotification, replyToMessageId, replyMarkup)

    fun sendMessage(
        text: String,
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendMessage(chatId, text, parseMode, disableWebPagePreview, disableNotification, replyToMessageId, replyMarkup)

    override fun image(url: String) {
        api.sendPhoto(chatId, url)
    }

    fun image(
        url: String,
        caption: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null
    ) = sendPhoto(url, caption, disableNotification, replyToMessageId)

    fun sendPhoto(
        url: String,
        caption: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null
    ) = api.sendPhoto(chatId, url, caption, disableNotification, replyToMessageId)

    fun sendVideo(
        url: String,
        duration: Int? = null,
        width: Int? = null,
        height: Int? = null,
        caption: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVideo(chatId, url, duration, width, height, caption, disableNotification, replyToMessageId, replyMarkup)

    fun sendVoice(
        url: String,
        duration: Int? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVoice(chatId, url, duration, disableNotification, replyToMessageId, replyMarkup)

    fun sendAudio(
        url: String,
        duration: Int? = null,
        performer: String? = null,
        title: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendAudio(chatId, url, duration, performer, title, disableNotification, replyToMessageId, replyMarkup)

    fun sendDocument(
        url: String,
        caption: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendDocument(chatId, url, caption, disableNotification, replyToMessageId, replyMarkup)

    fun sendVenue(
        latitude: Float,
        longitude: Float,
        title: String,
        address: String,
        foursquareId: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVenue(chatId, latitude, longitude, title, address, foursquareId, disableNotification, replyToMessageId, replyMarkup)

    fun sendContact(
        phoneNumber: String,
        firstName: String,
        lastName: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendContact(chatId, phoneNumber, firstName, lastName, disableNotification, replyToMessageId, replyMarkup)

    fun sendLocation(
        latitude: Float,
        longitude: Float,
        livePeriod: Int? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendLocation(chatId, latitude, longitude, livePeriod, disableNotification, replyToMessageId, replyMarkup)

    fun sendMediaGroup(
        media: List<InputMedia>,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null
    ) = api.sendMediaGroup(chatId, media, disableNotification, replyToMessageId)

    fun sendVideoNote(
        url: String,
        duration: Int? = null,
        length: Int? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVideoNote(chatId, url, duration, length, disableNotification, replyToMessageId, replyMarkup)
}