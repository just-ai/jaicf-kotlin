package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.channel.jaicp.http.HttpClientFactory
import com.justai.jaicf.channel.jaicp.polling.Dispatcher
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.features.logging.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * This class is used to create polling coroutines for each channel, polls requests and sends responses.
 * Supports all implementations of [JaicpBotChannel]
 *
 * Asynchronous responses are also supported for each channel, see [JaicpCompatibleAsyncBotChannel]
 *
 * See example at
 * examples/jaicp-telephony/src/main/kotlin/com/justai/jaicf/examples/jaicptelephony/channels/PollingConnection.kt
 *
 * @see JaicpNativeBotChannel
 * @see JaicpCompatibleBotChannel
 * @see JaicpCompatibleAsyncBotChannel
 *
 * @param botApi the [BotApi] implementation used to process the requests for all channels
 * @param accessToken can be configured in JAICP Web Interface
 * @param channels is a list of channels which will be managed by connector
 * */
open class JaicpPollingConnector(
    botApi: BotApi,
    accessToken: String,
    url: String = DEFAULT_PROXY_URL,
    channels: List<JaicpChannelFactory>,
    logLevel: LogLevel = LogLevel.INFO,
    httpClient: HttpClient = HttpClientFactory.create(logLevel),
    executor: Executor = DEFAULT_EXECUTOR,
) : JaicpConnector(botApi, channels, accessToken, url, httpClient, executor),
    WithLogger {

    constructor(
        botApi: BotApi,
        accessToken: String,
        url: String = DEFAULT_PROXY_URL,
        channels: List<JaicpChannelFactory>,
        logLevel: LogLevel = LogLevel.INFO,
        httpClient: HttpClient = HttpClientFactory.create(logLevel),
        executorThreadPoolSize: Int
    ) : this(
        botApi,
        accessToken,
        url,
        channels,
        logLevel,
        httpClient,
        Executors.newFixedThreadPool(executorThreadPoolSize)
    )

    private val dispatcher = Dispatcher(httpClient, jaicpExecutor)
    protected val channelMap = mutableMapOf<String, JaicpBotChannel>()

    init {
        loadConfig()
    }

    fun runBlocking() = dispatcher.startPollingBlocking()

    fun run() = dispatcher.startPolling()

    fun stop() = dispatcher.stopPolling()

    override fun register(channel: JaicpBotChannel, channelConfig: ChannelConfig) {
        logger.debug("Register channel ${channelConfig.channelType}")
        dispatcher.registerPolling(channel, getChannelProxyUrl(channelConfig))
        channelMap[channelConfig.channel] = channel
    }

    override fun evict(channelConfig: ChannelConfig) {
        logger.debug("Eviction for polling connector is not supported yet.")
    }

    override fun getRunningChannels() = channelMap
}