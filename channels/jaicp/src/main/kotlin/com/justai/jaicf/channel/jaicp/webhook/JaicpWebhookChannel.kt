package com.justai.jaicf.channel.jaicp.webhook

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.helpers.logging.WithLogger

class JaicpWebhookChannel(
    override val botApi: BotApi,
    channels: List<JaicpChannelFactory>
) : WithLogger, HttpBotChannel {

    private val channelMap: MutableMap<String, HttpBotChannel> = mutableMapOf()
    private fun parseRequest(input: String) = JSON.parse(
        JaicpBotRequest.serializer(), input
    )

    init {
        channels.forEach { channelFactory ->
            when (channelFactory) {
                is JaicpCompatibleChannelFactory -> channelMap[channelFactory.channelType] = channelFactory.create(botApi)
                    .also { logger.info("JAICP-compatible channel has been created for $it") }

                is JaicpNativeChannelFactory -> channelMap[channelFactory.channelType] = channelFactory.create(botApi)
                    .also { logger.info("JAICP-native channel has been created for $it") }

                else -> logger.info("Channel type ${channelFactory.channelType} is not supported by JAICP webhook channel")
            }
        }
    }

    override fun process(input: String): String? {
        val request = parseRequest(input)
        return channelMap[request.channelType]?.process(input)
    }
}
