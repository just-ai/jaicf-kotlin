package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.coroutines.coroutineContext

internal class RequestPoller(
    private val client: HttpClient,
    private val url: String
) : WithLogger {

    private var since: Long = runBlocking {
        client.get<JsonObject>("$url/getTimestamp")["timestamp"]?.jsonPrimitive?.long
            ?: error("Failed to get last message timestamp")
    }

    private var unprocessed: Boolean = false

    suspend fun getUpdates(): Flow<List<JaicpBotRequest>> = flow {
        while (coroutineContext.isActive) {
            try {
                emit(doPoll())
            } catch (ex: Exception) {
                delay(500)
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun doPoll() = client.get<List<JaicpBotRequest>>("$url/getUpdates".toUrl()) {
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