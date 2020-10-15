package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.*
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.channel.jaicp.http.HttpClientFactory
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.features.logging.LogLevel


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
    botApi: BotApi,
    accessToken: String,
    url: String = DEFAULT_PROXY_URL,
    channels: List<JaicpChannelFactory>,
    logLevel: LogLevel = LogLevel.INFO,
    httpClient: HttpClient = null ?: HttpClientFactory.create(logLevel)
) : WithLogger,
    HttpBotChannel,
    JaicpConnector(botApi, channels, accessToken, url, httpClient) {

    private val channelMap: MutableMap<String, JaicpBotChannel> = mutableMapOf()

    init {
        super.registerChannels()
    }

    override fun registerChannel(channel: JaicpBotChannel, channelConfig: ChannelConfig) {
        channelMap[channelConfig.channel] = channel
    }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = request.receiveText()
            .also { logger.debug("Received botRequest: $it") }
            .asJaicpBotRequest()
            .also { JaicpMDC.setFromRequest(it) }

        return when (val channel = channelMap[botRequest.channelBotId]) {
            is JaicpNativeBotChannel -> channel.process(botRequest).deserialized().asJsonHttpBotResponse()
            is JaicpCompatibleBotChannel -> channel.processCompatible(botRequest).deserialized().asJsonHttpBotResponse()
            is JaicpCompatibleAsyncBotChannel -> channel.process(botRequest.raw.asHttpBotRequest(botRequest.stringify()))
            else -> throw RuntimeException("Channel ${botRequest.channelType} is not configured or not supported")
        }
    }
}
