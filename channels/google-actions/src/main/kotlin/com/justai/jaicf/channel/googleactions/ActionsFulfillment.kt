package com.justai.jaicf.channel.googleactions

import com.google.actions.api.ActionsSdkApp
import com.google.actions.api.DefaultApp
import com.google.actions.api.DialogflowApp
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import java.util.*

class ActionsFulfillment private constructor(
    override val botApi: BotApi,
    private val app: DefaultApp
) : JaicpCompatibleBotChannel {

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val actionRequest = app.createRequest(request.receiveText(), mapOf<String, String>()).apply {
            userStorage.putIfAbsent(ACTIONS_USER_ID, UUID.randomUUID().toString())
        }

        val responseBuilder = app.getResponseBuilder(actionRequest)
        val botRequest = when(actionRequest.intent) {
            TEXT_INTENT -> ActionsTextRequest(actionRequest)
            else -> ActionsIntentRequest(actionRequest)
        }

        val response = ActionsBotResponse(responseBuilder)
        val reactions = ActionsReactions(actionRequest, response)

        botApi.process(botRequest, reactions)

        return reactions.response.builder.build().toJson().asJsonHttpBotResponse()
    }

    companion object {
        private const val TEXT_INTENT = "actions.intent.TEXT"

        fun sdk(botApi: BotApi) = ActionsFulfillment(botApi, ActionsSdkApp())
        fun dialogflow(botApi: BotApi) = ActionsFulfillment(botApi, DialogflowApp())
    }

    object ActionsFulfillmentDialogflow : JaicpCompatibleChannelFactory {
        override val channelType = "google"
        override fun create(botApi: BotApi) = dialogflow(botApi)
    }

    object ActionsFulfillmentSDK : JaicpCompatibleChannelFactory {
        override val channelType = "google"
        override fun create(botApi: BotApi) = sdk(botApi)
    }
}
