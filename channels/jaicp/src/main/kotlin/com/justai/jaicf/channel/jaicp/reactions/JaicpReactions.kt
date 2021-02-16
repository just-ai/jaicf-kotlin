package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.JaicpDialerData
import com.justai.jaicf.channel.jaicp.dto.JaicpResponseData
import com.justai.jaicf.channel.jaicp.dto.Reply
import com.justai.jaicf.channel.jaicp.dto.TextReply
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.channel.jaicp.reactions.reaction.EndSessionReaction
import com.justai.jaicf.channel.jaicp.reactions.reaction.NewSessionReaction
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

val Reactions.jaicp get() = this as? JaicpReactions

open class JaicpReactions : Reactions() {

    protected val replies: MutableList<Reply> = mutableListOf()

    internal val dialer by lazy { JaicpDialerData() }

    internal fun getCurrentState() = botContext.dialogContext.currentState

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
    fun startNewSession() {
        botContext.cleanSessionData()
        registerReaction(NewSessionReaction(getCurrentState()))
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
    fun endSession() {
        botContext.dialogContext.currentState = "/"
        botContext.dialogContext.currentContext = "/"
        registerReaction(EndSessionReaction(getCurrentState()))
    }

    fun collect(): JsonObject = JSON.encodeToJsonElement(
        serializer = JaicpResponseData.serializer(),
        value = JaicpResponseData(
            replies,
            telephony?.dialer,
            telephony?.bargeIn,
            telephony?.bargeInInterrupt,
            sessionId = SessionManager.get(executionContext).getOrCreateSessionId().sessionId
        )
    ).jsonObject
}