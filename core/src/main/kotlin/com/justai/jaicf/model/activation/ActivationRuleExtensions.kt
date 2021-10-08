package com.justai.jaicf.model.activation

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.reactions.Reactions

/**
 * Negates [predicate] for [ActivationRule.onlyIf]
 *
 * @param predicate condition to disable this rule
 *
 * @see ActivationRule.onlyIf
 */
fun ActivationRule.disableIf(predicate: ActivationRule.OnlyIfContext.() -> Boolean) = onlyIf { !predicate() }

/**
 * Allows activation of [this] rule only if [com.justai.jaicf.context.BotContext.client]
 * contains the given [key] of a specified type [T] and given [predicate] is `true`
 *
 * @param key key of an entry in client map
 * @param predicate condition on a stored object to enable this rule
 * @param T desired type of a stored object
 */
@JvmName("onlyIfInClientTyped")
inline fun <reified T> ActivationRule.onlyIfInClient(key: String, crossinline predicate: (T) -> Boolean = { true }) =
    onlyIf { (context.client[key] as? T)?.let(predicate) ?: false }

/**
 * Allows activation of [this] rule only if [com.justai.jaicf.context.BotContext.client]
 * contains the given [key] and given [predicate] is `true`
 *
 * @param key key of an entry in client map
 * @param predicate condition on a stored object to enable this rule
 * */
fun ActivationRule.onlyIfInClient(key: String, predicate: (Any) -> Boolean = { true }) =
    onlyIfInClient<Any>(key, predicate)

/**
 * Allows activation of [this] rule only if [com.justai.jaicf.context.BotContext.client]
 * does not contain the given [key]
 *
 * @param key key of an entry in client map
 * */
fun ActivationRule.onlyIfNotInClient(key: String) = disableIf { context.client.containsKey(key) }

/**
 * Allows activation of [this] rule only if [com.justai.jaicf.context.BotContext.session]
 * contains the given [key] of a specified type [T] and given [predicate] is `true`
 *
 * @param key key of an entry in client map
 * @param predicate condition on a stored object to enable this rule
 * @param T desired type of a stored object
 */
@JvmName("onlyIfInSessionTyped")
inline fun <reified T> ActivationRule.onlyIfInSession(key: String, crossinline predicate: (T) -> Boolean = { true }) =
    onlyIf { (context.session[key] as? T)?.let(predicate) ?: false }

/**
 * Allows activation of [this] rule only if [com.justai.jaicf.context.BotContext.session]
 * contains the given [key] and given [predicate] is `true`
 *
 * @param key key of an entry in client map
 * @param predicate condition on a stored object to enable this rule
 */
fun ActivationRule.onlyIfInSession(key: String, predicate: (Any) -> Boolean = { true }) =
    onlyIfInSession<Any>(key, predicate)

/**
 * Allows activation of [this] rule only if [com.justai.jaicf.context.BotContext.session]
 * does not contain the given [key]
 *
 * @param key key of an entry in client map
 * */
fun ActivationRule.onlyIfNotInSession(key: String) = disableIf { context.session.containsKey(key) }

/**
 * Allows activation of [this] rule only if the current request is received from channel specified by given [token]
 *
 * @param token type of a desired channel
 *
 * @see ChannelTypeToken
 * @see ChannelTypeToken.isInstance
 */
fun ActivationRule.onlyFrom(token: ChannelTypeToken<out BotRequest, out Reactions>) = onlyIf { token.isInstance(request) }