package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.http.HttpBotChannelServlet
import com.justai.jaicf.channel.jaicp.endpoints.CHANNEL_CHECK_URL
import com.justai.jaicf.channel.jaicp.endpoints.HEALTH_CHECK_URL
import com.justai.jaicf.channel.jaicp.endpoints.RELOAD_CONFIGS_URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * HttpServlet implementation that processes POST through the [HttpBotChannel] and creates service endpoints for JAICP.
 *
 * @param connector an [JaicpWebhookConnector]
 * @see HttpBotChannel
 */
open class JaicpServlet(private val connector: JaicpWebhookConnector) : HttpBotChannelServlet(connector) {

    override fun doPut(req: HttpServletRequest?, resp: HttpServletResponse?) {
        if (req?.requestURI?.endsWith(RELOAD_CONFIGS_URL) == true) {
            connector.reload()
            resp?.ok()
        }
    }

    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse) {
        if (req?.requestURI?.contains(CHANNEL_CHECK_URL) == true) {
            val channelId = req.requestURI.substringAfter("$CHANNEL_CHECK_URL/").split("/").firstOrNull()
            if (connector.getRunningChannels().containsKey(channelId))
                resp.ok()
            else
                resp.notFound("Channel $channelId is not configured.")
        }
        if (req?.requestURI?.endsWith(HEALTH_CHECK_URL) == true) {
            resp.ok()
        }
    }

    private fun HttpServletResponse.ok() {
        setStatus(HttpServletResponse.SC_OK)
        writer.write("OK")
        writer.flush()
    }

    private fun HttpServletResponse.notFound(message: String) {
        setStatus(HttpServletResponse.SC_NOT_FOUND)
        writer.write(message)
        writer.flush()
    }
}
