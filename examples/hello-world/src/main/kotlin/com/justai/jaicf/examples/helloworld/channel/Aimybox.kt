package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.aimybox.AimyboxChannel
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.examples.helloworld.helloWorldBot
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 7000) {
        routing {
            httpBotRouting("/" to AimyboxChannel(helloWorldBot))
        }
    }.start(wait = true)
}