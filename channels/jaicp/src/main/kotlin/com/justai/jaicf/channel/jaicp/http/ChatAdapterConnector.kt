package com.justai.jaicf.channel.jaicp.http

import com.justai.jaicf.channel.jaicp.DEFAULT_PROXY_URL
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.channel.jaicp.dto.JaicpLogModel
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorChannelConfiguredException
import com.justai.jaicf.channel.jaicp.livechat.exceptions.NoOperatorsOnlineException
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

internal class ChatAdapterConnector(
    val accessToken: String,
    val url: String = DEFAULT_PROXY_URL,
    private val httpClient: HttpClient
) : WithLogger, JaicpLiveChatProvider {

    private val baseUrl = "$url/restapi/external-bot/$accessToken"

    fun listChannels(): List<ChannelConfig> = runBlocking {
        try {
            httpClient.get("$baseUrl/channels").body()
        } catch (e: ClientRequestException) {
            throw error("Invalid access token: $e")
        }
    }

    suspend fun sendLogAsync(logModel: JaicpLogModel) {
        try {
            httpClient.post("$baseUrl/processLogs") {
                contentType(ContentType.Application.Json)
                setBody(logModel)
            }
        } catch (e: Exception) {
            logger.debug("Failed to produce logs to JAICP", e)
        }
    }

    fun initLiveChat(liveChatInitRequest: LiveChatInitRequest) {
        try {
            runBlocking {
                httpClient.post("$baseUrl/initLiveChatSwitch") {
                    setBody(liveChatInitRequest)
                    contentType(ContentType.Application.Json)
                }
            }
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.NotFound -> throw NoOperatorsOnlineException(liveChatInitRequest.request)
                HttpStatusCode.BadRequest -> throw NoOperatorChannelConfiguredException(liveChatInitRequest.request)
            }
        }
    }
}