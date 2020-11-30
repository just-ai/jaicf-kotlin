package com.justai.jaicf.channel.alexa.manager

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.channel.alexa.alexa
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.manager.BotContextManager

class AlexaBotContextManager: BotContextManager {

    private val mapper = jacksonObjectMapper().apply {
        activateDefaultTyping(
            polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.EVERYTHING,
            JsonTypeInfo.As.PROPERTY
        )
    }

    override fun loadContext(request: BotRequest): BotContext {
        return request.alexa?.handlerInput?.attributesManager?.let { attributesManager ->
            val client = attributesManager.persistentAttributes[ATTR_NAME]?.let { user ->
                mapper.readValue(user.toString(), MutableMap::class.java).mapKeys { it.key as String }
            }

            val session = attributesManager.sessionAttributes[ATTR_NAME]?.let { session ->
                mapper.readValue(session.toString(), SessionModel::class.java)
            }

            BotContext(
                clientId = request.clientId,
                dialogContext = session?.dialogContext ?: DialogContext()
            ).apply {
                this.result = session?.result
                this.client.putAll(client ?: emptyMap())
                this.session.putAll(session?.session ?: emptyMap())
            }
        } ?: BotContext(request.clientId, DialogContext())
    }

    override fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?) {
        request?.alexa?.handlerInput?.attributesManager?.let { attributesManager ->
            val session = mapper.writeValueAsString(SessionModel(botContext))
            val client = mapper.writeValueAsString(botContext.client.toMutableMap())
            attributesManager.sessionAttributes[ATTR_NAME] = session
            attributesManager.persistentAttributes = mapOf(ATTR_NAME to client)
            attributesManager.savePersistentAttributes()
        }
    }

    companion object {
        private const val ATTR_NAME = "com.justai.jaicf"
    }
}