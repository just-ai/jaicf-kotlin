package com.justai.jaicf.helpers.context

import com.justai.jaicf.context.BotContext
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * An alias for a property delegate of type [V] backed by [BotContext]
 */
typealias BotContextProperty<V> = MapBackedProperty<BotContext, V>

/**
 * Creates a property delegate of type [V] backed by [BotContext.client].
 *
 * Definition example:
 * ```kotlin
 *
 * var BotContext.username by clientProperty<String>()
 * var BotContext.isUserBlocked by clientProperty("blockStatus") { false }
 * var BotContext.order by clientProperty<Order?>(removeOnNull = true) { null }
 * val BotContext.userInfo by clientProperty<UserInfo>(saveDefault = true) { getUserInfo(it.clientId) }
 *
 * ```
 *
 * Usage example:
 * ```kotlin
 *
 * action {
 *   if (context.isUserBlocked) return@action
 *   reactions.say("Hello, ${context.username}!")
 * }
 *
 * ```
 *
 * @param key the key of the entry where to store the property value, if `null` property name is used
 * @param saveDefault whether to save generated [default] value in the [BotContext.client], `false` by default
 * @param removeOnNull whether to remove entry from [BotContext.client] on null set, `false` by default
 * @param default provider of a default value for the entry, [NoSuchElementException] will be thrown by default
 */
fun <V> clientProperty(
    key: String? = null,
    saveDefault: Boolean = false,
    removeOnNull: Boolean = false,
    default: (BotContext) -> V = { throw NoSuchElementException("No value found for the key specified") }
): BotContextProperty<V> = MapBackedProperty(BotContext::client, key, saveDefault, removeOnNull, default)

/**
 * Creates a property delegate of type [V] backed by [BotContext.session].
 *
 * @param key the key of the entry where to store the property value, if `null` property name is used
 * @param saveDefault whether to save generated [default] value in the [BotContext.session], `false` by default
 * @param removeOnNull whether to remove entry from [BotContext.session] on null set, `false` by default
 * @param default provider of a default value for the entry, [NoSuchElementException] will be thrown by default
 *
 * @see clientProperty for examples
 */
fun <V> sessionProperty(
    key: String? = null,
    saveDefault: Boolean = false,
    removeOnNull: Boolean = false,
    default: (BotContext) -> V = { throw NoSuchElementException("No value found for the key specified") }
): BotContextProperty<V> = MapBackedProperty(BotContext::session, key, saveDefault, removeOnNull, default)

/**
 * Creates a property delegate of type [V] backed by [BotContext.temp].
 *
 * @param key the key of the entry where to store the property value, if `null` property name is used
 * @param saveDefault whether to save generated [default] value in the [BotContext.temp], `false` by default
 * @param removeOnNull whether to remove entry from [BotContext.temp] on null set, `false` by default
 * @param default provider of a default value for the entry, [NoSuchElementException] will be thrown by default
 *
 * @see clientProperty for examples
 */
fun <V> tempProperty(
    key: String? = null,
    saveDefault: Boolean = false,
    removeOnNull: Boolean = false,
    default: (BotContext) -> V = { throw NoSuchElementException("No value found for the key specified") }
): BotContextProperty<V> = MapBackedProperty(BotContext::temp, key, saveDefault, removeOnNull, default)

/**
 * Allows to bind [ReadWriteProperty] defined on [BotContext] to a receiver of any type [T]
 * by providing [BotContext] selector function.
 *
 * Example:
 * ```kotlin
 * val DefaultActionContext.userName by clientProperty<String?>() withContext { context }
 * ```
 *
 * @param context provider of a [BotContext] for type [T]
 *
 * @return delegated property on a type [T] backed by the given [ReadWriteProperty]
 */
infix fun <T, V> ReadWriteProperty<BotContext, V>.withContext(context: T.() -> BotContext): ReadWriteProperty<T, V> =
    object : ReadWriteProperty<T, V> {
        override fun getValue(thisRef: T, property: KProperty<*>): V {
            return this@withContext.getValue(thisRef.context(), property)
        }

        override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
            this@withContext.setValue(thisRef.context(), property, value)
        }
    }

/**
 * Allows to bind [ReadOnlyProperty] defined on [BotContext] to a receiver of any type [T]
 * by providing [BotContext] selector function.
 *
 * @param context provider of a [BotContext] for type [T]
 *
 * @return delegated peroperty on a type [T] backed by the given [ReadOnlyProperty]
 */
infix fun <T, V> ReadOnlyProperty<BotContext, V>.withContext(context: T.() -> BotContext): ReadOnlyProperty<T, V> =
    ReadOnlyProperty<T, V> { thisRef, property -> this@withContext.getValue(thisRef.context(), property) }

/**
 * Allows to bind [MapBackedProperty] defined on [BotContext] to a receiver of any type [T]
 * by providing [BotContext] selector function.
 *
 * Also allows to override default value with [T] as a receiver.
 *
 * Example:
 * ```kotlin
 * val DefaultActionContext.userName by clientProperty<String>(saveDefault = true).with({ context }) { request.getUserName() }
 * ```
 *
 * @param context provider of a [BotContext] for type [T]
 * @param default new overriding default value
 *
 * @return delegated property on a receiver of type [T] backed by the given [MapBackedProperty]
 */
fun <T, V> MapBackedProperty<BotContext, V>.with(
    context: T.() -> BotContext,
    default: T.() -> V = { this@with.default(context()) }
): MapBackedProperty<T, V> = MapBackedProperty({ mapSelector(it.context()) }, key, saveDefault, removeOnNull, default)

/**
 * An implementation of [ReadWriteProperty] backed by some [MutableMap].
 *
 * @param T receiver type
 * @param V property type
 * @param mapSelector selector of the underlying [MutableMap]
 * @param key the key of the entry where to store the property value, if `null` property name is used
 * @param saveDefault whether to save generated [default] value in the map, `false` by default
 * @param removeOnNull whether to remove entry from the map on null set, `false` by default
 * @param default provider of a default value for the entry, [NoSuchElementException] will be thrown by default
 */
class MapBackedProperty<T, V>(
    internal val mapSelector: (T) -> MutableMap<String, Any?>,
    internal val key: String?,
    internal val saveDefault: Boolean,
    internal val removeOnNull: Boolean,
    internal val default: (T) -> V,
) : ReadWriteProperty<T, V> {

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val map = mapSelector(thisRef)
        val key = key ?: property.name

        val value = if (map.containsKey(key)) {
            map[key]
        } else {
            val default = default(thisRef)
            if (saveDefault) {
                map[key] = default
            }
            default
        }

        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        val map = mapSelector(thisRef)
        val key = key ?: property.name

        if (value == null && removeOnNull) {
            map.remove(key)
        } else {
            map[key] = value
        }
    }
}
