package com.justai.jaicf.context.manager.mapdb

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import org.mapdb.DBMaker
import org.mapdb.Serializer

class JacksonMapDbBotContextManager(dbFilePath: String? = null) : BotContextManager {

    private val db = dbFilePath?.let { DBMaker.fileDB(it).make() } ?: DBMaker.tempFileDB().make()

    private val map = db.hashMap("contexts", Serializer.STRING, Serializer.STRING).createOrOpen()

    private val mapper = jacksonObjectMapper().apply {
        activateDefaultTyping(
            polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.EVERYTHING,
            JsonTypeInfo.As.PROPERTY
        )
    }

    override fun loadContext(request: BotRequest, requestContext: RequestContext): BotContext {
        val json = map[request.clientId] ?: return BotContext(request.clientId)
        val model = mapper.readValue<JacksonBotContextModel>(json)

        return BotContext(request.clientId, model.dialogContext).apply {
            result = model.result
            client.putAll(model.client)
            session.putAll(model.session)
        }
    }

    override fun saveContext(
        botContext: BotContext,
        request: BotRequest?,
        response: BotResponse?,
        requestContext: RequestContext
    ) {
        val model = JacksonBotContextModel.create(botContext)
        map[botContext.clientId] = mapper.writeValueAsString(model)
        db.commit()
    }

    fun close() = db.close()
}

private class JacksonBotContextModel {
    var result: Any? = null
    lateinit var dialogContext: DialogContext
    lateinit var client: Map<String, Any?>
    lateinit var session: Map<String, Any?>

    companion object {
        fun create(botContext: BotContext) = JacksonBotContextModel().apply {
            dialogContext = botContext.dialogContext
            result = botContext.result
            client = botContext.client.toMap()
            session = botContext.session.toMap()
        }
    }
}
