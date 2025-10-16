package com.justai.jaicf.channel.telegram

/*
 * Example usage with custom TelegramStreamProcessor:
 *
 * class CustomStreamProcessor(
 *     api: Bot,
 *     chatId: ChatId,
 *     debounceMs: Long,
 *     dispatcher: CoroutineDispatcher
 * ) : TelegramStreamProcessor(api, chatId, debounceMs, dispatcher) {
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
 *     streamProcessorFactory = TelegramStreamProcessorFactory { api, chatId, debounceMs, dispatcher ->
 *         CustomStreamProcessor(api, chatId, debounceMs, dispatcher)
 *     }
 * )
 *
 * Example usage with message aggregation (for handling long messages split by Telegram):
 *
 * val channel = TelegramChannel(
 *     botApi = botEngine,
 *     telegramBotToken = "YOUR_TOKEN",
 *     requestDispatcher = Dispatchers.IO,
 *     aggregateUserMessages = true,  // Enable message aggregation
 *     aggregationWaitTimeMs = 500L   // Wait 500ms for additional messages
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
    override val requestDispatcher: CoroutineDispatcher,
    private val streamProcessorFactory: TelegramStreamProcessorFactory? = null,
    aggregateUserMessages: Boolean = true,
    aggregationWaitTimeMs: Long = UserMessageAggregator.DEFAULT_WAIT_TIME_MS,
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel, WithDispatcher {

    val mapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .addModule(Jdk8Module())
        .addModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    private var liveChatProvider: JaicpLiveChatProvider? = null

    private val messageAggregator: UserMessageAggregator? = if (aggregateUserMessages) {
        UserMessageAggregator(aggregationWaitTimeMs, CoroutineScope(requestDispatcher + SupervisorJob()))
    } else null

    private val bot: Bot = bot {
        apiUrl = telegramApiUrl.withTrailingSlash()
        token = telegramBotToken
        logLevel = telegramLogLevel

        dispatch {
            fun process(request: TelegramBotRequest) {
                botApi.process(
                    request,
                    TelegramReactions(bot, request, liveChatProvider, requestDispatcher, streamProcessorFactory),
                    RequestContext.fromHttp(request.update.httpBotRequest)
                )
            }

            text {
                val textRequest = TelegramTextRequest(update, message)

                if (messageAggregator != null && message.text != null) {
                    // Aggregate consecutive messages from the same user
                    messageAggregator.addMessage(
                        chatId = message.chat.id,
                        messageText = message.text!!
                    ) { combinedText ->
                        // Create a new request with combined text
                        val aggregatedRequest = TelegramTextRequest(
                            update = update,
                            message = message
                        ).let { req ->
                            // We need to override the input with combined text
                            object : TelegramBotRequest, QueryBotRequest(
                                clientId = req.clientId,
                                input = combinedText
                            ) {
                                override val update = req.update
                                override val message = req.message
                            }
                        }
                        process(aggregatedRequest)
                    }
                } else {
                    // Process immediately without aggregation
                    process(textRequest)
                }
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
        botApi.process(telegramRequest, TelegramReactions(bot, telegramRequest, liveChatProvider, requestDispatcher, streamProcessorFactory), requestContext)
    }

    fun run() {
        bot.startPolling()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
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
