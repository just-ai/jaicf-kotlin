package com.justai.jaicf.channel.googleactions.manager

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.channel.googleactions.ActionsBotResponse
import com.justai.jaicf.channel.googleactions.actions
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.manager.BotContextManager

class ActionsBotContextManager: BotContextManager {

    private val mapper = jacksonObjectMapper().apply {
        activateDefaultTyping(
            polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.EVERYTHING,
            JsonTypeInfo.As.PROPERTY
        )
    }

    override fun loadContext(request: BotRequest): BotContext {
        return request.actions?.request?.let { actionRequest ->
            val client = actionRequest.userStorage[ATTR_NAME]?.let { user ->
                mapper.readValue(user.toString(), MutableMap::class.java).mapKeys { it.key as String }
            }

            val session = actionRequest.conversationData[ATTR_NAME]?.let { session ->
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
        val actionsResponse = response as? ActionsBotResponse
        actionsResponse?.builder?.let { builder ->
            val session = mapper.writeValueAsString(SessionModel(botContext))
            val client = mapper.writeValueAsString(botContext.client.toMutableMap())
            builder.userStorage?.put(ATTR_NAME, client)
            builder.conversationData?.put(ATTR_NAME, session)
        }
    }

    companion object {
        private const val ATTR_NAME = "com.justai.jaicf"
    }
}