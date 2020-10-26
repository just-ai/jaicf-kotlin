package com.justai.jaicf.channel.jaicp.endpoints.ktor

import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.jaicp.JaicpWebhookConnector
import com.justai.jaicf.channel.jaicp.endpoints.CHANNEL_CHECK_URL
import com.justai.jaicf.channel.jaicp.endpoints.HEALTH_CHECK_URL
import com.justai.jaicf.channel.jaicp.endpoints.RELOAD_CONFIGS_URL
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * Provides endpoint for JAICP to check if JAICF bot is able to process incoming [HttpBotRequest].
 *
 * @param connector [JaicpWebhookConnector] for processing incoming [HttpBotRequest]
 * */
fun Routing.healthCheckEndpoint(connector: JaicpWebhookConnector) {
    get(HEALTH_CHECK_URL) {
        connector.getRunningChannels()
        call.respond(HttpStatusCode.OK, "OK")
    }
}

/**
 * Provides endpoint for JAICP to check if JAICF bot is able to process [HttpBotRequest] for exact channel.
 *
 * @param connector [JaicpWebhookConnector] for processing incoming [HttpBotRequest]
 * */
fun Routing.channelCheckEndpoint(connector: JaicpWebhookConnector) {
    route(CHANNEL_CHECK_URL) {
        get("{channelId}") {
            val channelId = call.parameters["channelId"]
            if (connector.getRunningChannels().contains(channelId)) {
                call.respond(HttpStatusCode.OK, "OK")
            } else {
                call.respond(HttpStatusCode.NotFound, "Channel $channelId is not configured")
            }
        }
    }
}

/**
 * Provides endpoint for JAICP to reload channel configuration, create new channels and evict deleted channels.
 *
 * @param connector [JaicpWebhookConnector] for processing incoming [HttpBotRequest]
 * */
fun Routing.reloadConfigEndpoint(connector: JaicpWebhookConnector) {
    put(RELOAD_CONFIGS_URL) {
        connector.reload()
        call.respond(HttpStatusCode.OK, "OK")
    }
}