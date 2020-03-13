package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.polling.Dispatcher


class JaicpPollingConnector(
    override val botApi: BotApi,
    val projectId: String,
    url: String = DEFAULT_URL,
    override val channels: List<JaicpChannelFactory>
) : JaicpConnector {

    private val dispatcher =
        Dispatcher("$url/polling/$projectId")

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
            }
        }

        dispatcher.startPollingBlocking()
    }

    companion object {
        private const val DEFAULT_URL = "http://jaicf01-demo-htz.lab.just-ai.com/chatadapter"
    }
}