package com.justai.jaicf.context.manager.mapdb

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import org.mapdb.DBMaker
import org.mapdb.Serializer

@Deprecated(
    "MapDbBotContextManager is deprecated. Use JacksonMapDbBotContextManager with the new dbFilePath instead, because the database format of this manager is incompatible with JacksonMapDbBotContextManager"
)
class MapDbBotContextManager(dbFilePath: String? = null) : BotContextManager {

    private val db = dbFilePath?.let { DBMaker.fileDB(it).closeOnJvmShutdown().make() } ?: DBMaker.tempFileDB().closeOnJvmShutdown().make()

    private val map = db.hashMap("contexts", Serializer.STRING, Serializer.JAVA).createOrOpen()

    override fun loadContext(request: BotRequest, requestContext: RequestContext): BotContext {
        val model = map[request.clientId] as? BotContextModel ?: return BotContext(request.clientId)

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
        map[botContext.clientId] = BotContextModel(botContext)
        db.commit()
    }

    fun close() = db.close()
}
