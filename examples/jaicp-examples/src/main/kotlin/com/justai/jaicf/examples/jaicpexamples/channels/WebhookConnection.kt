package com.justai.jaicf.examples.jaicpexamples.channels

import com.justai.jaicf.channel.facebook.FacebookChannel
import com.justai.jaicf.channel.googleactions.ActionsFulfillment
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.jaicp.webhook.JaicpWebhookChannel
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.jaicpexamples.citiesGameBot
import com.justai.jaicf.examples.jaicpexamples.accessToken
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/" to JaicpWebhookChannel(
                    botApi = citiesGameBot,
                    accessToken = accessToken,
                    channels = listOf(
                        TelegramChannel,
                        FacebookChannel,
                        ActionsFulfillment.ActionsFulfillmentSDK
                    )
                )
            )
        }
    }.start(wait = true)
}
