package com.justai.jaicf.channel.slack

import com.justai.jaicf.reactions.Reactions
import com.ullink.slack.simpleslackapi.SlackPreparedMessage
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.SlackUser

val Reactions.slack
    get() = this as? SlackReactions

class SlackReactions(
    private val session: SlackSession,
    private val user: SlackUser
): Reactions() {

    override fun say(text: String) {
        session.sendMessageToUser(user, text, null)
    }

    fun prepareMessage() = SlackPreparedMessage.Builder()

    fun sendMessage(message: SlackPreparedMessage) {
        session.sendMessageToUser(user, message)
    }

    fun sendMessage(message: SlackPreparedMessage.Builder) {
        sendMessage(message.build())
    }
}