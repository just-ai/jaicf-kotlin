package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBotChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import kotlinx.serialization.json.JsonElement
import java.util.concurrent.ConcurrentHashMap

object LiveChatEventAdapter {
    private val clients: MutableMap<String, JaicpBotRequest> = ConcurrentHashMap()
    private val events = listOf("livechatFinished")

    fun registerSwitch(req: JaicpBotRequest) {
        clients[req.clientId] = req
    }

    /**
     * @return true if request is livechat event
     * */
    fun ensureAsyncLiveChatEvent(channel: JaicpBotChannel, request: JaicpBotRequest): Boolean {
        val stored = clients[request.clientId] ?: return false
        return false
    }
}

private fun JsonElement.asLiveChatEvent() = JSON.decodeFromJsonElement(LiveChatEvent.serializer(), this)
