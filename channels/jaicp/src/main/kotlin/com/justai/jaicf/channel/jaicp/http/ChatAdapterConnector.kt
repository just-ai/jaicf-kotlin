package com.justai.jaicf.channel.jaicp.http

import com.justai.jaicf.channel.jaicp.DEFAULT_PROXY_URL
import com.justai.jaicf.channel.jaicp.dto.ChannelConfig
import com.justai.jaicf.channel.jaicp.dto.JaicpLogModel
import com.justai.jaicf.channel.jaicp.livechat.LiveChatInitRequest
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.annotations.TestOnly

internal class ChatAdapterConnector private constructor(
    val accessToken: String,
    val url: String = DEFAULT_PROXY_URL,
    private val httpClient: HttpClient
) : WithLogger {

    private val baseUrl = "$url/restapi/external-bot/$accessToken"
    private val versionUrl = "$url/version"

    fun listChannels(): List<ChannelConfig> = runBlocking {
        try {
            httpClient.get("$baseUrl/channels")
        } catch (e: ClientRequestException) {
            throw error("Invalid access token: $e")
        }
    }

    fun getVersion() = runBlocking { httpClient.get<JsonObject>(versionUrl)["buildBranch"]?.jsonPrimitive?.content }

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

    fun initLiveChat(liveChatInitRequest: LiveChatInitRequest) = runBlocking {
        httpClient.post<String>("$baseUrl/initLiveChatSwitch") {
            contentType(ContentType.Application.Json)
            body = liveChatInitRequest
        }
    }

    companion object {
        private var INSTANCE: ChatAdapterConnector? = null

        @Synchronized
        fun getOrCreate(accessToken: String, url: String, httpClient: HttpClient) = INSTANCE
            ?.apply {
                if (this.accessToken != accessToken || this.url != url)
                    error("Multiple connector instances detected. Make sure your JaicpConnection and JaicpConversationLogger are pointed to same host with same access token.")
            } ?: ChatAdapterConnector(accessToken, url, httpClient).also { INSTANCE = it }

        fun getIfExists() = INSTANCE

        @TestOnly
        fun removeInstance() {
            INSTANCE = null
        }
    }
}