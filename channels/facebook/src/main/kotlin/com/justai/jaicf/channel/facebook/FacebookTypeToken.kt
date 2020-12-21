package com.justai.jaicf.channel.facebook

import com.justai.jaicf.channel.facebook.api.*
import com.justai.jaicf.generic.ChannelTypeToken

typealias FacebookTypeToken = ChannelTypeToken<FacebookBotRequest, FacebookReactions>

val facebook: FacebookTypeToken = ChannelTypeToken()

val FacebookTypeToken.text get() = ChannelTypeToken<FacebookTextBotRequest, FacebookReactions>()
val FacebookTypeToken.quickReply get() = ChannelTypeToken<FacebookQuickReplyBotRequest, FacebookReactions>()
val FacebookTypeToken.event get() = ChannelTypeToken<FacebookEventBotRequest, FacebookReactions>()
val FacebookTypeToken.optIn get() = ChannelTypeToken<FacebookOptInBotRequest, FacebookReactions>()
val FacebookTypeToken.accountLinking get() = ChannelTypeToken<FacebookAccountLinkingBotRequest, FacebookReactions>()
val FacebookTypeToken.attachment get() = ChannelTypeToken<FacebookAttachmentBotRequest, FacebookReactions>()
val FacebookTypeToken.fallback get() = ChannelTypeToken<FacebookFallbackBotRequest, FacebookReactions>()
val FacebookTypeToken.instantGame get() = ChannelTypeToken<FacebookInstantGameBotRequest, FacebookReactions>()
val FacebookTypeToken.messageDelivered get() = ChannelTypeToken<FacebookMessageDeliveredBotRequest, FacebookReactions>()
val FacebookTypeToken.messageEcho get() = ChannelTypeToken<FacebookMessageEchoBotRequest, FacebookReactions>()
val FacebookTypeToken.messageRead get() = ChannelTypeToken<FacebookMessageReadBotRequest, FacebookReactions>()
val FacebookTypeToken.postBack get() = ChannelTypeToken<FacebookPostBackBotRequest, FacebookReactions>()
val FacebookTypeToken.referral get() = ChannelTypeToken<FacebookReferralBotRequest, FacebookReactions>()