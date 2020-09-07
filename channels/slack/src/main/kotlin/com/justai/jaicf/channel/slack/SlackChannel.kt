package com.justai.jaicf.channel.slack

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.kotlin.NoBackingFieldProperty
import com.slack.api.Slack
import com.slack.api.SlackConfig
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.request.RequestHeaders
import com.slack.api.bolt.util.SlackRequestParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SlackChannel private constructor(
    override val botApi: BotApi
) : JaicpCompatibleAsyncBotChannel,
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private lateinit var app: App
    private lateinit var parser: SlackRequestParser

    constructor(botApi: BotApi, config: SlackChannelConfig): this(botApi) {
        app = App(AppConfig.builder()
            .singleTeamBotToken(config.botToken)
            .signingSecret(config.signingSecret)
            .build()
        )
        start()
    }

    private constructor(botApi: BotApi, urlPrefix: String): this(botApi) {
        val config = SlackConfig().apply {
            methodsEndpointUrlPrefix = urlPrefix
        }

        val slack = Slack.getInstance(config)
        app = App(AppConfig.builder().slack(slack).build())
        start()
    }

    private fun start() {
        parser = SlackRequestParser(app.config())

        app.command(".*".toPattern()) { req, ctx ->
            launch(SlackCommandRequest(req.payload), SlackReactions(ctx), ctx.httpBotRequest)
            ctx.ack()
        }

        app.message(".*".toPattern()) { payload, ctx ->
            launch(SlackEventRequest(payload), SlackReactions(ctx), ctx.httpBotRequest)
            ctx.ack()
        }

        app.blockAction(".*".toPattern()) { req, ctx ->
            if (!req.payload.responseUrl.isNullOrEmpty()) {
                launch(SlackActionRequest(req.payload), SlackReactions(ctx), ctx.httpBotRequest)
            }
            ctx.ack()
        }

        slackEvents.forEach {
            app.event(it) { payload, ctx ->
                launch(SlackEventRequest(payload), SlackReactions(ctx), ctx.httpBotRequest)
                ctx.ack()
            }
        }

        app.start()
    }

    private fun launch(request: SlackBotRequest, reactions: SlackReactions, httpBotRequest: HttpBotRequest) = launch {
        botApi.process(request, reactions, RequestContext.fromHttp(httpBotRequest))
    }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val slackRequest = buildSlackRequest(request)
        val slackResponse = app.run(slackRequest)
        slackRequest.context.httpBotRequest = request

        return HttpBotResponse(slackResponse.body ?: "", slackResponse.contentType).apply {
            headers.putAll(slackResponse.headers.mapValues { it.value.first() })
        }
    }

    private fun buildSlackRequest(req: HttpBotRequest) = SlackRequestParser.HttpRequest.builder()
        .queryString(req.parameters.toMutableMap())
        .headers(RequestHeaders(req.headers.toMutableMap()))
        .requestBody(req.receiveText())
        .build()
        .let { parser.parse(it) }


    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "slack"
        override fun create(botApi: BotApi, apiUrl: String) = SlackChannel(botApi, apiUrl)
    }
}

internal var Context.httpBotRequest: HttpBotRequest by NoBackingFieldProperty {
    HttpBotRequest("".byteInputStream())
}
