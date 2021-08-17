package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.aimybox.AimyboxChannel
import com.justai.jaicf.channel.alexa.AlexaChannel
import com.justai.jaicf.channel.facebook.FacebookChannel
import com.justai.jaicf.channel.googleactions.jaicp.ActionsFulfillmentDialogflow
import com.justai.jaicf.channel.jaicp.JaicpServer
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.helloworld.helloWorldBot

fun main() {
    val accessToken =
        System.getenv("JAICP_API_TOKEN")
            ?: print("Enter your JAICP project API key: ").run { readLine() }

    accessToken?.let {
        JaicpServer(
            botApi = helloWorldBot,
            accessToken = accessToken,
            channels = listOf(
                ChatWidgetChannel,
                TelegramChannel,
                FacebookChannel,
                AimyboxChannel,
                AlexaChannel,
                ActionsFulfillmentDialogflow
            )
        ).start()
    }
}
