package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.channel.yandexalice.activator.AliceIntentActivatorContext
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken

typealias AliceTypeToken = ChannelTypeToken<AliceBotRequest, AliceReactions>

val alice: AliceTypeToken = ChannelTypeToken()

val AliceTypeToken.intent get() = ContextTypeToken<AliceIntentActivatorContext, AliceBotRequest, AliceReactions>()