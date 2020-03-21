package com.justai.jaicf.channel.yandexalice.api

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.channel.yandexalice.AliceEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

val BotRequest.alice
    get() = this as? AliceBotRequest

@Serializable
data class AliceBotRequest(
    val version: String,
    val meta: Meta,
    val session: Session,
    val request: Request
): BotRequest {
    override val clientId = session.userId

    override val type = when {
        request.command.isEmpty() -> BotRequestType.EVENT
        else -> BotRequestType.QUERY
    }

    override val input = when {
        request.command.isEmpty() -> AliceEvent.START
        else -> request.command
    }
}

@Serializable
data class Meta(
    val locale: String,
    val timezone: String,
    @SerialName("client_id")
    val clientId: String,
    val interfaces: Map<String, JsonObject>
)

@Serializable
data class Session(
    @SerialName("new")
    val newSession: Boolean,
    @SerialName("message_id")
    val messageId: Int,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("skill_id")
    val skillId: String
)

@Serializable
data class Request(
    val command: String,
    @SerialName("original_utterance")
    val originalUtterance: String,
    val type: String,
    val markup: JsonObject? = null,
    val payload: JsonObject? = null,
    val nlu: Nlu
) {
    @Serializable
    data class Nlu(
        val tokens: List<String>,
        val entities: List<Entity>
    ) {
        @Serializable
        data class Entity(
            val type: String,
            val tokens: Tokens,
            val value: JsonElement
        ) {
            @Serializable
            data class Tokens(
                val start: Int,
                val end: Int
            )
        }
    }
}