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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

class AliceBotContextManager : BotContextManager {

    private val mapper = jacksonObjectMapper().apply {
        activateDefaultTyping(
            polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.EVERYTHING,
            JsonTypeInfo.As.PROPERTY
        )
    }

    override fun loadContext(request: BotRequest): BotContext {
        return request.alice?.state?.let { state ->
            val client = state.user["com.justai.jaicf"]?.let { user ->
                mapper.readValue(user.toString(), MutableMap::class.java).mapKeys { it.key as String }
            }

            val session = state.session?.get("com.justai.jaicf")?.let { session ->
                mapper.readValue(session.toString(), MutableMap::class.java).mapKeys { it.key as String }
            }

            BotContext(
                clientId = request.clientId,
                dialogContext = session?.get("dialogContext") as? DialogContext ?: DialogContext()
            ).apply {
                this.result = session?.get("result")
                this.client.putAll(client ?: emptyMap())
                this.session.putAll(session ?: emptyMap())
            }
        } ?: BotContext(request.clientId, DialogContext())
    }

    override fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?) {
        (response as? AliceBotResponse)?.run {
            val model = BotContextModel(botContext)
            val json = JSON.decodeFromString<JsonObject>(mapper.writeValueAsString(model))
            val session = mutableMapOf<String, JsonElement>().apply {
                putAll(json["session"]!!.jsonObject)
                put("result", json["result"] ?: JsonNull)
                put("dialogContext", json["dialogContext"] ?: JsonNull)
            }.let {
                mapOf("com.justai.jaicf" to JsonObject(it))
            }

            userStateUpdate["com.justai.jaicf"] = json["client"]
            sessionState = JsonObject(sessionState?.plus(session) ?: session)
        }
    }
}