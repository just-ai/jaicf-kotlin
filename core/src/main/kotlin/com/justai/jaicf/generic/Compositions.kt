package com.justai.jaicf.generic

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions

// -- Type tokens' compositions

private typealias Act<A> = ActivatorContextTypeToken<A>
private typealias Ch<B, R> = ChannelTypeToken<B, R>
private typealias Ctx<A, B, R> = ContextTypeToken<A, B, R>

private typealias Req = BotRequest
private typealias React = Reactions
private typealias ActCtx = ActivatorContext

// ---- ActivatorTypeToken and ActivatorTypeToken
infix fun <A1: ActCtx, A2: A1> Act<A1>.and(other: Act<A2>): Act<A2> = other

// ---- ActivatorTypeToken and ChannelTypeToken
infix fun <A: ActCtx, B: Req, R: React> Act<A>.and(other: Ch<B, R>): Ctx<A, B, R> = Ctx(activatorType, other.requestType, other.reactionsType)

// ---- ActivatorTypeToken and ContextTypeToken
infix fun <A1: ActCtx, A2: A1, B: Req, R: React> Act<A1>.and(other: Ctx<A2, B, R>): Ctx<A2, B, R> = other

// ---- ChannelTypeToken and ActivatorTypeToken
infix fun <A: ActCtx, B: Req, R: React> Ch<B, R>.and(other: Act<A>): Ctx<A, B, R> = Ctx(other.activatorType, requestType, reactionsType)

// ---- ChannelTypeToken and ChannelTypeToken
infix fun <B1: Req, R1: React, B2: B1, R2: R1> Ch<B1, R1>.and(other: Ch<B2, R2>): Ch<B2, R2> = other

// ---- ChannelTypeToken and ContextTypeToken
infix fun <B1: Req, R1: React, A: ActCtx, B2: B1, R2: R1> Ch<B1, R1>.and(other: Ctx<A, B2, R2>): Ctx<A, B2, R2> = other

// ---- ContextTypeToken and ActivatorTypeToken
infix fun <A1: ActCtx, B: Req, R: React, A2: A1> Ctx<A1, B, R>.and(other: Act<A2>): Ctx<A2, B, R> = Ctx(other.activatorType, requestType, reactionsType)

// ---- ContextTypeToken and ChannelTypeToken
infix fun <A: ActCtx, B1: Req, R1: React, B2: B1, R2: R1> Ctx<A, B1, R1>.and(other: Ch<B2, R2>): Ctx<A, B2, R2> = Ctx(activatorType, other.requestType, other.reactionsType)

// ---- ContextTypeToken and ContextTypeToken
infix fun <A1: ActCtx, B1: Req, R1: React, A2: A1, B2: B1, R2: R1> Ctx<A1, B1, R1>.and(other: Ctx<A2, B2, R2>): Ctx<A2, B2, R2> = other
