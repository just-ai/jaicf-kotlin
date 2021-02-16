package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object TestJaicpRepliesParser {
    fun parse(replies: List<JsonElement>) = replies.map {
        when (it.jsonObject["type"]?.jsonPrimitive?.content) {
            "text" -> JSON.decodeFromJsonElement(TextReply.serializer(), it)
            "buttons" -> JSON.decodeFromJsonElement(ButtonsReply.serializer(), it)
            "audio" -> JSON.decodeFromJsonElement(AudioReply.serializer(), it)
            "image" -> JSON.decodeFromJsonElement(ImageReply.serializer(), it)
            "hangup" -> JSON.decodeFromJsonElement(HangupReply.serializer(), it)
            else -> error("Unknown reply type: it")
        }
    }
}