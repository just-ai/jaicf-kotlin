package com.justai.jaicf.channel.viber

import com.justai.jaicf.channel.viber.api.ViberBotRequest
import com.justai.jaicf.channel.viber.api.ViberContactMessageRequest
import com.justai.jaicf.channel.viber.api.ViberConversationStartedRequest
import com.justai.jaicf.channel.viber.api.ViberDeliveredRequest
import com.justai.jaicf.channel.viber.api.ViberFailedRequest
import com.justai.jaicf.channel.viber.api.ViberFileMessageRequest
import com.justai.jaicf.channel.viber.api.ViberLocationMessageRequest
import com.justai.jaicf.channel.viber.api.ViberMessageRequest
import com.justai.jaicf.channel.viber.api.ViberImageMessageRequest
import com.justai.jaicf.channel.viber.api.ViberRichMediaMessageRequest
import com.justai.jaicf.channel.viber.api.ViberSeenRequest
import com.justai.jaicf.channel.viber.api.ViberStickerMessageRequest
import com.justai.jaicf.channel.viber.api.ViberSubscribedRequest
import com.justai.jaicf.channel.viber.api.ViberTextMessageRequest
import com.justai.jaicf.channel.viber.api.ViberUnsubscribedRequest
import com.justai.jaicf.channel.viber.api.ViberUrlMessageRequest
import com.justai.jaicf.channel.viber.api.ViberVideoMessageRequest
import com.justai.jaicf.generic.ChannelTypeToken

typealias ViberTypeToken = ChannelTypeToken<ViberBotRequest, ViberReactions>

val viber: ViberTypeToken = ChannelTypeToken()

val ViberTypeToken.seen get() = ChannelTypeToken<ViberSeenRequest, ViberReactions>()
val ViberTypeToken.delivered get() = ChannelTypeToken<ViberDeliveredRequest, ViberReactions>()
val ViberTypeToken.subscribed get() = ChannelTypeToken<ViberSubscribedRequest, ViberReactions>()
val ViberTypeToken.unsubscribed get() = ChannelTypeToken<ViberUnsubscribedRequest, ViberReactions>()
val ViberTypeToken.conversationStarted get() = ChannelTypeToken<ViberConversationStartedRequest, ViberReactions>()
val ViberTypeToken.failed get() = ChannelTypeToken<ViberFailedRequest, ViberReactions>()
val ViberTypeToken.message get() = ChannelTypeToken<ViberMessageRequest, ViberReactions>()
val ViberTypeToken.richMedia get() = ChannelTypeToken<ViberRichMediaMessageRequest, ViberReactions>()
val ViberTypeToken.text get() = ChannelTypeToken<ViberTextMessageRequest, ViberReactions>()
val ViberTypeToken.contact get() = ChannelTypeToken<ViberContactMessageRequest, ViberReactions>()
val ViberTypeToken.file get() = ChannelTypeToken<ViberFileMessageRequest, ViberReactions>()
val ViberTypeToken.location get() = ChannelTypeToken<ViberLocationMessageRequest, ViberReactions>()
val ViberTypeToken.image get() = ChannelTypeToken<ViberImageMessageRequest, ViberReactions>()
val ViberTypeToken.sticker get() = ChannelTypeToken<ViberStickerMessageRequest, ViberReactions>()
val ViberTypeToken.url get() = ChannelTypeToken<ViberUrlMessageRequest, ViberReactions>()
val ViberTypeToken.video get() = ChannelTypeToken<ViberVideoMessageRequest, ViberReactions>()
