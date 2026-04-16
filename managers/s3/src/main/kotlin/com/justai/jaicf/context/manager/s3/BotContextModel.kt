package com.justai.jaicf.context.manager.s3

import com.justai.jaicf.context.DialogContext

data class BotContextModel(
    val clientId: String,

    val result: Any?,

    val client: Map<String, Any?>,
    val session: Map<String, Any?>,
    val dialogContext: DialogContext
)