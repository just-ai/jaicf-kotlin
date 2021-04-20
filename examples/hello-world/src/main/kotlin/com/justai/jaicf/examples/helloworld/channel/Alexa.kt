package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.alexa.AlexaChannel
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.examples.helloworld.helloWorldBot
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, 9000) {
        routing {
            httpBotRouting("/" to AlexaChannel(helloWorldBot))
        }
    }.start(wait = true)
}
