package com.justai.jaicf.channel.jaicp.logging

import com.justai.jaicf.context.BotContext
import java.util.*

internal data class JaicpConversationSessionData(val sessionId: String, val isNewSession: Boolean)

internal object JaicpSessionManager {
    private const val SESSION_ID_KEY = "com.justai.jaicf.jaicp.sessionId"

    fun setNewSession(ctx: BotContext): String {
        val sessionId = "${ctx.clientId}-${UUID.randomUUID()}"
        ctx.client[SESSION_ID_KEY] = sessionId
        return sessionId
    }

    fun getOrCreateSessionId(ctx: BotContext): JaicpConversationSessionData {
        var sessionId = ctx.client[SESSION_ID_KEY] as? String
        var isNewSession = false
        if (sessionId == null) {
            sessionId = setNewSession(ctx)
            isNewSession = true
            ctx.client[SESSION_ID_KEY] = sessionId
        }
        return JaicpConversationSessionData(
            sessionId,
            isNewSession
        )
    }
}