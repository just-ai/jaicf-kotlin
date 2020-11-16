package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.TelephonyBotRequest
import com.justai.jaicf.channel.jaicp.reactions.TelephonyReactions

/**
 * JAICP Telephony channel
 *
 * This channel can be used for processing incoming and outgoing calls.
 * TelephonyChannel can be configured only in JAICP Web Interface.
 * Caller information can be retrieved from [TelephonyBotRequest].
 * Telephony channel can receive channel-specific [TelephonyEvents].
 *
 * See example telephony bot: https://github.com/just-ai/jaicf-kotlin/tree/master/examples/jaicp-telephony
 *
 * @see TelephonyReactions
 * @see TelephonyBotRequest
 * @see TelephonyEvents
 * @see JaicpNativeChannel
 *
 * @param botApi the [BotApi] implementation used to process the requests to this channel
 * */
class TelephonyChannel(override val botApi: BotApi) : JaicpNativeChannel(botApi) {

    override fun createRequest(request: JaicpBotRequest) = TelephonyBotRequest(request)
    override fun createReactions() = TelephonyReactions()

    companion object : JaicpNativeChannelFactory {
        override val channelType = "resterisk"
        override fun create(botApi: BotApi) = TelephonyChannel(botApi)
    }
}