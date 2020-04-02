package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.polling.Dispatcher
import com.justai.jaicf.helpers.logging.WithLogger

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
class JaicpPollingConnector(
    override val botApi: BotApi,
    override val accessToken: String,
    override val url: String = DEFAULT_PROXY_URL,
    override val channels: List<JaicpChannelFactory>
) : JaicpConnector,
    WithLogger {

    private val dispatcher = Dispatcher(proxyUrl)

    fun runBlocking() {
        channels.forEach {
            when (it) {
                is JaicpExternalPollingChannelFactory -> {
                    dispatcher.runChannel(it, botApi)
                }
                is JaicpCompatibleChannelFactory -> {
                    dispatcher.registerPolling(it, it.create(botApi))
                }
                is JaicpNativeChannelFactory -> {
                    dispatcher.registerPolling(it, it.create(botApi))
                }
                is JaicpCompatibleAsyncChannelFactory -> {
                    dispatcher.registerPolling(it, it.create(botApi, "$proxyUrl/${it.channelType}"))
                }
            }
        }

        dispatcher.startPollingBlocking()
    }
}