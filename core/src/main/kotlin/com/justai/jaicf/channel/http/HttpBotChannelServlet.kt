package com.justai.jaicf.channel.http

import com.justai.jaicf.helpers.logging.WithLogger
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * HttpServlet implementation that processes POST requests through the [HttpBotChannel].
 *
 * @param channel an [HttpBotChannel] implementation
 * @see HttpBotChannel
 */
@Suppress("unchecked_cast")
open class HttpBotChannelServlet(
    private val channel: HttpBotChannel
) : HttpServlet(), WithLogger {

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        req?.run {
            val request = HttpBotRequest(
                stream = req.inputStream,
                headers = req.headerNames.asSequence().map { it as String to listOf(req.getHeader(it)) }.toMap(),
                parameters = req.parameterMap.map { (k, v) ->
                    (k as String) to (v as Array<out String>).toList()
                }.toMap()
            )

            logger.info("{} received request {}", channel, request)

            val response = channel.process(request)
            logger.info("{} responded with {}", channel, response)

            resp?.run {
                setStatus(response.statusCode)

                contentType = response.contentType.value
                response.headers.forEach { addHeader(it.key, it.value) }
                response.output.writeTo(outputStream)
                outputStream.flush()
            }
        }
    }
}
