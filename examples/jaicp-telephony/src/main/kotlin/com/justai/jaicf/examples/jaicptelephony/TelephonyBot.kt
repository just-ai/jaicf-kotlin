package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.channel.jaicp.logging.JaicpConversationLogger
import com.justai.jaicf.logging.ConsoleConversationLogger
import io.ktor.client.features.logging.LogLevel

val cailaIntentActivator = CailaIntentActivator.Factory(nluSettings)

val telephonyCallScenario = BotEngine(
    TelephonyBotScenario.model,
    activators = arrayOf(
        BaseEventActivator,
        RegexActivator,
        cailaIntentActivator,
        CatchAllActivator
    ), conversationLoggers = arrayOf(
        ConsoleConversationLogger(),
        JaicpConversationLogger(accessToken, url = caUrl, logLevel = LogLevel.BODY)
    )
)
