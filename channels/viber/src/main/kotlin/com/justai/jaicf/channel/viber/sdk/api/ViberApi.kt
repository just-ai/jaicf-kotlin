package com.justai.jaicf.channel.viber.sdk.api

import com.justai.jaicf.channel.viber.sdk.api.request.AccountInfoRequest
import com.justai.jaicf.channel.viber.sdk.api.request.AccountInfoResponse
import com.justai.jaicf.channel.viber.sdk.api.request.OnlineStatusRequest
import com.justai.jaicf.channel.viber.sdk.api.request.OnlineStatusResponse
import com.justai.jaicf.channel.viber.sdk.api.request.SendMessageRequest
import com.justai.jaicf.channel.viber.sdk.api.request.SendMessageResponse
import com.justai.jaicf.channel.viber.sdk.api.request.UserDetailsRequest
import com.justai.jaicf.channel.viber.sdk.api.request.UserDetailsResponse
import com.justai.jaicf.channel.viber.sdk.api.request.WebhookRequest
import com.justai.jaicf.channel.viber.sdk.api.request.WebhookResponse
import com.justai.jaicf.channel.viber.sdk.event.Event
import com.justai.jaicf.channel.viber.sdk.message.Message
import com.justai.jaicf.channel.viber.sdk.profile.BotProfile
import com.justai.jaicf.channel.viber.sdk.profile.UserProfile

class ViberApi @JvmOverloads constructor(
    viberHttpClient: ViberHttpClient,
    apiUrl: String = "https://chatapi.viber.com/pa"
) {

    private val client = ViberClient(apiUrl, viberHttpClient)

    @Throws(ApiException::class)
    fun setWebhook(url: String, authToken: String): WebhookResponse {
        return client.sendRequest(
            ViberHttpEndpoint.SET_WEBHOOK,
            WebhookRequest(url, Event.values().map(Event::serverEventName).toList()),
            authToken
        )
    }

    @Suppress("unused")
    @Throws(ApiException::class)
    fun sendMessages(
        from: BotProfile,
        to: UserProfile,
        messages: List<Message>,
        authToken: String
    ): List<SendMessageResponse> {
        return messages.map { sendMessage(from, to, it, authToken) }
    }

    @Throws(ApiException::class)
    fun sendMessage(
        from: BotProfile,
        to: UserProfile,
        message: Message,
        authToken: String
    ): SendMessageResponse {
        val request = SendMessageRequest(message, to.id, from)
        return client.sendRequest(ViberHttpEndpoint.SEND_MESSAGE, request, authToken)
    }

    @Throws(ApiException::class)
    fun accountInfo(authToken: String): AccountInfoResponse =
        client.sendRequest(ViberHttpEndpoint.GET_ACCOUNT_INFO, AccountInfoRequest(), authToken)

    @Suppress("unused")
    @Throws(ApiException::class)
    fun getUserDetails(userId: String, authToken: String): UserDetailsResponse =
        client.sendRequest(ViberHttpEndpoint.GET_USER_DETAILS, UserDetailsRequest(userId), authToken)

    @Suppress("unused")
    @Throws(ApiException::class)
    fun getOnlineStatus(userIds: List<String>, authToken: String): OnlineStatusResponse {
        require(userIds.isNotEmpty() && userIds.size < MAX_GET_ONLINE_IDS) {
            "Maximum $MAX_GET_ONLINE_IDS user ids per request are allowed, but actual is ${userIds.size}"
        }

        return client.sendRequest(ViberHttpEndpoint.GET_ONLINE_STATUS, OnlineStatusRequest(userIds), authToken)
    }

    companion object {
        private const val MAX_GET_ONLINE_IDS = 100
    }
}
