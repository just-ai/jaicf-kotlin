package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.http.HttpBotChannelServlet
import com.justai.jaicf.channel.jaicp.endpoints.CHANNEL_CHECK_URL
import com.justai.jaicf.channel.jaicp.endpoints.HEALTH_CHECK_URL
import com.justai.jaicf.channel.jaicp.endpoints.RELOAD_CONFIGS_URL
import com.justai.jaicf.helpers.logging.WithLogger
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * HttpServlet implementation that processes POST through the [HttpBotChannel] and creates service endpoints for JAICP.
 *
 * @param connector an [JaicpWebhookConnector]
 * @see HttpBotChannel
 */
open class JaicpServlet(
    private val connector: JaicpWebhookConnector
) : HttpBotChannelServlet(connector), WithLogger {

    override fun doPut(req: HttpServletRequest?, resp: HttpServletResponse?) {
        if (req?.requestURI == RELOAD_CONFIGS_URL) {
            connector.reload()
            ok(resp)
        }
    }

    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        if (req?.requestURI?.startsWith(CHANNEL_CHECK_URL) == true) {
            val channelId = req.requestURI.removePrefix("$CHANNEL_CHECK_URL/").split("/").firstOrNull()
            if (connector.getRunningChannels().contains(channelId)) ok(resp)
            else notFound(resp)
        }
        if (req?.requestURI == HEALTH_CHECK_URL) {
            connector.getRunningChannels()
            ok(resp)
        }
    }

    private fun ok(resp: HttpServletResponse?) = resp?.run {
        status = HttpServletResponse.SC_OK
        resp.writer.write("OK")
        resp.writer.flush()
    }

    private fun notFound(resp: HttpServletResponse?) = resp?.run {
        status = HttpServletResponse.SC_NOT_FOUND
        resp.writer.write("NOT_FOUND")
        resp.writer.flush()
    }
}