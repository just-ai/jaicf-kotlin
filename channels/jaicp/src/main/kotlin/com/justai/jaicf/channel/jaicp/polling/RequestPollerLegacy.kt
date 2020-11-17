package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingRequest
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.request.*

@Deprecated("Deprecated long poll api. Will be deleted with all references in further releases.")
internal class RequestPollerLegacy(
    private val client: HttpClient,
    private val url: String
) : WithLogger,
    AbstractRequestPoller() {

    override suspend fun doPoll(): List<JaicpBotRequest> {
        return try {
            return listOf(client.get<JaicpPollingRequest>("$url/getUpdates".toUrl()).request)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
