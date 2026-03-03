package com.justai.jaicf.channel.telegram

import com.google.gson.Gson
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.invocationapi.getRequestTemplateFromResources
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.kotlin.PropertyWithBackingField
import com.justai.jaicf.BotEngine.Defaults.DefaultRequestExecutor
import com.justai.jaicf.channel.telegram.streaming.StreamConfig
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String? = null,
    private val requestExecutor: Executor = DefaultRequestExecutor,
    private val streamConfig: StreamConfig = StreamConfig(),
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel {

    private val gson = Gson()
    private var liveChatProvider: JaicpLiveChatProvider? = null

    private val bot = if (telegramApiUrl != null) {
        TelegramBot.Builder(telegramBotToken)
            .apiUrl(telegramApiUrl)
            .build()
    } else {
        TelegramBot(telegramBotToken)
    }

    init {
        bot.setUpdatesListener { updates ->
            updates.forEach { update ->
                update.httpBotRequest = HttpBotRequest("".byteInputStream())
                processUpdate(update)
            }
            UpdatesListener.CONFIRMED_UPDATES_ALL
        }
    }

    private fun processUpdate(update: Update) {
        requestExecutor.execute {
            val request = createBotRequest(update) ?: return@execute
            botApi.process(
                request,
                TelegramReactions(bot, request, liveChatProvider, streamConfig),
                RequestContext.fromHttp(request.update.httpBotRequest)
            )
        }
    }

    private fun createBotRequest(update: Update): TelegramBotRequest? {
        update.callbackQuery()?.let { callbackQuery ->
            val message = callbackQuery.message() ?: return null
            return TelegramQueryRequest(update, message, callbackQuery.data())
        }

        update.preCheckoutQuery()?.let { preCheckoutQuery ->
            return TelegramPreCheckoutRequest(update, preCheckoutQuery)
        }

        val message = update.message() ?: return null

        message.text()?.let {
            return TelegramTextRequest(update, message)
        }

        message.location()?.let {
            return TelegramLocationRequest(update, message, it)
        }

        message.contact()?.let {
            return TelegramContactRequest(update, message, it)
        }

        message.audio()?.let {
            return TelegramAudioRequest(update, message, it)
        }

        message.document()?.let {
            return TelegramDocumentRequest(update, message, it)
        }

        message.animation()?.let {
            return TelegramAnimationRequest(update, message, it)
        }

        message.game()?.let {
            return TelegramGameRequest(update, message, it)
        }

        message.photo()?.let { photos ->
            if (photos.isNotEmpty()) {
                return TelegramPhotosRequest(update, message, photos)
            }
        }

        message.sticker()?.let {
            return TelegramStickerRequest(update, message, it)
        }

        message.video()?.let {
            return TelegramVideoRequest(update, message, it)
        }

        message.videoNote()?.let {
            return TelegramVideoNoteRequest(update, message, it)
        }

        message.voice()?.let {
            return TelegramVoiceRequest(update, message, it)
        }

        message.successfulPayment()?.let {
            return TelegramSuccessfulPaymentRequest(update, message, it)
        }

        return null
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val update = gson.fromJson(request.receiveText(), Update::class.java)
        update.httpBotRequest = request
        processUpdate(update)
        return HttpBotResponse.accepted()
    }

    private fun generateRequestFromTemplate(request: InvocationRequest) =
        getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
            .replace("\"{{ timestamp }}\"", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
            .replace("{{ messageId }}", UUID.randomUUID().toString())

    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generatedRequest = generateRequestFromTemplate(request)
        val update = gson.fromJson(generatedRequest, Update::class.java) ?: return
        val message = update.message() ?: return
        val telegramRequest = TelegramInvocationRequest.create(request, update, message) ?: return
        botApi.process(telegramRequest, TelegramReactions(bot, telegramRequest, liveChatProvider, streamConfig), requestContext)
    }

    fun run() {
        bot.removeGetUpdatesListener()
        bot.setUpdatesListener { updates ->
            updates.forEach { update ->
                update.httpBotRequest = HttpBotRequest("".byteInputStream())
                processUpdate(update)
            }
            UpdatesListener.CONFIRMED_UPDATES_ALL
        }

        Thread.currentThread().join()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "telegram"
        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider,
        ) = TelegramChannel(botApi, telegramBotToken = "", telegramApiUrl = apiUrl).apply {
            this.liveChatProvider = liveChatProvider
        }

        private const val REQUEST_TEMPLATE_PATH = "/TelegramRequestTemplate.json"
    }

    class Jaicp(
        private val executor: Executor
    ) : JaicpCompatibleAsyncChannelFactory {

        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ): JaicpCompatibleAsyncBotChannel = TelegramChannel(botApi, "", apiUrl, executor).apply {
            this.liveChatProvider = liveChatProvider
        }

        override val channelType: String = "telegram"
    }
}

internal var Update.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
