package com.justai.jaicf.channel.alexa

import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.channel.alexa.activator.AlexaActivator
import com.justai.jaicf.channel.alexa.activator.AlexaIntentActivatorContext
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken

typealias AlexaTypeToken = ChannelTypeToken<AlexaBotRequest, AlexaReactions>

val alexa: AlexaTypeToken = ChannelTypeToken()

val AlexaTypeToken.intent get() = ContextTypeToken<AlexaIntentActivatorContext, AlexaIntentRequest, AlexaReactions>()
val AlexaTypeToken.event get() = ContextTypeToken<EventActivatorContext, AlexaEventRequest, AlexaReactions>()
