package com.justai.jaicf.channel.http

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            val request = withContext(Dispatchers.IO) {
                HttpBotRequest(
                    stream = call.receiveStream(),
                    headers = call.request.headers.toMap(),
                    parameters = call.request.queryParameters.toMap()
                )
            }

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