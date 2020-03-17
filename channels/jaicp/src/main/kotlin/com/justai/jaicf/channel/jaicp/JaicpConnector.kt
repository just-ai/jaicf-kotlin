package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi

const val DEFAULT_PROXY_URL = "https://jaicf01-demo-htz.lab.just-ai.com/chatadapter"

interface JaicpConnector {
    val botApi: BotApi
    val channels: List<JaicpChannelFactory>
    val projectId: String
    val url: String

}

val JaicpConnector.proxyUrl: String
    get() = "$url/proxy/$projectId"