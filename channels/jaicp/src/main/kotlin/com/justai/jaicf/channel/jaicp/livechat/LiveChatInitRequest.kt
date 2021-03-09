package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.channel.jaicp.jaicpRequest
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.context.ExecutionContext
import kotlinx.serialization.Serializable

@Serializable
internal data class LiveChatInitRequest(
    val request: JaicpBotRequest,
    val sessionId: String,
    val switchData: LiveChatSwitchReply
) {
    companion object {
        fun create(executionContext: ExecutionContext, reply: LiveChatSwitchReply): LiveChatInitRequest? {
            val req = executionContext.jaicpRequest ?: return null
            return LiveChatInitRequest(
                request = req,
                switchData = reply,
                sessionId = SessionManager.get(executionContext).getOrCreateSessionId().sessionId
            )
        }
    }
}
