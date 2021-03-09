package com.justai.jaicf.channel.jaicp.logging.internal

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.reactions.reaction.EndSessionReaction
import com.justai.jaicf.channel.jaicp.reactions.reaction.NewSessionReaction
import com.justai.jaicf.context.ExecutionContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*


private const val SESSION_MANAGER_KEY = "com/justai/jaicf/jaicp/logging/conversationSession/sessionManager"

@Serializable
internal data class SessionData(
    val sessionId: String,
    val isNewSession: Boolean
)

@Serializable
internal class SessionManager {
    private var sessionId: String? = null
    private var isNewSession: Boolean = false

    @Transient
    private var thisRequestSessionData: SessionData? = null

    @Transient
    lateinit var executionContext: ExecutionContext

    fun getOrCreateSessionId(): SessionData = thisRequestSessionData ?: runAndSave {
        val sessionData = when {
            hasNewSessionReaction() -> SessionData(createSessionId(), true)
            hasEndSessionReaction() -> when (sessionId) {
                null -> SessionData(createSessionId(), true)
                else -> SessionData(requireNotNull(sessionId), false)
            }
            shouldStartNewSession() -> SessionData(createSessionId(), true)
            else -> SessionData(requireNotNull(sessionId), false)

        }

        sessionId = when (hasEndSessionReaction()) {
            true -> null
            false -> sessionData.sessionId
        }
        sessionData
    }

    private fun hasNewSessionReaction() = executionContext.reactions.any { it is NewSessionReaction }

    private fun hasEndSessionReaction() = executionContext.reactions.any { it is EndSessionReaction }

    private fun shouldStartNewSession() = sessionId == null || executionContext.requestContext.newSession

    private fun runAndSave(block: SessionManager.() -> SessionData): SessionData {
        val sessionData = block.invoke(this)
        thisRequestSessionData = sessionData

        executionContext.botContext.session[SESSION_MANAGER_KEY] = JSON.encodeToString(serializer(), this)
        executionContext.botContext.temp[SESSION_MANAGER_KEY] = this
        return sessionData
    }

    private fun createSessionId() = "${executionContext.botContext.clientId}-${UUID.randomUUID()}"

    override fun toString(): String {
        return "SessionManager(sessionId=$sessionId, isNewSession=$isNewSession, thisRequestSessionData=$thisRequestSessionData, executionContext=$executionContext)"
    }


    companion object {
        fun get(executionContext: ExecutionContext): SessionManager {
            // if any component requested session, we finalize session and use it during all request processing
            (executionContext.botContext.temp[SESSION_MANAGER_KEY] as? SessionManager)?.let { return it }

            // this is the first call for sessionData, we try to decode it from BotContext and reuse.
            val storedSessionState = executionContext.botContext.session[SESSION_MANAGER_KEY] as? String
            val sessionManager = storedSessionState?.let { JSON.decodeFromString(serializer(), it) } ?: SessionManager()
            sessionManager.executionContext = executionContext
            return sessionManager
        }
    }
}