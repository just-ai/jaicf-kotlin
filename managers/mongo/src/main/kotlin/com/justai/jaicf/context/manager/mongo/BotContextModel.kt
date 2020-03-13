package com.justai.jaicf.context.manager.mongo

import com.justai.jaicf.context.DialogContext

data class BotContextModel(
    val _id: String,

    val result: Any?,

    val client: Map<String, Any?>,
    val session: Map<String, Any?>,
    val dialogContext: DialogContext
)