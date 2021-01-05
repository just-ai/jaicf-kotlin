package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest

internal object LiveChatEventAdapter {
    private val events = listOf("livechatFinished")

    /**
     * @return true if request is livechat event
     * */
    fun ensureAsyncLiveChatEvent(channel: JaicpCompatibleAsyncBotChannel, request: JaicpBotRequest): Boolean {
        val event = events.find { it == request.event } ?: return false
        val eventData = JSON.decodeFromString(LiveChatEvent.serializer(), request.raw)
        channel.processLiveChatEventRequest(event, eventData.chatId, eventData.getHttpRequest(request))
        return true
    }
}
