package com.justai.jaicf.channel.slack

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.ContentType
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.invocationapi.getRequestTemplateFromResources
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.context.RequestContext
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
import java.util.*
import java.util.concurrent.TimeUnit

class SlackChannel private constructor(
    override val botApi: BotApi
) : JaicpCompatibleAsyncBotChannel,
    InvocableBotChannel,
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private lateinit var app: App
    private lateinit var parser: SlackRequestParser
    private var liveChatProvider: JaicpLiveChatProvider? = null

    constructor(botApi: BotApi, config: SlackChannelConfig) : this(botApi) {
        val appConfig = AppConfig.builder()
            .singleTeamBotToken(config.botToken)
            .signingSecret(config.signingSecret)
            .build()
        app = App(appConfig, config.middleware)
        start()
    }

    private constructor(botApi: BotApi, urlPrefix: String, liveChatProvider: JaicpLiveChatProvider) : this(botApi) {
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
        this.liveChatProvider = liveChatProvider

        start()
    }

    private fun start() {
        parser = SlackRequestParser(app.config())

        app.command(".*".toPattern()) { req, ctx ->
            launch(SlackCommandRequest(req.payload), SlackReactions(ctx, liveChatProvider), ctx.httpBotRequest)
            ctx.ack()
        }

        app.message(".*".toPattern()) { payload, ctx ->
            launch(SlackEventRequest(payload), SlackReactions(ctx, liveChatProvider), ctx.httpBotRequest)
            ctx.ack()
        }

        app.blockAction(".*".toPattern()) { req, ctx ->
            if (!req.payload.responseUrl.isNullOrEmpty()) {
                launch(SlackActionRequest(req.payload), SlackReactions(ctx, liveChatProvider), ctx.httpBotRequest)
            }
            ctx.ack()
        }

        slackEvents.forEach {
            app.event(it) { payload, ctx ->
                launch(SlackEventRequest(payload), SlackReactions(ctx, liveChatProvider), ctx.httpBotRequest)
                ctx.ack()
            }
        }

        app.start()
    }

    private fun launch(request: SlackBotRequest, reactions: SlackReactions, httpBotRequest: HttpBotRequest) = launch {
        botApi.process(request, reactions, RequestContext.fromHttp(httpBotRequest))
    }

    private fun generateRequestFromTemplate(request: InvocationRequest) =
        getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
            .replace("\"{{ timestamp }}\"", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
            .replace("{{ messageId }}", UUID.randomUUID().toString())

    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val invocationRequest = SlackInvocationRequest.create(request) ?: return
        val slackRequest = buildSlackRequest(generateRequestFromTemplate(request).asHttpBotRequest())
        botApi.process(invocationRequest, SlackReactions(slackRequest.context, liveChatProvider), requestContext)
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val slackRequest = buildSlackRequest(request).apply { context.httpBotRequest = request }
        val slackResponse = app.run(slackRequest)

        return HttpBotResponse(
            slackResponse.body ?: "",
            ContentType.parse(slackResponse.contentType)
        ).apply {
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
        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ) = SlackChannel(botApi, apiUrl, liveChatProvider)

        private const val REQUEST_TEMPLATE_PATH = "/SlackRequestTemplate.json"
    }
}

internal var Context.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
