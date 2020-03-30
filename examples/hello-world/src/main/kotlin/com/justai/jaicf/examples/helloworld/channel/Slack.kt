package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.slack.SlackChannel
import com.justai.jaicf.channel.slack.SlackChannelConfig
import com.justai.jaicf.examples.helloworld.helloWorldBot
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 10000) {
        routing {
            httpBotRouting("/" to SlackChannel(
                helloWorldBot,
                SlackChannelConfig(
                    "xoxb-990923682577-1022265134163-qXd9kd23740jFAv9UsxIR4OI",
                    "32f68ac7e23db90398d4cc74002462e5"
                )
            ))
        }
    }.start(wait = true)
}