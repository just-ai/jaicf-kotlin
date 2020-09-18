package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.*
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.helpers.logging.WithLogger


/**
 * This class is used to process webhook requests from JAICP.
 *
 * Supported channels are [JaicpCompatibleBotChannel], [JaicpNativeBotChannel], [JaicpCompatibleAsyncBotChannel]
 *
 * See example at
 * examples/jaicp-telephony/src/main/kotlin/com/justai/jaicf/examples/jaicptelephony/channels/WebhookConnection.kt
 *
 * @see JaicpNativeBotChannel
 * @see JaicpCompatibleBotChannel
 * @see JaicpCompatibleAsyncBotChannel
 *
 * @param botApi the [BotApi] implementation used to process the requests for all channels
 * @param accessToken can be configured in JAICP Web Interface
 * @param channels is a list of channels which will be managed by connector
 * */
class JaicpWebhookConnector(
    override val botApi: BotApi,
    override val accessToken: String,
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

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = request.receiveText().asJaicpBotRequest()

        return when (val channel = channelMap[botRequest.channelType]) {
            is JaicpNativeBotChannel -> channel.process(botRequest).deserialized().asJsonHttpBotResponse()
            is JaicpCompatibleBotChannel -> channel.processCompatible(botRequest).deserialized().asJsonHttpBotResponse()
            is JaicpCompatibleAsyncBotChannel -> channel.process(botRequest.rawRequest.toString().asHttpBotRequest(request.receiveText()))
            else -> throw RuntimeException("Channel ${botRequest.channelType} is not configured or not supported")
        }
    }
}
