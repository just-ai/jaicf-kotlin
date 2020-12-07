package com.justai.jaicf.channel.jaicp.context

import com.justai.jaicf.context.BotContext

internal interface ContextSerializable {
    fun serialized(): String

    val service: ContextSerializableService<*>

    fun saveToContext(ctx: BotContext) = ctx.session.put(service.key, serialized())

    fun removeFromContext(ctx: BotContext) = ctx.session.remove(service.key)
}

internal interface ContextSerializableService<out T : ContextSerializable> {
    val key: String

    fun deserialize(content: String): T

    fun fromContext(ctx: BotContext) = (ctx.session[key] as? String)?.let { deserialize(it) }

    fun cleanup(ctx: BotContext) = ctx.session.remove(key)
}

