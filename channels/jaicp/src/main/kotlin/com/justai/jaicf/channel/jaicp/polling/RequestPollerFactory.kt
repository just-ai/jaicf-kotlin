package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext


internal abstract class BaseRequestPoller : WithLogger {
    suspend fun getUpdates(): Flow<List<JaicpBotRequest>> = flow {
        while (coroutineContext.isActive) {
            try {
                emit(doPoll().also { logger.debug("Polled requests: $it") })
            } catch (ex: Exception) {
                delay(500)
            }
        }
    }.flowOn(Dispatchers.IO)

    protected abstract suspend fun doPoll(): List<JaicpBotRequest>
}

internal object RequestPollerFactory {

    fun getPoller(client: HttpClient, url: String, legacy: Boolean): BaseRequestPoller {
        return when (legacy) {
            true -> RequestPollerLegacy(client, url)
            false -> RequestPoller(client, url)
        }
    }
}