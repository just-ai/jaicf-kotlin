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
    val statusCode: HttpStatusCode = HttpStatusCode.OK
) {
    val headers = mutableMapOf<String, String>()

    constructor(
        text: String,
        contentType: ContentType,
        charset: Charset = StandardCharsets.UTF_8,
        statusCode: HttpStatusCode = HttpStatusCode.OK
    ) : this(
        output = ByteArrayOutputStream(text.length).apply { write(text.toByteArray(charset)) },
        contentType = contentType,
        statusCode = statusCode
    )

    companion object {
        fun accepted(text: String = "") =
            HttpBotResponse(text, ContentType.PLAIN_TEXT, statusCode = HttpStatusCode.ACCEPTED)

        fun forbidden(text: String = "") =
            HttpBotResponse(text, ContentType.PLAIN_TEXT, statusCode = HttpStatusCode.FORBIDDEN)

        fun notFound(text: String = "") =
            HttpBotResponse(text, ContentType.PLAIN_TEXT, statusCode = HttpStatusCode.NOT_FOUND)
    }
}

fun String.asJsonHttpBotResponse(statusCode: HttpStatusCode = HttpStatusCode.OK) =
    HttpBotResponse(this, ContentType.JSON, statusCode = statusCode)

fun String.asTextHttpBotResponse(statusCode: HttpStatusCode = HttpStatusCode.OK) =
    HttpBotResponse(this, ContentType.PLAIN_TEXT, statusCode = statusCode)

// TODO ???
enum class ContentType(val value: String) {
    JSON("application/json"),
    PLAIN_TEXT("text/plain")
}

enum class HttpStatusCode(val value: Int) {
    OK(200),
    ACCEPTED(202),
    FORBIDDEN(403),
    NOT_FOUND(404)
}

fun HttpStatusCode.isSuccess() =
    value in (200 until 300)
