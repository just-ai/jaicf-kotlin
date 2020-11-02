package com.justai.jaicf.examples.gameclock.channel

import com.justai.jaicf.channel.alexa.AlexaChannel
import com.justai.jaicf.channel.googleactions.ActionsFulfillment
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.examples.gameclock.gameClockBot
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/alexa" to AlexaChannel(gameClockBot),
                "/actions" to ActionsFulfillment.dialogflow(gameClockBot)
            )
        }
    }.start(wait = true)
}