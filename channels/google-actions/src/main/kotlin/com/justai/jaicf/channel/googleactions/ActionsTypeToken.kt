package com.justai.jaicf.channel.googleactions

import com.justai.jaicf.channel.googleactions.dialogflow.ActionsDialogflowActivatorContext
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken

typealias ActionsTypeToken = ChannelTypeToken<ActionsBotRequest, ActionsReactions>

val googleActions: ActionsTypeToken = ChannelTypeToken()

val ActionsTypeToken.intent get() = ContextTypeToken<ActionsDialogflowActivatorContext, ActionsIntentRequest, ActionsReactions>()
val ActionsTypeToken.text get() = ChannelTypeToken<ActionsTextRequest, ActionsReactions>()