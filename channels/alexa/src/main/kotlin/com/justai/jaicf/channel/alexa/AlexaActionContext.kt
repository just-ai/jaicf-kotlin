package com.justai.jaicf.channel.alexa

import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext

class AlexaActionContext(
    override val context: BotContext,
    override val activator: ActivatorContext,
    override val request: AlexaBotRequest,
    override val reactions: AlexaReactions
): ActionContext(context, activator, request, reactions)

fun ActionContext.withAlexa(block: AlexaActionContext.() -> Unit) {
    if (request is AlexaBotRequest) {
        block.invoke(AlexaActionContext(
            context,
            activator,
            request as AlexaBotRequest,
            reactions as AlexaReactions)
        )
    }
}
