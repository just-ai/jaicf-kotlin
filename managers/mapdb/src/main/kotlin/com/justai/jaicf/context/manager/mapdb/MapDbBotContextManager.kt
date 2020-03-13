package com.justai.jaicf.context.manager.mapdb

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.manager.BotContextManager
import org.mapdb.DBMaker
import org.mapdb.Serializer

class MapDbBotContextManager(dbFilePath: String? = null): BotContextManager {

    private val db = dbFilePath?.let { DBMaker.fileDB(it).make() } ?: DBMaker.tempFileDB().make()

    private val map = db.hashMap("contexts", Serializer.STRING, Serializer.JAVA).createOrOpen()

    override fun loadContext(clientId: String): BotContext {
        val model = map[clientId] as? BotContextModel ?: return BotContext(clientId)

        return BotContext(clientId, model.dialogContext).apply {
            result = model.result
            client.putAll(model.client)
            session.putAll(model.session)
        }
    }

    override fun saveContext(botContext: BotContext) {
        map[botContext.clientId] = BotContextModel(botContext)
        db.commit()
    }

    fun close() = db.close()

}