package com.justai.jaicf.context.manager.mapdb

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import java.io.Serializable

internal class BotContextModel private constructor(): Serializable {

    var result: Any? = null
    lateinit var dialogContext: DialogContext
    lateinit var client: Map<String, Any?>
    lateinit var session: Map<String, Any?>

    companion object {
        private const val serialVersionUID = -8182528581552214215L

        fun create(botContext: BotContext) = BotContextModel().apply {
            dialogContext = botContext.dialogContext
            result = botContext.result
            client = botContext.client.toMap()
            session = botContext.session.toMap()
        }
    }
}