package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.SwitchReply
import com.justai.jaicf.channel.jaicp.jaicpRequest
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.logging.LoggingContext
import kotlinx.serialization.Serializable

@Serializable
data class LiveChatStartRequest(
    private val request: JaicpBotRequest,
    private val sessionId: String,
    private val switchData: SwitchReply
) {
    companion object {
        fun createAndRegister(lc: LoggingContext, reply: SwitchReply): LiveChatStartRequest? {
            val req = lc.jaicpRequest ?: return null
            return LiveChatStartRequest(
                request = req,
                switchData = reply,
                sessionId = SessionManager.getOrCreateSessionId(lc).sessionId
            ).also {
                LiveChatEventAdapter.registerSwitch(req)
            }
        }
    }
}
