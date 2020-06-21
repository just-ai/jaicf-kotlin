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
    val userStateUpdate: MutableMap<String, JsonElement?> = mutableMapOf(),
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
