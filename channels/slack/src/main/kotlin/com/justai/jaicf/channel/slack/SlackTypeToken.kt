package com.justai.jaicf.channel.slack

import com.justai.jaicf.generic.ChannelTypeToken

typealias SlackTypeToken = ChannelTypeToken<SlackBotRequest, SlackReactions>

val slack: SlackTypeToken = ChannelTypeToken()

val SlackTypeToken.event get() = ChannelTypeToken<SlackEventRequest, SlackReactions>()
val SlackTypeToken.command get() = ChannelTypeToken<SlackCommandRequest, SlackReactions>()
val SlackTypeToken.action get() = ChannelTypeToken<SlackActionRequest, SlackReactions>()