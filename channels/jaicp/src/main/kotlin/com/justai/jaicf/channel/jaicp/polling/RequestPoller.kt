package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

internal class RequestPoller(
    private val client: HttpClient,
    private val url: String,
    private val executorContext: CoroutineContext,
) : WithLogger {

    private var since: Long = runBlocking {
        client.get("$url/getTimestamp").body<JsonObject>()["timestamp"]?.jsonPrimitive?.long
            ?: error("Failed to get last message timestamp")
    }

    private var unprocessed: Boolean = false
    private val isActive: AtomicBoolean = AtomicBoolean(false)

    suspend fun getUpdates(): Flow<List<JaicpBotRequest>> = flow {
        while (isActive.get()) {
            try {
                emit(doPoll())
            } catch (ex: Exception) {
                delay(2000)
            }
        }
    }.flowOn(executorContext)

    private suspend fun doPoll() = client.get("$url/getUpdates".toUrl()) {
        parameter("ts", since)
        parameter("unprocessed", unprocessed)
    }
        .body<List<JaicpBotRequest>>()
        .also {
            updateSince(it)
            unprocessed = true
        }

    fun stopPolling() {
        isActive.set(false)
    }

    fun startPolling() {
        isActive.set(true)
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