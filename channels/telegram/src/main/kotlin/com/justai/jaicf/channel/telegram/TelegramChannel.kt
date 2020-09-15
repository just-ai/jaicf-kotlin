package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.updater.Updater
import com.google.gson.Gson
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.kotlin.PropertyWithBackingField
import okhttp3.logging.HttpLoggingInterceptor

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String = "https://api.telegram.org/",
    private val telegramLogLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC
) : JaicpCompatibleAsyncBotChannel {

    private val gson = Gson()

    private lateinit var botUpdater: Updater

    private val bot = bot {
        apiUrl = telegramApiUrl
        token = telegramBotToken
        logLevel = telegramLogLevel
        botUpdater = updater

        dispatch {
            fun process(request: TelegramBotRequest, requestContext: RequestContext) {
                botApi.process(request, TelegramReactions(bot, request), requestContext)
            }

            text { _, update ->
                update.message?.let {
                    process(TelegramTextRequest(it), RequestContext.fromHttp(update.httpBotRequest))
                }
            }

            callbackQuery { _, update ->
                update.callbackQuery?.let {
                    process(
                        TelegramQueryRequest(it.message!!, it.data),
                        RequestContext.fromHttp(update.httpBotRequest)
                    )
                }
            }

            location { _, update, location ->
                update.message?.let {
                    process(TelegramLocationRequest(it, location), RequestContext.fromHttp(update.httpBotRequest))
                }
            }

            contact { _, update, contact ->
                update.message?.let {
                    process(TelegramContactRequest(it, contact), RequestContext.fromHttp(update.httpBotRequest))
                }
            }
        }
    }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val update = gson.fromJson(request.receiveText(), Update::class.java)
        update.httpBotRequest = request
        bot.processUpdate(update)
        return null
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
    }
}

internal var Update.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
