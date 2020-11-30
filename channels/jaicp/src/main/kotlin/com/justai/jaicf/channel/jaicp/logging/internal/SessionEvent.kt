package com.justai.jaicf.channel.jaicp.logging.internal

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.context.ContextSerializable
import com.justai.jaicf.channel.jaicp.context.ContextSerializableService
import kotlinx.serialization.Serializable

internal object SessionEventService : ContextSerializableService<SessionEvent> {
    override val key = "com.justai.jaicf.jaicp.logging.conversationSession.event"
    override fun deserialize(content: String) = JSON.parse(SessionEvent.serializer(), content)
}

@Serializable
internal sealed class SessionEvent : ContextSerializable {
    override fun serialized() = JSON.stringify(serializer(), this)
    override val service by lazy { SessionEventService }
}

@Serializable
internal object SessionStarted : SessionEvent()

@Serializable
internal object SessionEnded : SessionEvent()