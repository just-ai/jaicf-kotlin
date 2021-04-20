package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.aimybox.AimyboxChannel
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.examples.helloworld.helloWorldBot
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, 7000) {
        routing {
            httpBotRouting("/" to AimyboxChannel(helloWorldBot))
        }
    }.start(wait = true)
}
