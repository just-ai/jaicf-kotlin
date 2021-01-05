package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.SwitchReply
import com.justai.jaicf.channel.jaicp.jaicpRequest
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.logging.LoggingContext
import kotlinx.serialization.Serializable

@Serializable
data class LiveChatInitRequest(
    private val request: JaicpBotRequest,
    private val sessionId: String,
    private val switchData: SwitchReply
) {
    companion object {
        fun create(lc: LoggingContext, reply: SwitchReply): LiveChatInitRequest? {
            val req = lc.jaicpRequest ?: return null
            return LiveChatInitRequest(
                request = req,
                switchData = reply,
                sessionId = SessionManager.getOrCreateSessionId(lc).sessionId
            )
        }
    }
}
