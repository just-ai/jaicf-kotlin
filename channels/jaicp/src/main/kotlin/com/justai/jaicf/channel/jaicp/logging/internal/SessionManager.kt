package com.justai.jaicf.channel.jaicp.logging.internal

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.logging.LoggingContext

internal object SessionManager {

    fun processStartSessionReaction(ctx: BotContext) = SessionStarted.saveToContext(ctx)

    fun processEndSessionReaction(ctx: BotContext) = SessionEnded.saveToContext(ctx)

    fun getOrCreateSessionId(loggingContext: LoggingContext): SessionData {
        val ctx = loggingContext.botContext
        val sessionData = SessionDataService.fromContext(ctx)
        val sessionEvent = SessionEventService.fromContext(ctx)
        SessionEventService.cleanup(ctx)

        if (sessionData == null || loggingContext.requestContext.newSession) {
            return SessionData.new(ctx).apply { saveToContext(ctx) }
        }

        return when (sessionEvent) {
            // if session started, create new session and save it for further requests
            SessionStarted -> SessionData.new(ctx).apply { saveToContext(ctx) }
            // if session ended, delete current session
            SessionEnded -> sessionData.also {
                SessionDataService.cleanup(ctx)
                ctx.cleanSessionData()
            }
            null -> sessionData
        }
    }
}
