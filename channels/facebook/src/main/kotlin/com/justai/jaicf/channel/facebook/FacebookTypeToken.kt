package com.justai.jaicf.channel.facebook

import com.justai.jaicf.channel.facebook.api.FacebookBotRequest
import com.justai.jaicf.channel.facebook.api.FacebookEventBotRequest
import com.justai.jaicf.channel.facebook.api.FacebookQuickReplyBotRequest
import com.justai.jaicf.channel.facebook.api.FacebookTextBotRequest
import com.justai.jaicf.generic.ChannelTypeToken

typealias FacebookTypeToken = ChannelTypeToken<FacebookBotRequest, FacebookReactions>

val facebook: FacebookTypeToken = ChannelTypeToken()

val FacebookTypeToken.text get() = ChannelTypeToken<FacebookTextBotRequest, FacebookReactions>()
val FacebookTypeToken.quickReply get() = ChannelTypeToken<FacebookQuickReplyBotRequest, FacebookReactions>()
val FacebookTypeToken.event get() = ChannelTypeToken<FacebookEventBotRequest, FacebookReactions>()