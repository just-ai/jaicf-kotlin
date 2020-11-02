package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.channel.jaicp.logging.JaicpConversationLogger

val cailaIntentActivator = CailaIntentActivator.Factory(nluSettings)

val telephonyCallScenario = BotEngine(
    TelephonyBotScenario.model,
    activators = arrayOf(
        cailaIntentActivator,
        RegexActivator
    ),
    conversationLoggers = arrayOf(JaicpConversationLogger(accessToken))
)
