package com.justai.jaicf.channel.yandexalice.manager

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext

data class BotContextModel(
    val result: Any?,
    val client: Map<String, Any?>,
    val session: Map<String, Any?>,
    val dialogContext: DialogContext?
) {
    constructor(botContext: BotContext): this(
        result = botContext.result,
        client = botContext.client.toMutableMap(),
        session = botContext.session.toMutableMap(),
        dialogContext = botContext.dialogContext
    )
}