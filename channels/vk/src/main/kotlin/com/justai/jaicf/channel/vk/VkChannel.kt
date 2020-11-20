package com.justai.jaicf.channel.vk

import com.google.gson.JsonObject
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.exceptions.LongPollServerKeyExpiredException
import com.vk.api.sdk.httpclient.HttpTransportClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.CoroutineContext

class VkChannel(
    override val botApi: BotApi,
    configuration: VkChannelConfiguration,
    url: String = "https://api.vk.com/method/"
) : JaicpCompatibleAsyncBotChannel, CoroutineScope, WithLogger {

    private val supervisor = SupervisorJob()
    override val coroutineContext: CoroutineContext = supervisor + Dispatchers.Default

    private val transportClient = HttpTransportClient.getInstance()
    private val vk = VkApiClient(transportClient)
    private val group: GroupActor = GroupActor(configuration.groupId, configuration.accessToken)
    private val httpClient = HttpClient(CIO)

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = VkBotRequestConverter.convert(request) ?: return null
        val reactions = VkReactions(vk, group, httpClient, botRequest)
        botApi.process(botRequest, reactions, RequestContext.DEFAULT)
        return "".asJsonHttpBotResponse()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "vk"
        override fun create(botApi: BotApi, apiUrl: String): JaicpCompatibleAsyncBotChannel =
            VkChannel(botApi, VkChannelConfiguration("", 0), apiUrl)
    }

    fun runPolling() = Poller().run()

    internal inner class Poller {
        private var server = vk.groupsLongPoll().getLongPollServer(group, group.groupId).execute()
        private var timestamp = server.ts.toInt()

        fun run() = runBlocking { runPolling() }

        private suspend fun runPolling() = getUpdates().collect { update ->
            launch {
                logger.info("Received update: $update")
                process(update.toString().asHttpBotRequest())
            }
        }

        private fun getUpdates(): Flow<JsonObject> = flow {
            while (supervisor.isActive) {
                try {
                    logger.trace("Get updates with timestamp $timestamp")
                    val updates =
                        vk.longPoll().getEvents(server.server, server.key, timestamp).waitTime(30).execute().updates
                    updates.forEach { emit(it) }
                    timestamp += updates.size
                } catch (e: LongPollServerKeyExpiredException) {
                    server = vk.groupsLongPoll().getLongPollServer(group, group.groupId).execute()
                }
            }
        }
    }
}