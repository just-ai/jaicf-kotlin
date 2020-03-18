package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class RequestPoller(private val client: HttpClient) : WithLogger {
    fun getUpdates(url: String): Flow<String> = flow {
        while (coroutineContext.isActive) {
            try {
                client.get<HttpResponse>("$url/getUpdates".toUrl()).let { response ->
                    if (response.status == HttpStatusCode.OK) {
                        emit(response.receive<String>())
                    } else {
                        delay(500)
                    }
                }
            } catch (ex: Exception) {
                logger.warn("GetUpdates failed with exception: ", ex)
                delay(500)
            }
        }

    }
}