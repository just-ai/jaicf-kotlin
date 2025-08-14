package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.viber.ViberBotConfig
import com.justai.jaicf.channel.viber.ViberChannel
import com.justai.jaicf.examples.helloworld.helloWorldBot
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.coroutines.awaitCancellation

suspend fun main() {
    val authToken =
        System.getenv("VIBER_AUTH_TOKEN")
            ?: print("Enter your Viber auth token: ").run { readlnOrNull() }
            ?: error("Viber auth token is required")

    val viber = ViberChannel(
        helloWorldBot,
        ViberBotConfig(
            botName = "jaicf bot",
            authToken = authToken
        )
    )

    val engine = embeddedServer(Netty, port = 8000) {
        routing {
            httpBotRouting("/" to viber)
        }
    }

    engine.start(wait = false)


    viber.initWebhook("https://8d0c89a12176.ngrok.io") // Enter your url

    awaitCancellation()
}
