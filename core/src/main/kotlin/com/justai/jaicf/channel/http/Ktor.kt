package com.justai.jaicf.channel.http

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import java.nio.charset.Charset

/**
 * A helper extension for Ktor framework.
 * Routes every POST request from the specified context to the related [HttpBotChannel] implementation.
 *
 * Usage example:
 *
 * ```
 * embeddedServer(Netty, 8000) {
 *   routing {
 *     httpBotRouting(
 *       "/alexa" to alexaChannel,
 *       "/facebook" to facebookChannel
 *     )
 *   }
 * }.start(wait = true)
 * ```
 */
fun Routing.httpBotRouting(vararg channels: Pair<String, HttpBotChannel>) {
    channels.forEach { channel ->
        val contentType = ContentType.parse(channel.second.contentType)

        post(channel.first) {
            val input = call.receiveText(Charsets.UTF_8)
            val output = channel.second.process(input)
            when {
                output == null -> call.respond(HttpStatusCode.NotFound, "Bot didn't respond")
                output.isNotEmpty() -> call.respondText(output, contentType)
                else -> call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private suspend fun ApplicationCall.receiveText(charset: Charset)
        = receiveStream().bufferedReader(charset).readText()