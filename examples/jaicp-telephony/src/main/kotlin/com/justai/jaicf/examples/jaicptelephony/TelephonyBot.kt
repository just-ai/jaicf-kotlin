package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.channel.jaicp.logging.JaicpConversationLogger

val cailaIntentActivator = CailaIntentActivator.Factory(nluSettings)

val telephonyCallScenario = BotEngine(
    TelephonyBotScenario.model,
    activators = arrayOf(
        BaseEventActivator,
        RegexActivator,
        cailaIntentActivator,
        CatchAllActivator
    ),
    conversationLoggers = arrayOf(JaicpConversationLogger(accessToken))
)
