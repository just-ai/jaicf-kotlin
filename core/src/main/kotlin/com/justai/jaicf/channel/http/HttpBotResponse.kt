package com.justai.jaicf.channel.http

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

data class HttpBotResponse(
    val output: ByteArrayOutputStream,
    val contentType: String
) {
    val headers: Map<String, String> = mutableMapOf()

    constructor(
        text: String,
        contentType: String,
        charset: Charset = StandardCharsets.UTF_8
    ): this(
        output = ByteArrayOutputStream(text.length).apply { write(text.toByteArray(charset)) },
        contentType = contentType
    )
}

fun String.asJsonHttpBotResponse() = HttpBotResponse(this, "application/json")
fun String.asTextHttpBotResponse() = HttpBotResponse(this, "text/plain")