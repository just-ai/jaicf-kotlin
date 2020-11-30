package com.justai.jaicf.channel.jaicp.logging.internal

import com.justai.jaicf.context.BotContext

internal object SessionManager {

    fun processStartSessionReaction(ctx: BotContext) = SessionStarted.saveToContext(ctx)

    fun processEndSessionReaction(ctx: BotContext) = SessionEnded.saveToContext(ctx)

    fun getOrCreateSessionId(ctx: BotContext): SessionData {
        val sessionData = SessionDataService.fromContext(ctx)
        val sessionEvent = SessionEventService.fromContext(ctx)
        SessionEventService.cleanup(ctx)

        if (sessionData == null) {
            return SessionData.new(ctx).apply { saveToContext(ctx) }
        }

        return when (sessionEvent) {
            // if session started, create new session and save it for further requests
            SessionStarted -> SessionData.new(ctx).apply { saveToContext(ctx) }
            // if session ended, delete current session
            SessionEnded -> sessionData.also { SessionDataService.cleanup(ctx) }
            null -> sessionData
        }
    }
}
