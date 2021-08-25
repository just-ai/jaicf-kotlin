package com.justai.jaicf.channel.googleactions.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.googleactions.ActionsFulfillment
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory

class ActionsFulfillmentDialogflow(
    private val useDataStorage: Boolean = false
) : JaicpCompatibleChannelFactory {
    override val channelType = "dialogflow"
    override fun create(botApi: BotApi) = ActionsFulfillment.dialogflow(botApi, useDataStorage)

    companion object : JaicpCompatibleChannelFactory by ActionsFulfillmentDialogflow()
}
