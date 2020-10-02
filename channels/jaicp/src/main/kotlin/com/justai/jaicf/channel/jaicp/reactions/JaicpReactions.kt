package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.SayReaction
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray

open class JaicpReactions : Reactions() {

    internal val replies: MutableList<Reply> = mutableListOf()

    internal fun getCurrentState() = botContext.dialogContext.currentState

    override fun say(text: String): SayReaction {
        replies.add(TextReply(text))
        return SayReaction.create(text)
    }

    fun collect(): JsonObject {
        val jsonReplies: List<JsonElement> = replies.map { reply ->
            when (reply) {
                is TextReply -> JSON.toJson(
                    TextReply.serializer(), reply
                )
                is ButtonsReply -> JSON.toJson(
                    ButtonsReply.serializer(), reply
                )
                is ImageReply -> JSON.toJson(
                    ImageReply.serializer(), reply
                )
                is AudioReply -> JSON.toJson(
                    AudioReply.serializer(), reply
                )
                is HangupReply -> JSON.toJson(
                    HangupReply.serializer(), reply
                )
                is SwitchReply -> JSON.toJson(
                    SwitchReply.serializer(), reply
                )
            }
        }

        val answer = replies.joinToString(separator = "\n\n") {
            if (it is TextReply) {
                it.text
            } else {
                ""
            }
        }

        return json {
            "replies" to jsonArray {
                jsonReplies.forEach { +it }
            }
            "answer" to answer
        }
    }
}