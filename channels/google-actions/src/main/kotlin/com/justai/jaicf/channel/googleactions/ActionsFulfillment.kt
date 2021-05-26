package com.justai.jaicf.channel.googleactions

import com.google.actions.api.ActionsSdkApp
import com.google.actions.api.DefaultApp
import com.google.actions.api.DialogflowApp
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.googleactions.manager.ActionsBotContextManager
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.context.RequestContext
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

        return reactions.response.builder.build()
            .withInternalRestructuring()
            .toJson()
            .asJsonHttpBotResponse()
    }

    companion object {
        private const val TEXT_INTENT = "actions.intent.TEXT"

        fun sdk(botApi: BotApi, useDataStorage: Boolean = false) =
            ActionsFulfillment(botApi, ActionsSdkApp(), useDataStorage)

        fun dialogflow(botApi: BotApi, useDataStorage: Boolean = false) =
            ActionsFulfillment(botApi, DialogflowApp(), useDataStorage)
    }
}
