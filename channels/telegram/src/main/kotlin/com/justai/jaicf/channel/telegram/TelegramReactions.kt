package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.*

val Reactions.telegram
    get() = this as? TelegramReactions

class TelegramReactions(
    val api: Bot,
    val request: TelegramBotRequest
) : Reactions() {

    val chatId = request.chatId

    override fun say(text: String) {
        sendMessage(text)
    }

    fun say(text: String, inlineButtons: List<String>) = api.sendMessage(
        chatId,
        text,
        replyMarkup = InlineKeyboardMarkup(
            listOf(inlineButtons.map { InlineKeyboardButton(it, callbackData = it) })
        ).also {
            SayReaction.register(text)
            ButtonsReaction.register(inlineButtons)
        }
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
    ) = api.sendMessage(
        chatId,
        text,
        parseMode,
        disableWebPagePreview,
        disableNotification,
        replyToMessageId,
        replyMarkup
    ).also {
        SayReaction.register(text)
    }

    override fun image(url: String) {
        sendPhoto(url)
    }

    fun image(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = sendPhoto(url, caption, parseMode, disableNotification, replyToMessageId, replyMarkup)

    fun sendPhoto(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendPhoto(chatId, url, caption, parseMode, disableNotification, replyToMessageId, replyMarkup).also {
        ImageReaction.register(url)
    }

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

    override fun audio(url: String) {
        sendAudio(url)
    }

    fun sendAudio(
        url: String,
        duration: Int? = null,
        performer: String? = null,
        title: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendAudio(chatId, url, duration, performer, title, disableNotification, replyToMessageId, replyMarkup).also {
        AudioReaction.register(url)
    }

    fun sendDocument(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendDocument(chatId, url, caption, parseMode, disableNotification, replyToMessageId, replyMarkup)

    fun sendVenue(
        latitude: Float,
        longitude: Float,
        title: String,
        address: String,
        foursquareId: String? = null,
        foursquareType: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVenue(
        chatId,
        latitude,
        longitude,
        title,
        address,
        foursquareId,
        foursquareType,
        disableNotification,
        replyToMessageId,
        replyMarkup
    )

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
        mediaGroup: MediaGroup,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null
    ) = api.sendMediaGroup(chatId, mediaGroup, disableNotification, replyToMessageId)

    fun sendVideoNote(
        url: String,
        duration: Int? = null,
        length: Int? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVideoNote(chatId, url, duration, length, disableNotification, replyToMessageId, replyMarkup)
}