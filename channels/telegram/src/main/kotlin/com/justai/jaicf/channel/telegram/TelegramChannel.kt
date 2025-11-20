package com.justai.jaicf.channel.telegram

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.logging.LogLevel
import com.justai.jaicf.BotEngine.Defaults.DefaultRequestExecutor
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.invocationapi.getRequestTemplateFromResources
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.telegram.aggregation.AggregationConfig
import com.justai.jaicf.channel.telegram.aggregation.TelegramRequestAggregator
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.http.withTrailingSlash
import com.justai.jaicf.helpers.kotlin.PropertyWithBackingField
import com.justai.jaicf.helpers.kotlin.WithDispatcher
import com.justai.jaicf.channel.telegram.streaming.TelegramStreamProcessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String = "https://api.telegram.org/",
    private val telegramLogLevel: LogLevel = LogLevel.None,
    private val requestDispatcher: CoroutineDispatcher = DefaultRequestExecutor.asCoroutineDispatcher(),
    private val streamProcessorFactory: TelegramStreamProcessorFactory = DefaultStreamProcessorFactory,
    private val aggregation: AggregationConfig? = AggregationConfig(),
    private val defaultParseMode: ParseMode = ParseMode.MARKDOWN,
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel, WithDispatcher {

    val mapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .addModule(Jdk8Module())
        .addModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    private var liveChatProvider: JaicpLiveChatProvider? = null

    private val requestAggregator: TelegramRequestAggregator? = if (aggregation != null) {
        TelegramRequestAggregator(
            scope = CoroutineScope(requestDispatcher + SupervisorJob()),
            config = aggregation,
        )
    } else null

    val bot: Bot = bot {
        apiUrl = telegramApiUrl.withTrailingSlash()
        token = telegramBotToken
        logLevel = telegramLogLevel

        dispatch {
            fun process(request: TelegramBotRequest) {
                botApi.process(
                    request,
                    TelegramReactions(
                        bot,
                        request,
                        liveChatProvider,
                        requestDispatcher,
                        streamProcessorFactory,
                        defaultParseMode
                    ),
                    RequestContext.fromHttp(request.update.httpBotRequest)
                )
            }

            text {
                val textRequest = TelegramTextRequest(update, message)

                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(textRequest) { aggregated ->
                            process(aggregated)
                        }
                    } else {
                        process(textRequest)
                    }
                }
            }

            callbackQuery {
                val message = callbackQuery.message ?: return@callbackQuery
                // Callback queries are not aggregated - process immediately
                process(TelegramQueryRequest(update, message, callbackQuery.data))
            }

            location {
                val request = TelegramLocationRequest(update, message, location)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            contact {
                val request = TelegramContactRequest(update, message, contact)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            audio {
                val request = TelegramAudioRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            document {
                val request = TelegramDocumentRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            animation {
                val request = TelegramAnimationRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            game {
                val request = TelegramGameRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            photos {
                val request = TelegramPhotosRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            sticker {
                val request = TelegramStickerRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            video {
                val request = TelegramVideoRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            videoNote {
                val request = TelegramVideoNoteRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
            }

            voice {
                val request = TelegramVoiceRequest(update, message, media)
                runBlocking(requestDispatcher) {
                    if (requestAggregator != null) {
                        requestAggregator.addRequest(request) { process(it) }
                    } else {
                        process(request)
                    }
                }
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
        val update = mapper.readValue<Update>(request.receiveText())
        update.httpBotRequest = request

        runBlocking(requestDispatcher) {
            bot.processUpdate(update)
        }

        return HttpBotResponse.accepted()
    }

    private fun generateRequestFromTemplate(request: InvocationRequest) =
        getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
            .replace("\"{{ timestamp }}\"", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
            .replace("{{ messageId }}", UUID.randomUUID().toString())


    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generatedRequest = generateRequestFromTemplate(request)
        val update = mapper.readValue<Update?>(generatedRequest) ?: return
        val message = update.message ?: return
        val telegramRequest = TelegramInvocationRequest.create(request, update, message) ?: return
        botApi.process(
            telegramRequest,
            TelegramReactions(
                bot,
                telegramRequest,
                liveChatProvider,
                requestDispatcher,
                streamProcessorFactory,
                defaultParseMode
            ),
            requestContext
        )
    }

    fun run() {
        bot.startPolling()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        val DefaultStreamProcessorFactory =
            TelegramStreamProcessorFactory { api, chatId, debounceMs, dispatcher, parseMode ->
                TelegramStreamProcessor(api, chatId, debounceMs, dispatcher, parseMode)
            }

        override val channelType = "telegram"
        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider,
        ): JaicpCompatibleAsyncBotChannel {
            val requestDispatcher =
                if (botApi is WithDispatcher) {
                    botApi.requestDispatcher
                } else {
                    DefaultRequestExecutor.asCoroutineDispatcher()
                }

            return TelegramChannel(
                botApi,
                telegramApiUrl = apiUrl,
                telegramBotToken = "",
                requestDispatcher = requestDispatcher
            ).apply {
                this.liveChatProvider = liveChatProvider
                this.bot.startPolling()
            }
        }

        private const val REQUEST_TEMPLATE_PATH = "/TelegramRequestTemplate.json"
    }

    class Jaicp(
        private val executor: Executor,
        private val logLevel: LogLevel
    ) : JaicpCompatibleAsyncChannelFactory {

        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ): JaicpCompatibleAsyncBotChannel {
            val requestDispatcher =
                if (botApi is WithDispatcher) {
                    botApi.requestDispatcher
                } else {
                    executor.asCoroutineDispatcher()
                }

            return TelegramChannel(botApi, "", apiUrl, logLevel, requestDispatcher).apply {
                this.liveChatProvider = liveChatProvider
                this.bot.startPolling()
            }
        }

        override val channelType: String = "telegram"
    }
}

internal var Update.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
