package com.justai.jaicf.channel.http

import com.justai.jaicf.helpers.logging.WithLogger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * HttpServlet implementation that processes POST requests through the [HttpBotChannel].
 *
 * @param channel an [HttpBotChannel] implementation
 * @see HttpBotChannel
 */
open class HttpBotChannelServlet(
    private val channel: HttpBotChannel
): HttpServlet(), WithLogger {

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        req?.run {
            val input = String(inputStream.readBytes())
            logger.info("{} received request {}", channel, input)

            input.takeIf { it.isNotEmpty() }?.let {
                val output = channel.process(input)
                logger.info("{} responded with {}", channel, output)

                when (output) {
                    null -> resp?.sendError(HttpServletResponse.SC_NOT_FOUND, "Bot didn't respond")
                    else -> resp?.run {
                        status = HttpServletResponse.SC_OK

                        if (output.isNotEmpty()) {
                            characterEncoding = "UTF-8"
                            contentType = channel.contentType
                            outputStream.write(output.toByteArray())
                            outputStream.flush()
                        }

                    }
                }

            }
        }
    }
}