package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.ChatApiBotRequest
import com.justai.jaicf.channel.jaicp.dto.ChatWidgetBotRequest
import com.justai.jaicf.channel.jaicp.dto.TelephonyBotRequest
import com.justai.jaicf.channel.jaicp.reactions.ChatApiReactions
import com.justai.jaicf.channel.jaicp.reactions.ChatWidgetReactions
import com.justai.jaicf.channel.jaicp.reactions.TelephonyReactions
import com.justai.jaicf.generic.ChannelTypeToken


typealias ChatApiTypeToken = ChannelTypeToken<ChatApiBotRequest, ChatApiReactions>

val chatapi: ChatApiTypeToken = ChannelTypeToken()

typealias ChatWidgetTypeToken = ChannelTypeToken<ChatWidgetBotRequest, ChatWidgetReactions>

val chatwidget: ChatWidgetTypeToken = ChannelTypeToken()

typealias TelephonyTypeToken = ChannelTypeToken<TelephonyBotRequest, TelephonyReactions>

val telephony: TelephonyTypeToken = ChannelTypeToken()