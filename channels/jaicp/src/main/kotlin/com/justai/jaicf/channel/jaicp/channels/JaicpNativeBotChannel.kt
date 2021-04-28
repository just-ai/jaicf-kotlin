package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.jaicp.JaicpBotChannel
import com.justai.jaicf.channel.jaicp.JaicpChannelFactory
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpResponse

/**
 * Interface for JAICP Native Channels, which are:
 * 1. [TelephonyChannel]
 * 2. [ChatApiChannel]
 * 3. [ChatWidgetChannel]
 *
 * These channels communicate through [JaicpBotRequest] and [JaicpBotResponse].
 * All channels are synchronous and use channel-specific reactions.
 * These channels can only be configured in JAICP Web Interface.
 *
 * @see TelephonyChannel
 * @see ChatApiChannel
 * @see ChatWidgetChannel
 * @see JaicpNativeChannel
 */
interface JaicpNativeBotChannel : JaicpBotChannel, HttpBotChannel {
    fun process(request: JaicpBotRequest): JaicpResponse
}

interface JaicpNativeChannelFactory : JaicpChannelFactory {
    fun create(botApi: BotApi): JaicpNativeBotChannel
    override val channelType: String
}
