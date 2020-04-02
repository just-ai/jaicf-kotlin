package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi

const val DEFAULT_PROXY_URL = "https://jaicf01-demo-htz.lab.just-ai.com/chatadapter"

/**
 * Basic interface for JAICP Connectors
 *
 * Channels to work with JAICP Connectors must implement [JaicpBotChannel] interface.
 * Asynchronous responses are supported for both [JaicpPollingConnector] and [JaicpWebhookConnector].
 * @see JaicpWebhookConnector
 * @see JaicpPollingConnector
 *
 * @param botApi the [BotApi] implementation used to process the requests for all channels
 * @param channels is a list of channels which will be managed by connector
 * @param accessToken can be configured in JAICP Web Interface
 *
 * */
interface JaicpConnector {
    val botApi: BotApi
    val channels: List<JaicpChannelFactory>
    val accessToken: String
    val url: String

}

val JaicpConnector.proxyUrl: String
    get() = "$url/proxy/$accessToken"