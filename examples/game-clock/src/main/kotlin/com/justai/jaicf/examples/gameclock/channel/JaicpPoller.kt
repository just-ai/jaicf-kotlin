package com.justai.jaicf.examples.gameclock.channel

import com.justai.jaicf.channel.alexa.AlexaChannel
import com.justai.jaicf.channel.googleactions.jaicp.ActionsFulfillmentDialogflow
import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.examples.gameclock.gameClockBot

fun main() {
    val accessToken =
        System.getenv("JAICP_API_TOKEN")
            ?: print("Enter your JAICP project API key: ").run { readLine() }

    accessToken?.let {
        JaicpPollingConnector(
            botApi = gameClockBot,
            accessToken = accessToken,
            channels = listOf(
                AlexaChannel,
                ActionsFulfillmentDialogflow
            )
        ).runBlocking()
    }
}