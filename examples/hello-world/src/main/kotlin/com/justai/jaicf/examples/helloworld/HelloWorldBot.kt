package com.justai.jaicf.examples.helloworld

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.dialogflow.DialogflowAgentConfig
import com.justai.jaicf.activator.dialogflow.DialogflowConnector
import com.justai.jaicf.activator.dialogflow.DialogflowIntentActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.channel.alexa.activator.AlexaActivator

val dialogflowActivator = DialogflowIntentActivator.Factory(
    DialogflowConnector(DialogflowAgentConfig(
        language = "en",
        credentialsResourcePath = "/dialogflow_account.json"
    ))
)

val helloWorldBot = BotEngine(
    model = HelloWorldScenario.model,
    activators = arrayOf(
        AlexaActivator,
        dialogflowActivator,
        RegexActivator
    )
)