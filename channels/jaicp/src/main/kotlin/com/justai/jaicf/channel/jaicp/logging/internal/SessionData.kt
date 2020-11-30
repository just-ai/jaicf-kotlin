package com.justai.jaicf.channel.jaicp.logging.internal

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.context.ContextSerializable
import com.justai.jaicf.channel.jaicp.context.ContextSerializableService
import com.justai.jaicf.context.BotContext
import kotlinx.serialization.Serializable
import java.util.*

internal object SessionDataService : ContextSerializableService<SessionData> {
    override val key = "com.justai.jaicf.jaicp.logging.conversationSession.session"
    override fun deserialize(content: String) = JSON.parse(SessionData.serializer(), content)
}

@Serializable
internal data class SessionData(
    val sessionId: String,
    val isNewSession: Boolean
) : ContextSerializable {

    override fun serialized() = JSON.stringify(serializer(), copy(isNewSession = false))
    override val service: ContextSerializableService<*> =
        SessionDataService

    companion object {
        fun new(ctx: BotContext) = SessionData(
            "${ctx.clientId}-${UUID.randomUUID()}",
            true
        )
    }
}