package com.justai.jaicf.channel.telegram

import com.justai.jaicf.generic.ChannelTypeToken

typealias TelegramTypeToken = ChannelTypeToken<TelegramBotRequest, TelegramReactions>

val telegram: TelegramTypeToken = ChannelTypeToken()

val TelegramTypeToken.location get() = ChannelTypeToken<TelegramLocationRequest, TelegramReactions>()
val TelegramTypeToken.contact get() = ChannelTypeToken<TelegramContactRequest, TelegramReactions>()