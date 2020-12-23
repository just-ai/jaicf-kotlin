package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

internal class RequestPoller(
    private val client: HttpClient,
    private val url: String
) : WithLogger, BaseRequestPoller() {

    private var since: Long = runBlocking {
        client.get<JsonObject>("$url/getTimestamp")["timestamp"]?.jsonPrimitive?.long
            ?: error("Failed to get last message timestamp")
    }
    private var unprocessed: Boolean = false

    override suspend fun doPoll() = client.get<List<JaicpBotRequest>>("$url/getUpdates".toUrl()) {
        parameter("ts", since)
        parameter("unprocessed", unprocessed)
    }.also {
        updateSince(it)
        unprocessed = true
    }

    private fun updateSince(requests: List<JaicpBotRequest>) {
        since = requests
            .maxByOrNull { it.timestamp.asSerializedOffsetDateTime() }?.timestamp?.asSerializedOffsetDateTime()
            ?: since
    }
}

private fun String.asSerializedOffsetDateTime(): Long {
    val (sec, nanos) = split(".")
    return (sec + nanos.subSequence(0, 3)).toLong()
}