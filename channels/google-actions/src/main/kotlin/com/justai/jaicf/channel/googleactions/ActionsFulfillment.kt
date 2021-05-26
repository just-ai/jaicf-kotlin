package com.justai.jaicf.channel.googleactions

import com.google.actions.api.ActionResponse
import com.google.actions.api.ActionsSdkApp
import com.google.actions.api.DefaultApp
import com.google.actions.api.DialogflowApp
import com.google.api.services.actions_fulfillment.v2.model.RichResponseItem
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.googleactions.manager.ActionsBotContextManager
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class ActionsFulfillment private constructor(
    override val botApi: BotApi,
    private val app: DefaultApp,
    useDataStorage: Boolean = false,
) : JaicpCompatibleBotChannel {

    private val contextManager = useDataStorage.takeIf { it }?.let { ActionsBotContextManager() }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val actionRequest = app.createRequest(request.receiveText(), mapOf<String, String>()).apply {
            userStorage.putIfAbsent(ACTIONS_USER_ID, UUID.randomUUID().toString())
        }

        val responseBuilder = app.getResponseBuilder(actionRequest)
        val botRequest = when (actionRequest.intent) {
            TEXT_INTENT -> ActionsTextRequest(actionRequest)
            else -> ActionsIntentRequest(actionRequest)
        }

        val response = ActionsBotResponse(responseBuilder)
        val reactions = ActionsReactions(actionRequest, response)

        botApi.process(botRequest, reactions, RequestContext.fromHttp(request), contextManager)

        return reactions.response.builder.build().arrange().toJson().asJsonHttpBotResponse()
    }

    companion object {
        private const val TEXT_INTENT = "actions.intent.TEXT"

        val logger: Logger = LoggerFactory.getLogger("ActionsFulfillment")

        fun sdk(botApi: BotApi, useDataStorage: Boolean = false) =
            ActionsFulfillment(botApi, ActionsSdkApp(), useDataStorage)

        fun dialogflow(botApi: BotApi, useDataStorage: Boolean = false) =
            ActionsFulfillment(botApi, DialogflowApp(), useDataStorage)
    }
}

private fun ActionResponse.arrange() = apply {
    this.richResponse?.items?.let {
        val simpleItem: RichResponseItem = it.reduceSimpleResponses()
            ?: run {
                ActionsFulfillment.logger.warn("Any google action block must call at least once reactions.say()")
                RichResponseItem().setSimpleResponse(SimpleResponse().setDisplayText(" ").setSsml(" "))
            }

        val complexItems = it.filter { item -> item.simpleResponse == null }
            .also { items ->
                if (items.size > 1)
                    ActionsFulfillment.logger.warn("""
                        In any google action block, a maximum of one response with an 
                        image, audio, or other rich media response is allowed""".trimIndent()
                    )
            }

        this.richResponse?.items = listOf(simpleItem) + complexItems
        this.webhookResponse?.fulfillmentText = simpleItem.simpleResponse.displayText
    }
}

private fun List<RichResponseItem>.reduceSimpleResponses(): RichResponseItem? =
    mapNotNull { item -> item.simpleResponse }
        .ifEmpty { return null }
        .fold(SimpleResponse()) { acc, response ->
            acc.displayText = acc.displayText?.plus("  \n${response.displayText}") ?: response.displayText
            acc.ssml = acc.ssml?.plus(" ${response.ssml}") ?: response.ssml
            acc.textToSpeech = acc.textToSpeech?.plus(" ${response.textToSpeech}") ?: response.textToSpeech
            acc
        }
        .also { acc ->
            acc.ssml?.let {
                acc.ssml = "<speak>" + (acc.ssml) + "</speak>"
            }
        }
        .let(RichResponseItem()::setSimpleResponse)
