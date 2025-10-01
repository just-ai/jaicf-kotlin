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
import com.github.kotlintelegrambot.network.Response
import com.github.kotlintelegrambot.types.TelegramBotResult
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.telegram.helpers.downloadFileByUrl
import com.justai.jaicf.channel.telegram.helpers.findOptimalSplitPoint
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.logging.VideoReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.StreamReactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.stream.Stream

val Reactions.telegram
    get() = this as? TelegramReactions

@Suppress("MemberVisibilityCanBePrivate")
class TelegramReactions(
    val api: Bot,
    val request: TelegramBotRequest,
    override val liveChatProvider: JaicpLiveChatProvider?
) : StreamReactions, Reactions(), JaicpCompatibleAsyncReactions {

    val chatId = ChatId.fromId(request.chatId)
    private val messages = mutableListOf<Message?>()

    private fun addResponse(pair: Pair<retrofit2.Response<Response<Message>?>?, Exception?>) {
        pair.first?.body()?.result?.let { message ->
            when (val index = messages.indexOfFirst { it?.messageId == message.messageId }) {
                -1 -> messages.add(message)
                else -> messages.set(index, message)
            }
        }
    }

    private fun addResponse(res: TelegramBotResult<Message>) {
        res.let { message ->
            when (val index = messages.indexOfFirst { it?.messageId == message.getOrNull()?.messageId }) {
                -1 -> messages.add(message.getOrNull())
                else -> messages.set(index, message.getOrNull())
            }
        }
    }

    override fun say(text: String): SayReaction {
        return sendMessage(text)
    }

    override fun say(stream: Stream<String>): SayReaction {
        return say(stream, DEFAULT_DEBOUNCE_MS)
    }

    fun say(stream: Stream<String>, debounceMs: Long = DEFAULT_DEBOUNCE_MS): SayReaction {
        val fullText = StringBuilder()
        val messageStates = mutableListOf<MessageState>()
        var currentMessageState: MessageState? = null

        val debouncer = MessageDebouncer(debounceMs, debounceScope)

        stream.forEach { chunk ->
            fullText.append(chunk)

            if (currentMessageState == null) {
                currentMessageState = MessageState(
                    text = StringBuilder(chunk),
                    messageId = null,
                    debouncer = debouncer
                )
                    .also(messageStates::add)
            } else {
                currentMessageState?.text?.append(chunk)
            }

            currentMessageState?.let {
                if (it.text.length > SAFE_MESSAGE_LIMIT) {
                    val textToProcess = it.text.toString()
                    val splitPoint = findOptimalSplitPoint(textToProcess, SAFE_MESSAGE_LIMIT)

                    val messageToSend = textToProcess.take(splitPoint).trim()
                    val remainder = textToProcess.substring(splitPoint).trim()

                    it.text.clear().append(messageToSend)
                    scheduleMessageUpdate(it)

                    currentMessageState = if (remainder.isNotEmpty()) {
                        MessageState(
                            text = StringBuilder(remainder),
                            messageId = null,
                            debouncer = debouncer
                        ).also(messageStates::add)
                    } else {
                        null
                    }
                } else {
                    currentMessageState?.let { messageState -> scheduleMessageUpdate(messageState) }
                }
            }
        }

        runBlocking(debounceScope.coroutineContext) {
            messageStates.forEach { it.debouncer.flush() }
        }

        return SayReaction.create(fullText.toString())
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        messages.lastOrNull()?.let { message ->
            val keyboard = message.replyMarkup?.inlineKeyboard?.toMutableList() ?: mutableListOf()
            keyboard.addAll(buttons.map { listOf(InlineKeyboardButton.CallbackData(it, callbackData = it)) })

            api.editMessageReplyMarkup(
                chatId,
                message.messageId,
                replyMarkup = InlineKeyboardMarkup.create(keyboard)
            ).also { addResponse(it) }
        }

        return ButtonsReaction.create(buttons.asList())
    }

    fun say(text: String, inlineButtons: List<String>) = api.sendMessage(
        chatId,
        text,
        replyMarkup = InlineKeyboardMarkup.create(
            listOf(inlineButtons.map { InlineKeyboardButton.CallbackData(it, callbackData = it) })
        ).also {
            SayReaction.create(text)
            ButtonsReaction.create(inlineButtons)
        }
    )

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

        api.sendMessage(
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
            .also { addResponse(it) }

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
        api.sendPhoto(
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
            .also { addResponse(it) }

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
    ) = api.sendVideo(
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
    ).also { addResponse(it) }

    fun sendVoice(
        url: String,
        duration: Int? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVoice(
        chatId,
        TelegramFile.ByUrl(url),
        duration = duration,
        parseMode = parseMode,
        disableNotification = disableNotification,
        protectContent = protectContent,
        replyToMessageId = replyToMessageId,
        allowSendingWithoutReply = allowSendingWithoutReply,
        replyMarkup = replyMarkup,
    ).also { addResponse(it) }

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
        api.sendAudio(
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
        ).also { addResponse(it) }

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
    ) = api.sendDocument(
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
    ).also { addResponse(it) }

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
    ) = api.sendVenue(
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
    ).also { addResponse(it) }

    fun sendContact(
        phoneNumber: String,
        firstName: String,
        lastName: String? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendContact(
        chatId,
        phoneNumber,
        firstName,
        lastName,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    ).also { addResponse(it) }

    fun sendLocation(
        latitude: Float,
        longitude: Float,
        livePeriod: Int? = null,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendLocation(
        chatId,
        latitude,
        longitude,
        livePeriod,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    ).also { addResponse(it) }

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

        api.sendVideoNote(
            chatId,
            TelegramFile.ByFile(downloadFileByUrl(url)),
            duration,
            length,
            disableNotification,
            protectContent,
            replyToMessageId,
            allowSendingWithoutReply,
            replyMarkup
        ).also { addResponse(it) }

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
    ) = api.sendVideoNote(
        chatId,
        TelegramFile.ByFile(file),
        duration,
        length,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup
    ).also { addResponse(it) }

    fun sendInvoice(
        paymentInvoiceInfo: PaymentInvoiceInfo,
        disableNotification: Boolean? = null,
        protectContent: Boolean? = null,
        replyToMessageId: Long? = null,
        allowSendingWithoutReply: Boolean? = null,
        inlineKeyboardMarkup: InlineKeyboardMarkup? = null
    ) = api.sendInvoice(
        chatId,
        paymentInvoiceInfo,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        inlineKeyboardMarkup
    ).also { addResponse(it) }

    fun answerPreCheckoutQuery(preCheckoutQueryId: String, ok: Boolean, errorMessage: String? = null) =
        api.answerPreCheckoutQuery(preCheckoutQueryId, ok, errorMessage)

    companion object {
        private const val TELEGRAM_MESSAGE_LIMIT = 4096
        private const val SAFE_MESSAGE_LIMIT = 3900
        private const val DEFAULT_DEBOUNCE_MS = 100L
    }

    private val debounceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private class MessageDebouncer(
        private val debounceMs: Long,
        private val scope: CoroutineScope
    ) {
        private var pendingJob: Job? = null

        fun debounce(action: suspend () -> Unit) {
            pendingJob?.cancel()
            pendingJob = scope.launch {
                delay(debounceMs)
                action()
            }
        }

        suspend fun flush() {
            pendingJob?.join()
        }
    }

    private data class MessageState(
        val text: StringBuilder,
        var messageId: Long?,
        val debouncer: MessageDebouncer
    )

    private fun scheduleMessageUpdate(messageState: MessageState) {
        messageState.debouncer.debounce {
            val textToSend = messageState.text.toString()

            if (messageState.messageId == null) {
                val result = api.sendMessage(chatId, textToSend).getOrNull()
                messageState.messageId = result?.messageId
                result?.let { addResponse(TelegramBotResult.Success(it)) }
            } else {
                val result =
                    api.editMessageText(chatId, messageState.messageId, text = textToSend).first?.body()?.result
                result?.let { addResponse(TelegramBotResult.Success(it)) }
            }
        }
    }
}
