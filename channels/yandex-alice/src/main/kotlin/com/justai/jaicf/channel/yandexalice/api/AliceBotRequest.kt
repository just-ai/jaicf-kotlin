package com.justai.jaicf.channel.yandexalice.api

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.channel.yandexalice.AliceEvent
import com.justai.jaicf.channel.yandexalice.AliceIntent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

val BotRequest.alice
    get() = this as? AliceBotRequest

@Serializable
data class AliceBotRequest(
    val version: String,
    val meta: Meta? = null,
    val session: Session,
    val request: Request? = null,
    val state: State? = null,
    @SerialName("account_linking_complete_event")
    val accountLinkingCompleteEvent: JsonObject? = null
): BotRequest {
    override val clientId = session.application.applicationId
    val headers = mutableMapOf<String, List<String>>()

    val accessToken by lazy {
        headers["Authorization"]
            ?.firstOrNull()
            ?.substringAfter("Bearer ", "")
    }

    override val type = when {
        request == null -> BotRequestType.EVENT
        request.command.isEmpty() -> BotRequestType.EVENT
        else -> BotRequestType.QUERY
    }

    override val input = when {
        accountLinkingCompleteEvent != null -> AliceEvent.ACCOUNT_LINKING_COMPLETE
        request!!.command.isEmpty() -> AliceEvent.START
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
    @SerialName("skill_id")
    val skillId: String,
    val application: Application,
    val user: User? = null
) {
    // see: https://yandex.ru/dev/dialogs/alice/doc/protocol-docpage/
    // application.applicationId contains the same value `user_id` contained
    @Deprecated(
        message = "use application.applicationId instead as recommended in documentation",
        replaceWith = ReplaceWith(expression = "application.applicationId")
    )
    val userId: String
        get() = application.applicationId
}

@Serializable
data class User(
    @SerialName("user_id")
    val userId: String,
    @SerialName("access_token")
    val accessToken: String? = null
)

@Serializable
data class Application(
    @SerialName("application_id")
    val applicationId: String
)

@Serializable
data class State(
    val session: JsonObject? = null,
    val user: Map<String, JsonElement> = emptyMap()
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
        val entities: List<Entity>,
        /**
         * @see AliceIntent for predefined
         * */
        val intents: Map<String, Intent>
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

        @Serializable
        data class Intent(
            val slots: Map<String, Slot> = emptyMap()
        )

        @Serializable
        data class Slot(
            val type: String,
            val value: JsonElement,
            val tokens: Tokens? = null
        )

        @Serializable
        data class Tokens(
            val start: Int,
            val end: Int
        )
    }
}
