package com.justai.jaicf.context.manager.mapdb

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.manager.BotContextManager
import org.mapdb.DBMaker
import org.mapdb.Serializer

class MapDbBotContextManager(dbFilePath: String? = null): BotContextManager {

    private val db = dbFilePath?.let { DBMaker.fileDB(it).make() } ?: DBMaker.tempFileDB().make()

    private val map = db.hashMap("contexts", Serializer.STRING, Serializer.JAVA).createOrOpen()

    override fun loadContext(request: BotRequest): BotContext {
        val model = map[request.clientId] as? BotContextModel ?: return BotContext(request.clientId)

        return BotContext(request.clientId, model.dialogContext).apply {
            result = model.result
            client.putAll(model.client)
            session.putAll(model.session)
        }
    }

    override fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?) {
        map[botContext.clientId] = BotContextModel(botContext)
        db.commit()
    }

    fun close() = db.close()

}