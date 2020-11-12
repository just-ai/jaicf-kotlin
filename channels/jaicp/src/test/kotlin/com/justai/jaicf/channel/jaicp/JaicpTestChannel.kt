package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.model.scenario.Scenario

class JaicpTestChannel(
    override val botApi: BotApi,
    factory: JaicpNativeChannelFactory
) : HttpBotChannel {

    constructor(scenario: Scenario, factory: JaicpNativeChannelFactory) : this(BotEngine(scenario.model), factory)

    private val channel = factory.create(botApi)

    override fun process(request: HttpBotRequest): HttpBotResponse {
        return channel.process(request) ?: error("No bot response")
    }
}