package com.justai.jaicf.examples.viber

import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.viber.ViberChannel

fun main() {
    val accessToken =
        System.getenv("JAICP_API_TOKEN")
            ?: print("Enter your JAICP project API key: ").run { readLine() }

    accessToken?.let {
        JaicpPollingConnector(
            botApi = viberTestBot,
            accessToken = accessToken,
            channels = listOf(
                ViberChannel.Factory()
            ),
            url = "http://jaicf-test.lo.test-ai.net/chatadapter"
        ).runBlocking()
    }
}
