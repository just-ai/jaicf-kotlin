package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.logging.JaicpSessionManager
import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.channel.jaicp.toJson
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.logging.SayReaction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray

open class JaicpReactions : Reactions() {

    protected val replies: MutableList<Reply> = mutableListOf()
    internal val dialer by lazy { JaicpDialerAPI() }

    internal fun getCurrentState() = botContext.dialogContext.currentState

    override fun say(text: String): SayReaction {
        replies.add(TextReply(text))
        return SayReaction.create(text)
    }

    fun startNewSession() {
        botContext.dialogContext.currentState = "/"
        botContext.dialogContext.currentContext = "/"
        botContext.cleanSessionData()
        JaicpSessionManager.setNewSession(botContext)
    }

    fun collect(): JsonObject {
        val jsonReplies = replies.map { it.serialized().toJson() }
        val answer = replies.joinToString(separator = "\n\n") {
            if (it is TextReply) {
                it.text
            } else {
                ""
            }
        }

        return json {
            if (this@JaicpReactions is TelephonyReactions) {
                "dialer" to dialer.getApiResponse()
            }
            "replies" to jsonArray {
                jsonReplies.forEach { +it }
            }
            "answer" to answer
        }
    }
}