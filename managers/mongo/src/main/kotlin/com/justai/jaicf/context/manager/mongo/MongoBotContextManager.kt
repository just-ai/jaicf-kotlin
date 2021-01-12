package com.justai.jaicf.context.manager.mongo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import org.bson.Document

class MongoBotContextManager(
    private val collection: MongoCollection<Document>
): BotContextManager {

    @Suppress("DEPRECATION")
    private val mapper = jacksonObjectMapper().enableDefaultTyping()

    override fun loadContext(request: BotRequest, requestContext: RequestContext): BotContext {
        return collection
            .find(Filters.eq("_id", request.clientId))
            .iterator().tryNext()?.let { doc ->
                val model = mapper.readValue(doc.toJson(), BotContextModel::class.java)

                BotContext(model._id, model.dialogContext).apply {
                    result = model.result
                    client.putAll(model.client)
                    session.putAll(model.session)
                }

        } ?: BotContext(request.clientId)
    }

    override fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?) {
        BotContextModel(
            _id = botContext.clientId,
            result = botContext.result,
            client = botContext.client.toMap(),
            session = botContext.session.toMap(),
            dialogContext = botContext.dialogContext
        ).apply {
            val doc = Document.parse(mapper.writeValueAsString(this))
            collection.replaceOne(Filters.eq("_id", _id), doc, UpdateOptions().upsert(true))
        }
    }
}