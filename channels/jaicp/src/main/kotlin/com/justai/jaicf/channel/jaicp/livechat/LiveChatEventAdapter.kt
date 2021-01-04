package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.reactions.Reactions
import kotlinx.serialization.json.JsonElement
import java.util.concurrent.ConcurrentHashMap

internal object LiveChatEventAdapter {
    private val clients: MutableMap<String, Reactions> = ConcurrentHashMap()
    private val events = listOf("livechatFinished")

    /**
     * JAVADOC ME
     * */
    fun registerSwitch(req: JaicpBotRequest, reactions: Reactions) {
        clients[req.clientId] = reactions
    }

    /**
     * @return true if request is livechat event
     * */
    fun ensureAsyncLiveChatEvent(channel: JaicpCompatibleAsyncBotChannel, request: JaicpBotRequest): Boolean {
        val reactions = clients.remove(request.clientId) ?: return false
        val event = events.find { it == request.event } ?: return false
        channel.processLiveChatEventRequest(event, reactions)
        return true
    }
}
