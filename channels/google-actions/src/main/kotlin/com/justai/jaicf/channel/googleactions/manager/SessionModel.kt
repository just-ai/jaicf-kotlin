package com.justai.jaicf.channel.googleactions.manager

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext

data class SessionModel(
    val result: Any?,
    val session: Map<String, Any?>,
    val dialogContext: DialogContext?
) {
    constructor(botContext: BotContext): this(
        result = botContext.result,
        session = botContext.session.toMutableMap(),
        dialogContext = botContext.dialogContext
    )
}