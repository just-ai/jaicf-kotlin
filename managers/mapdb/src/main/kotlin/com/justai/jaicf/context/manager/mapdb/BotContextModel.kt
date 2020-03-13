package com.justai.jaicf.context.manager.mapdb

import com.justai.jaicf.context.BotContext
import java.io.Serializable

class BotContextModel(botContext: BotContext): Serializable {
    val dialogContext = botContext.dialogContext
    val result: Any? = botContext.result
    val client = botContext.client.toMap()
    val session = botContext.session.toMap()
}