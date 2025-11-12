package com.justai.jaicf.channel.telegram

/*
 * Example usage with custom TelegramStreamProcessor:
 *
 * class CustomStreamProcessor(
 *     api: Bot,
 *     chatId: ChatId,
 *     debounceMs: Long,
 *     dispatcher: CoroutineDispatcher,
 *     parseMode: ParseMode?
 * ) : TelegramStreamProcessor(api, chatId, debounceMs, dispatcher, parseMode) {
 *     override fun shouldSplitMessage(state: MessageState): Boolean {
 *         // Custom splitting logic
 *         return state.text.length > 2000
 *     }
 * }
 *
 * val channel = TelegramChannel(
 *     botApi = botEngine,
 *     telegramBotToken = "YOUR_TOKEN",
 *     requestDispatcher = Dispatchers.IO,
 *     streamProcessorFactory = TelegramStreamProcessorFactory { api, chatId, debounceMs, dispatcher, parseMode ->
 *         CustomStreamProcessor(api, chatId, debounceMs, dispatcher, parseMode)
 *     }
 * )
 *
 * Example usage with message aggregation (for handling long messages and media groups):
 *
 * val channel = TelegramChannel(
 *     botApi = botEngine,
 *     telegramBotToken = "YOUR_TOKEN",
 *     requestDispatcher = Dispatchers.IO,
 *     aggregation = AggregationConfig(
 *         enabled = true,
 *         waitTimeMs = 500L,
 *         useMediaGroupId = true,
 *         strategy = DefaultAggregationStrategy()
 *     )
 * )
 *
 * Example usage with custom parse mode:
 *
 * val channel = TelegramChannel(
 *     botApi = botEngine,
 *     telegramBotToken = "YOUR_TOKEN",
 *     defaultParseMode = ParseMode.MARKDOWN_V2  // Use Markdown v2 instead of default v1
 * )
 */

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
import com.justai.jaicf.api.QueryBotRequest
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

/**
 * Configuration for message aggregation in Telegram channel.
 *
 * @param enabled Enable message aggregation (default: true)
 * @param waitTimeMs Debounce delay in milliseconds (default: 500ms)
 * @param useMediaGroupId Use Telegram's mediaGroupId for instant media group detection (default: true)
 * @param strategy Custom aggregation strategy (default: DefaultAggregationStrategy)
 * @param maxItems Maximum number of items to aggregate (default: 20)
 */
data class AggregationConfig(
    val enabled: Boolean = true,
    val waitTimeMs: Long = 500L,
    val useMediaGroupId: Boolean = true,
    val strategy: AggregationStrategy = DefaultAggregationStrategy(),
    val maxItems: Int = 20
)

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String = "https://api.telegram.org/",
    private val telegramLogLevel: LogLevel = LogLevel.None,
    override val requestDispatcher: CoroutineDispatcher = DefaultRequestExecutor.asCoroutineDispatcher(),
    private val streamProcessorFactory: TelegramStreamProcessorFactory = DefaultStreamProcessorFactory,
    aggregation: AggregationConfig = AggregationConfig(),
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

    private val requestAggregator: TelegramRequestAggregator? = if (aggregation.enabled) {
        TelegramRequestAggregator(
            waitTimeMs = aggregation.waitTimeMs,
            scope = CoroutineScope(requestDispatcher + SupervisorJob()),
            strategy = aggregation.strategy,
            maxItems = aggregation.maxItems,
            useMediaGroupId = aggregation.useMediaGroupId
        )
    } else null

    private val bot: Bot = bot {
        apiUrl = telegramApiUrl.withTrailingSlash()
        token = telegramBotToken
        logLevel = telegramLogLevel

        dispatch {
            fun process(request: TelegramBotRequest) {
                botApi.process(
                    request,
                    TelegramReactions(bot, request, liveChatProvider, requestDispatcher, streamProcessorFactory, defaultParseMode),
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
        botApi.process(telegramRequest, TelegramReactions(bot, telegramRequest, liveChatProvider, requestDispatcher, streamProcessorFactory, defaultParseMode), requestContext)
    }

    fun run() {
        bot.startPolling()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        /**
         * Default factory for creating TelegramStreamProcessor instances.
         * This factory creates the standard processor with default debouncing (100ms)
         * and automatic message splitting at 3900 characters.
         */
        val DefaultStreamProcessorFactory = TelegramStreamProcessorFactory { api, chatId, debounceMs, dispatcher, parseMode ->
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
