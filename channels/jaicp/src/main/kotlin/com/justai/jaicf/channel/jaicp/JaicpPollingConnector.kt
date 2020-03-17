package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.polling.Dispatcher
import com.justai.jaicf.helpers.logging.WithLogger


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
                else -> logger.warn(
                    "No channel will be created for ${it.channelType} as it's not supported by JaicpChannelFactory"
                )
            }
        }

        dispatcher.startPollingBlocking()
    }
}