package com.justai.jaicf.channel.facebook

import com.github.messenger4j.send.MessagePayload
import com.github.messenger4j.send.MessagingType
import com.github.messenger4j.send.Payload
import com.github.messenger4j.send.message.Message
import com.github.messenger4j.send.message.RichMediaMessage
import com.github.messenger4j.send.message.TextMessage
import com.github.messenger4j.send.message.richmedia.RichMediaAsset
import com.github.messenger4j.send.message.richmedia.UrlRichMediaAsset
import com.justai.jaicf.channel.facebook.api.FacebookBotRequest
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.reactions.Reactions
import java.net.URL

val Reactions.facebook
    get() = this as? FacebookReactions

class FacebookReactions(
    private val messenger: Messenger,
    private val request: FacebookBotRequest
): Reactions() {

    fun send(payload: Payload) = messenger.send(payload)

    fun sendResponse(message: Message) = send(
        MessagePayload.create(request.event.senderId(), MessagingType.RESPONSE, message)
    )

    fun sendUrlRichMediaResponse(url: String, type: RichMediaAsset.Type)
            = sendResponse(RichMediaMessage.create(UrlRichMediaAsset.create(type, URL(url))))

    fun queryUserProfile() = messenger.queryUserProfile(request.event.senderId())

    override fun say(text: String) {
        sendResponse(TextMessage.create(text))
    }

    override fun image(url: String) {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.IMAGE)
    }

    fun video(url: String) {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.VIDEO)
    }

    fun audio(url: String) {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.AUDIO)
    }

    fun file(url: String) {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.FILE)
    }
}