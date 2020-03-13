package com.justai.jaicf.channel.slack

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted

val BotRequest.slack
    get() = this as? SlackBotRequest

data class SlackBotRequest(
    val message: SlackMessagePosted
): QueryBotRequest(
    clientId = message.user.id,
    input = message.messageContent
)