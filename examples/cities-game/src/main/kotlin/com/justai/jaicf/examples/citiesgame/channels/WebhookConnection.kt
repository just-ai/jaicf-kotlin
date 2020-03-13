package com.justai.jaicf.examples.citiesgame.channels

import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.examples.citiesgame.citiesGameBot
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.webhook.JaicpWebhookChannel
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {

    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/" to JaicpWebhookChannel(
                    botApi = citiesGameBot,
                    channels = listOf(TelephonyChannel)
                )
            )
        }
    }.start(wait = true)
}
