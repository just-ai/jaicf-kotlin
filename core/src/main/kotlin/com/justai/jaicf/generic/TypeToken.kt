package com.justai.jaicf.generic

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions
import kotlin.reflect.KClass

/**
 * Type token that holds information about certain types of [ActivatorContext], [BotRequest] and [Reactions].
 * Can be used in some contexts in order to provide type-specific functionality.
 * Can be composed with other type tokens, see [and] functions.
 */
data class ContextTypeToken<A: ActivatorContext, B: BotRequest, R: Reactions>(
    val activatorType: KClass<A>,
    val requestType: KClass<B>,
    val reactionsType: KClass<R>
) {
    fun isInstance(activatorContext: ActivatorContext) = activatorType.isInstance(activatorContext)
    fun isInstance(request: BotRequest) = requestType.isInstance(request)
    fun isInstance(reactions: Reactions) = reactionsType.isInstance(reactions)
}

/**
 * Type token that holds information about certain type of [ActivatorContext].
 * Can be used in some contexts in order to provide type-specific functionality.
 * Can be composed with other type tokens, see [and] functions.
 */
data class ActivatorContextTypeToken<A: ActivatorContext>(
    val activatorType: KClass<A>
) {
    fun isInstance(activatorContext: ActivatorContext) = activatorType.isInstance(activatorContext)
}

/**
 * Type token that holds information about certain types of [BotRequest] and [Reactions].
 * Can be used in some contexts in order to provide type-specific functionality.
 * Can be composed with other type tokens, see [and] functions.
 */
data class ChannelTypeToken<B: BotRequest, R: Reactions>(
    val requestType: KClass<B>,
    val reactionsType: KClass<R>
) {
    fun isInstance(request: BotRequest) = requestType.isInstance(request)
    fun isInstance(reactions: Reactions) = reactionsType.isInstance(reactions)
}


/**
 * Creates [ContextTypeToken] of types [A], [B] and [R]
 */
inline fun <reified A: ActivatorContext, reified B: BotRequest, reified R: Reactions> ContextTypeToken(): ContextTypeToken<A, B, R> =
    ContextTypeToken(A::class, B::class, R::class)

/**
 * Creates [ActivatorContextTypeToken] of type [A]
 */
inline fun <reified A: ActivatorContext> ActivatorContextTypeToken(): ActivatorContextTypeToken<A> =
    ActivatorContextTypeToken(A::class)

/**
 * Creates [ChannelTypeToken] of types [B] and [R]
 */
inline fun <reified B: BotRequest, reified R: Reactions> ChannelTypeToken(): ChannelTypeToken<B, R> =
    ChannelTypeToken(B::class, R::class)
