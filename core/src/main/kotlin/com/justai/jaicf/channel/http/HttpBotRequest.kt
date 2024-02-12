package com.justai.jaicf.channel.http

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset

/**
 * Contains details of the HTTP request to the corresponding [HttpBotChannel]
 *
 * @param stream input stream containing a request data
 * @property headers request HTTP headers
 * @property parameters HTTP query parameters
 * @property requestMetadata optional metadata for request processing
 */
class HttpBotRequest(
    stream: InputStream,
    val headers: Map<String, List<String>> = mapOf(),
    val parameters: Map<String, List<String>> = mapOf(),
    val requestMetadata: String? = null
) {
    private val streamBody: ByteArray

    init {
        streamBody = stream.readBytes()
    }

    fun receiveText(charset: Charset = Charset.forName("UTF-8")) = streamBody.toString(charset)

    fun firstHeader(name: String) = headers[name]?.first()

    fun firstParameter(name: String) = parameters[name]?.first()

    override fun toString() =
        "HttpBotRequest(streamBody=$streamBody, headers=$headers, parameters=$parameters, requestMetadata=$requestMetadata"
}

fun String.asHttpBotRequest(requestMetadata: String? = null) = HttpBotRequest(
    ByteArrayInputStream(this.toByteArray()),
    requestMetadata = requestMetadata
)