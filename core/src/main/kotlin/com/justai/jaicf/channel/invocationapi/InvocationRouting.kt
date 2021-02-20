package com.justai.jaicf.channel.invocationapi

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*

typealias RouteToInvocableChannel = Pair<String, InvocableBotChannel>

/**
 * A helper extensions for Ktor framework with [InvocableBotChannel] routing.
 *
 * Usage example:
 * ```
 * embeddedServer(Netty, 8000) {
 *   routing {
 *     botInvocationRouting(
 *       "/invocation/telegram" to telegramChannel,
 *       "/invocation/facebook" to facebookChannel
 *     )
 *   }
 * }.start(wait = true)
 * ```
 *
 * example requests:
 * curl -X POST {host}/invocation/telegram?clientId={clientId}&event=myEvent -d '{"key": "value"}'
 *
 * @see InvocableBotChannel
 * @see InvocationRequest
 * @see InvocationRequestType
 */


fun Routing.botInvocationRouting(vararg routes: RouteToInvocableChannel) {
    routes.forEach { channel ->
        post(channel.first) {
            channel.second.processInvocation(call)
        }

        get(channel.first) {
            channel.second.processInvocation(call)
        }
    }
}

private suspend fun InvocableBotChannel.processInvocation(call: ApplicationCall) =
    processInvocation(queryParams = InvocationQueryParams(call.request), requestData = call.receiveText())


