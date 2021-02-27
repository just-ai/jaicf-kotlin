package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.execution.ThreadPoolRequestExecutor
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient

const val DEFAULT_PROXY_URL = "https://bot.jaicp.com"

/**
 * Basic interface for JAICP Connectors
 *
 * Channels to work with JAICP Connectors must implement [JaicpBotChannel] interface.
 * Asynchronous responses are supported for both [JaicpPollingConnector] and [JaicpWebhookConnector].
 * @see JaicpWebhookConnector
 * @see JaicpPollingConnector
 *
 * @property botApi the [BotApi] implementation used to process the requests for all channels
 * @property channels is a list of channels which will be managed by connector
 * @property accessToken can be configured in JAICP Web Interface
 * */
abstract class JaicpConnector(
    val botApi: BotApi,
    val channels: List<JaicpChannelFactory>,
    val accessToken: String,
    val url: String,
    httpClient: HttpClient,
    executorThreadPoolSize: Int = DEFAULT_REQUEST_EXECUTOR_THREAD_POOL_SIZE
) : WithLogger {

    protected val threadPoolRequestExecutor = ThreadPoolRequestExecutor(executorThreadPoolSize)
    private val chatAdapterConnector = ChatAdapterConnector.getOrCreate(accessToken, url, httpClient)
    private var registeredChannels = fetchChannels()

    protected fun loadConfig() {
        registeredChannels.forEach { (factory, cfg) ->
            createChannel(factory, cfg)
        }
    }

    private fun createChannel(factory: JaicpChannelFactory, cfg: ChannelConfig) = when (factory) {
        is JaicpCompatibleChannelFactory -> registerInternal(factory.create(botApi), cfg)
        is JaicpNativeChannelFactory -> registerInternal(factory.create(botApi), cfg)
        is JaicpCompatibleAsyncChannelFactory -> registerInternal(factory.create(botApi, getChannelProxyUrl(cfg)), cfg)
        else -> logger.info("Channel type ${factory.channelType} is not added to list of channels in BotEngine")
    }

    protected fun reloadConfig() {
        val fetched = fetchChannels()
        val stillRegistered = registeredChannels.map { it.second.channel }.intersect(fetched.map { it.second.channel })

        registeredChannels.filter { it.second.channel !in stillRegistered }.forEach {
            evict(it.second)
        }
        fetched.filter { it.second.channel !in stillRegistered }.forEach {
            createChannel(it.first, it.second)
        }

        registeredChannels = fetched
    }

    private fun fetchChannels(): List<Pair<JaicpChannelFactory, ChannelConfig>> {
        val registeredChannels: List<ChannelConfig> = chatAdapterConnector.listChannels()
        logger.info("Retrieved ${registeredChannels.size} channels configuration")

        return channels.flatMap { factory ->
            registeredChannels.mapNotNull {
                if (factory.channelType.equals(it.channelType, ignoreCase = true)) {
                    factory to it
                } else
                    null
            }
        }
    }

    private fun registerInternal(channel: JaicpBotChannel, channelConfig: ChannelConfig) {
        if (channel is JaicpCompatibleChannelWithApiClient) {
            channel.configureApiUrl("${getApiProxyUrl(channelConfig)}/proxyApiCall")
        }
        register(channel, channelConfig)
    }

    abstract fun register(channel: JaicpBotChannel, channelConfig: ChannelConfig)

    abstract fun evict(channelConfig: ChannelConfig)

    protected fun getChannelProxyUrl(config: ChannelConfig) =
        "$proxyUrl/${config.channel}/${config.channelType.toLowerCase()}".toUrl()

    private fun getApiProxyUrl(config: ChannelConfig) =
        "$apiProxyUrl/${config.channel}/${config.channelType.toLowerCase()}".toUrl()

    protected open fun processJaicpRequest(request: JaicpBotRequest, channel: JaicpBotChannel): JaicpBotResponse? =
        threadPoolRequestExecutor.executeSync(request, channel)

    companion object {
        const val PING_REQUEST_TYPE = "ping"
    }
}

internal const val DEFAULT_REQUEST_EXECUTOR_THREAD_POOL_SIZE = 5

val JaicpConnector.apiProxyUrl: String
    get() = "$url/api-proxy/$accessToken"

val JaicpConnector.proxyUrl: String
    get() = "$url/proxy/$accessToken"