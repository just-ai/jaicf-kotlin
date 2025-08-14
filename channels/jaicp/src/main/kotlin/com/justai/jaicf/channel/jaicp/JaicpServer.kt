package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeBotChannel
import com.justai.jaicf.channel.jaicp.endpoints.ktor.channelCheckEndpoint
import com.justai.jaicf.channel.jaicp.endpoints.ktor.healthCheckEndpoint
import com.justai.jaicf.channel.jaicp.endpoints.ktor.reloadConfigEndpoint
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.routing


/**
 * JAICP Server for incoming webhook connections. Provides required endpoints for JAICP connectivity.
 * Supported channels are [JaicpCompatibleBotChannel], [JaicpNativeBotChannel], [JaicpCompatibleAsyncBotChannel].
 *
 * Example usage:
 * ```
 * fun main() {
 *  JaicpServer(
 *      exampleBot,
 *      "<JAICP_PROJECT_TOKEN>",
 *      channels = listOf(
 *          ChatWidgetChannel,
 *          ChatApiChannel,
 *          TelephonyChannel
 *      )
 *  ).start()
 * }
 * ```
 *
 * @see JaicpNativeBotChannel
 * @see JaicpCompatibleBotChannel
 * @see JaicpCompatibleAsyncBotChannel
 * @see JaicpWebhookConnector
 *
 * @param botApi the [BotApi] implementation used to process the requests for all channels
 * @param accessToken can be configured in JAICP Web Interface
 * @param channels is a list of channels which will be managed by connector
 */
open class JaicpServer(
    botApi: BotApi,
    accessToken: String,
    channels: List<JaicpChannelFactory>,
    port: Int = System.getenv(PORT_PROP_NAME)?.toInt() ?: 8080,
    url: String = System.getenv(PROXY_PROP_NAME) ?: DEFAULT_PROXY_URL
) {
    open val connector = JaicpWebhookConnector(
        botApi = botApi,
        accessToken = accessToken,
        channels = channels,
        url = url
    )

    open val server = embeddedServer(Netty, port) {
        routing {
            httpBotRouting("/" to connector)
            healthCheckEndpoint(connector)
            channelCheckEndpoint(connector)
            reloadConfigEndpoint(connector)
        }
    }

    fun start(wait: Boolean = true) {
        server.start(wait)
    }

    companion object {
        private const val PROXY_PROP_NAME = "CA_URL"
        private const val PORT_PROP_NAME = "PORT"
    }
}