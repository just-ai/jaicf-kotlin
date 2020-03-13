package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.facebook.FacebookChannel
import com.justai.jaicf.channel.facebook.FacebookPageConfig
import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.examples.helloworld.helloWorldBot
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val channel = FacebookChannel(
        helloWorldBot,
        FacebookPageConfig(
            pageAccessToken = "EAAIBNxZCCzjoBADyorVAY21KniOikUxVYjhmnZBElHpeN1vr9lEJzXJdLGUsvcvwTRMmNwwZBZBDEZCBPXlZB0UuwU1o3CZCdm0WJILg1ucoNB9ezKeZBbOvy29prWeZAuLA4L5G9lg5yZBZCfwnLAPEZB9W3YLvO20uZBCfHtARowF8PPG2VKk6YAmPZC",
            appSecret = "11deaea42beda58ddfef1b1eeab57338",
            verifyToken = "jaicf-verify-token"
        )
    )

    embeddedServer(Netty, 8000) {
        routing {

            httpBotRouting("/" to channel)

            get("/") {
                call.respondText(
                    channel.verifyToken(
                        call.parameters["hub.mode"],
                        call.parameters["hub.verify_token"],
                        call.parameters["hub.challenge"]
                    )
                )
            }
        }
    }.start(wait = true)
}