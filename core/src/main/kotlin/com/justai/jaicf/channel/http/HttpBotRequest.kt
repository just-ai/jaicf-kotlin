package com.justai.jaicf.channel.http

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset

/**
 * Contains details of the HTTP request to the corresponding [HttpBotChannel]
 *
 * @property stream input stream containing a request data
 * @property headers request HTTP headers
 * @property parameters HTTP query parameters
 * @property requestMetadata optional metadata for request processing
 */
data class HttpBotRequest(
    val stream: InputStream,
    val headers: Map<String, List<String>> = mapOf(),
    val parameters: Map<String, List<String>> = mapOf(),
    val requestMetadata: String? = null
) {

    fun receiveText(charset: Charset = Charset.forName("UTF-8")) = stream.bufferedReader(charset).readText()

    fun firstHeader(name: String) = headers[name]?.first()

    fun firstParameter(name: String) = parameters[name]?.first()
}

fun String.asHttpBotRequest() = HttpBotRequest(ByteArrayInputStream(this.toByteArray()))