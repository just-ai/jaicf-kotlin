package com.justai.jaicf.channel.jaicp.webhook

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.jaicp.*
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.helpers.logging.WithLogger

class JaicpWebhookChannel(
    override val botApi: BotApi,
    override val projectId: String,
    override val url: String = DEFAULT_PROXY_URL,
    override val channels: List<JaicpChannelFactory>
) : WithLogger,
    HttpBotChannel,
    JaicpConnector {

    private val channelMap: MutableMap<String, HttpBotChannel> = mutableMapOf()

    init {
        channels.forEach { channelFactory ->
            when (channelFactory) {
                is JaicpCompatibleChannelFactory -> {
                    channelMap[channelFactory.channelType] = channelFactory.create(botApi)
                        .also { logger.info("JAICP-compatible channel has been created for $it") }
                }

                is JaicpNativeChannelFactory -> {
                    channelMap[channelFactory.channelType] = channelFactory.create(botApi)
                        .also { logger.info("JAICP-native channel has been created for $it") }
                }

                is JaicpCompatibleAsyncChannelFactory -> {
                    channelMap[channelFactory.channelType] = channelFactory.create(botApi, proxyUrl)
                        .also { logger.info("JAICP-compatible async channel has been created for $it") }
                }

                else -> logger.info("Channel type ${channelFactory.channelType} is not supported by JAICP webhook channel")
            }
        }
    }

    override fun process(input: String): String? {
        val request = input.asJaicpBotRequest()

        return when (val channel = channelMap[request.channelType]) {
            is JaicpNativeBotChannel -> {
                channel.process(request).deserialized()
            }
            is JaicpCompatibleBotChannel -> {
                channel.processCompatible(request).deserialized()
            }
            is JaicpCompatibleAsyncBotChannel -> {
                channel.process(request.rawRequest.toString())
            }
            else -> throw RuntimeException("Channel ${request.channelType} is not configured or not supported")
        }
    }

}
