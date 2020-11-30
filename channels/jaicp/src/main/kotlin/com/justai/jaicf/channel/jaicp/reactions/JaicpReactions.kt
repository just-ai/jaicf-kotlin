package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.channel.jaicp.toJson
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.logging.SayReaction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray

val Reactions.jaicp get() = this as? JaicpReactions

open class JaicpReactions : Reactions() {

    protected val replies: MutableList<Reply> = mutableListOf()
    internal val dialer by lazy { JaicpDialerAPI() }

    internal fun getCurrentState() = botContext.dialogContext.currentState

    override fun say(text: String): SayReaction {
        replies.add(TextReply(text))
        return SayReaction.create(text)
    }

    /**
     * Starts a new conversation session with client.
     *
     * Each session is logged to JAICP analytics with unique session identifier.
     * Using this reaction creates new session identifier, clears existing [DialogContext]
     *  and sends a response with new session identifier.
     *
     * @see [com.justai.jaicf.channel.jaicp.logging.JaicpConversationLogger]
     * */
    fun startNewSession() {
        botContext.dialogContext.currentState = "/"
        botContext.dialogContext.currentContext = "/"
        botContext.cleanSessionData()
        SessionManager.processStartSessionReaction(botContext)
    }

    /**
     * Ends the conversation session with client.
     *
     * Each session is logged to JAICP analytics with unique session identifier.
     * Using this reaction removes session identifier, clears existing [DialogContext],
     *  therefore user next request will create a new session.
     *
     * @see [com.justai.jaicf.channel.jaicp.logging.JaicpConversationLogger]
     * */
    fun endSession() {
        botContext.dialogContext.currentState = "/"
        botContext.dialogContext.currentContext = "/"
        botContext.cleanSessionData()
        SessionManager.processEndSessionReaction(botContext)
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