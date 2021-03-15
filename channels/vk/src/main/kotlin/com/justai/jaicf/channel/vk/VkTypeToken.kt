package com.justai.jaicf.channel.vk

import com.justai.jaicf.generic.ChannelTypeToken

typealias VkTypeToken = ChannelTypeToken<VkBotRequest, VkReactions>

val vk: VkTypeToken = ChannelTypeToken()
