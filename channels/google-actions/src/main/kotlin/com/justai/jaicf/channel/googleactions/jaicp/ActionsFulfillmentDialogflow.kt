package com.justai.jaicf.channel.googleactions.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.googleactions.ActionsFulfillment
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory

object ActionsFulfillmentDialogflow : JaicpCompatibleChannelFactory {
    override val channelType = "dialogflow"
    override fun create(botApi: BotApi) = ActionsFulfillment.dialogflow(botApi)
}