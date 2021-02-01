package com.justai.jaicf.helpers.action

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.generic.ActivatorTypeToken
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken
import com.justai.jaicf.reactions.Reactions

fun ActionContext<*, *, *>.ofType(activatorToken: ActivatorTypeToken<*>): Boolean =
    activatorToken.isInstance(activator)

fun ActionContext<*, *, *>.ofType(channelToken: ChannelTypeToken<*, *>): Boolean =
    channelToken.isInstance(request) && channelToken.isInstance(reactions)

fun ActionContext<*, *, *>.ofType(contextToken: ContextTypeToken<*, *, *>): Boolean =
    contextToken.isInstance(activator) && contextToken.isInstance(request) && contextToken.isInstance(reactions)

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
fun <A : ActivatorContext, B : BotRequest, R : Reactions, A1 : A> ActionContext<A, B, R>.cast(
    activatorToken: ActivatorTypeToken<A1>
): ActionContext<A1, B, R> = this as ActionContext<A1, B, R>

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
fun <A : ActivatorContext, B : BotRequest, R : Reactions, B1: B, R1: R> ActionContext<A, B, R>.cast(
    channelToken: ChannelTypeToken<B1, R1>
): ActionContext<A, B1, R1> = this as ActionContext<A, B1, R1>

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
fun <A : ActivatorContext, B : BotRequest, R : Reactions, A1 : A, B1: B, R1: R> ActionContext<A, B, R>.cast(
    contextToken: ContextTypeToken<A1, B1, R1>
): ActionContext<A1, B1, R1> = this as ActionContext<A1, B1, R1>

@Suppress("UNCHECKED_CAST")
fun <A : ActivatorContext, B : BotRequest, R : Reactions, A1 : A> ActionContext<A, B, R>.safeCast(
    activatorToken: ActivatorTypeToken<A1>
): ActionContext<A1, B, R>? = if (ofType(activatorToken)) cast(activatorToken) else null

@Suppress("UNCHECKED_CAST")
fun <A : ActivatorContext, B : BotRequest, R : Reactions, B1: B, R1: R> ActionContext<A, B, R>.safeCast(
    channelToken: ChannelTypeToken<B1, R1>
): ActionContext<A, B1, R1>? = if (ofType(channelToken)) cast(channelToken) else null

@Suppress("UNCHECKED_CAST")
fun <A : ActivatorContext, B : BotRequest, R : Reactions, A1 : A, B1: B, R1: R> ActionContext<A, B, R>.safeCast(
    contextToken: ContextTypeToken<A1, B1, R1>
): ActionContext<A1, B1, R1>? = if (ofType(contextToken)) cast(contextToken) else null