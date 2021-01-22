package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.network.serialization.GsonFactory
import com.github.kotlintelegrambot.updater.Updater
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.kotlin.PropertyWithBackingField
import com.justai.jaicf.gateway.BotGateway
import com.justai.jaicf.gateway.BotGatewayRequest

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String = "https://api.telegram.org/"
) : JaicpCompatibleAsyncBotChannel, BotGateway() {

    private val gson = GsonFactory.createForApiClient()

    private lateinit var botUpdater: Updater

    private val bot = bot {
        apiUrl = telegramApiUrl
        token = telegramBotToken
        botUpdater = updater

        dispatch {
            fun process(request: TelegramBotRequest, update: Update) {
                botApi.process(request, TelegramReactions(bot, request), RequestContext.fromHttp(update.httpBotRequest))
            }

            text {
                process(TelegramTextRequest(message), update)
            }

            callbackQuery {
                callbackQuery.message?.let { message ->
                    process(TelegramQueryRequest(message, callbackQuery.data), update)
                }
            }

            location {
                process(TelegramLocationRequest(message, location), update)
            }

            contact {
                process(TelegramContactRequest(message, contact), update)
            }

            audio {
                process(TelegramAudioRequest(message, media), update)
            }

            document {
                process(TelegramDocumentRequest(message, media), update)
            }

            animation {
                process(TelegramAnimationRequest(message, media), update)
            }

            game {
                process(TelegramGameRequest(message, media), update)
            }

            photos {
                process(TelegramPhotosRequest(message, media), update)
            }

            sticker {
                process(TelegramStickerRequest(message, media), update)
            }

            video {
                process(TelegramVideoRequest(message, media), update)
            }

            videoNote {
                process(TelegramVideoNoteRequest(message, media), update)
            }

            voice {
                process(TelegramVoiceRequest(message, media), update)
            }
        }
    }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val update = gson.fromJson(request.receiveText(), Update::class.java)
        update.httpBotRequest = request
        bot.processUpdate(update)
        return null
    }

    override fun processGatewayRequest(request: BotGatewayRequest) {
        val template = getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
        val message = gson.fromJson(template, Update::class.java).message ?: return
        val telegramRequest = TelegramGatewayRequest.create(request, message) ?: return
        botApi.process(
            telegramRequest         ,
            TelegramReactions(bot, telegramRequest),
            RequestContext.DEFAULT
        )
    }

    fun run() {
        bot.startPolling()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "telegram"
        override fun create(botApi: BotApi, apiUrl: String) =
            TelegramChannel(botApi, telegramApiUrl = apiUrl, telegramBotToken = "").also {
                it.botUpdater.startCheckingUpdates()
            }

        private const val REQUEST_TEMPLATE_PATH = "/TelegramRequestTemplate.json"
    }
}

internal var Update.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
