package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.channel.aimybox.api.AimyboxBotRequest
import com.justai.jaicf.generic.ChannelTypeToken

typealias AimyboxTypeToken = ChannelTypeToken<AimyboxBotRequest, AimyboxReactions>

val aimybox: AimyboxTypeToken = ChannelTypeToken()