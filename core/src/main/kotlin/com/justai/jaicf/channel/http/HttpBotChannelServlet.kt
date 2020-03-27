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
            val request = HttpBotRequest(
                stream = req.inputStream,
                headers = req.headerNames.asSequence().map { it to listOf(req.getHeader(it)) }.toMap(),
                parameters = req.parameterMap.mapValues { it.value.toList() }
            )

            logger.info("{} received request {}", channel, request)

            val response = channel.process(request)
            logger.info("{} responded with {}", channel, response)

            when (response) {
                null -> resp?.sendError(HttpServletResponse.SC_NOT_FOUND, "Bot didn't respond")
                else -> resp?.run {
                    status = HttpServletResponse.SC_OK

                    contentType = response.contentType
                    response.headers.forEach { addHeader(it.key, it.value) }
                    response.output.writeTo(outputStream)
                    outputStream.flush()
                }
            }
        }
    }
}