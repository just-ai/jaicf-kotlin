package com.justai.jaicf.channel.viber.sdk

import com.fasterxml.jackson.module.kotlin.readValue
import com.justai.jaicf.channel.viber.sdk.event.IncomingEvent
import java.io.InputStream

class Request internal constructor(val event: IncomingEvent)

fun InputStream.asViberRequest() = this.use { bufferedReader().readText().asViberRequest() }
fun String.asViberRequest() = Request(viberObjectMapper.readValue(this))
