package com.justai.jaicf.gateway

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*

typealias RouteToGatewayChannel = Pair<String, BotGateway>

/**
 * A helper extensions for Ktor framework with [BotGateway] routing.
 *
 * Usage example:
 * ```
 * embeddedServer(Netty, 8000) {
 *   routing {
 *     httpBotRouting(
 *       "/gw/telegram" to telegramChannel,
 *       "/gw/facebook" to facebookChannel
 *     )
 *   }
 * }.start(wait = true)
 * ```
 *
 * example requests:
 * curl -X POST {host}/gw/telegram/{clientId}?event=gatewayEvent -d '{"key": "value"}'
 *
 * @see BotGateway
 * @see BotGatewayRequest
 * @see BotGatewayRequestType
 */
fun Routing.botGatewayRouting(vararg routes: RouteToGatewayChannel) {
    routes.forEach { channel ->
        post("${channel.first}/{clientId}") {
            val clientId = requireNotNull(call.parameters["clientId"]) {
                "clientId path variable must be specified for gateway call"
            }
            val requestData = call.receiveText()
            val queryParameters = BotGatewayQueryParams(call.request.queryParameters.toMap())
            if (!queryParameters.isValid()) {
                error("event or text must be specified in query parameters")
            }
            processGatewayRequest(
                clientId = clientId,
                input = queryParameters.getInput(),
                type = queryParameters.getType(),
                requestData = requestData,
                bot = channel.second
            )
        }
    }
}

/**
 * Processes request from [botGatewayRouting].
 *
 * @param clientId a recipient, chat or channel identifier from a concrete channel implementation.
 * @param input text or event sent via gateway.
 * @param type a [BotGatewayRequestType] of [BotGatewayRequest].
 * @param requestData a stringified data sent with request.
 * @param bot a [BotGateway] implementation to process request.
 * */
private fun processGatewayRequest(
    clientId: String,
    input: String,
    type: BotGatewayRequestType,
    requestData: String,
    bot: BotGateway
) = bot.processGatewayRequest(
    request = when (type) {
        BotGatewayRequestType.EVENT -> BotGatewayEventRequest(clientId, input, requestData)
        BotGatewayRequestType.TEXT -> BotGatewayQueryRequest(clientId, input, requestData)
    }
)


private class BotGatewayQueryParams(queryParamsMap: Map<String, List<String>>) {
    val event: String? = queryParamsMap["event"]?.firstOrNull()
    val text: String? = queryParamsMap["text"]?.firstOrNull()

    fun isValid() = event != null || text != null

    fun getType(): BotGatewayRequestType {
        if (event != null) return BotGatewayRequestType.EVENT
        if (text != null) return BotGatewayRequestType.TEXT
        error("event or text must be specified in query parameters")
    }

    fun getInput() = when (getType()) {
        BotGatewayRequestType.EVENT -> event!!
        BotGatewayRequestType.TEXT -> text!!
    }
}

private enum class BotGatewayRequestType {
    EVENT, TEXT;
}