package com.justai.jaicf.channel.googleactions.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.googleactions.ActionsFulfillment
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory

class ActionsFulfillmentSDK(
    private val useDataStorage: Boolean = false
) : JaicpCompatibleChannelFactory {
    override val channelType = "google"
    override fun create(botApi: BotApi) = ActionsFulfillment.sdk(botApi, useDataStorage)

    companion object : JaicpCompatibleChannelFactory {
        override val channelType = "google"
        override fun create(botApi: BotApi) = ActionsFulfillmentSDK().create(botApi)
    }
}