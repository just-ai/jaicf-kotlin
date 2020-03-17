package com.justai.jaicf.examples.citiesgame.channels

import com.justai.jaicf.channel.facebook.FacebookChannel
import com.justai.jaicf.channel.googleactions.ActionsFulfillment
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.jaicp.webhook.JaicpWebhookChannel
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.citiesgame.citiesGameBot
import com.justai.jaicf.examples.citiesgame.projectId
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/" to JaicpWebhookChannel(
                    botApi = citiesGameBot,
                    projectId = projectId,
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
