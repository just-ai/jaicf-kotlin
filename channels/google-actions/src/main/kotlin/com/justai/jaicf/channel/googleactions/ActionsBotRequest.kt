package com.justai.jaicf.channel.googleactions

import com.google.actions.api.ActionRequest
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.IntentBotRequest
import com.justai.jaicf.api.QueryBotRequest

const val ACTIONS_USER_ID = "user_id"

val BotRequest.actions
    get() = this as? ActionsBotRequest

interface ActionsBotRequest: BotRequest {
    val request: ActionRequest
}

data class ActionsIntentRequest(
    override val request: ActionRequest
): ActionsBotRequest, IntentBotRequest(
    clientId = request.user?.userId ?: request.userStorage[ACTIONS_USER_ID] as String,
    input = request.intent
)

data class ActionsTextRequest(
    override val request: ActionRequest
): ActionsBotRequest, QueryBotRequest(
    clientId = request.user?.userId ?: request.userStorage[ACTIONS_USER_ID] as String,
    input = request.rawText!!
)