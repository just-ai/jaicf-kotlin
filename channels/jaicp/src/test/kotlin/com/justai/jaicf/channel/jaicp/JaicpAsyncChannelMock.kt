package com.justai.jaicf.channel.jaicp

import com.github.messenger4j.send.MessagePayload
import com.github.messenger4j.send.MessagingType
import com.github.messenger4j.send.message.TextMessage
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.facebook.api.FacebookTextBotRequest
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.reactions.Reactions
import java.util.*

class JaicpAsyncChannelMock(
    override val botApi: BotApi,
    private val mockedChannelType: String
) : JaicpCompatibleAsyncBotChannel {

    private val fb = Messenger.create("", "", "", "")
    private var response = ""

    inner class FacebookReactionsMock(
        private val fbRequest: FacebookTextBotRequest
    ) : Reactions() {

        override fun say(text: String) {
            val tm = MessagePayload.create(
                fbRequest.event.senderId(),
                MessagingType.RESPONSE,
                TextMessage.create(text)
            )
            response = tm.toString()
        }

    }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val body = request.receiveText()
        when (mockedChannelType) {
            "facebook" -> fb.onReceiveEvents(body, Optional.empty()) { event ->
                val fbRequest = FacebookTextBotRequest(event.asTextMessageEvent())
                botApi.process(fbRequest, FacebookReactionsMock(fbRequest))
            }
        }
        return response.asJsonHttpBotResponse()
    }

    object FB: JaicpCompatibleAsyncChannelFactory {
        override val channelType = "facebook"
        override fun create(botApi: BotApi, apiUrl: String) = JaicpAsyncChannelMock(botApi, channelType)
    }
}