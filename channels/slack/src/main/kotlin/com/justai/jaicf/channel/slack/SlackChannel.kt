package com.justai.jaicf.channel.slack

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.BotChannel
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

class SlackChannel(
    override val botApi: BotApi,
    private val authToken: String
) : BotChannel {

    private lateinit var session: SlackSession

    fun run() {
        session = SlackSessionFactory.createWebSocketSlackSession(authToken)
        session.connect()
        session.addMessagePostedListener { message, _ ->
            message.sender.isBot.takeIf { !it }?.let {
                process(message)
            }
        }
    }

    private fun process(message: SlackMessagePosted) {
        val request = SlackBotRequest(message)
        val reactions = SlackReactions(session, message.user)
        botApi.process(request, reactions)
    }
}