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
class HttpBotRequest(
    stream: InputStream,
    val headers: Map<String, List<String>> = mapOf(),
    val parameters: Map<String, List<String>> = mapOf(),
    val requestMetadata: String? = null
) {
    val stream = stream.buffered()

    fun receiveText(charset: Charset = Charset.forName("UTF-8")) = stream.runAndReset { bufferedReader(charset).readText() }

    fun firstHeader(name: String) = headers[name]?.first()

    fun firstParameter(name: String) = parameters[name]?.first()
}

fun <R> InputStream.runAndReset(action: InputStream.() -> R): R {
    mark(0)
    return action().also { reset() }
}

fun String.asHttpBotRequest(requestMetadata: String? = null) = HttpBotRequest(
    ByteArrayInputStream(this.toByteArray()),
    requestMetadata = requestMetadata
)