package com.justai.jaicf.context.manager.mapdb

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import java.io.Serializable

class BotContextModel(botContext: BotContext): Serializable {
    val dialogContext = botContext.dialogContext
    val result: Any? = botContext.result
    val client = botContext.client.toMap()
    val session = botContext.session.toMap()

    companion object {
        private const val serialVersionUID = -8182528581552214215L
    }
}
