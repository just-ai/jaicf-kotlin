package com.justai.jaicf.channel.slack

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.gateway.BotGateway
import com.justai.jaicf.gateway.BotGatewayRequest
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.kotlin.PropertyWithBackingField
import com.slack.api.Slack
import com.slack.api.SlackConfig
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.middleware.builtin.IgnoringSelfEvents
import com.slack.api.bolt.request.RequestHeaders
import com.slack.api.bolt.util.SlackRequestParser
import com.slack.api.methods.MethodsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SlackChannel private constructor(
    override val botApi: BotApi
) : JaicpCompatibleAsyncBotChannel,
    BotGateway(),
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private lateinit var app: App
    private lateinit var parser: SlackRequestParser

    constructor(botApi: BotApi, config: SlackChannelConfig) : this(botApi) {
        val appConfig = AppConfig.builder()
            .singleTeamBotToken(config.botToken)
            .signingSecret(config.signingSecret)
            .build()
        app = App(appConfig, config.middleware)
        start()
    }

    private constructor(botApi: BotApi, urlPrefix: String) : this(botApi) {
        val config = SlackConfig().apply {
            methodsEndpointUrlPrefix = "$urlPrefix/".toUrl()
            methodsConfig = MethodsConfig().apply {
                isStatsEnabled = false
            }
        }

        val slack = Slack.getInstance(config)
        app = App(
            AppConfig.builder()
                .slack(slack)
                .alwaysRequestUserTokenNeeded(false)
                .singleTeamBotToken("empty")
                .signingSecret("empty")
                .build(),
            listOf(IgnoringSelfEvents(config))
        )

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

    override fun processGatewayRequest(request: BotGatewayRequest) {
        val gwRequest = SlackGatewayRequest.create(request) ?: return
        val slackRequest =
            buildSlackRequest(getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH).asHttpBotRequest())
        SlackGatewayRequest.create(request)
        botApi.process(
            gwRequest,
            SlackReactions(slackRequest.context),
            RequestContext.DEFAULT
        )
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val slackRequest = buildSlackRequest(request).apply { context.httpBotRequest = request }
        val slackResponse = app.run(slackRequest)

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

        private const val REQUEST_TEMPLATE_PATH = "/SlackRequestTemplate.json"
    }
}

internal var Context.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
