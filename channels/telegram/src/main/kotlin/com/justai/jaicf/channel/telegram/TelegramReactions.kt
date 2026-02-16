package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.payments.PaymentInvoiceInfo
import com.github.kotlintelegrambot.types.TelegramBotResult
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.telegram.helpers.downloadFileByUrl
import com.justai.jaicf.channel.telegram.streaming.TelegramStreamProcessor
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.logging.VideoReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.StreamReactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import java.io.File
import java.util.stream.Stream

val Reactions.telegram
    get() = this as? TelegramReactions

@Suppress("MemberVisibilityCanBePrivate")
open class TelegramReactions(
    val api: Bot,
    val request: TelegramBotRequest,
    override val liveChatProvider: JaicpLiveChatProvider?,
    val requestDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO,
    private val streamProcessorFactory: TelegramStreamProcessorFactory = TelegramChannel.DefaultStreamProcessorFactory,
    private val defaultParseMode: ParseMode = ParseMode.MARKDOWN,
) : StreamReactions, Reactions(), JaicpCompatibleAsyncReactions {

    val chatId = ChatId.fromId(request.chatId)
    private val messages = mutableListOf<Message?>()

    protected open fun createStreamProcessor(debounceMs: Long = DEFAULT_DEBOUNCE_MS): TelegramStreamProcessor {
        return streamProcessorFactory.create(api, chatId, debounceMs, requestDispatcher, defaultParseMode)
    }

    private fun addResponse(res: TelegramBotResult<Message>) {
        res.getOrNull()?.let { message ->
            val index = messages.indexOfFirst { it?.messageId == message.messageId }
            if (index == -1) {
                messages.add(message)
            } else {
                messages[index] = message
            }
        }
    }

    private fun addResponse(pair: Pair<retrofit2.Response<com.github.kotlintelegrambot.network.Response<Message>?>?, Exception?>) {
        pair.first?.body()?.result?.let { message ->
            val index = messages.indexOfFirst { it?.messageId == message.messageId }
            if (index == -1) {
                messages.add(message)
            } else {
                messages[index] = message
            }
        }
    }

    override fun say(text: String): SayReaction {
        return sendMessage(text, parseMode = defaultParseMode)
    }

    override fun say(stream: Stream<String>): SayReaction {
        return say(stream, DEFAULT_DEBOUNCE_MS)
    }

    open fun say(stream: Stream<String>, debounceMs: Long = DEFAULT_DEBOUNCE_MS): SayReaction {
        val processor = createStreamProcessor(debounceMs)
        val fullText = processor.processStream(stream)
        return SayReaction.create(fullText)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        messages.lastOrNull()?.let { message ->
            val keyboard = message.replyMarkup?.inlineKeyboard?.toMutableList() ?: mutableListOf()
            keyboard.addAll(buttons.map { listOf(InlineKeyboardButton.CallbackData(it, callbackData = it)) })

            addResponse(api.editMessageReplyMarkup(
                chatId,
                message.messageId,
                replyMarkup = InlineKeyboardMarkup.create(keyboard)
            ))
        }

        return ButtonsReaction.create(buttons.asList())
    }

    fun say(text: String, inlineButtons: List<String>): TelegramBotResult<Message> {
        val result = api.sendMessage(
            chatId,
            text,
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(inlineButtons.map { InlineKeyboardButton.CallbackData(it, callbackData = it) })
            )
        )
        SayReaction.create(text)
        ButtonsReaction.create(inlineButtons)
        return result
    }

    fun say(
        text: String,
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null,
        messageThreadId: Long? = null,
    ) = sendMessage(
        text,
        parseMode,
        disableWebPagePreview,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup,
        messageThreadId,
    )

    fun sendMessage(
        text: String,
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null,
        messageThreadId: Long? = null,
    ): SayReaction {
        val result = api.sendMessage(
            chatId,
            text,
            parseMode,
            disableWebPagePreview,
            disableNotification,
            protectContent,
            replyToMessageId,
            allowSendingWithoutReply,
            replyMarkup,
            messageThreadId,
        )

        when {
            result.getOrNull() == null && parseMode != null -> {
                val fallbackResult = api.sendMessage(
                    chatId,
                    text,
                    parseMode = null, // Remove parseMode for safer delivery
                    disableWebPagePreview,
                    disableNotification,
                    protectContent,
                    replyToMessageId,
                    allowSendingWithoutReply,
                    replyMarkup,
                    messageThreadId,
                )
                addResponse(fallbackResult)
            }
            else -> {
                addResponse(result)
            }
        }

        return SayReaction.create(text)
    }

    override fun image(url: String): ImageReaction {
        return sendPhoto(url)
    }

    fun image(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = sendPhoto(
        url,
        caption,
        parseMode,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    )

    fun sendPhoto(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ): ImageReaction {
        val result = api.sendPhoto(
            chatId,
            TelegramFile.ByUrl(url),
            caption,
            parseMode,
            disableNotification,
            protectContent,
            replyToMessageId,
            allowSendingWithoutReply,
            replyMarkup,
        )

        when {
            (result.first == null || result.second != null) && parseMode != null -> {
                val fallbackResult = api.sendPhoto(
                    chatId,
                    TelegramFile.ByUrl(url),
                    caption,
                    parseMode = null, // Remove parseMode for safer delivery
                    disableNotification,
                    protectContent,
                    replyToMessageId,
                    allowSendingWithoutReply,
                    replyMarkup,
                )
                addResponse(fallbackResult)
            }
            else -> {
                addResponse(result)
            }
        }

        return ImageReaction.create(url)
    }

    fun sendVideo(
        url: String,
        duration: Int? = null,
        width: Int? = null,
        height: Int? = null,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) {
        val result = api.sendVideo(
            chatId,
            TelegramFile.ByUrl(url),
            duration,
            width,
            height,
            caption,
            parseMode,
            disableNotification,
            protectContent,
            replyToMessageId,
            allowSendingWithoutReply,
            replyMarkup
        )

        when {
            (result.first == null || result.second != null) && parseMode != null -> {
                val fallbackResult = api.sendVideo(
                    chatId,
                    TelegramFile.ByUrl(url),
                    duration,
                    width,
                    height,
                    caption,
                    parseMode = null, // Remove parseMode for safer delivery
                    disableNotification,
                    protectContent,
                    replyToMessageId,
                    allowSendingWithoutReply,
                    replyMarkup
                )
                addResponse(fallbackResult)
            }
            else -> {
                addResponse(result)
            }
        }
    }

    fun sendVoice(
        url: String,
        duration: Int? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) {
        val result = api.sendVoice(
            chatId,
            TelegramFile.ByUrl(url),
            duration = duration,
            parseMode = parseMode,
            disableNotification = disableNotification,
            protectContent = protectContent,
            replyToMessageId = replyToMessageId,
            allowSendingWithoutReply = allowSendingWithoutReply,
            replyMarkup = replyMarkup,
        )

        when {
            (result.first == null || result.second != null) && parseMode != null -> {
                val fallbackResult = api.sendVoice(
                    chatId,
                    TelegramFile.ByUrl(url),
                    duration = duration,
                    parseMode = null, // Remove parseMode for safer delivery
                    disableNotification = disableNotification,
                    protectContent = protectContent,
                    replyToMessageId = replyToMessageId,
                    allowSendingWithoutReply = allowSendingWithoutReply,
                    replyMarkup = replyMarkup,
                )
                addResponse(fallbackResult)
            }
            else -> {
                addResponse(result)
            }
        }
    }

    override fun audio(url: String): AudioReaction {
        return sendAudio(url)
    }

    fun sendAudio(
        url: String,
        duration: Int? = null,
        performer: String? = null,
        title: String? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ): AudioReaction {
        addResponse(api.sendAudio(
            chatId,
            TelegramFile.ByUrl(url),
            duration,
            performer,
            title,
            disableNotification,
            protectContent,
            replyToMessageId,
            allowSendingWithoutReply,
            replyMarkup
        ))

        return AudioReaction.create(url)
    }

    fun sendDocument(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableContentTypeDetection: Boolean? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null,
        mimeType: String? = null,
    ) {
        val result = api.sendDocument(
            chatId,
            TelegramFile.ByUrl(url),
            caption,
            parseMode,
            disableContentTypeDetection,
            disableNotification,
            protectContent,
            replyToMessageId,
            allowSendingWithoutReply,
            replyMarkup,
            mimeType,
        )

        when {
            (result.first == null || result.second != null) && parseMode != null -> {
                val fallbackResult = api.sendDocument(
                    chatId,
                    TelegramFile.ByUrl(url),
                    caption,
                    parseMode = null, // Remove parseMode for safer delivery
                    disableContentTypeDetection,
                    disableNotification,
                    protectContent,
                    replyToMessageId,
                    allowSendingWithoutReply,
                    replyMarkup,
                    mimeType,
                )
                addResponse(fallbackResult)
            }
            else -> {
                addResponse(result)
            }
        }
    }

    fun sendVenue(
        latitude: Float,
        longitude: Float,
        title: String,
        address: String,
        foursquareId: String? = null,
        foursquareType: String? = null,
        googlePlaceId: String? = null,
        googlePlaceType: String? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = addResponse(api.sendVenue(
        chatId,
        latitude,
        longitude,
        title,
        address,
        foursquareId,
        foursquareType,
        googlePlaceId,
        googlePlaceType,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    ))

    fun sendContact(
        phoneNumber: String,
        firstName: String,
        lastName: String? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = addResponse(api.sendContact(
        chatId,
        phoneNumber,
        firstName,
        lastName,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    ))

    fun sendLocation(
        latitude: Float,
        longitude: Float,
        livePeriod: Int? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = addResponse(api.sendLocation(
        chatId,
        latitude,
        longitude,
        livePeriod,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    ))

    fun sendMediaGroup(
        mediaGroup: MediaGroup,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
    ) = api.sendMediaGroup(
        chatId,
        mediaGroup,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply
    )

    fun sendVideoNote(
        url: String,
        duration: Int? = null,
        length: Int? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ): VideoReaction {
        addResponse(api.sendVideoNote(
            chatId,
            TelegramFile.ByFile(downloadFileByUrl(url)),
            duration,
            length,
            disableNotification,
            protectContent,
            replyToMessageId,
            allowSendingWithoutReply,
            replyMarkup
        ))

        return VideoReaction.create(url)
    }

    fun sendVideoNote(
        file: File,
        duration: Int? = null,
        length: Int? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = addResponse(api.sendVideoNote(
        chatId,
        TelegramFile.ByFile(file),
        duration,
        length,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    ))

    fun sendInvoice(
        paymentInvoiceInfo: PaymentInvoiceInfo,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        inlineKeyboardMarkup: InlineKeyboardMarkup? = null
    ) = addResponse(api.sendInvoice(
        chatId,
        paymentInvoiceInfo,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        inlineKeyboardMarkup
    ))

    fun answerPreCheckoutQuery(preCheckoutQueryId: String, ok: Boolean, errorMessage: String? = null) =
        api.answerPreCheckoutQuery(preCheckoutQueryId, ok, errorMessage)

    companion object {
        const val DEFAULT_DEBOUNCE_MS = 100L
    }
}

