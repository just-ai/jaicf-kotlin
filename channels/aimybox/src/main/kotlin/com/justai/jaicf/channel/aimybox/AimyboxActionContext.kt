package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.channel.aimybox.api.AimyboxBotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext

class AimyboxActionContext(
    override val context: BotContext,
    override val activator: ActivatorContext,
    override val request: AimyboxBotRequest,
    override val reactions: AimyboxReactions
): ActionContext(context, activator, request, reactions)

fun ActionContext.withAimybox(block: AimyboxActionContext.() -> Unit) {
    if (request is AimyboxBotRequest) {
        block.invoke(AimyboxActionContext(
            context,
            activator,
            request as AimyboxBotRequest,
            reactions as AimyboxReactions
        ))
    }
}