package com.justai.jaicf.channel.yandexalice.api

import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.channel.yandexalice.api.model.Button
import com.justai.jaicf.channel.yandexalice.api.model.Card
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AliceBotResponse(
    val response: Response,
    val session: Session,
    val version: String
): BotResponse {

    constructor(request: AliceBotRequest): this(Response(), request.session, request.version)

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