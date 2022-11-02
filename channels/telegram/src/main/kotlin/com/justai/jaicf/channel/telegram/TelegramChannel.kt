package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.animation
import com.github.kotlintelegrambot.dispatcher.audio
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.contact
import com.github.kotlintelegrambot.dispatcher.document
import com.github.kotlintelegrambot.dispatcher.game
import com.github.kotlintelegrambot.dispatcher.location
import com.github.kotlintelegrambot.dispatcher.photos
import com.github.kotlintelegrambot.dispatcher.preCheckoutQuery
import com.github.kotlintelegrambot.dispatcher.sticker
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.dispatcher.video
import com.github.kotlintelegrambot.dispatcher.videoNote
import com.github.kotlintelegrambot.dispatcher.voice
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.network.serialization.GsonFactory
import com.github.kotlintelegrambot.updater.Updater
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
import com.justai.jaicf.helpers.http.withTrailingSlash
import com.justai.jaicf.helpers.kotlin.PropertyWithBackingField
import java.util.*
import java.util.concurrent.TimeUnit

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String = "https://api.telegram.org/"
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel {

    private val gson = GsonFactory.createForApiClient()
    private var liveChatProvider: JaicpLiveChatProvider? = null
    private lateinit var botUpdater: Updater

    private val bot = bot {
        apiUrl = telegramApiUrl.withTrailingSlash()
        token = telegramBotToken
        botUpdater = updater

        botUpdater.startCheckingUpdates()

        dispatch {
            fun process(request: TelegramBotRequest) {
                botApi.process(request, TelegramReactions(bot, request, liveChatProvider), RequestContext.fromHttp(request.update.httpBotRequest))
            }

            text {
                process(TelegramTextRequest(update, message))
            }

            callbackQuery {
                val message = callbackQuery.message ?: return@callbackQuery
                process(TelegramQueryRequest(update, message, callbackQuery.data))
            }

            location {
                process(TelegramLocationRequest(update, message, location))
            }

            contact {
                process(TelegramContactRequest(update, message, contact))
            }

            audio {
                process(TelegramAudioRequest(update, message, media))
            }

            document {
                process(TelegramDocumentRequest(update, message, media))
            }

            animation {
                process(TelegramAnimationRequest(update, message, media))
            }

            game {
                process(TelegramGameRequest(update, message, media))
            }

            photos {
                process(TelegramPhotosRequest(update, message, media))
            }

            sticker {
                process(TelegramStickerRequest(update, message, media))
            }

            video {
                process(TelegramVideoRequest(update, message, media))
            }

            videoNote {
                process(TelegramVideoNoteRequest(update, message, media))
            }

            voice {
                process(TelegramVoiceRequest(update, message, media))
            }

            preCheckoutQuery {
                process(TelegramPreCheckoutRequest(update, preCheckoutQuery))
            }

            successfulPayment {
                process(TelegramSuccessfulPaymentRequest(update, message, successfulPayment))
            }
        }
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val update = gson.fromJson(request.receiveText(), Update::class.java)
        update.httpBotRequest = request
        bot.processUpdate(update)
        return HttpBotResponse.accepted()
    }

    private fun generateRequestFromTemplate(request: InvocationRequest) =
        getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
            .replace("\"{{ timestamp }}\"", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
            .replace("{{ messageId }}", UUID.randomUUID().toString())


    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generatedRequest = generateRequestFromTemplate(request)
        val update = gson.fromJson(generatedRequest, Update::class.java) ?: return
        val message = update.message ?: return
        val telegramRequest = TelegramInvocationRequest.create(request, update, message) ?: return
        botApi.process(telegramRequest, TelegramReactions(bot, telegramRequest, liveChatProvider), requestContext)
    }

    fun run() {
        botUpdater.stopCheckingUpdates()
        bot.startPolling()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "telegram"
        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ) = TelegramChannel(botApi, telegramApiUrl = apiUrl, telegramBotToken = "").apply {
            this.liveChatProvider = liveChatProvider
        }

        private const val REQUEST_TEMPLATE_PATH = "/TelegramRequestTemplate.json"
    }
}

internal var Update.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
