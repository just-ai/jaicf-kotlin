package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.JaicpDialerAPI
import com.justai.jaicf.channel.jaicp.dto.JaicpResponseData
import com.justai.jaicf.channel.jaicp.dto.Reply
import com.justai.jaicf.channel.jaicp.dto.TextReply
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.logging.EndSessionReaction
import com.justai.jaicf.logging.NewSessionReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

val Reactions.jaicp get() = this as? JaicpReactions

open class JaicpReactions : Reactions() {

    protected val replies: MutableList<Reply> = mutableListOf()

    internal val dialer by lazy { JaicpDialerAPI() }

    protected val responseData: MutableMap<String, JsonObject> = mutableMapOf()

    internal fun getCurrentState() = botContext.dialogContext.currentState

    /**
     * Manual way to add reply in JAICP channels.
     *
     * @param reply to send in response
     *
     * @see [Reply]
     * @see [TextReply]
     * */
    fun reply(reply: Reply) {
        replies.add(reply)
    }

    override fun say(text: String): SayReaction {
        replies.add(TextReply(text))
        return SayReaction.create(text)
    }

    /**
     * Starts a new conversation session with client for JAICP analytics.
     *
     * Each session is logged to JAICP analytics with unique session identifier.
     * Using this reaction creates new session identifier and sends a response with new session identifier.
     *
     * @see [com.justai.jaicf.channel.jaicp.logging.JaicpConversationLogger]
     * */
    fun startNewSession(): NewSessionReaction {
        botContext.cleanSessionData()
        return NewSessionReaction.create()
    }

    /**
     * Ends the conversation session with client, clears [DialogContext] for current client,
     *  thus client next question will start a new dialogue session.
     *
     * Each session is logged to JAICP analytics with unique session identifier.
     * Using this reaction removes session identifier, clears existing [DialogContext],
     *  therefore user next request will create a new session.
     *
     * @see [com.justai.jaicf.channel.jaicp.logging.JaicpConversationLogger]
     * */
    fun endSession(): EndSessionReaction {
        botContext.dialogContext.currentState = "/"
        botContext.dialogContext.currentContext = "/"
        return EndSessionReaction.create()
    }

    fun collect(): JsonObject {
        doBeforeCollect()

        return JSON.encodeToJsonElement(
            serializer = JaicpResponseData.serializer(),
            value = JaicpResponseData(
                replies = replies,
                dialer = telephony?.dialer,
                bargeInData = telephony?.bargeIn,
                bargeInInterrupt = telephony?.bargeInInterrupt,
                sessionId = SessionManager.get(executionContext).getOrCreateSessionId().sessionId,
                responseData = responseData
            )
        ).jsonObject
    }

    internal open fun doBeforeCollect() {}
}
