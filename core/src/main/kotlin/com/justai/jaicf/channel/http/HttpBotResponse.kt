package com.justai.jaicf.channel.http

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Contains details of the HTTP responce returned by [HttpBotChannel]
 *
 * @property output stream that contains a response data
 * @property contentType the type of response ("application/json" for example)
 */
data class HttpBotResponse(
    val output: ByteArrayOutputStream,
    val contentType: ContentType,
    val statusCode: Int = HttpStatusCode.OK
) {
    val headers = mutableMapOf<String, String>()

    constructor(
        text: String,
        contentType: ContentType,
        charset: Charset = StandardCharsets.UTF_8,
        statusCode: Int = HttpStatusCode.OK
    ) : this(
        output = ByteArrayOutputStream(text.length).apply { write(text.toByteArray(charset)) },
        contentType = contentType,
        statusCode = statusCode
    )

    fun isSuccess() = statusCode in (200 until 300)

    companion object {
        fun ok(text: String = "") =
            HttpBotResponse(text, ContentType.PlainText, statusCode = HttpStatusCode.OK)

        fun accepted(text: String = "") =
            HttpBotResponse(text, ContentType.PlainText, statusCode = HttpStatusCode.ACCEPTED)

        fun forbidden(text: String = "") =
            HttpBotResponse(text, ContentType.PlainText, statusCode = HttpStatusCode.FORBIDDEN)

        fun notFound(text: String = "") =
            HttpBotResponse(text, ContentType.PlainText, statusCode = HttpStatusCode.NOT_FOUND)

        fun error(text: String = "") =
            HttpBotResponse(text, ContentType.PlainText, statusCode = HttpStatusCode.INTERNAL_SERVER_ERROR)
    }
}

fun String.asJsonHttpBotResponse(statusCode: Int = HttpStatusCode.OK) =
    HttpBotResponse(this, ContentType.Json, statusCode = statusCode)

fun String.asTextHttpBotResponse(statusCode: Int = HttpStatusCode.OK) =
    HttpBotResponse(this, ContentType.PlainText, statusCode = statusCode)

class ContentType private constructor(val value: String) {
    companion object {
        val Json = ContentType("application/json")
        val PlainText = ContentType("text/plain")

        fun parse(value: String) = ContentType(value)
    }
}

object HttpStatusCode {
    const val OK = 200
    const val ACCEPTED = 202
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val INTERNAL_SERVER_ERROR = 500
}
