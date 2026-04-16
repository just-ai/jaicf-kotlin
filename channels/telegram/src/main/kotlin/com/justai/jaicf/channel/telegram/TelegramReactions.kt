package com.justai.jaicf.channel.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.*
import com.pengrad.telegrambot.request.*
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.telegram.streaming.StreamConfig
import com.justai.jaicf.channel.telegram.streaming.TelegramStreamingHandler
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.logging.*
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.StreamReactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import java.util.stream.Stream

val Reactions.telegram get() = this as? TelegramReactions

@Suppress("MemberVisibilityCanBePrivate")
class TelegramReactions(
    val api: TelegramBot,
    val request: TelegramBotRequest,
    override val liveChatProvider: JaicpLiveChatProvider?,
    private val streamConfig: StreamConfig = StreamConfig()
) : Reactions(), StreamReactions, JaicpCompatibleAsyncReactions, WithLogger {

    val chatId = request.chatId
    private val messages = mutableListOf<Message>()

    override fun say(text: String): SayReaction {
        val response = api.execute(SendMessage(chatId, text))
        response.message()?.let { messages.add(it) }
        return SayReaction.create(text)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        messages.lastOrNull()?.let { msg ->
            val rows = mutableListOf<Array<InlineKeyboardButton>>()
            buttons.forEach { buttonText ->
                rows.add(arrayOf(InlineKeyboardButton(buttonText).callbackData(buttonText)))
            }
            api.execute(
                EditMessageReplyMarkup(chatId, msg.messageId())
                    .replyMarkup(InlineKeyboardMarkup(*rows.toTypedArray()))
            )
        }
        return ButtonsReaction.create(buttons.asList())
    }

    override fun image(url: String): ImageReaction {
        api.execute(SendPhoto(chatId, url))
        return ImageReaction.create(url)
    }

    override fun audio(url: String): AudioReaction {
        api.execute(SendAudio(chatId, url))
        return AudioReaction.create(url)
    }

    fun sendMessage(text: String, parseMode: ParseMode? = null): SayReaction {
        var req = SendMessage(chatId, text)
        parseMode?.let { req = req.parseMode(it) }
        api.execute(req)
        return SayReaction.create(text)
    }

    fun sendPhoto(url: String, caption: String? = null): ImageReaction {
        var req = SendPhoto(chatId, url)
        caption?.let { req = req.caption(it) }
        api.execute(req)
        return ImageReaction.create(url)
    }

    fun sendVideo(url: String) {
        api.execute(SendVideo(chatId, url))
    }

    fun sendAudio(url: String): AudioReaction {
        api.execute(SendAudio(chatId, url))
        return AudioReaction.create(url)
    }

    fun sendVoice(url: String) {
        api.execute(SendVoice(chatId, url))
    }

    fun sendDocument(url: String) {
        api.execute(SendDocument(chatId, url))
    }

    fun sendLocation(latitude: Float, longitude: Float) {
        api.execute(SendLocation(chatId, latitude, longitude))
    }

    fun sendContact(phoneNumber: String, firstName: String) {
        api.execute(SendContact(chatId, phoneNumber, firstName))
    }

    fun sendVenue(latitude: Float, longitude: Float, title: String, address: String) {
        api.execute(SendVenue(chatId, latitude, longitude, title, address))
    }

    fun sendVideoNote(url: String) {
        api.execute(SendVideoNote(chatId, url))
    }

    fun sendMediaGroup(vararg media: InputMedia<*>) {
        api.execute(SendMediaGroup(chatId, *media))
    }

    fun sendInvoice(title: String, description: String, payload: String, currency: String, vararg prices: LabeledPrice) {
        api.execute(SendInvoice(chatId, title, description, payload, currency, *prices))
    }

    fun answerPreCheckoutQuery(preCheckoutQueryId: String, ok: Boolean, errorMessage: String? = null) {
        // Note: AnswerPreCheckoutQuery in Pengrad API uses different constructors
        // For now, we only support the error message variant
        api.execute(AnswerPreCheckoutQuery(preCheckoutQueryId, errorMessage ?: ""))
    }

    override fun say(stream: Stream<String>): SayReaction {
        val handler = TelegramStreamingHandler(
            api = api,
            chatId = chatId,
            config = streamConfig,
            onMessageSent = { message -> messages.add(message) }
        )

        val resultText = handler.processStream(stream)
        return SayReaction.create(resultText)
    }
}
