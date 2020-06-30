package com.justai.jaicf.channel.yandexalice.manager

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.channel.yandexalice.JSON
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.api.alice
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.manager.BotContextManager
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

class AliceBotContextManager: BotContextManager {

    private val mapper = jacksonObjectMapper().apply {
        activateDefaultTyping(
            polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.EVERYTHING,
            JsonTypeInfo.As.PROPERTY
        )
    }

    override fun loadContext(request: BotRequest): BotContext {
        return request.alice?.state?.let { state ->
            val client = state.user.takeIf { it.isNotEmpty() }?.let {
                mapper.readValue(JsonObject(state.user).toString(), MutableMap::class.java)
            }?.mapKeys { it.key as String }

            val session = state.session?.takeIf { it.isNotEmpty() }?.let {
                mapper.readValue(it.toString(), MutableMap::class.java)
            }?.mapKeys { it.key as String }

            BotContext(
                clientId = request.clientId,
                dialogContext = session?.get("_dialog_context") as? DialogContext ?: DialogContext()
            ).apply {
                this.result = session?.get("_result")
                this.client.putAll(client ?: emptyMap())
                this.session.putAll(session ?: emptyMap())
            }
        } ?: BotContext(request.clientId, DialogContext())
    }

    override fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?) {
        (response as? AliceBotResponse)?.run {
            val model = BotContextModel(botContext)
            val json = JSON.parseJson(mapper.writeValueAsString(model)).jsonObject
            val sessionMap = mutableMapOf<String, JsonElement>().apply {
                putAll(json.getObject("session").content)
                put("_result", json["result"] ?: JsonNull)
                put("_dialog_context", json["dialogContext"] ?: JsonNull)
            }
            userStateUpdate.putAll(json.getObject("client").content)
            sessionState = JsonObject(sessionState?.content?.plus(sessionMap) ?: sessionMap)
        }
    }
}