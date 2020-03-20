package com.justai.jaicf.examples.alice

import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.yandexalice.AliceChannel
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting("/" to AliceChannel(skill, "place your oauth token"))
        }
    }.start(wait = true)
}