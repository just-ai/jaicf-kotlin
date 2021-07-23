package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.jaicpNative
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.context.ExecutionContext

internal val ExecutionContext.jaicpRequest: JaicpBotRequest?
    get() {
        return request.jaicpNative?.jaicp ?: try {
            requestContext.httpBotRequest?.requestMetadata?.let {
                JSON.decodeFromString(JaicpBotRequest.serializer(), it)
            }
        } catch (e: Exception) {
            return null
        }
    }

val DefaultActionContext.sessionId: String
    get() = SessionManager.get(reactions.executionContext).getOrCreateSessionId().sessionId
