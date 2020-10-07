package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import org.slf4j.MDC

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
    httpClient: HttpClient
) : WithLogger {

    private val chatAdapterConnector = ChatAdapterConnector(accessToken, url, httpClient)
    private val registeredChannels = parseChannels()
    protected val accountId = registeredChannels.first().second.accountId.also {
        MDC.put("accountId", it)
    }

    protected fun registerChannels() {
        registeredChannels.forEach { (factory, cfg) ->
            when (factory) {
                is JaicpCompatibleChannelFactory -> {
                    registerChannel(factory.create(botApi), cfg)
                        .also { logger.info("JAICP-compatible channel has been created for ${factory.channelType}") }
                }

                is JaicpNativeChannelFactory -> {
                    registerChannel(factory.create(botApi), cfg)
                        .also { logger.info("JAICP-native channel has been created for ${factory.channelType}") }
                }

                is JaicpCompatibleAsyncChannelFactory -> {
                    registerChannel(factory.create(botApi, getChannelProxyUrl(cfg)), cfg)
                        .also { logger.info("JAICP-compatible async channel has been created for ${factory.channelType}") }
                }
                else -> logger.info("Channel type ${factory.channelType} is not added to list of channels in BotEngine")
            }
        }
    }

    private fun parseChannels(): List<Pair<JaicpChannelFactory, ChannelConfig>> {
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

    abstract fun registerChannel(channel: JaicpBotChannel, channelConfig: ChannelConfig)

    protected fun getChannelProxyUrl(config: ChannelConfig) =
        "$proxyUrl/${config.channel}/${config.channelType.toLowerCase()}".toUrl()
}

val JaicpConnector.proxyUrl: String
    get() = "$url/proxy"