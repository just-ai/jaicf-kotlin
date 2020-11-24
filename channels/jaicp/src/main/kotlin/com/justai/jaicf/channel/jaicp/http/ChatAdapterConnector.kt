package com.justai.jaicf.channel.jaicp.http

import com.justai.jaicf.channel.jaicp.DEFAULT_PROXY_URL
import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.channel.jaicp.dto.JaicpLogModel
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking


internal class ChatAdapterConnector(
    accessToken: String,
    url: String = DEFAULT_PROXY_URL,
    private val httpClient: HttpClient
) : WithLogger {

    private val baseUrl = "$url/restapi/external-bot/$accessToken"

    fun listChannels(): List<ChannelConfig> = runBlocking {
        try {
            httpClient.get<List<ChannelConfig>>("$baseUrl/channels")
        } catch (e: ClientRequestException) {
            throw error("Invalid access token: $e")
        }
    }

    suspend fun processLogAsync(logModel: JaicpLogModel) {
        try {
            httpClient.post<String>("$baseUrl/processLogs") {
                contentType(ContentType.Application.Json)
                body = logModel
            }
        } catch (e: Exception) {
            logger.debug("Failed to produce logs to JAICP", e)
        }
    }
}