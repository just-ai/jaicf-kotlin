package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.jaicp.channels.JaicpNativeChannelFactory
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.model.scenario.Scenario
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.content.*

class JaicpTestChannel(
    override val botApi: BotApi,
    factory: JaicpNativeChannelFactory
) : HttpBotChannel {

    constructor(scenario: Scenario, factory: JaicpNativeChannelFactory) : this(
        BotEngine(scenario, activators = arrayOf(RegexActivator)), factory
    )

    var mockHttpRequestBody: String? = null

    private fun getMockHttp() = HttpClient(MockEngine) {
        install(JsonFeature) { serializer = KotlinxSerializer(JSON) }
        engine {
            addHandler { request ->
                mockHttpRequestBody = (request.body as TextContent).text
                respond("ok")
            }
        }
    }

    private val channel = factory.create(botApi, ChatAdapterConnector("", "", getMockHttp()))

    override fun process(request: HttpBotRequest): HttpBotResponse {
        return channel.process(request)
    }
}