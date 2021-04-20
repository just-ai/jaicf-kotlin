package com.justai.jaicf.examples.helloworld

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.dialogflow.DialogflowAgentConfig
import com.justai.jaicf.activator.dialogflow.DialogflowConnector
import com.justai.jaicf.activator.dialogflow.DialogflowIntentActivator
import com.justai.jaicf.activator.lex.LexBotConfig
import com.justai.jaicf.activator.lex.LexConnector
import com.justai.jaicf.activator.lex.LexIntentActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.channel.alexa.activator.AlexaActivator
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import java.util.*

val dialogflowActivator = DialogflowIntentActivator.Factory(
    DialogflowConnector(
        DialogflowAgentConfig(
            language = "en",
            credentialsResourcePath = "/dialogflow_account.json"
        )
    )
)

val lexActivator = LexIntentActivator.Factory(
    LexConnector(
        LexBotConfig(
            "FEM4TENNMH",
            "DPWGWCXTJA",
            Region.EU_WEST_2,
            Locale.US
        ),
        AwsBasicCredentials.create(
            "AKIA3RQMQLZRJZGQEUZ7",
            "0rocE4nYItJ0Vmtimlg7rzQmLXTkFIe/eiCH0E6P"
        )
    )
)

val helloWorldBot = BotEngine(
    scenario = HelloWorldScenario,
    activators = arrayOf(
        AlexaActivator,
        dialogflowActivator,
        lexActivator,
        RegexActivator
    )
)
