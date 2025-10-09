package com.justai.jaicf.examples.viber

import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.viber.ViberBotConfig
import com.justai.jaicf.channel.viber.ViberChannel
import com.justai.jaicf.channel.viber.ViberChannelConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.routing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

suspend fun main() {
    val authToken =
        System.getenv("VIBER_AUTH_TOKEN")
            ?: print("Enter your Viber auth token: ").run { readLine() }
            ?: throw IllegalArgumentException()

    val viber = ViberChannel(
        viberTestBot,
        ViberBotConfig(
            botName = "<your_bot_name>",
            authToken = authToken
        ),
        channelConfig = ViberChannelConfig(
            ignoreSeenEvents = false,
            ignoreDeliveredEvents = false
        )
    )

    val server = GlobalScope.async {
        val server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> = embeddedServer(Netty, 8000) {
            routing {
                httpBotRouting("/" to viber)
            }
        }
        server.start(wait = true)
    }

    viber.initWebhook("<your_webhook_url>") // Enter your url
    server.await()
}
