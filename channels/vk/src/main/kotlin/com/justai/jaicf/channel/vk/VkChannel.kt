package com.justai.jaicf.channel.vk

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.vk.api.sdk.callback.CallbackApi
import com.vk.api.sdk.callback.longpoll.CallbackApiLongPoll
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.messages.Message
import java.util.concurrent.Executors

class VkChannel(
    override val botApi: BotApi,
    configuration: VkChannelConfiguration,
    url: String = "https://api.vk.com/method/"
) : JaicpCompatibleAsyncBotChannel, WithLogger, CallbackApi() {

    private val transportClient = HttpTransportClient.getInstance()
    private val vk = VkApiClient(transportClient)
    private val group: GroupActor = GroupActor(configuration.groupId, configuration.accessToken)
    private val storage = configuration.reactionsContentStorage

    override fun messageNew(groupId: Int, message: Message) {
        process(message)
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        parse(request.receiveText())
        return "".asJsonHttpBotResponse()
    }

    private fun process(message: Message) = VkBotRequestFactory.getRequestForMessage(message)
        ?.let { process(it) }
        ?: logger.debug("No request converter found for message: $message")

    private fun process(request: VkBotRequest): HttpBotResponse {
        val reactions = VkReactions(vk, group, request, storage)
        botApi.process(request, reactions, RequestContext.DEFAULT)
        return "".asJsonHttpBotResponse()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "vk"
        override fun create(botApi: BotApi, apiUrl: String): JaicpCompatibleAsyncBotChannel =
            VkChannel(botApi, VkChannelConfiguration("", 0), apiUrl)
    }

    fun run() = object : CallbackApiLongPoll(vk, group) {
        private val executor = Executors.newWorkStealingPool()

        override fun messageNew(groupId: Int, message: Message) {
            executor.submit { this@VkChannel.messageNew(groupId, message) }
        }
    }.run()
}