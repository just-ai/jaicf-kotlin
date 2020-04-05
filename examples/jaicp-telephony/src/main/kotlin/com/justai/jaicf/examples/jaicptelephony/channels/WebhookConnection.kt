package com.justai.jaicf.examples.jaicptelephony.channels

import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.jaicp.JaicpWebhookConnector
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.examples.jaicptelephony.accessToken
import com.justai.jaicf.examples.jaicptelephony.citiesGameBot
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/" to JaicpWebhookConnector(
                    botApi = citiesGameBot,
                    accessToken = accessToken,
                    channels = listOf(TelephonyChannel)
                )
            )
        }
    }.start(wait = true)
}
