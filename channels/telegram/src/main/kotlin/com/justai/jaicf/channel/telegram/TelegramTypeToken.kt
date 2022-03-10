package com.justai.jaicf.channel.telegram

import com.justai.jaicf.generic.ChannelTypeToken

typealias TelegramTypeToken = ChannelTypeToken<TelegramBotRequest, TelegramReactions>

val telegram: TelegramTypeToken = ChannelTypeToken()

val TelegramTypeToken.text get() = ChannelTypeToken<TelegramTextRequest, TelegramReactions>()
val TelegramTypeToken.callback get() = ChannelTypeToken<TelegramQueryRequest, TelegramReactions>()
val TelegramTypeToken.location get() = ChannelTypeToken<TelegramLocationRequest, TelegramReactions>()
val TelegramTypeToken.contact get() = ChannelTypeToken<TelegramContactRequest, TelegramReactions>()
val TelegramTypeToken.audio get() = ChannelTypeToken<TelegramAudioRequest, TelegramReactions>()
val TelegramTypeToken.document get() = ChannelTypeToken<TelegramDocumentRequest, TelegramReactions>()
val TelegramTypeToken.animation get() = ChannelTypeToken<TelegramAnimationRequest, TelegramReactions>()
val TelegramTypeToken.game get() = ChannelTypeToken<TelegramGameRequest, TelegramReactions>()
val TelegramTypeToken.photos get() = ChannelTypeToken<TelegramPhotosRequest, TelegramReactions>()
val TelegramTypeToken.sticker get() = ChannelTypeToken<TelegramStickerRequest, TelegramReactions>()
val TelegramTypeToken.video get() = ChannelTypeToken<TelegramVideoRequest, TelegramReactions>()
val TelegramTypeToken.videoNote get() = ChannelTypeToken<TelegramVideoNoteRequest, TelegramReactions>()
val TelegramTypeToken.voice get() = ChannelTypeToken<TelegramVoiceRequest, TelegramReactions>()
val TelegramTypeToken.preCheckout get() = ChannelTypeToken<TelegramPreCheckoutRequest, TelegramReactions>()
val TelegramTypeToken.successfulPayment get() = ChannelTypeToken<TelegramSuccessfulPaymentRequest, TelegramReactions>()
