package com.justai.jaicf.channel.http

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondOutputStream
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.util.toMap

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
 *
 * @see HttpBotChannel
 * @see HttpBotRequest
 */
fun Routing.httpBotRouting(vararg channels: Pair<String, HttpBotChannel>) {
    channels.forEach { channel ->
        post(channel.first) {
            val request = HttpBotRequest(
                stream = call.receiveStream(),
                headers = call.request.headers.toMap(),
                parameters = call.request.queryParameters.toMap()
            )

            val response = channel.second.process(request)
            response?.headers?.forEach { call.response.headers.append(it.key, it.value, false) }

            when (response) {
                null -> call.respond(HttpStatusCode.NotFound, "Bot didn't respond")
                else -> call.respondOutputStream(ContentType.parse(response.contentType)) {
                    response.output.writeTo(this)
                }
            }
        }
    }
}