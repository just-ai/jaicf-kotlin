package com.justai.jaicf.channel.yandexalice.api

import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.channel.yandexalice.api.model.Button
import com.justai.jaicf.channel.yandexalice.api.model.Card
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class AliceBotResponse(
    var response: Response?,
    @SerialName("start_account_linking")
    var startAccountLinking: JsonObject? = null,
    val version: String,
    @Deprecated(message = "May be omitted")
    val session: Session? = null,
    /**
     * User state is incremental, meaning that the skill returns only increment to the user_state in response as it's
     * shared among all surfaces of the user and there might be a race in changing it. That's why it's of [MutableMap]
     * type.
     * By design a developer has to pass `null` value associated with a key to remove the key's entry from the user
     * state.
     */
    @SerialName("user_state_update")
    val userStateUpdate: MutableMap<String, JsonElement?> = mutableMapOf(),
    @SerialName("session_state")
    var sessionState: JsonObject? = null
): BotResponse {

    constructor(request: AliceBotRequest): this(Response(), null, request.version)

    @Serializable
    data class Response(
        var text: String = "",
        var tts: String = "",
        @SerialName("end_session")
        var endSession: Boolean = false,
        var card: Card? = null,
        val buttons: MutableList<Button> = mutableListOf()
    )
}
